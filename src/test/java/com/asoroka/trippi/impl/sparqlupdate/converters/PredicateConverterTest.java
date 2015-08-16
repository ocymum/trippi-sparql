
package com.asoroka.trippi.impl.sparqlupdate.converters;

import static java.net.URI.create;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.trippi.impl.RDFFactories.createResource;

import org.apache.jena.graph.Node_URI;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.URIReference;
import org.junit.Assert;
import org.junit.Test;

import com.asoroka.trippi.impl.sparqlupdate.converters.PredicateConverter;

public class PredicateConverterTest extends Assert {

    private static final PredicateConverter predicateConverter = new PredicateConverter();

    private static final String testURI = "info:test";

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
    public void testUriPredicate() {
        assertEquals(testURIReference.getURI().toString(), testNodeURI.getURI());
        assertEquals(testNodeURI, predicateConverter.convert(testURIReference));
        assertEquals(testURIReference, predicateConverter.reverse().convert(testNodeURI));
        assertEquals(testURIReference, predicateConverter.reverse().convert(predicateConverter.convert(
                testURIReference)));
        assertEquals(testNodeURI, predicateConverter.convert(predicateConverter.reverse().convert(testNodeURI)));
    }
}
