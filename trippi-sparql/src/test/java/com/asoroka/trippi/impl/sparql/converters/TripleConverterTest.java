
package com.asoroka.trippi.impl.sparql.converters;

import static java.net.URI.create;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.graph.Triple.create;
import static org.trippi.impl.RDFFactories.createResource;
import static org.trippi.impl.RDFFactories.createTriple;

import org.apache.jena.ext.com.google.common.base.Converter;
import org.apache.jena.graph.Triple;
import org.jrdf.graph.GraphElementFactoryException;

import com.asoroka.trippi.impl.sparql.converters.TripleConverter;

public class TripleConverterTest extends TestConversionAndInversion<org.jrdf.graph.Triple,Triple> {

    private static final String subject = "info:subject";

    private static final String predicate = "info:predicate";

    private static final String object = "info:object";

    @Override
    protected Converter<org.jrdf.graph.Triple, Triple> converter() {
        return TripleConverter.tripleConverter;
    }

    @Override
    protected org.jrdf.graph.Triple from() throws GraphElementFactoryException {
        return createTriple(createResource(create(subject)), createResource(create(predicate)),
                createResource(create(object)));
    }

    @Override
    protected Triple to() {
        return create(createURI(subject), createURI(predicate), createURI(object));
    }
}
