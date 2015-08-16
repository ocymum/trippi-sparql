package com.asoroka.trippi.impl.jena;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_URI;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.URIReference;

public class PredicateConverter extends NodeConverter<PredicateNode, Node> {

    private final UriConverter uriConverter = new UriConverter();

    @Override
    protected Node doForward(final PredicateNode p) {
        return uriConverter.convert((URIReference) p);
    }

    @Override
    protected PredicateNode doBackward(final Node p) {
        return uriConverter.reverse().convert((Node_URI) p);
    }

}
