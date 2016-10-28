
package edu.si.trippi.impl.sparql.converters;

import static java.net.URI.create;
import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.trippi.impl.RDFFactories.createResource;

import org.apache.jena.ext.com.google.common.base.Converter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Blank;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.SubjectNode;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.si.trippi.impl.sparql.converters.SubjectConverterTest.BlankSubject;
import edu.si.trippi.impl.sparql.converters.SubjectConverterTest.UriSubject;

@RunWith(Suite.class)
@SuiteClasses({UriSubject.class, BlankSubject.class})
public class SubjectConverterTest {

    public static class UriSubject extends TestConversionAndInversion<SubjectNode, Node> {

        @Override
        protected Converter<SubjectNode, Node> converter() {
            return SubjectConverter.subjectConverter;
        }

        private static final String testURI = "info:test";

        @Override
        protected SubjectNode from() throws GraphElementFactoryException {
            return createResource(create(testURI));
        }

        @Override
        protected Node to() {
            return createURI(testURI);
        }
    }

    public static class BlankSubject extends TestConversionAndInversion<SubjectNode, Node> {

        @Override
        protected Converter<SubjectNode, Node> converter() {
            return SubjectConverter.subjectConverter;
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
        protected SubjectNode from() {
            return testBlankNode;
        }

        @Override
        protected Node to() {
            return testNodeBlank;
        }
    }
}
