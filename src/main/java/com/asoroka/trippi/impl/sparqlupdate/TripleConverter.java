
package com.asoroka.trippi.impl.sparqlupdate;

import static org.apache.jena.graph.Triple.create;
import static org.trippi.impl.RDFFactories.createTriple;

import org.apache.jena.ext.com.google.common.base.Converter;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.Triple;

/**
 * Converts an RDF triple in the JRDF system to the same triple in the Jena
 * system, and vice versa. Subtypes of this class are required to be fully
 * invertible. Blank node conversion must occur via some invertible
 * transformation of label, which means that subtypes of this class may NOT take
 * responsibility for the scope of blank nodes.
 *
 * @author A. Soroka
 */
public class TripleConverter extends Converter<Triple, org.apache.jena.graph.Triple> {

    private final SubjectConverter subjectConverter = new SubjectConverter();

    private final ObjectConverter objectConverter = new ObjectConverter();

    private final PredicateConverter predicateConverter = new PredicateConverter();

    @Override
    protected org.apache.jena.graph.Triple doForward(final Triple t) {
        return create(subjectConverter.convert(t.getSubject()), predicateConverter.convert(t.getPredicate()),
                objectConverter.convert(t.getObject()));
    }

    @Override
    protected Triple doBackward(final org.apache.jena.graph.Triple t) {
        try {
            return createTriple(subjectConverter.reverse().convert(t.getSubject()), predicateConverter.reverse()
                    .convert(t.getPredicate()), objectConverter.reverse().convert(t.getObject()));
        } catch (final GraphElementFactoryException e) {
            throw new GraphElementFactoryRuntimeException(e);
        }
    }
}
