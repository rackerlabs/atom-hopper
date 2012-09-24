package org.atomhopper.postgres.adapter;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Arrays;

@RunWith(Enclosed.class)
public class PostgreSQLTextArrayTest {

    public static class WhenCallingPostgresSQLTextArray {

        private String[] empty = {};
        private String[] singleEntry = {"Cat1"};
        private String[] multiEntry = {"Cat1", "Cat2", "Cat3"};

        private String nullString = "NULL";
        private String emptyString = "{}";
        private String singleString = "{Cat1}";
        private String multiString = "{Cat1,Cat2,Cat3}";

        private String baseType = "text";

        @Test(expected = UnsupportedOperationException.class)
        public void ShouldGetResultSet() throws Exception {
            PostgreSQLTextArray array = new PostgreSQLTextArray(multiEntry);
            array.getResultSet();
        }

        @Test(expected = UnsupportedOperationException.class)
        public void ShouldGetResultSetByMap() throws Exception {
            PostgreSQLTextArray array = new PostgreSQLTextArray(multiEntry);
            array.getResultSet(null);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void ShouldGetResultSetByIndexAndCount() throws Exception {
            PostgreSQLTextArray array = new PostgreSQLTextArray(multiEntry);
            array.getResultSet(1, 0);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void ShouldGetResultSetByIndexAndCountByMap() throws Exception {
            PostgreSQLTextArray array = new PostgreSQLTextArray(multiEntry);
            array.getResultSet(1, 0, null);
        }

        @Test
        public void ShouldGetBaseType() throws Exception {
            PostgreSQLTextArray array = new PostgreSQLTextArray(multiEntry);
            assertEquals(array.getBaseType(), java.sql.Types.VARCHAR);
        }

        @Test
        public void ShouldGetBaseTypeName() throws Exception {
            PostgreSQLTextArray array = new PostgreSQLTextArray(multiEntry);
            assertEquals(array.getBaseTypeName(), baseType);
        }

        @Test
        public void ShouldGetArray() throws Exception {
            PostgreSQLTextArray array = new PostgreSQLTextArray(multiEntry);
            String[] s = (String[])array.getArray();
            assertTrue(Arrays.equals(s, multiEntry));
        }

        @Test
        public void ShouldGetArrayByMap() throws Exception {
            PostgreSQLTextArray array = new PostgreSQLTextArray(multiEntry);
            String[] s = (String[])array.getArray(null);
            assertTrue(Arrays.equals(s, multiEntry));
        }

        @Test
        public void ShouldGetArrayByIndexAndCount() throws Exception {
            PostgreSQLTextArray array = new PostgreSQLTextArray(multiEntry);
            String[] s = (String[])array.getArray(0, 1);
            assertTrue(Arrays.equals(s, singleEntry));
        }

        @Test
        public void ShouldGetArrayByIndexAndCountByMap() throws Exception {
            PostgreSQLTextArray array = new PostgreSQLTextArray(multiEntry);
            String[] s = (String[])array.getArray(0, 1, null);
            assertTrue(Arrays.equals(s, singleEntry));
        }

        @Test
        public void ShouldGetArrayForNull() throws Exception {
            PostgreSQLTextArray array = new PostgreSQLTextArray(null);
            assertNull(array.getArray());
        }

        @Test
        public void ShouldGetArrayByMapForNull() throws Exception {
            PostgreSQLTextArray array = new PostgreSQLTextArray(null);
            assertNull(array.getArray(null));
        }

        @Test
        public void ShouldGetArrayByIndexAndCountForNull() throws Exception {
            PostgreSQLTextArray array = new PostgreSQLTextArray(null);
            assertNull(array.getArray(0, 1));
        }

        @Test
        public void ShouldGetArrayByIndexAndCountByMapForNull() throws Exception {
            PostgreSQLTextArray array = new PostgreSQLTextArray(null);
            assertNull(array.getArray(0, 1, null));
        }

        @Test
        public void ShouldGetToString() throws Exception {
            PostgreSQLTextArray nullArray = new PostgreSQLTextArray(null);
            PostgreSQLTextArray emptyArray = new PostgreSQLTextArray(empty);
            PostgreSQLTextArray singleArray = new PostgreSQLTextArray(singleEntry);
            PostgreSQLTextArray multiArray = new PostgreSQLTextArray(multiEntry);

            assertEquals(nullArray.toString(), nullString);
            assertEquals(emptyArray.toString(), emptyString);
            assertEquals(singleArray.toString(), singleString);
            assertEquals(multiArray.toString(), multiString);
        }

        @Test
        public void ShouldHandleStaticMethod() throws Exception {
            assertEquals(PostgreSQLTextArray.stringArrayToPostgreSQLTextArray(null), nullString);
            assertEquals(PostgreSQLTextArray.stringArrayToPostgreSQLTextArray(empty), emptyString);
            assertEquals(PostgreSQLTextArray.stringArrayToPostgreSQLTextArray(singleEntry), singleString);
            assertEquals(PostgreSQLTextArray.stringArrayToPostgreSQLTextArray(multiEntry), multiString);
        }

    }
}
