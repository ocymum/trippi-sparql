
package com.asoroka.trippi.impl.sparqlupdate;

import static org.apache.jena.update.UpdateExecutionFactory.createRemote;
import static org.apache.jena.update.UpdateFactory.create;

import org.trippi.impl.base.TriplestoreSessionFactory;

public class SparqlUpdateSessionFactory implements TriplestoreSessionFactory {

    public static final String[] LANGUAGES = new String[] {};

    private final String endpoint;

    public SparqlUpdateSessionFactory(final String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public SparqlUpdateSession newSession() {
        return new SparqlUpdateSession(query -> createRemote(create(query), endpoint).execute());
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
