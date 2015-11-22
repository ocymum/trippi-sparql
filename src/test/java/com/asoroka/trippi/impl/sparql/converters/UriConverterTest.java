
package com.asoroka.trippi.impl.sparql.converters;

import static com.asoroka.trippi.impl.sparql.converters.UriConverter.uriConverter;
import static java.net.URI.create;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.trippi.impl.RDFFactories.createResource;

import org.apache.jena.ext.com.google.common.base.Converter;
import org.apache.jena.graph.Node_URI;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.URIReference;

public class UriConverterTest extends TestConversionAndInversion<URIReference, Node_URI> {

    private static final String testURI = "info:test";

    @Override
    protected Converter<URIReference, Node_URI> converter() {
        return uriConverter;
    }

    @Override
    protected URIReference from() throws GraphElementFactoryException {
        return createResource(create(testURI));
    }

    @Override
    protected Node_URI to() {
        return (Node_URI) createURI(testURI);
    }
}
