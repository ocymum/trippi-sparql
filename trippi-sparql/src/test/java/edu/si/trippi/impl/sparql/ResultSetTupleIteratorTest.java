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

import static java.util.Arrays.asList;
import static org.apache.jena.rdf.model.ResourceFactory.createPlainLiteral;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.Map;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.jrdf.graph.Node;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.trippi.TrippiException;

@RunWith(MockitoJUnitRunner.class)
public class ResultSetTupleIteratorTest extends Assert {
    
    @Mock
    private ResultSet mockResultSet;
    
    @Mock
    private QueryExecution mockQueryExec;

    @Test
    public void test() throws TrippiException {

        final QuerySolutionMap q1 = new QuerySolutionMap();
        final Resource solutionNode1 = createResource("info:test");
        final Literal solutionNode2 = createPlainLiteral("Fomalhaut");
        final Resource solutionNode3 = createResource();
        q1.add("name", solutionNode1);
        q1.add("rank", solutionNode2);
        q1.add("serialNumber", solutionNode3);

        final QuerySolution q2 = new QuerySolutionMap();
        final QuerySolution q3 = new QuerySolutionMap();
        final QuerySolution q4 = new QuerySolutionMap();
        final Iterator<QuerySolution> i = asList(q1, q2, q3, q4).iterator();

        when(mockResultSet.hasNext()).thenAnswer(inv -> i.hasNext());
        when(mockResultSet.next()).thenAnswer(inv -> i.next());
        when(mockResultSet.getResultVars()).thenReturn(asList("name", "rank", "serialNumber"));

        final Query query = new Query();
        query.setQuerySelectType();

        when(mockQueryExec.execSelect()).thenReturn(mockResultSet);
        when(mockQueryExec.getQuery()).thenReturn(query);


        final ResultSetTupleIterator results = new ResultSetTupleIterator(mockQueryExec);
        assertEquals(3, results.names().length);
        final Map<String, Node> solution = results.next();
        assertTrue(solution.containsKey("name"));
        assertEquals(solutionNode1.getURI(), solution.get("name").stringValue());
        assertTrue(solution.containsKey("rank"));
        assertEquals(solutionNode2.getLexicalForm(), solution.get("rank").stringValue());
        assertTrue(solution.containsKey("serialNumber"));
        assertEquals(solutionNode3.getId().getLabelString(), solution.get("serialNumber").stringValue());

        assertEquals(3, results.count());
        verify(mockQueryExec).close();
    }
}
