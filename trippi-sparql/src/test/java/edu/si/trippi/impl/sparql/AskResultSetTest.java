package edu.si.trippi.impl.sparql;

import static java.util.Arrays.asList;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.junit.Assert;
import org.junit.Test;

public class AskResultSetTest extends Assert {

    private static final TypeMapper TYPES = TypeMapper.getInstance();
    private static final RDFDatatype BOOLEAN_DATATYPE = TYPES.getTypeByName("http://www.w3.org/2001/XMLSchema#boolean");

    @Test
    public void test() {
        asList(true, false).forEach(value -> {
            AskResultSet testResult = new AskResultSet(value);
            assertTrue(testResult.hasNext());
            QuerySolution sol = testResult.nextSolution();
            RDFNode ans = sol.get("k0");
            assertTrue(ans.isLiteral());
            Literal lit = ans.asLiteral();
            assertEquals(BOOLEAN_DATATYPE, lit.getDatatype());
            assertEquals(value, lit.getBoolean());
            assertFalse(testResult.hasNext());
        });
    }
}
