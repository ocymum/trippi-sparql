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
