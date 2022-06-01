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

	static enum TYPE_KEY { // TODO give a better name
		PACKAGE,
		CLASS,
		CONTENT,
	}

	private static final String template = """
						public static List<#class#> #queryName#(ResultSet rs) {
							var list = new ArrayList<#class#>();
							return list;
						}
					""";

	private static final String ANONYMOUS_TABLE = null;

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

			// It will create one MapResultSet class in each package that
			// contains a @Query
			// map: package -> list of methods ?
			Map<String, List<String>> packageMethods = new HashMap<>();
			packageMethods.put(ANONYMOUS_TABLE, new ArrayList<>());

			List<ClassToCreate> listClassesToCreate = new ArrayList<>();
			
			// Parse all queries
			for (var queryElement : queries) {
				final String queryName = queryElement.toString();
				List<String> generatedColumns = new ArrayList<>();
				Set<String> queryImports = new HashSet<>();
				String queryImportsStr = "";
				final String packageName = getPackageName(queryElement);
				final String queryClassName = getQueryClassName(queryName);
				final String generatedColumnsClassName = uppercaseFirstLetter(queryName) + "GeneratedColumns";
				String classContent = """
						package %s;

						import java.util.ArrayList;
						import java.util.List;

						#import#

						public class %s {

							
						""".formatted(packageName, queryClassName);
				var queryClassToCreate = new ClassToCreate(packageName, queryClassName, classContent);
				listClassesToCreate.add(queryClassToCreate);
				String query = (String) ((VariableElement) queryElement).getConstantValue();
				System.out.println("-->> Parsing query: " + query);
				var p = new ParseQuery(query);
				p.parse();
				System.out.println("p.getTables() = " + p.getTables());

				for (var column : p.getColumns()) {
					if (column.isGeneratedValue()) {
						String alias = column.alias();
						generatedColumns.add(alias);
					} else {
						String table = column.table();
						if (table == null) {
							// if it's not a generated column and table is null
							// then it means the from clause must have a single table
							if (p.getTables().size() > 1) {
								throw new RuntimeException("Columns must be preceded by the table name " +
										"when there are more than one table in the 'from' clause.");
							}
							for (var es : p.getTables().entrySet()) table = es.getValue();
						} else {
							table = p.getTables().get(table);
						}
						System.out.println("table = " + table + ", tableMap.get(table) = " + tableMap.get(table));
						if (queryImports.add(tableMap.get(table))) {
							queryImportsStr += "import " + tableMap.get(table) + ";\n";
							String classTableName = splitPackageClass(tableMap.get(table))[1];
							String content = """
									public List<%s> list%s = new ArrayList<>();
								""".formatted(classTableName, classTableName);
							queryClassToCreate.content += content;
						}
					}
				}

				if (!generatedColumns.isEmpty()) {
					queryClassToCreate.content += """
							public List<%s> generatedColumns = new ArrayList<>();
						""".formatted(generatedColumnsClassName);
				}

				queryClassToCreate.content += "}";
				queryClassToCreate.content = queryClassToCreate.content.replaceAll("#import#", queryImportsStr);

				System.out.println(queryClassToCreate.content);
				writeBuilderFile(queryClassToCreate);

				if (!generatedColumns.isEmpty()) {
					// Create GeneratedColumns class

					classContent = """
						package %s;

						import java.util.List;

						public class %s {

							
						""".formatted(packageName, generatedColumnsClassName);
					var generatedColumnsClassToCreate = new ClassToCreate(packageName, generatedColumnsClassName, classContent);

					for (var field : generatedColumns) {
						String content = "\tpublic Object " + field + ";\n";
						generatedColumnsClassToCreate.content += content;
					}
					generatedColumnsClassToCreate.content += "}";
					System.out.println(generatedColumnsClassToCreate.content);
					writeBuilderFile(packageName, generatedColumnsClassName, generatedColumnsClassToCreate.content);
				}

				// Create map file
				final String content = createMapResultSetClassForQuery(queryName, packageName, queryClassName, queryImports);
				writeBuilderFile(packageName, "MapResultSet", content);
			}
		} else {
			processAnnotations(annotations, roundEnvironment);
		}

        return true;
    }

    private String createMapResultSetClassForQuery(String queryName, String packageName, String queryClassName,
			Set<String> queryImports) {
		if (packageName.equals("tests")) {
			return "public class MapResultSet {}";
		}
		String imports = """
				import java.sql.ResultSet;
				import java.sql.SQLException;
				""";
		for (var i : queryImports) {
			imports += "import " + i + ";\n";
		}
		
		// String methodBody = "return null;";
		String methodBody = """
					ListNotebooksRecords records = new ListNotebooksRecords();
					System.out.println(records);
					while (rs.next()) {
						Notebook n = new Notebook();
						n.setId(rs.getInt(1));
						n.setName(rs.getString(2));
						System.out.println(n);
			
						records.listNotebook.add(n);
						System.out.println("added notebook");
						ListNotebooksGeneratedColumns c = new ListNotebooksGeneratedColumns();
						c.four = rs.getString(3);
						System.out.println(c.four);
						records.generatedColumns.add(c);
					}
			
					return records;
			""";
		return """
				package %s;

				%s

				public class MapResultSet {
					public static %s %s(ResultSet rs) throws SQLException {
						%s
					}
				}
				""".formatted(packageName, imports, queryClassName, queryName, methodBody);
	}

	private String[] splitPackageClass(final String packageClass) {
		final int lastDotIdx = packageClass.lastIndexOf(".");
		return new String[] {
			packageClass.substring(0, lastDotIdx),
			packageClass.substring(lastDotIdx + 1)
		};
	}

	private String getQueryClassName(final String queryName) {
		return uppercaseFirstLetter(queryName) + "Records";
	}

	private String uppercaseFirstLetter(final String str) {
		String Up = str.substring(0, 1).toUpperCase();
		if (str.length() > 1) {
			Up += str.substring(1);
		}
		return Up;
	}

	private String getPackageName(Element qe) {
		var enclosingElement = qe.getEnclosingElement().toString();
		int lastDot = enclosingElement.lastIndexOf(".");

		return enclosingElement.substring(0, lastDot);
	}

	private void processAnnotations(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnvironment) {
		for ( TypeElement annotation : annotations ) {
			System.out.println("///////////////////////////////////////////////////////////////////////");
			System.out.println("========= Processing Annotation: " + annotation + " ========");
			System.out.println("///////////////////////////////////////////////////////////////////////");
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

	private void writeBuilderFile(final String className, List<String> methods) {

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
	    
		JavaFileObject builderFile;
		try {
			builderFile = processingEnv.getFiler().createSourceFile(className);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	    
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
	    } catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	private void writeBuilderFile(final ClassToCreate clazz) {
		writeBuilderFile(clazz.packageName, clazz.className, clazz.content);
	}

	private void writeBuilderFile(final String packageName, final String className, final String content) {

		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
				"Creating map file ... className = " + className);

	    System.out.println("filer options: " + processingEnv.getOptions());
	    JavaFileObject builderFile;
		try {
			builderFile = processingEnv.getFiler().createSourceFile(packageName + "." + className);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	    
	    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
	        out.println(content);
	    } catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}

/*

References:

https://www.baeldung.com/java-annotation-processing-builder

https://www.zdnet.com/article/writing-and-processing-custom-annotations-part-3/

*/
