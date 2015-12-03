
package com.asoroka.trippi.impl.sparql.converters;

import static java.net.URI.create;
import static org.apache.jena.graph.NodeFactory.createURI;

import org.apache.jena.graph.Node_URI;
import org.jrdf.graph.URIReference;
import org.trippi.impl.FreeURIReference;

/**
 * @see NodeConverter
 * @author A. Soroka
 */
public class UriConverter extends NodeConverter<URIReference, Node_URI> {

    public static final UriConverter uriConverter = new UriConverter();

    private UriConverter() {}

    @Override
    protected Node_URI doForward(final URIReference uri) {
        return (Node_URI) createURI(uri.toString());
    }

    @Override
    protected URIReference doBackward(final Node_URI uri) {
        return new FreeURIReference(create(uri.getURI()));
    }
}
