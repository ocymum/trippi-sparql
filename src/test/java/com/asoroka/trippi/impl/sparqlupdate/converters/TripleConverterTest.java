
package com.asoroka.trippi.impl.sparqlupdate.converters;

import static java.net.URI.create;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.graph.Triple.create;
import static org.trippi.impl.RDFFactories.createResource;
import static org.trippi.impl.RDFFactories.createTriple;

import org.apache.jena.graph.Triple;
import org.jrdf.graph.GraphElementFactoryException;
import org.junit.Assert;
import org.junit.Test;

import com.asoroka.trippi.impl.sparqlupdate.converters.TripleConverter;

public class TripleConverterTest extends Assert {

    private static final TripleConverter tripleConverter = new TripleConverter();

    private static final String subject = "info:subject";

    private static final String predicate = "info:predicate";

    private static final String object = "info:object";

    private static final Triple jenaTriple = create(createURI(subject), createURI(predicate), createURI(object));

    private static final org.jrdf.graph.Triple jrdfTriple;

    static {
        try {
            jrdfTriple = createTriple(createResource(create(subject)), createResource(create(predicate)),
                    createResource(create(object)));
        } catch (final GraphElementFactoryException e) {
            throw new AssertionError();
        }
    }

    @Test
    public void testEquals() {
        assertEquals(jenaTriple, tripleConverter.convert(jrdfTriple));
        assertEquals(jrdfTriple, tripleConverter.reverse().convert(jenaTriple));
    }

    @Test public void testInvertible(){
        assertEquals(jenaTriple, tripleConverter.convert(tripleConverter.reverse().convert(jenaTriple)));
        assertEquals(jrdfTriple, tripleConverter.reverse().convert(tripleConverter.convert(jrdfTriple)));
    }
}
