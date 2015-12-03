
package com.asoroka.trippi.impl.sparql;

import static com.asoroka.trippi.impl.sparql.converters.BlankNodeConverter.blankNodeConverter;
import static com.asoroka.trippi.impl.sparql.converters.LiteralConverter.literalConverter;
import static com.asoroka.trippi.impl.sparql.converters.UriConverter.uriConverter;
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