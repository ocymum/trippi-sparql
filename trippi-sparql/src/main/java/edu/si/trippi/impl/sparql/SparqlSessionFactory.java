
package edu.si.trippi.impl.sparql;

import static org.apache.jena.query.QueryExecutionFactory.sparqlService;
import static org.apache.jena.update.UpdateExecutionFactory.createRemote;

import org.apache.jena.graph.Node;
import org.trippi.impl.base.TriplestoreSessionFactory;

/**
 * A {@link TriplestoreSessionFactory} using SPARQL Update.
 *
 * @author A. Soroka
 */
public class SparqlSessionFactory implements TriplestoreSessionFactory {

    static final String[] LANGUAGES = new String[] {"SPARQL"};

    private final String updateEndpoint, queryEndpoint, constructEndpoint;

    private Node graphName;
    
    /**
     * Full constructor.
     *
     * @param updateEndpoint the SPARQL Update endpoint against which to act
     * @param queryEndpoint the SPARQL Query endpoint against which to act for non-CONSTRUCT queries
     * @param constructEndpoint the SPARQL Query endpoint against which to act for CONSTRUCT queries
     */
    public SparqlSessionFactory(final String updateEndpoint, final String queryEndpoint,
            final String constructEndpoint, final Node gN) {
        this.updateEndpoint = updateEndpoint;
        this.queryEndpoint = queryEndpoint;
        this.constructEndpoint = constructEndpoint;
        this.graphName = gN;
    }

    @Override
    public SparqlSession newSession() {
        return new SparqlSession(u -> createRemote(u, updateEndpoint).execute(), q -> sparqlService(queryEndpoint, q)
                .execSelect(), c -> sparqlService(constructEndpoint, c).execConstruct(), graphName);
    }

    @Override
    public String[] listTripleLanguages() {
        return LANGUAGES;
    }

    @Override
    public String[] listTupleLanguages() {
        return LANGUAGES;
    }

    @Override
    public void close() {
        // NO OP
    }

}
