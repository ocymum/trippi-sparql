
package com.asoroka.trippi.impl.sparqlupdate;

import static org.apache.jena.update.UpdateExecutionFactory.createRemote;

import org.trippi.impl.base.TriplestoreSessionFactory;

/**
 * A {@link TriplestoreSessionFactory} using SPARQL Update.
 *
 * @author A. Soroka
 *
 */
public class SparqlUpdateSessionFactory implements TriplestoreSessionFactory {

    static final String[] LANGUAGES = new String[] {};

    /**
     * The SPARQL Update endpoint against which to act.
     */
    private final String endpoint;

    /**
     * Default constructor.
     *
     * @param endpoint the SPARQL Update endpoint against which to act
     */
    public SparqlUpdateSessionFactory(final String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public SparqlUpdateSession newSession() {
        return new SparqlUpdateSession(query -> createRemote(query, endpoint).execute());
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
