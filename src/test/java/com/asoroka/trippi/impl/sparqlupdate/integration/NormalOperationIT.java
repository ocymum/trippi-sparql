
package com.asoroka.trippi.impl.sparqlupdate.integration;

import static com.asoroka.trippi.impl.sparqlupdate.converters.TripleConverter.tripleConverter;
import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static org.apache.jena.ext.com.google.common.collect.ImmutableMap.of;
import static org.apache.jena.query.QueryExecutionFactory.sparqlService;
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.apache.jena.riot.web.HttpOp.execHttpPostForm;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openrdf.rio.RDFFormat.NTRIPLES;
import static org.openrdf.rio.Rio.createParser;
import static org.trippi.impl.RDFFactories.createTriple;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.http.Params;
import org.jrdf.graph.Triple;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.trippi.TriplestoreWriter;

import com.asoroka.trippi.impl.sparqlupdate.SparqlUpdateConnector;

/**
 * This integration test expects to find a Fuseki instance running at a port as
 * defined below and under the webapp name defined below. This is handled by
 * Maven in the course of a normal build, but to run this test inside an
 * IDE, it will be necessary to manually start a webapp to perform that role.
 *
 * @author A. Soroka
 */
public class NormalOperationIT {

    private static final String ALL_TRIPLES = "CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o.}";

    private static final String FUSEKI_PORT_PROPERTY = "fuseki.dynamic.test.port";

    private final int PORT = parseInt(getProperty(FUSEKI_PORT_PROPERTY, "3030"));

    @Test
    public void testNormalOperation() throws Exception {

        final String fusekiUrl = "http://localhost:" + PORT + "/jena-fuseki-war/";

        // build a dataset to work with
        final String datasetName = "testNormalOperation";
        final Params params = new Params();
        params.addParam("dbType", "mem");
        params.addParam("dbName", datasetName);
        execHttpPostForm(fusekiUrl + "$/datasets", params);

        // configure our SPARQL Update-based Trippi connector
        final SparqlUpdateConnector sparqlUpdateConnector = new SparqlUpdateConnector();
        final String datasetUrl = fusekiUrl + datasetName;
        final Map<String, String> config = of("sparqlUpdateEndpoint", datasetUrl + "/update");
        sparqlUpdateConnector.setConfiguration(config);

        // load some simple sample triples
        final List<Triple> triples = new ArrayList<>();
        final RDFParser parser = createParser(NTRIPLES);
        final StatementCollector handler = new StatementCollector();
        parser.setRDFHandler(handler);
        try (InputStream rdf = getClass().getResourceAsStream("/normaltestdata.nt")) {
            parser.parse(rdf, "");
        }
        for (final Statement s : handler.getStatements()) {
            triples.add(createTriple(s.getSubject(), s.getPredicate(), s.getObject()));
        }

        final Model jenaStatements = createDefaultModel();
        triples.stream().map(tripleConverter::convert).map(jenaStatements::asStatement).forEach(jenaStatements::add);

        // add them to our triplestore via our SPARQL Update connector
        final TriplestoreWriter writer = sparqlUpdateConnector.getWriter();
        // this is a write-only connector
        assertEquals(0, writer.listTripleLanguages().length);
        assertEquals(0, writer.listTupleLanguages().length);

        writer.add(triples, true);

        // check that they were all added
        Model results = sparqlService(datasetUrl + "/query", ALL_TRIPLES).execConstruct();
        assertTrue(results.containsAll(jenaStatements));

        // now delete them from our triplestore via our SPARQL Update connector
        writer.delete(triples, true);

        // check that they were all removed
        results = sparqlService(datasetUrl + "/query", ALL_TRIPLES).execConstruct();
        assertFalse(results.containsAny(jenaStatements));
        sparqlUpdateConnector.close();
    }
}
