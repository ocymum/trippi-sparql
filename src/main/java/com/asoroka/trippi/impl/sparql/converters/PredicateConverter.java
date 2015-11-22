
package com.asoroka.trippi.impl.sparql.converters;

import static com.asoroka.trippi.impl.sparql.converters.UriConverter.uriConverter;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_URI;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.URIReference;

/**
 * For use with RDF predicate nodes.
 *
 * @see NodeConverter
 * @author A. Soroka
 */
public class PredicateConverter extends NodeConverter<PredicateNode, Node> {

    public static final PredicateConverter predicateConverter = new PredicateConverter();

    private PredicateConverter() {}

    @Override
    protected Node doForward(final PredicateNode p) {
        return uriConverter.convert((URIReference) p);
    }

    @Override
    protected PredicateNode doBackward(final Node p) {
        return uriConverter.reverse().convert((Node_URI) p);
    }

}
