
package com.asoroka.trippi.impl.sparqlupdate.converters;

import static java.net.URI.create;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.trippi.impl.RDFFactories.createResource;

import org.apache.jena.graph.Node_URI;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.URIReference;
import org.junit.Assert;
import org.junit.Test;

import com.asoroka.trippi.impl.sparqlupdate.converters.UriConverter;

public class UriConverterTest extends Assert {

    private static final String testURI = "info:test";

    private static final UriConverter uriConverter = new UriConverter();

    private static final URIReference testURIReference;

    private static final Node_URI testNodeURI = (Node_URI) createURI(testURI);

    static {
        try {
            testURIReference = createResource(create(testURI));
        } catch (final GraphElementFactoryException e) {
            throw new AssertionError();
        }

    }

    @Test
    public void testEqual() {
        assertEquals(testURIReference.toString(), testNodeURI.toString());
        assertEquals(testNodeURI, uriConverter.convert(testURIReference));
        assertEquals(testURIReference, uriConverter.reverse().convert(testNodeURI));
    }

    @Test
    public void testInvertible() {
        assertEquals(testURIReference, uriConverter.reverse().convert(uriConverter.convert(testURIReference)));
        assertEquals(testNodeURI, uriConverter.convert(uriConverter.reverse().convert(testNodeURI)));
    }
}
