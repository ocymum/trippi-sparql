
package com.asoroka.trippi.impl.jena;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Node_URI;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.URIReference;

public class ObjectConverter extends NodeConverter<ObjectNode, Node> {

    private final UriConverter uriConverter = new UriConverter();

    private final BlankNodeConverter bnodeConverter = new BlankNodeConverter();

    private final LiteralConverter literalConverter = new LiteralConverter();

    @Override
    protected Node doForward(final ObjectNode object) {
        if (object.isURIReference()) return uriConverter.convert((URIReference) object);
        if (object.isBlankNode()) return bnodeConverter.convert((BlankNode) object);
        if (object.isLiteral()) return literalConverter.convert((Literal) object);
        throw new AssertionError("Discovered an RDF node that is neither URI, blank, nor literal!");
    }

    @Override
    protected ObjectNode doBackward(final Node object) {
        if (object.isURI()) return uriConverter.reverse().convert((Node_URI) object);
        if (object.isBlank()) return bnodeConverter.reverse().convert((Node_Blank) object);
        if (object.isLiteral()) return literalConverter.reverse().convert((Node_Literal) object);
        throw new AssertionError("Discovered an RDF node that is neither URI, blank, nor literal!");
    }
}
