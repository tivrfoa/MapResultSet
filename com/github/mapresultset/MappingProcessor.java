package com.github.mapresultset;

import java.io.IOException;
import java.io.PrintWriter;
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
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes({"com.github.mapresultset.api.Column", "com.github.mapresultset.api.Table", "com.github.mapresultset.api.Query"})
public class MappingProcessor extends AbstractProcessor {

	private static final String GENERATED_COLUMNS = "GeneratedColumns";

	static enum TYPE_KEY { // TODO give a better name
		PACKAGE,
		CLASS,
		CONTENT,
	}

	private static final String ANONYMOUS_TABLE = null;

	private Set<Element> annotatedElements = new HashSet<>(); // TODO this is probably not necessary
	private List<Element> tables = new ArrayList<>();
	private List<Element> queries = new ArrayList<>();
	private Map<FullClassName, Map<ColumnName, FieldName>> classMappedColumns = new HashMap<>();
	private Map<String, Structure> structures = new HashMap<>();

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
			processLastRound(annotations, roundEnvironment);
		} else {
			processAnnotations(annotations, roundEnvironment);
		}

        return true;
    }

	public void processLastRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnvironment) {
		/*
			Last round ... we already collected all tables and queries
		*/
		System.out.println(">>>>>>>>>>>> Last round! Fight! <<<<<<<<<<<<");
		System.out.println("tables.: " + tables);
		System.out.println("queries: " + queries);
		System.out.println("classMappedColumns: " + classMappedColumns);
		System.out.println("structures: " + structures);


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
			
			tableName = getAnnotationParameter(te, "name()");

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
			Map<String, Structure> queryStructures = new HashMap<>();
			Set<String> queryImports = new HashSet<>();
			String queryImportsStr = "";
			final String packageName = getPackageName(queryElement);
			final String queryClassName = getQueryClassName(queryName);
			final String generatedColumnsClassName = uppercaseFirstLetter(queryName) + GENERATED_COLUMNS;
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
					var m = queryStructures.get(generatedColumnsClassName);
					if (m == null) {
						m = new Structure(generatedColumnsClassName, "CLASS");
						queryStructures.put(generatedColumnsClassName, m);
					}
					m.fields.put(alias, "Object"); // TODO let user specify a type
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
					String fullClassName = tableMap.get(table);
					System.out.println("table = " + table + ", tableMap.get(table) = " + fullClassName);
					if (queryImports.add(fullClassName)) {
						queryImportsStr += "import " + fullClassName + ";\n";
						String classTableName = splitPackageClass(fullClassName)[1];
						String content = """
								public List<%s> list%s = new ArrayList<>();
							""".formatted(classTableName, classTableName);
						queryClassToCreate.content += content;
					}
					var structure = structures.get(fullClassName);
					var structureType = structure.type;
					var m = queryStructures.get(fullClassName);
					if (m == null) {
						m = new Structure(fullClassName, structureType);
						queryStructures.put(fullClassName, m);
					}
					var classFieldType = structure.fields.get(column.name());
					if (classFieldType == null) {
						var field = classMappedColumns.get(new FullClassName(fullClassName)).get(new ColumnName(column.name()));
						if (field == null)
							throw new RuntimeException("Class " + fullClassName + " does not have field named: " + column.name());
						classFieldType = structure.fields.get(field.name());
					}
					m.fields.put(column.name(), classFieldType);
				}
			}

			if (!generatedColumns.isEmpty()) {
				queryClassToCreate.content += """
						private List<%s> generatedColumns = new ArrayList<>();
						public List<%s> getGeneratedColumns() {
							return generatedColumns;
						}
					""".formatted(generatedColumnsClassName, generatedColumnsClassName);
			}

			queryClassToCreate.content += "}";
			queryClassToCreate.content = queryClassToCreate.content.replaceAll("#import#", queryImportsStr);

			System.out.println(queryClassToCreate.content);
			writeBuilderFile(queryClassToCreate);

			System.out.println("-------- Query Structures ------------");
			System.out.println(queryStructures);

			if (!generatedColumns.isEmpty()) {
				createGeneratedColumnsClass(packageName, generatedColumnsClassName, generatedColumns);
			}

			// Create map file
			final String content = createMapResultSetClassForQuery(queryName, packageName, queryClassName, queryImports, queryStructures);
			writeBuilderFile(packageName, "MapResultSet", content);
		}
	}

	/**
	 * Very ugly code
	 * 
	 * TODO maybe there is a better way instead of using toString
	 * 
	 * @param te
	 * @param string
	 * @return
	 */
    private String getAnnotationParameter(Element te, String param) {
		for (var am : te.getAnnotationMirrors()) {
			for (var entry : am.getElementValues().entrySet()) {
				if (entry.getKey().toString().equals(param)) {
					return entry.getValue().getValue().toString();
				}
			}
		}
		return null;
	}

	private void createGeneratedColumnsClass(String packageName, String generatedColumnsClassName,
			List<String> generatedColumns) {
		String classContent = """
			package %s;

			import java.util.List;

			public class %s {

				
			""".formatted(packageName, generatedColumnsClassName);
		var generatedColumnsClassToCreate = new ClassToCreate(packageName, generatedColumnsClassName, classContent);

		for (var field : generatedColumns) {
			String Field = uppercaseFirstLetter(field);
			String content = """
					private Object %s;
					public Object get%s() {
						return %s;
					}
					public void set%s(Object %s) {
						this.%s = %s;
					}
				""".formatted(field, Field, field, Field, field, field, field);
			generatedColumnsClassToCreate.content += content;
		}
		generatedColumnsClassToCreate.content += "}";
		System.out.println(generatedColumnsClassToCreate.content);
		writeBuilderFile(packageName, generatedColumnsClassName, generatedColumnsClassToCreate.content);
	}

	private String createMapResultSetClassForQuery(String queryName, String packageName, String queryClassName,
			Set<String> queryImports, Map<String, Structure> queryStructures) {

		String imports = """
				import java.sql.ResultSet;
				import java.sql.SQLException;
				""";
		for (var i : queryImports) {
			imports += "import " + i + ";\n";
		}

		String methodBody = """
		%s records = new %s();

				while (rs.next()) {
		""".formatted(queryClassName, queryClassName);

		
		for (var entry : queryStructures.entrySet()) {
			String className = entry.getKey();
			if (className.contains("."))
				className = splitPackageClass(entry.getKey())[1];
			String createObject = """
						{
							%s obj = new %s();
			""".formatted(className, className);
			methodBody += createObject;
			String setFields = "";
			for (var field : entry.getValue().fields.entrySet()) {
				String fieldName = field.getKey();
				String columnName = fieldName;
				var mappedClassFields = classMappedColumns.get(new FullClassName(entry.getKey()));
				if (mappedClassFields != null) {
					if (mappedClassFields.get(new ColumnName(fieldName)) != null) {
						fieldName = mappedClassFields.get(new ColumnName(fieldName)).name();
					}
				}
				String fieldSetMethod = "set" + uppercaseFirstLetter(fieldName);
				System.out.println("field: " + field);
				var resultSetType = ResultSetTypes.fromString(field.getValue());
				if (resultSetType == ResultSetTypes.CHAR) {
					setFields += """
									var str = rs.getString("%s");
									if (str != null)
										obj.%s(str.charAt(0));
					""".formatted(columnName, fieldSetMethod);
				} else {
					String resultSetGetMethod = resultSetType.getResultSetGetMethod();
					setFields += """
									obj.%s(rs.%s("%s"));
					""".formatted(fieldSetMethod, resultSetGetMethod, columnName);
				}
			}
			methodBody += setFields;

			String closeCreateObject;
			if (className.endsWith(GENERATED_COLUMNS)) {
				closeCreateObject = """
								records.getGeneratedColumns().add(obj);
							}
				""";
			} else {
				closeCreateObject = """
								records.list%s.add(obj);
							}
				""".formatted(className);
			}
			methodBody += closeCreateObject;
		}

		methodBody += """
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
			System.out.println("====== Processing Annotation: " + annotation + " ========");
			System.out.println("///////////////////////////////////////////////////////////////////////");

			Set<? extends Element> annotatedElements
					= roundEnvironment.getElementsAnnotatedWith(annotation);
			System.out.println("Annotated Elements: " + annotatedElements);
			this.annotatedElements.addAll(annotatedElements);
			
			for (var e : annotatedElements) {
				String elementName = e.toString();
				System.out.println("Element: " + elementName + " and it's type is " + e.getKind());

				Element enclosingElement = e.getEnclosingElement();
				System.out.println("element enclosingElement: " + enclosingElement);
				System.out.println("element enclosedElements: " + e.getEnclosedElements());

				switch (annotation.toString()) {
					case "com.github.mapresultset.api.Column":
						processColumn(elementName, e);
					break;
					case "com.github.mapresultset.api.Table":
						processTable(elementName, e);
						break;
					case "com.github.mapresultset.api.Query":
						processQuery(elementName, e);
						break;
				}
			}
		}
	}

	private void processQuery(String elementName, Element e) {
		String query = (String) ((VariableElement) e).getConstantValue();
		if (query == null) {
			throw new RuntimeException("Variable annotated with @Query must be final and not null");
		}
		queries.add(e);
	}

	private void processTable(String elementName, Element e) {
		tables.add(e);
		List<VariableElement> fields = ElementFilter.fieldsIn(e.getEnclosedElements());
		Map<String, String> mapField = new HashMap<>();
		for (VariableElement field : fields) {
			String fieldType = field.asType().toString();
			mapField.put(field.toString(), fieldType);
		}
		structures.put(elementName, new Structure(elementName, e.getKind().toString(), mapField));
	}

	private void processColumn(String elementName, Element e) {
		var fieldName = new FieldName(e.toString());
		var columnName = new ColumnName(getAnnotationParameter(e, "name()"));
		System.out.println("column: " + fieldName + ", value = " + columnName);

		var structureStr = e.getEnclosingElement().toString();
		var structure = new FullClassName(structureStr);
		var map = classMappedColumns.get(structure);
		if (map == null) {
			map = new HashMap<>();
			classMappedColumns.put(structure, map);
		}
		map.put(columnName, fieldName);
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
