
package com.asoroka.trippi.impl.sparqlupdate.converters;

import java.net.URI;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Literal;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.Literal;
import org.junit.Assert;
import org.junit.Test;
import org.trippi.impl.RDFFactories;

public class LiteralConverterTest extends Assert {

    private static final TypeMapper tm = TypeMapper.getInstance();

    private static final LiteralConverter literalConverter = new LiteralConverter();

    @Test
    public void testSimpleLiteral() throws GraphElementFactoryException {
        final String simple = "Simple literal.";
        final Literal jrdfLiteral = RDFFactories.createLiteral(simple);
        final Node_Literal jenaLiteral = (Node_Literal) NodeFactory.createLiteral(simple);
        assertEquals(jrdfLiteral.getLexicalForm(), jenaLiteral.getLiteralLexicalForm());
        assertEquals(jenaLiteral, literalConverter.convert(jrdfLiteral));
        // JRDF does not implement RDF 1.1 literal equality, wherein a literal with no datatype and a literal with the
        // same lexical form but a xsd:string datatype are in fact equal, so we tweak this test to be as close as it
        // can be to correct.
        assertEquals(jrdfLiteral.getLexicalForm(), literalConverter.reverse().convert(jenaLiteral).getLexicalForm());
        assertEquals(jenaLiteral, literalConverter.convert(literalConverter.reverse().convert(jenaLiteral)));
        // JRDF does not implement RDF 1.1 literal equality, wherein a literal with no datatype and a literal with the
        // same lexical form but a xsd:string datatype are in fact equal, so we tweak this test to be as close as it
        // can be to correct.
        assertEquals(jrdfLiteral.getLexicalForm(), literalConverter.reverse().convert(literalConverter.convert(
                jrdfLiteral)).getLexicalForm());
    }

    @Test
    public void testLangLiteral() throws GraphElementFactoryException {
        final String lex = "Lexical form.";
        final String lang = "language";
        final Literal jrdfLiteral = RDFFactories.createLiteral(lex, lang);
        final Node_Literal jenaLiteral = (Node_Literal) NodeFactory.createLiteral(lex, lang);
        assertEquals(jrdfLiteral.getLexicalForm(), jenaLiteral.getLiteralLexicalForm());
        assertEquals(jrdfLiteral.getLanguage(), jenaLiteral.getLiteralLanguage());
        assertEquals(jenaLiteral, literalConverter.convert(jrdfLiteral));
        assertEquals(jrdfLiteral, literalConverter.reverse().convert(jenaLiteral));
        assertEquals(jenaLiteral, literalConverter.convert(literalConverter.reverse().convert(jenaLiteral)));
        assertEquals(jrdfLiteral, literalConverter.reverse().convert(literalConverter.convert(jrdfLiteral)));
    }

    @Test
    public void testTypedLiteral() throws GraphElementFactoryException {
        final String datatypeURI = "http://example.com/datatype";
        final String lex = "7777";
        final Literal jrdfLiteral = RDFFactories.createLiteral(lex, URI.create(datatypeURI));
        final BaseDatatype dtype = new BaseDatatype(datatypeURI);
        tm.registerDatatype(dtype);
        final Node_Literal jenaLiteral = (Node_Literal) NodeFactory.createLiteral(lex, dtype);
        assertEquals(jrdfLiteral.getLexicalForm(), jenaLiteral.getLiteralLexicalForm());
        assertEquals(jenaLiteral, literalConverter.convert(jrdfLiteral));
        assertEquals(jrdfLiteral, literalConverter.reverse().convert(jenaLiteral));
        assertEquals(jenaLiteral, literalConverter.convert(literalConverter.reverse().convert(jenaLiteral)));
        assertEquals(jrdfLiteral, literalConverter.reverse().convert(literalConverter.convert(jrdfLiteral)));
    }

    @Test
    public void testOddlyTypedLiteral() throws GraphElementFactoryException {
        final String datatypeURI = "http://example.com/datatypeOdd";
        final String lex = "7777";
        final Literal jrdfLiteral = RDFFactories.createLiteral(lex, URI.create(datatypeURI));
        final BaseDatatype dtype = new BaseDatatype(datatypeURI);
        final Node_Literal jenaLiteral = (Node_Literal) NodeFactory.createLiteral(lex, dtype);
        assertEquals(jrdfLiteral.getLexicalForm(), jenaLiteral.getLiteralLexicalForm());
        assertTypedEquals(jenaLiteral, literalConverter.convert(jrdfLiteral));
        assertEquals(jrdfLiteral, literalConverter.reverse().convert(jenaLiteral));
        assertTypedEquals(jenaLiteral, literalConverter.convert(literalConverter.reverse().convert(jenaLiteral)));
        assertEquals(jrdfLiteral, literalConverter.reverse().convert(literalConverter.convert(jrdfLiteral)));
    }

    /**
     * Because Jena treats datatypes as singletons, we need to compare
     * serialized values for typed literals when testing the conversion of
     * literals with ad hoc types.
     *
     * @param literal
     * @param converted
     */
    private static void assertTypedEquals(final Node_Literal literal, final Node_Literal converted) {
        assertEquals(literal.getLiteralLexicalForm(), converted.getLiteralLexicalForm());
        assertEquals(literal.getLiteralDatatypeURI(), converted.getLiteralDatatypeURI());
    }
}
