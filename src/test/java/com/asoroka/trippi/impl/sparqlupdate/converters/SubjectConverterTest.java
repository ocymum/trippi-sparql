
package com.asoroka.trippi.impl.sparqlupdate.converters;

import static java.net.URI.create;
import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.trippi.impl.RDFFactories.createResource;

import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Node_URI;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.URIReference;
import org.junit.Assert;
import org.junit.Test;

import com.asoroka.trippi.impl.sparqlupdate.converters.SubjectConverter;

public class SubjectConverterTest extends Assert {

    private static final SubjectConverter subjectConverter = new SubjectConverter();

    private static final String testURI = "info:test";

    private static final URIReference testURIReference;

    private static final Node_URI testNodeURI = (Node_URI) createURI(testURI);

    private static final BlankNode testBlankNode;

    private static final Node_Blank testNodeBlank;

    static {
        try {
            testURIReference = createResource(create(testURI));
            testBlankNode = createResource();
            testNodeBlank = (Node_Blank) createBlankNode(testBlankNode.getID());
        } catch (final GraphElementFactoryException e) {
            throw new AssertionError();
        }
    }

    @Test
    public void testUriSubject() {
        assertEquals(testURIReference.getURI().toString(), testNodeURI.getURI());
        assertEquals(testNodeURI, subjectConverter.convert(testURIReference));
        assertEquals(testURIReference, subjectConverter.reverse().convert(testNodeURI));
        assertEquals(testURIReference, subjectConverter.reverse().convert(subjectConverter.convert(testURIReference)));
        assertEquals(testNodeURI, subjectConverter.convert(subjectConverter.reverse().convert(testNodeURI)));
    }

    @Test
    public void testBlankSubject() {
        assertEquals(testBlankNode.getID(), testNodeBlank.getBlankNodeLabel());
        assertEquals(testNodeBlank, subjectConverter.convert(testBlankNode));
        assertEquals(testBlankNode, subjectConverter.reverse().convert(testNodeBlank));
        assertEquals(testBlankNode, subjectConverter.reverse().convert(subjectConverter.convert(testBlankNode)));
        assertEquals(testNodeBlank, subjectConverter.convert(subjectConverter.reverse().convert(testNodeBlank)));
    }
}
