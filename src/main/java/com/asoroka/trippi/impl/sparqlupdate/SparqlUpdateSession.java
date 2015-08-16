
package com.asoroka.trippi.impl.sparqlupdate;

import static com.asoroka.trippi.impl.sparqlupdate.SparqlUpdateSession.Operation.DELETE;
import static com.asoroka.trippi.impl.sparqlupdate.SparqlUpdateSession.Operation.INSERT;
import static com.asoroka.trippi.impl.sparqlupdate.SparqlUpdateSessionFactory.LANGUAGES;
import static java.util.stream.Collectors.toSet;
import static org.apache.jena.graph.Factory.createGraphMem;
import static org.apache.jena.riot.RDFDataMgr.write;
import static org.apache.jena.riot.RDFFormat.TURTLE_BLOCKS;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
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

    private final Consumer<String> executor;

    private final TripleConverter tripleConverter = new TripleConverter();

    public SparqlUpdateSession(final Consumer<String> executor) {
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
     * {@link #executor}.
     *
     * @param triples the triples with which to perform the operation
     * @param operation the type of mutating operation to perform
     */
    private void mutate(final Set<org.jrdf.graph.Triple> triples, final Operation operation) {
        final Set<Triple> ts = triples.stream().map(tripleConverter::convert).collect(toSet());
        final String query = operation + " { " + datablock(ts) + " } WHERE {}";
        executor.accept(query);
    }

    public static enum Operation {
        INSERT, DELETE
    }

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
    public TripleIterator findTriples(final SubjectNode subject, final PredicateNode predicate,
            final ObjectNode object) {
        throw new UnsupportedOperationException();
    }
}
