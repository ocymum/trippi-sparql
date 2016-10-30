/*
 * Copyright 2015-2016 Smithsonian Institution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.You may obtain a copy of
 * the License at: http://www.apache.org/licenses/
 *
 * This software and accompanying documentation is supplied without
 * warranty of any kind. The copyright holder and the Smithsonian Institution:
 * (1) expressly disclaim any warranties, express or implied, including but not
 * limited to any implied warranties of merchantability, fitness for a
 * particular purpose, title or non-infringement; (2) do not assume any legal
 * liability or responsibility for the accuracy, completeness, or usefulness of
 * the software; (3) do not represent that use of the software would not
 * infringe privately owned rights; (4) do not warrant that the software
 * is error-free or will be maintained, supported, updated or enhanced;
 * (5) will not be liable for any indirect, incidental, consequential special
 * or punitive damages of any kind or nature, including but not limited to lost
 * profits or loss of data, on any basis arising from contract, tort or
 * otherwise, even if any of the parties has been warned of the possibility of
 * such loss or damage.
 *
 * This distribution includes several third-party libraries, each with their own
 * license terms. For a complete copy of all copyright and license terms, including
 * those of third-party libraries, please see the product release notes.
 *
 */

package edu.si.trippi.impl.sparql;

import static edu.si.trippi.impl.sparql.SparqlConnector.rebase;
import static edu.si.trippi.impl.sparql.SparqlSession.Operation.DELETE;
import static edu.si.trippi.impl.sparql.SparqlSession.Operation.INSERT;
import static edu.si.trippi.impl.sparql.converters.ObjectConverter.objectConverter;
import static edu.si.trippi.impl.sparql.converters.PredicateConverter.predicateConverter;
import static edu.si.trippi.impl.sparql.converters.SubjectConverter.subjectConverter;
import static edu.si.trippi.impl.sparql.converters.TripleConverter.tripleConverter;
import static java.lang.String.format;
import static org.apache.jena.ext.com.google.common.collect.FluentIterable.from;
import static org.apache.jena.riot.writer.NTriplesWriter.write;
import static org.apache.jena.sparql.util.FmtUtils.stringForNode;
import static org.apache.jena.update.UpdateFactory.create;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
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
 * A {@link TriplestoreSession} implementation using SPARQL Query and Update for all operations.
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
     * The service against which to execute SPARQL Query requests, except CONSTRUCT requests.
     */
    private final Function<Query, ResultSet> queryExecutor;

    /**
     * The service against which to execute SPARQL Query CONSTRUCT requests.
     */
    private final Function<Query, Model> constructExecutor;

    private final String graphName;

    /**
     * Default constructor.
     *
     * @param updateExecutor the service against which to execute SPARQL Update requests
     * @param queryExecutor the service against which to execute SPARQL Query non-CONSTRUCT requests
     * @param constructExecutor the service against which to execute SPARQL Query CONSTRUCT requests
     */
    public SparqlSession(final Consumer<UpdateRequest> updateExecutor, final Function<Query, ResultSet> queryExecutor,
                    final Function<Query, Model> constructExecutor, final Node gN) {
        this.updateExecutor = updateExecutor;
        this.queryExecutor = queryExecutor;
        this.constructExecutor = constructExecutor;
        this.graphName = stringForNode(gN);
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
     * Perform a mutating operation against {@link #updateExecutor} using SPARQL Update.
     *
     * @param triples the triples with which to perform the operation
     * @param operation the type of mutating operation to perform
     */
    protected void mutate(final Set<org.jrdf.graph.Triple> triples, final Operation operation) {
        final Iterable<Triple> trips = from(triples).transform(tripleConverter::convert);
        final String datablock = datablock(trips);
        final String payload = rebase(format("%1$s DATA { GRAPH %2$s { %3$s } . }", operation, graphName, datablock));
        log.debug("Sending SPARQL Update operation:\n{}", payload);
        final UpdateRequest request = create(payload);
        updateExecutor.accept(request);
    }

    /**
     * The various operations that can be performed against a triplestore via SPARQL Update.
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
        return SparqlSessionFactory.LANGUAGES;
    }

    @Override
    public String[] listTripleLanguages() {
        return SparqlSessionFactory.LANGUAGES;
    }

    @Override
    public void close() {
        // NO OP
    }

    @Override
    public TupleIterator query(final String queryText, final String lang) throws TrippiException {
        checkLang(lang);
        final Query query = QueryFactory.create(rebase(queryText));
        return new ResultSetTupleIterator(queryExecutor.apply(query));
    }

    @Override
    public TripleIterator findTriples(final String lang, final String queryText) throws TrippiException {
        checkLang(lang);
        final Query query = QueryFactory.create(rebase(queryText));
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
        if (!lang.toLowerCase().contains("sparql"))
            throw new UnsupportedLanguageException("This Trippi connector uses only SPARQL!");
    }

    public static class UnsupportedLanguageException extends TrippiException {

        private static final long serialVersionUID = 1L;

        public UnsupportedLanguageException(final String message) {
            super(message);
        }
    }

    @Override
    public TripleIterator findTriples(final SubjectNode subj, final PredicateNode pred, final ObjectNode obj)
                    throws TrippiException {
        final String s = stringForNode(subj == null ? new Node_Variable("s") : subjectConverter.convert(subj));
        final String p = stringForNode(pred == null ? new Node_Variable("p") : predicateConverter.convert(pred));
        final String o = stringForNode(obj == null ? new Node_Variable("o") : objectConverter.convert(obj));
        final String triplePattern = format(" { %1$s %2$s %3$s} ", s, p, o);
        final String queryText = format("CONSTRUCT %1$s WHERE { GRAPH %2$s %1$s . }", triplePattern, graphName);
        return findTriples("sparql", queryText);
    }

    public static class ReadOnlySparqlSession extends SparqlSession {

        public ReadOnlySparqlSession(final Consumer<UpdateRequest> updateExecutor, final Function<Query, ResultSet> queryExecutor,
                        final Function<Query, Model> constructExecutor, final Node gN) {
            super(updateExecutor, queryExecutor, constructExecutor, gN);
        }

        /*
         * No-op override.
         *
         * (non-Javadoc)
         * @see edu.si.trippi.impl.sparql.SparqlSession#mutate(java.util.Set, edu.si.trippi.impl.sparql.SparqlSession.Operation)
         */
        @Override
        protected void mutate(final Set<org.jrdf.graph.Triple> triples, final Operation operation) {
            return;
        }
    }
}
