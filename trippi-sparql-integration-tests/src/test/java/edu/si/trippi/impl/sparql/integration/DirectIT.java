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

package edu.si.trippi.impl.sparql.integration;

import static edu.si.trippi.impl.sparql.SparqlConnector.rebase;
import static edu.si.trippi.impl.sparql.converters.TripleConverter.tripleConverter;
import static org.apache.jena.ext.com.google.common.collect.ImmutableMap.of;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.query.QueryExecutionFactory.sparqlService;
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.apache.jena.sparql.util.FmtUtils.stringForNode;
import static org.trippi.TripleMaker.createResource;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.Node;
import org.jrdf.graph.Triple;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.trippi.TriplestoreReader;
import org.trippi.TriplestoreWriter;
import org.trippi.TrippiException;
import org.trippi.TupleIterator;
import org.trippi.impl.RDFFactories;

import edu.si.trippi.impl.sparql.SparqlConnector;
import edu.si.trippi.impl.sparql.SparqlSession.UnsupportedLanguageException;

/**
 * Tests of interaction directly between this Trippi implementation and a SPARQL endpoint. (Eliding Fedora.) This
 * integration test expects to find a Fuseki endpoint running at a port as defined below and under the webapp name
 * defined below. This is handled by Maven in the course of a normal build, but to run this test inside an IDE, it will
 * be necessary to manually start a webapp to perform that role.
 *
 * @author A. Soroka
 */
public class DirectIT extends IT {

    /**
     * The dataset to work with.
     */
    private static final String DATASET_NAME = "direct";

    /**
     * The read-only dataset to work with.
     */
    private static final String READONLY_DATASET_NAME = "readOnlyTests";

    private static final String graphName = stringForNode(createURI("#test"));

    private static final String ALL_TRIPLES =
                    rebase("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH " + graphName + " { ?s ?p ?o } . }");

    @Test
    public void testNormalOperation() throws Exception {

        final String fusekiUrl = "http://localhost:" + PORT + "/jena-fuseki-war/";

        // configure our SPARQL-based Trippi connector
        final SparqlConnector sparqlConnector = new SparqlConnector();
        final String datasetUrl = fusekiUrl + DATASET_NAME;
        final String queryService = datasetUrl + "/query";
        final String updateService = datasetUrl + "/update";
        sparqlConnector.setConfiguration(
                        of("updateEndpoint", updateService, "queryEndpoint", queryService, "graphName", "#test"));

        final TriplestoreReader reader = sparqlConnector.getReader();
        try {
            reader.countTriples("itql", "", 0, false);
            fail("This connector should not accept iTQL queries!");
        } catch (final TrippiException e) {
            assertTrue("Should not accept iTQL queries! ", e instanceof UnsupportedLanguageException);
        }

        // add them to our triplestore via our SPARQL Update connector
        final TriplestoreWriter writer = sparqlConnector.getWriter();
        // this is a SPARQL-only connector
        assertArrayEquals(new String[] {"SPARQL"}, writer.listTripleLanguages());
        assertArrayEquals(new String[] {"SPARQL"}, writer.listTupleLanguages());
        writer.add(triples, true);

        // check that they were all added
        Model results = sparqlService(queryService, ALL_TRIPLES).execConstruct();
        assertTrue("Failed to discover the triples we stored!", results.containsAll(jenaStatements));

        // check that we can query them via our connector
        assertEquals(1, reader.countTriples(createResource("info:subject4"), null, null, 0));
        String tupleQuery = "CONSTRUCT { <info:subject2> ?p ?o } WHERE { GRAPH <#test> { <info:subject2> ?p ?o } . }";
        assertEquals(2, reader.findTriples("sparql", tupleQuery, 0, true).count());
        assertEquals(1, reader.countTriples(createResource("info:subject1"), null, null, 0));
        tupleQuery = "SELECT ?s WHERE { GRAPH <#test> {?s <info:predicate3> _:o } . }";
        final TupleIterator tuples = reader.findTuples("sparql", tupleQuery, 0, false);
        assertTrue(tuples.hasNext());
        final Map<String, Node> solution = tuples.next();
        assertFalse(tuples.hasNext());
        assertEquals(1, solution.size());
        assertTrue(solution.containsKey("s"));
        assertTrue(solution.get("s").equals(createResource("info:subject3")));

        // now delete them from our triplestore via our SPARQL Update connector
        writer.delete(triples, true);

        // check that they were all removed
        results = sparqlService(queryService, ALL_TRIPLES).execConstruct();
        assertFalse(results.containsAny(jenaStatements));

        sparqlConnector.close();
    }

    @Test
    public void readOnlyOperation() throws TrippiException, IOException {
        final String fusekiUrl = "http://localhost:" + PORT + "/jena-fuseki-war/";

        // writer
        final SparqlConnector sparqlConnector = new SparqlConnector();
        final String datasetUrl = fusekiUrl + READONLY_DATASET_NAME;
        final String queryService = datasetUrl + "/query";
        final String updateService = datasetUrl + "/update";
        sparqlConnector.setConfiguration(
                        of("updateEndpoint", updateService, "queryEndpoint", queryService, "graphName", "#test"));
        final TriplestoreWriter writer = sparqlConnector.getWriter();
        // read-only "writer"
        final SparqlConnector readOnlysparqlConnector = new SparqlConnector();
        readOnlysparqlConnector.setConfiguration(of("updateEndpoint", updateService, "queryEndpoint", queryService,
                        "graphName", "#test", "readOnly", "true"));
        final TriplestoreWriter readOnlywriter = readOnlysparqlConnector.getWriter();
        // should not do anything
        readOnlywriter.add(triples, true);
        // check that they were all added
        Model results = sparqlService(queryService, ALL_TRIPLES).execConstruct();
        assertTrue("Should not be able to write with a read-only session!", results.isEmpty());
        writer.add(triples, true);
        // now they really should have been added
        results = sparqlService(queryService, ALL_TRIPLES).execConstruct();
        assertTrue("Failed to discover the triples we stored!", results.containsAll(jenaStatements));
        // should not do anything
        readOnlywriter.delete(triples, true);
        // now they should still be there
        results = sparqlService(queryService, ALL_TRIPLES).execConstruct();
        assertTrue("Should not be able to delete with a read-only session!", results.containsAll(jenaStatements));
        // should really remove them
        writer.delete(triples, true);
        // now they should really be gone
        results = sparqlService(queryService, ALL_TRIPLES).execConstruct();
        assertTrue("Should be able to delete with an ordinary session!", results.isEmpty());

        sparqlConnector.close();
        readOnlysparqlConnector.close();
    }

    /**
     * Note the absence of triples using blank nodes. Fedora does not create or manage such triples, so this is
     * reasonable test data.
     */
    private static final String testData = "<info:subject1> <info:predicate1> <info:subject2> .\n" +
                    "<info:subject2> <info:predicate2> \"1234\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n" +
                    "<info:subject2> <info:predicate2> \"Chrysophylax\"^^<http://www.example.com/dives> .\n" +
                    "<info:subject3>  <info:predicate3> \"Shalom!\"@he .\n" +
                    "<info:subject4>  <info:predicate4> \"Oingoboingo\" .\n";

    private static Model jenaStatements = createDefaultModel();

    private static List<Triple> triples = new ArrayList<>();

    static {
        try (Reader rdf = new StringReader(testData)) {
            jenaStatements.read(rdf, null, "N-TRIPLE");
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
        triples = jenaStatements.listStatements().mapWith(org.apache.jena.rdf.model.Statement::asTriple)
                        .mapWith(tripleConverter.reverse()::convert).toList();
    }
}
