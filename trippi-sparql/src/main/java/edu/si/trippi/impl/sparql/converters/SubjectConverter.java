
package edu.si.trippi.impl.sparql.converters;

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
        return subject.isURIReference() ? UriConverter.uriConverter.convert((URIReference) subject) : BlankNodeConverter.blankNodeConverter.convert(
                (BlankNode) subject);
    }

    @Override
    protected SubjectNode doBackward(final Node subject) {
        return subject.isURI() ? UriConverter.uriConverter.reverse().convert((Node_URI) subject) : BlankNodeConverter.blankNodeConverter.reverse()
                .convert((Node_Blank) subject);
    }
}
