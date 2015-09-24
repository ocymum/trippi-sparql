
package com.asoroka.trippi.impl.sparqlupdate.converters;

import static java.net.URI.create;
import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.trippi.impl.RDFFactories.createResource;

import org.apache.jena.ext.com.google.common.base.Converter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Blank;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.ObjectNode;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.trippi.impl.RDFFactories;

import com.asoroka.trippi.impl.sparqlupdate.converters.ObjectConverterTest.BlankObject;
import com.asoroka.trippi.impl.sparqlupdate.converters.ObjectConverterTest.LiteralObject;
import com.asoroka.trippi.impl.sparqlupdate.converters.ObjectConverterTest.UriObject;

@RunWith(Suite.class)
@SuiteClasses({UriObject.class, BlankObject.class, LiteralObject.class})
public class ObjectConverterTest {

    static final ObjectConverter objectConverter = new ObjectConverter();

    public static class UriObject extends TestConversionAndInversion<ObjectNode, Node> {

        @Override
        protected Converter<ObjectNode, Node> converter() {
            return objectConverter;
        }

        private static final String testURI = "info:test";

        @Override
        protected ObjectNode from() throws GraphElementFactoryException {
            return createResource(create(testURI));
        }

        @Override
        protected Node to() {
            return createURI(testURI);
        }
    }

    public static class BlankObject extends TestConversionAndInversion<ObjectNode, Node> {

        @Override
        protected Converter<ObjectNode, Node> converter() {
            return objectConverter;
        }

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
        protected ObjectNode from() {
            return testBlankNode;
        }

        @Override
        protected Node to() {
            return testNodeBlank;
        }
    }

    public static class LiteralObject extends TestConversionAndInversion<ObjectNode, Node> {

        @Override
        protected Converter<ObjectNode, Node> converter() {
            return objectConverter;
        }

        final String simple = "Simple literal.";

        @Override
        protected ObjectNode from() throws GraphElementFactoryException {
            return RDFFactories.createLiteral(simple);
        }

        @Override
        protected Node to() {
            return NodeFactory.createLiteral(simple);
        }
    }
}
