
package com.asoroka.trippi.impl.sparqlupdate.converters;

import static java.net.URI.create;
import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.trippi.impl.RDFFactories.createResource;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Node_URI;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;
import org.junit.Assert;
import org.junit.Test;
import org.trippi.impl.RDFFactories;

import com.asoroka.trippi.impl.sparqlupdate.converters.ObjectConverter;

public class ObjectConverterTest extends Assert {

    private static final ObjectConverter objectConverter = new ObjectConverter();

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
    public void testUriObject() {
        assertEquals(testURIReference.getURI().toString(), testNodeURI.getURI());
        assertEquals(testNodeURI, objectConverter.convert(testURIReference));
        assertEquals(testURIReference, objectConverter.reverse().convert(testNodeURI));
        assertEquals(testURIReference, objectConverter.reverse().convert(objectConverter.convert(testURIReference)));
        assertEquals(testNodeURI, objectConverter.convert(objectConverter.reverse().convert(testNodeURI)));
    }

    @Test
    public void testBlankObject() {
        assertEquals(testBlankNode.getID(), testNodeBlank.getBlankNodeLabel());
        assertEquals(testNodeBlank, objectConverter.convert(testBlankNode));
        assertEquals(testBlankNode, objectConverter.reverse().convert(testNodeBlank));
        assertEquals(testBlankNode, objectConverter.reverse().convert(objectConverter.convert(testBlankNode)));
        assertEquals(testNodeBlank, objectConverter.convert(objectConverter.reverse().convert(testNodeBlank)));
    }

    @Test
    public void testLiteralObject() throws GraphElementFactoryException {
        final String simple = "Simple literal.";
        final Literal jrdfLiteral = RDFFactories.createLiteral(simple);
        final Node_Literal jenaLiteral = (Node_Literal) NodeFactory.createLiteral(simple);
        assertEquals(jrdfLiteral.getLexicalForm(), jenaLiteral.getLiteralLexicalForm());
        assertEquals(jenaLiteral, objectConverter.convert(jrdfLiteral));
        assertEquals(jrdfLiteral, objectConverter.reverse().convert(jenaLiteral));
        assertEquals(jrdfLiteral, objectConverter.reverse().convert(objectConverter.convert(jrdfLiteral)));
        assertEquals(jenaLiteral, objectConverter.convert(objectConverter.reverse().convert(jenaLiteral)));
    }
}
