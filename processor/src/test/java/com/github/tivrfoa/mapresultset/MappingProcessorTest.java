package com.github.tivrfoa.mapresultset;

import org.joor.CompileOptions;
import org.joor.Reflect;
import org.joor.ReflectException;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

public class MappingProcessorTest {
    @Test
    public void testGetDefaultValueForType() {

    }

    @Test
    public void testUppercaseFirstLetter() {
        assertEquals("Oi", MappingProcessor.uppercaseFirstLetter("oi"));
    }

    @Test
    public void testQueryNotFinal() {
        MappingProcessor p = new MappingProcessor();

        try {
            Reflect.compile(
                "org.joor.test.FailAnnotationProcessing",
                """
                package com.github.tivrfoa.mapresultset;

                import com.github.tivrfoa.mapresultset.api.Query;

                class QueryNotFinal {
                    @Query
                    String sql = "select id from phone";
                }
                """,
                new CompileOptions().processors(p)
            ).create().get();
            throw new RuntimeException("Failed to throw exception on a non-final query.");
        }
        catch (ReflectException expected) {
           assertEquals("java.lang.RuntimeException: Variable annotated with @Query must be final and not null",
                expected.getCause().getMessage());
        }
    }

    @Test
    public void testQueryNull() {
        MappingProcessor p = new MappingProcessor();

        try {
            Reflect.compile(
                "org.joor.test.FailAnnotationProcessing",
                """
                package com.github.tivrfoa.mapresultset;

                import com.github.tivrfoa.mapresultset.api.Query;

                class QueryNull {
                    @Query
                    final String sql = null;
                }
                """,
                new CompileOptions().processors(p)
            ).create().get();
            throw new RuntimeException("Failed to throw exception on a null query.");
        }
        catch (ReflectException expected) {
           assertEquals("java.lang.RuntimeException: Variable annotated with @Query must be final and not null",
                expected.getCause().getMessage());
        }
    }

    /**
     * FIXME this query should not fail
     * 
     */
    /*@Test
    public void testQueryFromOneTableAndColumnWithoutAlias() {
        MappingProcessor p = new MappingProcessor();

        try {
            Reflect.compile(
                "org.joor.test.FailAnnotationProcessing",
                """
                package com.github.tivrfoa.mapresultset;

                import com.github.tivrfoa.mapresultset.api.Query;

                class QueryPhoneId {
                    @Query
                    final String listPhones = "select id, phone.id from Phone as phone";
                }
                """,
                new CompileOptions().processors(p)
            ).create().get();
            throw new RuntimeException("Failed to throw exception on a non-final query.");
        }
        catch (ReflectException expected) {
            expected.printStackTrace();
           assertEquals("java.lang.RuntimeException: Variable annotated with @Query must be final and not null",
                expected.getCause().getMessage());
        }
    }*/

    @Test
    public void testQueryMoreThanOneTableAndNoAliasInColumn() throws IOException {
        MappingProcessor p = new MappingProcessor();

        URL url = getClass().getResource("/Phone.java");
        final String phoneClass = Files.readString(new File(url.getPath()).toPath());

        try {
            Reflect.compile(
                "org.joor.test.FailAnnotationProcessing",
                """
                package com.github.tivrfoa.mapresultset;

                import com.github.tivrfoa.mapresultset.api.Query;
                import com.github.tivrfoa.mapresultset.api.Table;

                %s

                class QueryPhone {
                    @Query
                    final String listPhones = \"""
                        select Phone.id, wakeup_time
                        from Phone join person as person
                        on Phone.person_id = person.id
                    \""";
                }
                """.formatted(phoneClass),
                new CompileOptions().processors(p)
            ).create().get();
            throw new RuntimeException("Failed to throw exception.");
        }
        catch (ReflectException expected) {
           assertEquals("java.lang.RuntimeException: Columns must be preceded by the table name when there are more than one table in the 'from' clause.",
                expected.getCause().getMessage());
        }
    }
}
