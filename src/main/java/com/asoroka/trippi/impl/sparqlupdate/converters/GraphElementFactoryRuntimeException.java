
package com.asoroka.trippi.impl.sparqlupdate.converters;

import org.jrdf.graph.GraphElementFactoryException;

/**
 * Semantically equal to {@link GraphElementFactoryException} but a subtype of
 * {@link RuntimeException}.
 *
 * @author A. Soroka
 */
public class GraphElementFactoryRuntimeException extends RuntimeException {

    /**
     * Default constructor.
     *
     * @param e
     */
    public GraphElementFactoryRuntimeException(final GraphElementFactoryException e) {
        super(e);
    }

    private static final long serialVersionUID = 1L;

}
