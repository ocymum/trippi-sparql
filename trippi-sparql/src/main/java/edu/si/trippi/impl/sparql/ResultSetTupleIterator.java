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

import static edu.si.trippi.impl.sparql.converters.BlankNodeConverter.blankNodeConverter;
import static edu.si.trippi.impl.sparql.converters.LiteralConverter.literalConverter;
import static edu.si.trippi.impl.sparql.converters.UriConverter.uriConverter;
import static java.util.stream.Collectors.toMap;
import static org.apache.jena.atlas.iterator.Iter.asStream;

import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.jrdf.graph.Node;
import org.trippi.TupleIterator;

/**
 * Implements {@link TupleIterator} by wrapping {@link ResultSet}.
 *
 * @author A. Soroka
 */
public class ResultSetTupleIterator extends TupleIterator {

    private final ResultSet results;

    /**
     * Default constructor.
     *
     * @param r the {@code ResultSet} to wrap
     */
    public ResultSetTupleIterator(final ResultSet r) {
        this.results = r;
    }

    @Override
    public boolean hasNext() {
        return results.hasNext();
    }

    @Override
    public Map<String, Node> next() {
        final QuerySolution solution = results.next();
        return asStream(solution.varNames()).collect(toMap(name -> name, name -> {
            final RDFNode rdfNode = solution.get(name);
            final org.apache.jena.graph.Node node = rdfNode.asNode();
            return (Node) (rdfNode.isURIResource() ? uriConverter.reverse().convert((Node_URI) node) : rdfNode
                    .isAnon() ? blankNodeConverter.reverse().convert((Node_Blank) node) : literalConverter.reverse()
                            .convert((Node_Literal) node));
        }));
    }

    @Override
    public String[] names() {
        final List<String> vars = results.getResultVars();
        return vars.toArray(new String[vars.size()]);
    }

    @Override
    public void close() {/* NO OP */}
}