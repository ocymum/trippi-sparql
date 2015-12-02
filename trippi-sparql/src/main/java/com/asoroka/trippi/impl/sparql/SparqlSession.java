
package com.asoroka.trippi.impl.sparql;

import static com.asoroka.trippi.impl.sparql.SparqlSession.Operation.DELETE;
import static com.asoroka.trippi.impl.sparql.SparqlSession.Operation.INSERT;
import static com.asoroka.trippi.impl.sparql.SparqlSessionFactory.LANGUAGES;
import static com.asoroka.trippi.impl.sparql.converters.ObjectConverter.objectConverter;
import static com.asoroka.trippi.impl.sparql.converters.PredicateConverter.predicateConverter;
import static com.asoroka.trippi.impl.sparql.converters.SubjectConverter.subjectConverter;
import static com.asoroka.trippi.impl.sparql.converters.TripleConverter.tripleConverter;
import static org.apache.jena.ext.com.google.common.collect.FluentIterable.from;
import static org.apache.jena.riot.writer.NTriplesWriter.write;
import static org.apache.jena.sparql.util.FmtUtils.stringForNode;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.jena.graph.Node_Variable;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.slf4j.Logger;
import org.trippi.TripleIterator;
import org.trippi.TrippiException;
import org.trippi.TupleIterator;
import org.trippi.impl.base.DefaultAliasManager;
import org.trippi.impl.base.TriplestoreSession;
import org.trippi.io.SimpleTripleIterator;

/**
 * A write-only {@link TriplestoreSession} implementation. The expectation for this class is that query access to the
 * triplestore will be provided by some means outside of Trippi, for example, via some SPARQL-over-HTTP service.
 *
 * @author A. Soroka
 */
public class SparqlSession implements TriplestoreSession {

	private static final Logger log = getLogger(SparqlSession.class);

	/**
	 * The service against which to execute SPARQL Update requests.
	 */
	private final Consumer<UpdateRequest> updateExecutor;

	/**
	 * The service against which to execute SPARQL Query requests, exceptCONSTRUCT requests.
	 */
	private final Function<Query, ResultSet> queryExecutor;

	/**
	 * The service against which to execute SPARQL Query CONSTRUCTrequests.
	 */
	private final Function<Query, Model> constructExecutor;

	/**
	 * Default constructor.
	 *
	 * @param updateExecutor the service against which to execute SPARQL Update requests
	 * @param queryExecutor the service against which to execute SPARQL Query non-CONSTRUCT requests
	 * @param constructExecutor the service against which to execute SPARQL Query CONSTRUCT requests
	 */
	public SparqlSession(final Consumer<UpdateRequest> updateExecutor, final Function<Query, ResultSet> queryExecutor,
			final Function<Query, Model> constructExecutor) {
		this.updateExecutor = updateExecutor;
		this.queryExecutor = queryExecutor;
		this.constructExecutor = constructExecutor;
	}

	@Override
	public void add(final Set<org.jrdf.graph.Triple> triples) {
		log.debug("Adding triples: {}", triples);
		mutate(triples, INSERT);
	}

	@Override
	public void delete(final Set<org.jrdf.graph.Triple> triples) {
		log.debug("Deleting triples: {}", triples);
		mutate(triples, DELETE);
	}

	/**
	 * Perform a mutating operation against {@link #updateExecutor} using SPARQL
	 * Update.
	 *
	 * @param triples the triples with which to perform the operation
	 * @param operation the type of mutating operation to perform
	 */
	private void mutate(final Set<org.jrdf.graph.Triple> triples, final Operation operation) {
		final Iterable<Triple> trips = from(triples).transform(tripleConverter::convert);
		final UpdateRequest request = UpdateFactory.create(operation + " { " + datablock(trips) + " } WHERE {}");
		updateExecutor.accept(request);
	}

	/**
	 * The various operations that can be performed against a triplestore via
	 * SPARQL Update.
	 */
	public static enum Operation {
		INSERT, DELETE
	}

	/**
	 * Creates serialized RDF appropriate for use in a SPARQL Update request.
	 *
	 * @param triples the RDF to serialize
	 * @return a block of serialized RDF
	 */
	private static String datablock(final Iterable<Triple> triples) {
		try (final StringWriter w = new StringWriter()) {
			write(w, triples.iterator());
			return w.toString();
		} catch (final IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public String[] listTupleLanguages() {
		return LANGUAGES;
	}

	@Override
	public String[] listTripleLanguages() {
		return LANGUAGES;
	}

	@Override
	public void close() {
		// NO OP
	}

	@Override
	public TupleIterator query(final String queryText, final String lang) throws TrippiException {
		checkLang(lang);
		final Query query = QueryFactory.create(queryText);
		return new ResultSetTupleIterator(queryExecutor.apply(query));
	}

	@Override
	public TripleIterator findTriples(final String lang, final String queryText) throws TrippiException {
		checkLang(lang);
		final Query query = QueryFactory.create(queryText);
		final Model answer = constructExecutor.apply(query);
		final Set<org.jrdf.graph.Triple> triples = answer.listStatements().mapWith(Statement::asTriple).mapWith(
				tripleConverter.reverse()::convert).toSet();
		final DefaultAliasManager aliases = new DefaultAliasManager(answer.getNsPrefixMap());
		return new SimpleTripleIterator(triples, aliases);
	}

	/**
	 * This connector uses only SPARQL.
	 *
	 * @param lang the language of a query
	 * @throws TrippiException
	 */
	private static void checkLang(final String lang) throws TrippiException {
		if (!lang.toLowerCase().equals("sparql")) throw new TrippiException("This Trippi connector uses only SPARQL!",
				new UnsupportedOperationException("This Trippi connector uses only SPARQL!"));
	}

	@Override
	public TripleIterator findTriples(final SubjectNode s, final PredicateNode p, final ObjectNode o)
			throws TrippiException {
		final String subj = stringForNode(s == null ? new Node_Variable("s") : subjectConverter.convert(s));
		final String pred = stringForNode(p == null ? new Node_Variable("p") : predicateConverter.convert(p));
		final String obj = stringForNode(o == null ? new Node_Variable("o") : objectConverter.convert(o));

		final String queryText = "CONSTRUCT { " + subj + " " + pred + " " + obj + " } WHERE { " + subj + " " + pred +
				" " + obj + " }";
		return findTriples("sparql", queryText);
	}
}
