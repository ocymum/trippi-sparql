
package edu.si.trippi.impl.sparql.converters;

import static org.junit.Assert.assertEquals;

import org.apache.jena.ext.com.google.common.base.Converter;
import org.jrdf.graph.GraphElementFactoryException;
import org.junit.Test;

public abstract class TestConversionAndInversion<From, To> {

    protected abstract Converter<From, To> converter();

    protected abstract From from() throws GraphElementFactoryException;

    protected abstract To to();

    @Test
    public void testConversion() throws GraphElementFactoryException {
        assertEquals(to(), converter().convert(from()));
        assertEquals(from(), converter().reverse().convert(to()));
    }

    @Test
    public void testInvertible() throws GraphElementFactoryException {
        assertEquals(to(), converter().convert(converter().reverse().convert(to())));
        assertEquals(from(), converter().reverse().convert(converter().convert(from())));
    }
}
