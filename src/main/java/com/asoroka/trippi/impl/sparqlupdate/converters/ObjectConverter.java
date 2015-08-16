
package com.asoroka.trippi.impl.sparqlupdate.converters;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Node_URI;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.URIReference;

/**
 * For use with RDF object nodes.
 *
 * @see NodeConverter
 * @author A. Soroka
 */
public class ObjectConverter extends NodeConverter<ObjectNode, Node> {

    private final UriConverter uriConverter = new UriConverter();

    private final BlankNodeConverter bnodeConverter = new BlankNodeConverter();

    private final LiteralConverter literalConverter = new LiteralConverter();

    @Override
    protected Node doForward(final ObjectNode object) {
        if (object.isURIReference()) return uriConverter.convert((URIReference) object);
        if (object.isBlankNode()) return bnodeConverter.convert((BlankNode) object);
        return literalConverter.convert((Literal) object);
    }

    @Override
    protected ObjectNode doBackward(final Node object) {
        if (object.isURI()) return uriConverter.reverse().convert((Node_URI) object);
        if (object.isBlank()) return bnodeConverter.reverse().convert((Node_Blank) object);
        return literalConverter.reverse().convert((Node_Literal) object);
    }
}
