
package com.asoroka.trippi.impl.sparqlupdate;

import org.apache.jena.ext.com.google.common.base.Converter;
import org.apache.jena.graph.Node;

/**
 * Subtypes of this class convert any RDF node in the JRDF system to the same
 * RDF node in the Jena system, and vice versa. Subtypes of this class are
 * required to be fully invertible. Blank node conversion must occur via some
 * invertible transformation of label, which means that subtypes of this class
 * may NOT take responsibility for the scope of blank nodes.
 *
 * @author A. Soroka
 * @param <JRDFNode> a particular type of RDF node in the JRDF system
 * @param <JenaNode> a particular type of RDF node in the Jena system
 */
public abstract class NodeConverter<JRDFNode extends org.jrdf.graph.Node, JenaNode extends Node> extends
Converter<JRDFNode, JenaNode> {
    // THIS PAGE INTENTIONALLY LEFT BLANK
}
