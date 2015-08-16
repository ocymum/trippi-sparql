package com.asoroka.trippi.impl.jena;

import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.trippi.impl.RDFFactories.createResource;

import org.apache.jena.graph.Node_Blank;
import org.jrdf.graph.BlankNode;

public class BlankNodeConverter extends NodeConverter<BlankNode, Node_Blank> {

    @Override
    protected Node_Blank doForward(final BlankNode bnode) {
        return (Node_Blank) createBlankNode(bnode.getID());
    }

    @Override
    protected BlankNode doBackward(final Node_Blank bnode) {
        return createResource(bnode.hashCode());
    }
}
