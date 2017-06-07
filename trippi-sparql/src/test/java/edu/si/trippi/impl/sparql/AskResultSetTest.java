package edu.si.trippi.impl.sparql;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.*;

@RunWith(Parameterized.class)
public class AskResultSetTest extends Assert {

    @Parameters(name="AskResultSetTest with value: {0}")
    public static Boolean[] data() {
        return new Boolean[] { true, false };
    }

    @Parameter
    public boolean value;

    private static final TypeMapper TYPES = TypeMapper.getInstance();
    private static final RDFDatatype BOOLEAN_DATATYPE = TYPES.getTypeByName("http://www.w3.org/2001/XMLSchema#boolean");

    @Test
    public void test() {
        final AskResultSet testResult = new AskResultSet(value);
        assertTrue(testResult.hasNext());
        final QuerySolution sol = testResult.nextSolution();
        final RDFNode ans = sol.get("k0");
        assertTrue(ans.isLiteral());
        final Literal lit = ans.asLiteral();
        assertEquals(BOOLEAN_DATATYPE, lit.getDatatype());
        assertEquals(value, lit.getBoolean());
        assertFalse(testResult.hasNext());
    }
}
