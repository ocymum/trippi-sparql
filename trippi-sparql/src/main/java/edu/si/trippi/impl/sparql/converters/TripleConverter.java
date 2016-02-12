
package edu.si.trippi.impl.sparql.converters;

import static edu.si.trippi.impl.sparql.converters.ObjectConverter.objectConverter;
import static edu.si.trippi.impl.sparql.converters.SubjectConverter.subjectConverter;
import static org.apache.jena.graph.Triple.create;

import org.apache.jena.ext.com.google.common.base.Converter;
import org.jrdf.graph.Triple;
import org.trippi.impl.FreeTriple;

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

    public static final TripleConverter tripleConverter = new TripleConverter();

    @Override
    protected org.apache.jena.graph.Triple doForward(final Triple t) {
        return create(subjectConverter.convert(t.getSubject()), PredicateConverter.predicateConverter.convert(t.getPredicate()),
                objectConverter.convert(t.getObject()));
    }

    @Override
    protected Triple doBackward(final org.apache.jena.graph.Triple t) {
        return new FreeTriple(subjectConverter.reverse().convert(t.getSubject()), PredicateConverter.predicateConverter.reverse().convert(t
                .getPredicate()), objectConverter.reverse().convert(t.getObject()));
    }
}
