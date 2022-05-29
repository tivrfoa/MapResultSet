package com.github.mapresultset;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes({"com.github.mapresultset.Table", "com.github.mapresultset.Query"})
public class MappingProcessor extends AbstractProcessor {

	private static final String template = """
						public static List<#class#> #queryName#(ResultSet rs) {
							var list = new ArrayList<#class#>();
							return list;
						}
					""";

	private Set<Element> annotatedElements = new HashSet<>(); // TODO this is probably not necessary
	private List<Element> tables = new ArrayList<>();
	private List<Element> queries = new ArrayList<>();

	@Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init( processingEnv );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnvironment) {
		if ( roundEnvironment.processingOver() ) {
			/*
				Last round ... we already collected all tables and queries
			*/
			System.out.println(">>>>>>>>>>>> Last round! Fight! <<<<<<<<<<<<");
			System.out.println("tables.: " + tables);
			System.out.println("queries: " + queries);


			// Map Table name -> to Class (including package)
			Map<String, String> tableMap = new HashMap<>();
			for (var te : tables) {
				final String packageAndClass = te.toString();
				int ld = packageAndClass.lastIndexOf(".");
				String tableName;
				if (ld == -1) {
					tableName = packageAndClass;
				} else {
					tableName = packageAndClass.substring(ld + 1);
				}
				
				for (var am : te.getAnnotationMirrors()) {
					for (var entry : am.getElementValues().entrySet()) {
						// TODO maybe there is a better way instead of using toString
						if (entry.getKey().toString().equals("name()")) {
							tableName = entry.getValue().getValue().toString();
						}
					}
				}

				if (tableMap.get(tableName) != null) {
					throw new RuntimeException("It can't map " + packageAndClass + " to " + tableName +
							", because class '" + tableMap.get(tableName) +
							"' is already mapped to that table.");
				}
				tableMap.put(tableName, packageAndClass);
			}

			System.out.println("------ map: Table -> Class -------");
			System.out.println(tableMap);
			
			// Parse all queries
			for (var qe : queries) {
				String query = (String) ((VariableElement) qe).getConstantValue();
				System.out.println("-->> Parsing query: " + query);
				var p = new ParseQuery(query);
				p.parse();
				System.out.println("# Columns: " + p.getColumns());
				System.out.println("# Tables.: " + p.getTables());

				// Link columns to tables
				List<TableColumns> listTableColumns = new ArrayList<>();
				for (var table : p.getTables()) {
					TableColumns tc = new TableColumns();
					tc.setTableName(table);

					for (var column : p.getColumns()) {
						int dotIndex = column.indexOf(".");
						if (dotIndex != -1) {
							if (column.substring(0, dotIndex).equals(table)) {
								if (!tc.getColumns().add(column.substring(dotIndex + 1))) {
									throw new RuntimeException("Duplicate column '" + column.substring(dotIndex + 1) +
											"' for table: " + table);
								}
							}
						} else {
							// Find which table this column belongs. If the columns
							// was preceded with the table name it would make my life easier
							// and the query more readable. Maybe I should impose this
							// restriction if it's querying from more than one table.
							if (p.getTables().size() == 1) {
								if (!tc.getColumns().add(column)) {
									throw new RuntimeException("Duplicate column '" + column +
											"' for table: " + table);
								}
							} else {
								throw new RuntimeException("Columns must be preceded by the table name " +
										"when there are more than one table in the 'from' clause.");
							}
						}
					}
					listTableColumns.add(tc);
				}
				System.out.println(listTableColumns);
			}

			// Create map file
				try {
					final String method1 = """
						public static List<String> listPhones(ResultSet rs) {
							// TODO instead of List<String> must be the class, eg Phone
							//   so I also need to pass to writeBuilderFile the imports
							return null;
						}
					""";

					final String method2 = """
						public static List<String> listPerson(ResultSet rs) {
							return null;
						}
					""";

					final String method3 = template.replaceAll("#class#", "String")
							.replaceAll("#queryName#", "listSomething");

					// TODO
					//   - how many classes create? Just one MapResultSet?
					//   - create in which package?
					writeBuilderFile("org.acme.MapResultSet", List.of(method1, method2, method3));
				} catch (IOException ex) {
					ex.printStackTrace();
				}
		} else {
			processAnnotations(annotations, roundEnvironment);
		}

		// https://docs.oracle.com/javase/8/docs/api/javax/annotation/processing/AbstractProcessor.html#process-java.util.Set-javax.annotation.processing.RoundEnvironment-
        return true;
    }

    private void processAnnotations(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnvironment) {
		for ( TypeElement annotation : annotations ) {
			System.out.println("Annotation: " + annotation);
			Set<? extends Element> annotatedElements
					= roundEnvironment.getElementsAnnotatedWith(annotation);
			System.out.println("Annotated Elements: " + annotatedElements);
			this.annotatedElements.addAll(annotatedElements);
			for (var e : annotatedElements) {
				System.out.println("Element: " + e + " and it's type is " + e.getKind());
				System.out.println("AnnotationMirrors: " + e.getAnnotationMirrors());
				for (var am : e.getAnnotationMirrors()) {
					System.out.println(am.getElementValues());
				}
				Element enclosingElement = e.getEnclosingElement();
				String name = e.getSimpleName().toString();
				System.out.println("element name: " + name);
				System.out.println("element enclosingElement: " + enclosingElement);
				System.out.println("element enclosedElements: " + e.getEnclosedElements());

				for (var enclosed : e.getEnclosedElements()) {
					System.out.println(enclosed + " type is " + enclosed.getKind());
				}


				if (e instanceof VariableElement ve) {
					String query = (String) ve.getConstantValue();
					if (query == null) {
						throw new RuntimeException("Variable annotated with @Query must be final and not null");
					}
					queries.add(e);
				} else {
					tables.add(e);
				}
			}
		}
	}

	private void writeBuilderFile(final String className, List<String> methods)
			throws IOException {

		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
				"Creating map file ... className = " + className);
	    String packageName = null;
	    int lastDot = className.lastIndexOf('.');
	    if (lastDot > 0) {
	        packageName = className.substring(0, lastDot);
	    }

	    String simpleClassName = className.substring(lastDot + 1);
	    // String mapClassName = simpleClassName + "MapResultSet";
	    String mapClassName = simpleClassName;

	    System.out.println("filer options: " + processingEnv.getOptions());
	    JavaFileObject builderFile = processingEnv.getFiler()
	      .createSourceFile(className);
	    
	    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

	        if (packageName != null) {
	            out.print("package ");
	            out.print(packageName);
	            out.println(";");
	            out.println();
	        }

	        // imports
	        out.println("import java.sql.ResultSet;");
	        out.println("import java.util.ArrayList;");
	        out.println("import java.util.List;");
	        out.println();

	        out.println("public class " + mapClassName + " {\n");

	        for (var m : methods) {
	        	out.println(m);
	        }

	        out.println("}");
	    }
	}
}

/*

References:

https://www.baeldung.com/java-annotation-processing-builder

https://www.zdnet.com/article/writing-and-processing-custom-annotations-part-3/

*/
