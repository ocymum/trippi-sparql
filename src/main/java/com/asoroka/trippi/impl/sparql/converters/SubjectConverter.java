
package com.asoroka.trippi.impl.sparql.converters;

import static com.asoroka.trippi.impl.sparql.converters.BlankNodeConverter.blankNodeConverter;
import static com.asoroka.trippi.impl.sparql.converters.UriConverter.uriConverter;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Node_URI;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.URIReference;

/**
 * For use with RDF subject nodes.
 *
 * @see NodeConverter
 * @author A. Soroka
 */
public class SubjectConverter extends NodeConverter<SubjectNode, Node> {

    public static final SubjectConverter subjectConverter = new SubjectConverter();

    private SubjectConverter() {}

    @Override
    protected Node doForward(final SubjectNode subject) {
        return subject.isURIReference() ? uriConverter.convert((URIReference) subject) : blankNodeConverter.convert(
                (BlankNode) subject);
    }

    @Override
    protected SubjectNode doBackward(final Node subject) {
        return subject.isURI() ? uriConverter.reverse().convert((Node_URI) subject) : blankNodeConverter.reverse()
                .convert((Node_Blank) subject);
    }
}
