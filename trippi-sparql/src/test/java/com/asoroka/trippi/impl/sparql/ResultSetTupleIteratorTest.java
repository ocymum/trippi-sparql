
package com.asoroka.trippi.impl.sparql;

import static java.util.Arrays.asList;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.engine.binding.Binding;
import org.jrdf.graph.Node;
import org.junit.Assert;
import org.junit.Test;
import org.trippi.TrippiException;

public class ResultSetTupleIteratorTest extends Assert {

    @Test
    public void test() throws TrippiException {

        final QuerySolutionMap q1 = new QuerySolutionMap();
        final Resource solutionNode = createResource("info:test");
        q1.add("name", solutionNode);
        final QuerySolution q2 = new QuerySolutionMap();
        final QuerySolution q3 = new QuerySolutionMap();
        final QuerySolution q4 = new QuerySolutionMap();
        final Iterator<QuerySolution> i = asList(q1, q2, q3, q4).iterator();

        final ResultSet testResultSet = new ResultSet() {

            @Override
            public boolean hasNext() { return i.hasNext(); }

            @Override
            public QuerySolution next() { return i.next(); }

            @Override
            public QuerySolution nextSolution() { return next(); }

            @Override
            public Binding nextBinding() { throw new UnsupportedOperationException(); }

            @Override
            public int getRowNumber() { throw new UnsupportedOperationException(); }

            @Override
            public List<String> getResultVars() { return asList("name", "rank", "serialNumber"); }

            @Override
            public Model getResourceModel() { throw new UnsupportedOperationException(); }
        };
        final ResultSetTupleIterator results = new ResultSetTupleIterator(testResultSet);
        assertEquals(3, results.names().length);
        final Map<String, Node> solution = results.next();
        assertTrue(solution.containsKey("name"));
        assertEquals(solutionNode.getURI(), solution.get("name").stringValue());
        assertEquals(3, results.count());
    }
}
