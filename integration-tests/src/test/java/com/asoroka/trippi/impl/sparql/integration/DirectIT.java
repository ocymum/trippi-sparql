
package com.asoroka.trippi.impl.sparql.integration;

import static com.asoroka.trippi.impl.sparql.converters.TripleConverter.tripleConverter;
import static org.apache.jena.ext.com.google.common.collect.ImmutableMap.of;
import static org.apache.jena.query.QueryExecutionFactory.sparqlService;
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.apache.jena.riot.web.HttpOp.execHttpPostForm;
import static org.openrdf.rio.RDFFormat.NTRIPLES;
import static org.openrdf.rio.Rio.createParser;
import static org.trippi.TripleMaker.createResource;
import static org.trippi.impl.RDFFactories.createTriple;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.http.Params;
import org.jrdf.graph.Node;
import org.jrdf.graph.Triple;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.trippi.TriplestoreReader;
import org.trippi.TriplestoreWriter;
import org.trippi.TrippiException;
import org.trippi.TupleIterator;

import com.asoroka.trippi.impl.sparql.SparqlConnector;

/**
 * This integration test expects to find a Fuseki endpoint running at a port as
 * defined below and under the webapp name defined below. This is handled by
 * Maven in the course of a normal build, but to run this test inside an
 * IDE, it will be necessary to manually start a webapp to perform that role.
 *
 * @author A. Soroka
 */
public class DirectIT extends IT {

	private static final String ALL_TRIPLES = "CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o.}";

	@Test
	public void testNormalOperation() throws Exception {

		final String fusekiUrl = "http://localhost:" + PORT + "/jena-fuseki-war/";

		// build a dataset to work with
		final String datasetName = "testNormalOperation";
		final Params params = new Params();
		params.addParam("dbType", "mem");
		params.addParam("dbName", datasetName);
		execHttpPostForm(fusekiUrl + "$/datasets", params);

		// configure our SPARQL-based Trippi connector
		final SparqlConnector sparqlConnector = new SparqlConnector();
		final String datasetUrl = fusekiUrl + datasetName;
		final Map<String, String> config = of("updateEndpoint", datasetUrl + "/update", "queryEndpoint", datasetUrl +
				"/query");
		sparqlConnector.setConfiguration(config);

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
		final TriplestoreWriter writer = sparqlConnector.getWriter();
		// this is a SPARQL-only connector
		assertArrayEquals(new String[] {"SPARQL"}, writer.listTripleLanguages());
		assertArrayEquals(new String[] {"SPARQL"}, writer.listTupleLanguages());
		final TriplestoreReader reader = sparqlConnector.getReader();
		try {
			reader.countTriples("itql", "", 0, false);
			fail("This connector should not accept iTQL queries!");
		} catch (final TrippiException e) {}

		writer.add(triples, true);

		// check that they were all added
		Model results = sparqlService(datasetUrl + "/query", ALL_TRIPLES).execConstruct();
		assertTrue(results.containsAll(jenaStatements));

		// check that we can query them via our connector
		assertEquals(5, reader.countTriples(null, null, null, 0));
		assertEquals(2, reader.findTriples("sparql",
				"CONSTRUCT { <info:subject2> ?p ?o } WHERE { <info:subject2> ?p ?o }", 0, true).count());
		assertEquals(1, reader.countTriples(createResource("info:subject1"), null, null, 0));
		final TupleIterator tuples = reader.findTuples("sparql", "SELECT ?s WHERE {?s <info:predicate3> _:o }", 0,
				false);
		assertTrue(tuples.hasNext());
		final Map<String, Node> solution = tuples.next();
		assertFalse(tuples.hasNext());
		assertEquals(1, solution.size());
		assertTrue(solution.containsKey("s"));
		assertTrue(solution.get("s").equals(createResource("info:subject3")));

		// now delete them from our triplestore via our SPARQL Update connector
		writer.delete(triples, true);

		// check that they were all removed
		results = sparqlService(datasetUrl + "/query", ALL_TRIPLES).execConstruct();
		assertFalse(results.containsAny(jenaStatements));
		assertEquals(0, reader.countTriples(null, null, null, 0));

		sparqlConnector.close();
	}
}
