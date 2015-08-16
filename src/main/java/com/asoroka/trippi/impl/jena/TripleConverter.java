
package com.asoroka.trippi.impl.jena;

import static org.apache.jena.graph.Triple.create;
import static org.trippi.impl.RDFFactories.createTriple;

import org.apache.jena.ext.com.google.common.base.Converter;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.Triple;

public class TripleConverter extends Converter<Triple, org.apache.jena.graph.Triple> {

    private final SubjectConverter subjectConverter = new SubjectConverter();

    private final ObjectConverter objectConverter = new ObjectConverter();

    private final PredicateConverter predicateConverter = new PredicateConverter();

    @Override
    protected org.apache.jena.graph.Triple doForward(final Triple t) {
        return create(subjectConverter.convert(t.getSubject()),
                predicateConverter.convert(t.getPredicate()),
                objectConverter.convert(t.getObject()));
    }

    @Override
    protected Triple doBackward(final org.apache.jena.graph.Triple t) {
        try {
            return createTriple(subjectConverter.reverse().convert(t.getSubject()),
                    predicateConverter.reverse().convert(t.getPredicate()),
                    objectConverter.reverse().convert(t.getObject()));
        } catch (final GraphElementFactoryException e) {
            throw new GraphElementFactoryRuntimeException(e);
        }
    }
}
