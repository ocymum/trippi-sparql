
package com.asoroka.trippi.impl.sparqlupdate.integration;

import static com.asoroka.trippi.impl.sparqlupdate.converters.TripleConverter.tripleConverter;
import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static java.net.URI.create;
import static java.util.Arrays.asList;
import static org.apache.jena.ext.com.google.common.collect.ImmutableMap.of;
import static org.apache.jena.query.QueryExecutionFactory.sparqlService;
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;
import static org.trippi.impl.RDFFactories.createResource;
import static org.trippi.impl.RDFFactories.createTriple;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.jena.fuseki.EmbeddedFusekiServer;
import org.apache.jena.rdf.model.Model;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.Triple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.trippi.TrippiException;

import com.asoroka.trippi.impl.sparqlupdate.SparqlUpdateConnector;

public class NormalOperationIT {

    private static final String ALL_TRIPLES = "CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o.}";

    private EmbeddedFusekiServer server;

    private static final Logger log = getLogger(NormalOperationIT.class);

    private static final String FUSEKI_PORT_PROPERTY = "fuseki.dynamic.test.port";

    private final int PORT = parseInt(getProperty(FUSEKI_PORT_PROPERTY, "3030"));

    @Before
    public void setUpFuseki() {
        server = EmbeddedFusekiServer.mem(PORT, "/test");
        log.info("Starting EmbeddedFusekiServer on port {}", PORT);
        server.start();
    }

    @After
    public void tearDownFuseki() {
        log.info("Stopping EmbeddedFusekiServer");
        server.stop();
    }

    @Test
    public void testNormalOperation() throws TrippiException, GraphElementFactoryException, IOException {

        // configure our SPARQL Update-based Trippi connector
        final SparqlUpdateConnector sparqlUpdateConnector = new SparqlUpdateConnector();
        final String datasetUrl = "http://localhost:" + PORT + "/test";
        final Map<String, String> config = of("sparqlUpdateEndpoint", datasetUrl + "/update");
        sparqlUpdateConnector.setConfiguration(config);

        // create some simple sample triples
        final List<Triple> triples = asList(createTriple(createResource(create("info:subject1")), createResource(create(
                "info:predicate1")), createResource(create("info:object1"))), createTriple(createResource(create(
                        "info:subject2")), createResource(create("info:predicate2")), createResource(create(
                                "info:object2"))));
        final Model statements = createDefaultModel();
        triples.stream().map(tripleConverter::convert).map(statements::asStatement).forEach(statements::add);

        // add them to our triplestore via our SPARQL Update connector
        sparqlUpdateConnector.getWriter().add(triples, true);

        // check that they were all added
        Model results = sparqlService(datasetUrl + "/query", ALL_TRIPLES).execConstruct();
        assertTrue(results.containsAll(statements));

        // now delete them from our triplestore via our SPARQL Update connector
        sparqlUpdateConnector.getWriter().delete(triples, true);

        // check that they were all removed
        results = sparqlService(datasetUrl + "/query", ALL_TRIPLES).execConstruct();
        assertFalse(results.containsAny(statements));
    }
}
