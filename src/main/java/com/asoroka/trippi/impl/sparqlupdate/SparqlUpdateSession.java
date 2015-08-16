
package com.asoroka.trippi.impl.sparqlupdate;

import static com.asoroka.trippi.impl.sparqlupdate.SparqlUpdateSession.Operation.DELETE;
import static com.asoroka.trippi.impl.sparqlupdate.SparqlUpdateSession.Operation.INSERT;
import static com.asoroka.trippi.impl.sparqlupdate.SparqlUpdateSessionFactory.LANGUAGES;
import static com.asoroka.trippi.impl.sparqlupdate.converters.TripleConverter.tripleConverter;
import static java.util.stream.Collectors.toSet;
import static org.apache.jena.graph.Factory.createGraphMem;
import static org.apache.jena.riot.RDFDataMgr.write;
import static org.apache.jena.riot.RDFFormat.TURTLE_BLOCKS;
import static org.apache.jena.update.UpdateFactory.create;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.update.UpdateRequest;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.trippi.TripleIterator;
import org.trippi.TupleIterator;
import org.trippi.impl.base.TriplestoreSession;

/**
 * @author A. Soroka
 */
public class SparqlUpdateSession implements TriplestoreSession {

    /**
     * Encapsulates the service against which to execute SPARQL Update requests.
     */
    private final Consumer<UpdateRequest> executor;

    public SparqlUpdateSession(final Consumer<UpdateRequest> executor) {
        this.executor = executor;
    }

    @Override
    public void add(final Set<org.jrdf.graph.Triple> triples) {
        mutate(triples, INSERT);
    }

    @Override
    public void delete(final Set<org.jrdf.graph.Triple> triples) {
        mutate(triples, DELETE);
    }

    /**
     * Perform a mutating operation against the service encapsulated in
     * {@link #executor} using SPARQL Update.
     *
     * @param triples the triples with which to perform the operation
     * @param operation the type of mutating operation to perform
     */
    private void mutate(final Set<org.jrdf.graph.Triple> triples, final Operation operation) {
        final Set<Triple> ts = triples.stream().map(tripleConverter::convert).collect(toSet());
        final UpdateRequest request = create(operation + " { " + datablock(ts) + " } WHERE {}");
        executor.accept(request);
    }

    public static enum Operation {
        INSERT, DELETE
    }

    /**
     * Creates serialized RDF appropriate for use in a SPARQL Update request.
     *
     * @param triples the RDF to serialize
     * @return a block of serialized RDF as described
     */
    private static String datablock(final Set<Triple> triples) {
        try (StringWriter w = new StringWriter()) {
            final Graph g = createGraphMem();
            triples.forEach(g::add);
            write(w, g, TURTLE_BLOCKS);
            return w.toString();
        } catch (final IOException e) {
            throw new AssertionError();
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
    public TupleIterator query(final String queryText, final String language) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TripleIterator findTriples(final String lang, final String queryText) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TripleIterator findTriples(final SubjectNode s, final PredicateNode p, final ObjectNode o) {
        throw new UnsupportedOperationException();
    }
}
