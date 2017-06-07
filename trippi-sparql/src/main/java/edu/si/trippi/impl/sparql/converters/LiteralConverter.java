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

package edu.si.trippi.impl.sparql.converters;

import static java.net.URI.create;
import static org.apache.jena.graph.NodeFactory.createLiteral;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.vocabulary.XSD;
import org.jrdf.graph.Literal;
import org.openrdf.model.URI;
import org.trippi.impl.FreeLiteral;

/**
 * @see NodeConverter
 * @author A. Soroka
 */
public class LiteralConverter extends NodeConverter<Literal, Node_Literal> {

    /**
     * Maps between URIs and {@link RDFDatatype}s in Jena.
     */
    private static final TypeMapper TYPE_MAPPER = TypeMapper.getInstance();

    public static final LiteralConverter literalConverter = new LiteralConverter();

    private LiteralConverter() {}

    @Override
    protected Node_Literal doForward(final Literal literal) {
        final String label = literal.getLabel();
        final String lang = literal.getLanguage();
        if (lang != null) return (Node_Literal) createLiteral(label, lang);
        final URI datatype = literal.getDatatype();
        if (datatype != null) {
            RDFDatatype jenaDatatype = TYPE_MAPPER.getTypeByName(datatype.stringValue());
            if (jenaDatatype == null) {
                // new datatype
                TYPE_MAPPER.registerDatatype(new BaseDatatype(datatype.stringValue()));
                jenaDatatype = TYPE_MAPPER.getTypeByName(datatype.stringValue());
            }
            return (Node_Literal) createLiteral(label, jenaDatatype);
        }
        return (Node_Literal) createLiteral(label);
    }

    @Override
    protected Literal doBackward(final Node_Literal literal) {
        final String lex = literal.getLiteralLexicalForm();
        final String lang = literal.getLiteralLanguage();
        if (!lang.isEmpty()) return new FreeLiteral(lex, lang);
        final RDFDatatype datatype = literal.getLiteralDatatype();
        if (datatype != null && !datatype.getURI().equals(XSD.xstring.getURI()))
            return new FreeLiteral(lex, create(datatype.getURI()));
        return new FreeLiteral(lex);
    }
}
