
package edu.si.trippi.impl.sparql.converters;

import static edu.si.trippi.impl.sparql.converters.BlankNodeConverter.blankNodeConverter;
import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.trippi.impl.RDFFactories.createResource;

import org.apache.jena.ext.com.google.common.base.Converter;
import org.apache.jena.graph.Node_Blank;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.GraphElementFactoryException;

public class BlankNodeConverterTest extends TestConversionAndInversion<BlankNode, Node_Blank> {

    private static final BlankNode testBlankNode;

    private static final Node_Blank testNodeBlank;

    static {
        try {
            testBlankNode = createResource();
            testNodeBlank = (Node_Blank) createBlankNode(testBlankNode.getID());
        } catch (final GraphElementFactoryException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    protected Converter<BlankNode, Node_Blank> converter() {
        return blankNodeConverter;
    }

    @Override
    protected BlankNode from() {
        return testBlankNode;
    }

    @Override
    protected Node_Blank to() {
        return testNodeBlank;
    }
}
