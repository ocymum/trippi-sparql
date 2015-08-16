/**
 *
 */

package com.asoroka.trippi.impl.jena;

import static java.net.URI.create;
import static org.apache.jena.graph.NodeFactory.createLiteral;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node_Literal;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.Literal;
import org.openrdf.model.URI;
import org.trippi.impl.RDFFactories;

/**
 * @author ajs6f
 *
 */
public class LiteralConverter extends NodeConverter<Literal, Node_Literal> {

    private static final TypeMapper TYPE_MAPPER = TypeMapper.getInstance();

    @Override
    protected Node_Literal doForward(final Literal literal) {
        final String label = literal.getLabel();
        final String lang = literal.getLanguage();
        if (lang != null) {
            // an literal with language
            return (Node_Literal) createLiteral(label, lang);
        }
        final URI datatype = literal.getDatatype();
        if (datatype != null) {
            // a literal with specific datatype
            final RDFDatatype jenaDatatype = TYPE_MAPPER.getTypeByName(datatype.stringValue());
            return (Node_Literal) createLiteral(label, jenaDatatype);
        }
        return (Node_Literal) createLiteral(label);
    }

    @Override
    protected Literal doBackward(final Node_Literal literal) {
        try {
            final String lex = literal.getLiteralLexicalForm();
            final String lang = literal.getLiteralLanguage();
            if (!lang.isEmpty()) {
                return RDFFactories.createLiteral(lex, lang);
            }
            final RDFDatatype datatype = literal.getLiteralDatatype();
            if (datatype != null) {
                return RDFFactories.createLiteral(lex, create(datatype.getURI()));
            }
            return RDFFactories.createLiteral(lex);
        } catch (final GraphElementFactoryException e) {
            throw new GraphElementFactoryRuntimeException(e);
        }
    }
}
