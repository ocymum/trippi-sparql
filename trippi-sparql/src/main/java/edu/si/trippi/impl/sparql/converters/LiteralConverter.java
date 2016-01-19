/**
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
        if (lang != null) {
            return (Node_Literal) createLiteral(label, lang);
        }
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
        if (!lang.isEmpty()) {
            return new FreeLiteral(lex, lang);
        }
        final RDFDatatype datatype = literal.getLiteralDatatype();
        if (datatype != null && !datatype.getURI().equals(XSD.xstring.getURI())) {
            return new FreeLiteral(lex, create(datatype.getURI()));
        }
        return new FreeLiteral(lex);
    }
}
