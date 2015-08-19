
package com.asoroka.trippi.impl.sparqlupdate.converters;

import static java.net.URI.create;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.trippi.impl.RDFFactories.createResource;

import org.apache.jena.ext.com.google.common.base.Converter;
import org.apache.jena.graph.Node;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.PredicateNode;

public class PredicateConverterTest extends TestConversionAndInversion<PredicateNode, Node> {

    private static final PredicateConverter predicateConverter = new PredicateConverter();

    private static final String testURI = "info:test";

    @Override
    protected Converter<PredicateNode, Node> converter() {
        return predicateConverter;
    }

    @Override
    protected PredicateNode from() throws GraphElementFactoryException {
        return createResource(create(testURI));
    }

    @Override
    protected Node to() {
        return createURI(testURI);
    }
}
