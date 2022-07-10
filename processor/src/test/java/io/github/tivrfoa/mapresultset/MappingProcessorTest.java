package io.github.tivrfoa.mapresultset;

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
                "does.this.path.Matter",
                """
                package io.github.tivrfoa.mapresultset;

                import io.github.tivrfoa.mapresultset.api.Query;

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
                "io.github.tivrfoa.mapresultset.QueryNull",
                """
                package io.github.tivrfoa.mapresultset;

                import io.github.tivrfoa.mapresultset.api.Query;

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

    @Test
    public void testQueryFromOneTableAndColumnWithoutAlias() throws IOException {
        MappingProcessor p = new MappingProcessor();

        URL url = getClass().getResource("/Phone.java");
        final String phoneClass = Files.readString(new File(url.getPath()).toPath());

        final String source =  """
            package io.github.tivrfoa.mapresultset;

            import io.github.tivrfoa.mapresultset.api.Query;
            import io.github.tivrfoa.mapresultset.api.Table;

            %s

            class QueryPhoneId {
                @Query
                final String listPhones = "select id, phone.id from Phone as phone";
            }
            """.formatted(phoneClass);

        try {
            Reflect.compile(
                "io.github.tivrfoa.mapresultset.QueryPhoneId",
                source,
                new CompileOptions().processors(p)
            ).create().get();
            assertEquals(1, p.tables.size());
            assertEquals(1, p.javaStructures.size());
            for (var es : p.javaStructures.entrySet()) {
                assertEquals(new FullClassName("io.github.tivrfoa.mapresultset.Phone"), es.getKey());
            }
            assertEquals(1, p.tableMap.size());
            assertEquals("io.github.tivrfoa.mapresultset.Phone", p.tableMap.get("Phone"));
        }
        catch (ReflectException ex) {
            throw ex;
        }
    }

    @Test
    public void testTwoClassesMappedToTheSameTable() throws IOException {
        MappingProcessor p = new MappingProcessor();

        URL url = getClass().getResource("/TwoClassesMappedToTheSameTable.java");
        final String classes = Files.readString(new File(url.getPath()).toPath());

        final String source =  """
            package two.classes;

            import io.github.tivrfoa.mapresultset.api.Query;
            import io.github.tivrfoa.mapresultset.api.Table;

            %s

            class ClassesWithSameTableName {
                @Query
                final String listPhones = "select id from Phone as phone";
            }
            """.formatted(classes);

        try {
            Reflect.compile(
                "two.classes.ClassesWithSameTableName",
                source,
                new CompileOptions().processors(p)
            ).create().get();
        }
        catch (ReflectException expected) {
            assertEquals("java.lang.RuntimeException: It can't map two.classes.AnotherPhone to phone, because class 'two.classes.Phone' is already mapped to that table.",
                expected.getCause().getMessage());
        }
    }

    @Test
    public void testQueryMoreThanOneTableAndNoAliasInColumn() throws IOException {
        MappingProcessor p = new MappingProcessor();

        URL url = getClass().getResource("/Phone.java");
        final String phoneClass = Files.readString(new File(url.getPath()).toPath());

        try {
            Reflect.compile(
                "io.github.tivrfoa.mapresultset.QueryPhone",
                """
                package io.github.tivrfoa.mapresultset;

                import io.github.tivrfoa.mapresultset.api.Query;
                import io.github.tivrfoa.mapresultset.api.Table;

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

    @Test
    public void testTableNotMappedToAClass() throws IOException {
        MappingProcessor p = new MappingProcessor();

        try {
            Reflect.compile(
                "io.github.tivrfoa.mapresultset.TableNotMapped",
                """
                package io.github.tivrfoa.mapresultset;

                import io.github.tivrfoa.mapresultset.api.Query;

                class TableNotMapped {
                    @Query
                    final String listPhones = "select ops from not_mapped";
                }
                """,
                new CompileOptions().processors(p)
            ).create().get();
            throw new RuntimeException("Failed to throw exception.");
        }
        catch (ReflectException expected) {
           assertEquals("java.lang.RuntimeException: Table 'not_mapped' is not mapped to a class, so no value from this table can be in the 'SELECT' clause.\n" +
                "You might want to annotate it with: @Table (name = \"not_mapped\")",
                expected.getCause().getMessage());
        }
    }

    @Test
    public void testClassWithoutFields() throws IOException {
        MappingProcessor p = new MappingProcessor();

        try {
            Reflect.compile(
                "io.github.tivrfoa.mapresultset.ClassWithoutFields",
                """
                package io.github.tivrfoa.mapresultset;

                import io.github.tivrfoa.mapresultset.api.Query;
                import io.github.tivrfoa.mapresultset.api.Table;

                @Table
                class T1 {}

                class ClassWithoutFields {
                    @Query
                    final String list = "select ops from T1";
                }
                """,
                new CompileOptions().processors(p)
            ).create().get();
            throw new RuntimeException("Failed to throw exception.");
        }
        catch (ReflectException expected) {
           assertEquals("java.lang.RuntimeException: Class io.github.tivrfoa.mapresultset.T1 does not have field named: ops",
                expected.getCause().getMessage());
        }
    }

    @Test
    public void testFieldNotInClass() throws IOException {
        MappingProcessor p = new MappingProcessor();

        try {
            Reflect.compile(
                "io.github.tivrfoa.mapresultset.FieldNotInClass",
                """
                package io.github.tivrfoa.mapresultset;

                import io.github.tivrfoa.mapresultset.api.Column;
                import io.github.tivrfoa.mapresultset.api.Query;
                import io.github.tivrfoa.mapresultset.api.Table;

                @Table
                class T1 { int id; @Column(name = "hey") int money; }

                class FieldNotInClass {
                    @Query
                    final String list = "select ops from T1";
                }
                """,
                new CompileOptions().processors(p)
            ).create().get();
            throw new RuntimeException("Failed to throw exception.");
        }
        catch (ReflectException expected) {
           assertEquals("java.lang.RuntimeException: Class io.github.tivrfoa.mapresultset.T1 does not have field named: ops",
                expected.getCause().getMessage());
        }
    }

    /**
     * TODO jOOR does not with @Column, strange error:
     * 
     * java.lang.ClassFormatError: Incompatible magic value 1885430635 in class file xyz/MapResultSet
	at java.base/java.lang.ClassLoader.defineClass1(Native Method)
	at java.base/java.lang.ClassLoader.defineClass(ClassLoader.java:1012)
	at java.base/java.lang.ClassLoader.defineClass(ClassLoader.java:874)
	at org.joor.Compile$ByteArrayClassLoader.findClass(Compile.java:183)
	at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:587)
	at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:520)
	at org.joor.Compile.lambda$compile$3(Compile.java:150)
	at org.joor.Compile$ClassFileManager.loadAndReturnMainClass(Compile.java:251)
	at org.joor.Compile.compile(Compile.java:149)
	at org.joor.Reflect.compile(Reflect.java:104)

     */
    /*
    @Test
    public void testMappedColumn() throws IOException {
        MappingProcessor p = new MappingProcessor();

        final String source = """
            package xyz;

            import io.github.tivrfoa.mapresultset.api.Column;
            import io.github.tivrfoa.mapresultset.api.Query;
            import io.github.tivrfoa.mapresultset.api.Table;

            @Table (name = "t1")
            class T1 {
                public int id;
                @Column(name = "hey") // TODO this annotation causes the error
                public int money;
            }

            class MappedColumn {
                @Query
                final String list = "select id, hey from t1 as wharever";
            }
            """;
            System.out.println(source);

        try {
            Reflect.compile(
                "xyz.MappedColumn",
                source,
                new CompileOptions().processors(p)
            ).create().get();
            
            assertEquals(1, p.tables.size());
            assertEquals(1, p.javaStructures.size());
            for (var es : p.javaStructures.entrySet()) {
                assertEquals(new FullClassName("io.github.tivrfoa.mapresultset.T1"), es.getKey());
            }
            assertEquals(1, p.tableMap.size());
            assertEquals("io.github.tivrfoa.mapresultset.T1", p.tableMap.get("t1"));
        }
        catch (ReflectException ex) {
            throw ex;
        }
    } */
}
