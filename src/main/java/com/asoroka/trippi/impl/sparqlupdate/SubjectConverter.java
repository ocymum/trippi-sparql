
package com.asoroka.trippi.impl.sparqlupdate;

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

    private final UriConverter uriConverter = new UriConverter();

    private final BlankNodeConverter bnodeConverter = new BlankNodeConverter();

    @Override
    protected Node doForward(final SubjectNode subject) {
        return subject.isURIReference() ? uriConverter.convert((URIReference) subject) : bnodeConverter.convert(
                (BlankNode) subject);
    }

    @Override
    protected SubjectNode doBackward(final Node subject) {
        return subject.isURI() ? uriConverter.reverse().convert((Node_URI) subject) : bnodeConverter.reverse().convert(
                (Node_Blank) subject);
    }

}
