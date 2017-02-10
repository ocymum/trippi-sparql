/*
 * Copyright 2015-2016 Smithsonian Institution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.You may obtain a copy of
 * the License at: http://www.apache.org/licenses/
 *
 * This software and accompanying documentation is supplied without
 * warranty of any kind. The copyright holder and the Smithsonian Institution:
 * (1) expressly disclaim any warranties, express or implied, including but not
 * limited to any implied warranties of merchantability, fitness for a
 * particular purpose, title or non-infringement; (2) do not assume any legal
 * liability or responsibility for the accuracy, completeness, or usefulness of
 * the software; (3) do not represent that use of the software would not
 * infringe privately owned rights; (4) do not warrant that the software
 * is error-free or will be maintained, supported, updated or enhanced;
 * (5) will not be liable for any indirect, incidental, consequential special
 * or punitive damages of any kind or nature, including but not limited to lost
 * profits or loss of data, on any basis arising from contract, tort or
 * otherwise, even if any of the parties has been warned of the possibility of
 * such loss or damage.
 *
 * This distribution includes several third-party libraries, each with their own
 * license terms. For a complete copy of all copyright and license terms, including
 * those of third-party libraries, please see the product release notes.
 *
 */

package edu.si.trippi.impl.sparql;

import static java.util.Collections.singletonList;
import static org.apache.jena.ext.com.google.common.collect.Iterators.singletonIterator;
import static org.apache.jena.graph.NodeFactory.createLiteralByValue;
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.apache.jena.sparql.engine.binding.BindingFactory.binding;

import java.util.List;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ResultSetStream;

/**
 * A simple {@link org.apache.jena.query.ResultSet} which presents a single boolean value.
 * 
 * @author ajs6f
 *
 */
public class AskResultSet extends ResultSetStream {

    private static final Model DEFAULT_MODEL = createDefaultModel();

    private static final TypeMapper types = TypeMapper.getInstance();
    private static final RDFDatatype DATATYPE = types.getTypeByClass(Boolean.class);
    private static final Node TRUE = createLiteralByValue(true, DATATYPE);
    private static final Node FALSE = createLiteralByValue(false, DATATYPE);

    private static final String VARIABLE_NAME = "k0";
    private static final Var VAR = Var.alloc(VARIABLE_NAME);
    private static final List<String> names = singletonList(VARIABLE_NAME);

    public AskResultSet(Boolean result) {
        super(names, DEFAULT_MODEL, singletonIterator(binding(VAR, result ? TRUE : FALSE)));
    }
}
