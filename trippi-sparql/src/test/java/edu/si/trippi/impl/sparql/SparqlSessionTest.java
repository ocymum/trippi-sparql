package edu.si.trippi.impl.sparql;

import static edu.si.trippi.impl.sparql.converters.TripleConverter.tripleUnconverter;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.core.DatasetGraphFactory.createTxnMem;
import static org.apache.jena.sparql.sse.SSE.parseQuad;

import java.util.Set;
import java.util.function.Consumer;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateRequest;
import org.jrdf.graph.Triple;
import org.junit.Assert;
import org.junit.Test;
import org.trippi.TrippiException;

import edu.si.trippi.impl.sparql.SparqlSession.UnsupportedLanguageException;

public class SparqlSessionTest extends Assert{

    @Test(expected = UnsupportedLanguageException.class)
    public void shouldAcceptSparqlOnly() throws TrippiException {
        final SparqlSession testSession = new SparqlSession(null, null, null, null);
        testSession.findTriples("itql", "doesn't matter");
    }
    
    @Test
    public void shouldGenerateReasonablyCorrectUpdates()  {
        DatasetGraph dataset = createTxnMem();
        Node graphName = createURI("info:graph");
        Quad q1 = parseQuad("(quad <info:graph> <info:subject> <info:predicate1> <info:object>)");
        Quad q2 = parseQuad("(quad <info:graph> <info:subject> <info:predicate2> \"123\"^^<http://example/type>)");
        Quad q3 = parseQuad("(quad <info:graph> <info:subject> <info:predicate3> \"bonjour\"@fr)");

        Consumer<UpdateRequest> updateExecutor = u -> UpdateExecutionFactory.create(u, dataset).execute();
        final SparqlSession testSession = new SparqlSession(updateExecutor, null, null, graphName);
        
        Set<Triple> triples = asList(q1,q2,q3).stream()
                        .map(Quad::asTriple)
                        .map(tripleUnconverter::convert)
                        .collect(toSet());

        Graph namedGraph = dataset.getGraph(graphName);

        testSession.add(triples);
        assertEquals(3, namedGraph.size());
        testSession.delete(triples);
        assertTrue(namedGraph.isEmpty());
    }
}
