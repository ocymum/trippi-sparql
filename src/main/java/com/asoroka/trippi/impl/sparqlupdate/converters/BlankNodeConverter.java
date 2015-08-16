
package com.asoroka.trippi.impl.sparqlupdate.converters;

import static org.apache.jena.graph.NodeFactory.createBlankNode;

import org.apache.jena.graph.Node_Blank;
import org.jrdf.graph.AbstractBlankNode;
import org.jrdf.graph.BlankNode;

/**
 * @see NodeConverter
 * @author A. Soroka
 */
public class BlankNodeConverter extends NodeConverter<BlankNode, Node_Blank> {

    @Override
    protected Node_Blank doForward(final BlankNode bnode) {
        return (Node_Blank) createBlankNode(bnode.getID());
    }

    @Override
    protected BlankNode doBackward(final Node_Blank bnode) {
        return new LabeledBlankNode(bnode.getBlankNodeLabel());
    }

    public static class LabeledBlankNode extends AbstractBlankNode {

        private final String label;

        public LabeledBlankNode(final String l) {
            this.label = l;
        }

        @Override
        public String getID() {
            return label;
        }

        private static final long serialVersionUID = 1L;

    }
}
