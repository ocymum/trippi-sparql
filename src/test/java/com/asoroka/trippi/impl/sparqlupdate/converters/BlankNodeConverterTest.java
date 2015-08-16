
package com.asoroka.trippi.impl.sparqlupdate.converters;

import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.trippi.impl.RDFFactories.createResource;

import org.apache.jena.graph.Node_Blank;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.GraphElementFactoryException;
import org.junit.Assert;
import org.junit.Test;

import com.asoroka.trippi.impl.sparqlupdate.converters.BlankNodeConverter;

public class BlankNodeConverterTest extends Assert {

    private static final BlankNodeConverter bnodeConverter = new BlankNodeConverter();

    private static final BlankNode testBlankNode;

    private static final Node_Blank testNodeBlank;

    static {
        try {
            testBlankNode = createResource();
            testNodeBlank = (Node_Blank) createBlankNode(testBlankNode.getID());
        } catch (final GraphElementFactoryException e) {
            throw new AssertionError();
        }
    }

    @Test
    public void testEqual() {
        assertEquals(testBlankNode.getID(), testNodeBlank.getBlankNodeLabel());
        assertEquals(testNodeBlank, bnodeConverter.convert(testBlankNode));
        assertEquals(testBlankNode, bnodeConverter.reverse().convert(testNodeBlank));
    }

    @Test
    public void testInvertible() {
        assertEquals(testBlankNode, bnodeConverter.reverse().convert(bnodeConverter.convert(testBlankNode)));
        assertEquals(testNodeBlank, bnodeConverter.convert(bnodeConverter.reverse().convert(testNodeBlank)));
    }
}
