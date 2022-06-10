package com.github.mapresultset;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.github.mapresultset.JavaStructure.Type;

@SupportedAnnotationTypes({"com.github.mapresultset.api.Column", "com.github.mapresultset.api.Table",
		"com.github.mapresultset.api.Query", "com.github.mapresultset.api.Id", "com.github.mapresultset.api.ManyToOne",
		"com.github.mapresultset.api.OneToMany"})
public class MappingProcessor extends AbstractProcessor {

	private static final String GENERATED_COLUMNS = "GeneratedColumns";

	private List<Element> tables = new ArrayList<>();
	private List<Element> queries = new ArrayList<>();
	private Map<FullClassName, Map<ColumnName, FieldName>> classMappedColumns = new HashMap<>();
	private Map<FullClassName, JavaStructure> javaStructures = new HashMap<>();
	private List<Relationship> relationships = new ArrayList<>();
	private Map<FullClassName, List<FieldName>> primaryKeys = new HashMap<>();

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
		System.out.println("Java Structures: " + javaStructures);
		System.out.println("\n---------- Primary Keys ---------\n" + primaryKeys);
		System.out.println("\n---------- Relationships ---------\n" + relationships);


		// Map: Table Name -> Full Class Name (including package)
		Map<String, String> tableMap = new HashMap<>();
		for (var te : tables) {
			final String packageAndClass = te.toString();
			String tableName = getAnnotationParameter(te, "name()");
			if (tableName != null) {
				if (tableMap.get(tableName) != null) {
					throw new RuntimeException("It can't map " + packageAndClass + " to " + tableName +
							", because class '" + tableMap.get(tableName) +
							"' is already mapped to that table.");
				}
			} else {
				int ld = packageAndClass.lastIndexOf(".");
				if (ld == -1) {
					tableName = packageAndClass;
				} else {
					tableName = packageAndClass.substring(ld + 1);
				}
			}
			
			tableMap.put(tableName, packageAndClass);
		}

		System.out.println("------ map: Table -> Class -------");
		System.out.println(tableMap);

		// It will create one MapResultSet class in each package that
		// contains a @Query
		// map: package -> list of methods ?
		Map<String, QueryMethodsAndImports> packageMethods = new HashMap<>();
		
		// Parse all queries
		for (var queryElement : queries) {
			final String queryName = queryElement.toString();
			List<String> generatedColumns = new ArrayList<>();
			Map<FullClassName, QueryStructure> queryStructures = new HashMap<>();
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
			String query = (String) ((VariableElement) queryElement).getConstantValue();
			System.out.println("-->> Parsing query: " + query);
			var p = new ParseQuery(query);
			p.parse();
			System.out.println("p.getTables() = " + p.getTables());

			for (var column : p.getColumns()) {
				System.out.println(column);
				if (column.isGeneratedValue()) {
					String alias = column.alias();
					generatedColumns.add(alias);
					var m = queryStructures.get(new FullClassName(generatedColumnsClassName));
					if (m == null) {
						m = new QueryStructure(new FullClassName(generatedColumnsClassName), JavaStructure.Type.CLASS);
						queryStructures.put(new FullClassName(generatedColumnsClassName), m);
					}
					// TODO let user specify a type
					m.fields.put(new ColumnName(alias), new ColumnField(alias, new Field("", "Object")));
				} else {
					String table = column.table();
					System.out.println("table = " + table);
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
					String fullClassNameStr = tableMap.get(table);
					var fullClassName = new FullClassName(tableMap.get(table));
					System.out.println("table = " + table + ", tableMap.get(table) = " + fullClassName);
					if (queryImports.add(fullClassNameStr)) {
						queryImportsStr += "import " + fullClassName.name() + ";\n";
						String classTableName = splitPackageClass(fullClassNameStr)[1];
						String content = """
								private List<%s> list%s = new ArrayList<>();
								public List<%s> getList%s() {
									return list%s;
								}
							""".formatted(classTableName, classTableName, classTableName, classTableName, classTableName);
						queryClassToCreate.content += content;
					}
					var structure = javaStructures.get(fullClassName);
					var structureType = structure.type;
					var m = queryStructures.get(fullClassName);
					if (m == null) {
						m = new QueryStructure(fullClassName, structureType);
						queryStructures.put(fullClassName, m);
					}
					
					var fieldName = new FieldName(column.name());
					var classFieldType = structure.fields.get(fieldName);
					if (classFieldType == null) {
						fieldName = classMappedColumns.get(fullClassName).get(new ColumnName(column.name()));
						if (fieldName == null)
							throw new RuntimeException("Class " + fullClassNameStr + " does not have field named: " + column.name());
						classFieldType = structure.fields.get(fieldName);
					}
					String alias = column.alias();
					if (alias == null) alias = column.name();
					m.fields.put(new ColumnName(column.name()), new ColumnField(alias, new Field(fieldName.name(), classFieldType.name())));
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

			writeBuilderFile(queryClassToCreate);

			System.out.println("-------- Query Structures ------------");
			System.out.println(queryStructures);

			if (!generatedColumns.isEmpty()) {
				createGeneratedColumnsClass(packageName, generatedColumnsClassName, generatedColumns);
			}

			var queryMethodsAndImports = packageMethods.get(packageName);
			if (queryMethodsAndImports == null) {
				queryMethodsAndImports = new QueryMethodsAndImports();
				packageMethods.put(packageName, queryMethodsAndImports);
			}
			
			queryMethodsAndImports.imports.addAll(queryImports);
			queryMethodsAndImports.methods += createQueryMethod(queryName, queryClassName, queryStructures);
		}
		
		// Create a MapResultClass for each package that contains a @Query
		for (var pm : packageMethods.entrySet()) {
			final String packageName = pm.getKey();
			final QueryMethodsAndImports qmi = pm.getValue();
			final String content = createMapResultSetClassForQuery(packageName, qmi.imports, qmi.methods);
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

	private String getRecordConstructor(FullClassName fullClassName, String recordName, Map<ColumnName, ColumnField> fields, int objCounter) {
		RecordComponent recordComponents = javaStructures.get(fullClassName).recordComponents;

		List<String> fieldsInQuery = new ArrayList<>();
		String fieldsInitialization = "";
		for (var fieldEntry : fields.entrySet()) {
			String fieldName = fieldEntry.getKey().name();
			String columnAlias = fieldEntry.getValue().columnAlias();
			Field field = fieldEntry.getValue().field();
			var mappedClassFields = classMappedColumns.get(fullClassName);
			if (mappedClassFields != null) {
				if (mappedClassFields.get(new ColumnName(fieldName)) != null) {
					fieldName = mappedClassFields.get(new ColumnName(fieldName)).name();
				}
			}
			fieldsInQuery.add(fieldName);
			var resultSetType = ResultSetType.fromString(field.type());
			if (resultSetType == ResultSetType.CHAR) {
				String fieldString = fieldName + "String";
				fieldsInitialization += """
							%s %s = rs.getString("%s");
							var %s = ' ';
							if (%s != null && %s.length() >= 1)
								%s = %s.charAt(0);
				""".formatted(field.type(), fieldString, columnAlias, fieldName, fieldString, fieldString,
						fieldName, fieldString);
			} else {
				fieldsInitialization += """
							%s %s = rs.%s("%s");
				""".formatted(field.type(), fieldName, resultSetType.getResultSetGetMethod(), columnAlias);
			}
		}
		
		String constructorParameters = "";
		for (int i = 0; i < recordComponents.fields.size(); i++) {
			String fieldName = recordComponents.fields.get(i);
			String fieldType = recordComponents.types.get(i);
			if (!fieldsInQuery.contains(fieldName)) {
				fieldsInitialization += """
							%s %s = %s;
				""".formatted(fieldType, fieldName, getDefaultValueForType(fieldType));
			}
			constructorParameters += fieldName;
			if (i + 1 < recordComponents.fields.size()) constructorParameters += ", ";
		}
		
		return """
		%s
					%s obj%s = new %s(%s);
		""".formatted(fieldsInitialization, recordName, objCounter, recordName, constructorParameters);
	}

	private String getDefaultValueForType(String fieldType) {
		return switch (fieldType) {
			case "boolean" -> "false";
			case "char" -> "' '";
			case "int" -> "0";
			case "float" -> "0.0f";
			case "double" -> "0.0";
			case "long" -> "0L";
			default -> "null";
		};
	}

	private String createQueryMethod(String queryName, String queryClassName,
			Map<FullClassName, QueryStructure> queryStructures) {

		String methodBody = """
		%s records = new %s();

				while (rs.next()) {
		""".formatted(queryClassName, queryClassName);

		Map<FullClassName, String> objects = new HashMap<>();
		int objCounter = 0;
		for (var entry : queryStructures.entrySet()) {
			objCounter++;
			FullClassName fullClassName = entry.getKey();
			objects.put(fullClassName, "obj" + objCounter);
			QueryStructure queryStructure = entry.getValue();
			String className = fullClassName.name();
			if (className.contains("."))
				className = splitPackageClass(className)[1];
			
			if (queryStructure.type == Type.RECORD) {
				methodBody += getRecordConstructor(fullClassName, className, queryStructure.fields, objCounter);
			} else {
				String createObject = """
							%s obj%s = new %s();
				""".formatted(className, objCounter, className);
				methodBody += createObject;
				String setFields = "";
				for (var fieldEntry : queryStructure.fields.entrySet()) {
					String fieldName = fieldEntry.getKey().name();
					String columnAlias = fieldEntry.getValue().columnAlias();
					Field field = fieldEntry.getValue().field();
					var mappedClassFields = classMappedColumns.get(fullClassName);
					if (mappedClassFields != null) {
						if (mappedClassFields.get(new ColumnName(fieldName)) != null) {
							fieldName = mappedClassFields.get(new ColumnName(fieldName)).name();
						}
					}

					String fieldSetMethod = getFieldSetMethod(fieldName, field);
					var resultSetType = ResultSetType.fromString(field.type());
					if (resultSetType == ResultSetType.CHAR) {
						setFields += """
									var str = rs.getString("%s");
									if (str != null && str.length() >= 1)
										obj%s.%s(str.charAt(0));
						""".formatted(columnAlias, objCounter, fieldSetMethod);
					} else {
						String resultSetGetMethod = resultSetType.getResultSetGetMethod();
						setFields += """
									obj%s.%s(rs.%s("%s"));
						""".formatted(objCounter, fieldSetMethod, resultSetGetMethod, columnAlias);
					}
				}
				methodBody += setFields;
			}

			if (className.endsWith(GENERATED_COLUMNS)) {
				methodBody += """
							records.getGeneratedColumns().add(obj%s);

				""".formatted(objCounter);
			} else {
				methodBody += """
							records.getList%s().add(obj%s);

				""".formatted(className, objCounter);
			}
		}

		// Check relationships
		for (var rel : relationships) {
			var owner = objects.get(rel.javaStructWithMappingAnnotation());
			var partner = objects.get(rel.partner());
			if (owner == null || partner == null) continue;
			switch (rel.type()) {
				case ManyToOne:
					String set = "set" + uppercaseFirstLetter(rel.partnerFieldName().name());
					methodBody += """
								%s.%s(%s);
					""".formatted(owner, set, partner);
					// methodBody += owner + "." + set + "(" + partner + ");";
					break;
				default: throw new RuntimeException(rel.type() + " is not implemented yet.");
			}
		}

		methodBody += """
				}

				return records;
		""";
	
		return """

					public static %s %s(ResultSet rs) throws SQLException {
						%s
					}

				""".formatted(queryClassName, queryName, methodBody);
	}

	private String createMapResultSetClassForQuery(String packageName,
			Set<String> mapResultSetImports, String methods) {

		String imports = """
				import java.sql.ResultSet;
				import java.sql.SQLException;
				""";
		for (var i : mapResultSetImports) {
			imports += "import " + i + ";\n";
		}
	
		return """
				package %s;

				%s

				public class MapResultSet {
					%s
				}
				""".formatted(packageName, imports, methods);
	}

	private String getFieldSetMethod(String fieldName, Field field) {
		if (field.type().equals("boolean")) {
			if (fieldName.startsWith("is") && fieldName.length() > 2) {
				return "set" + fieldName.substring(2);
			} else {
				return "set" + uppercaseFirstLetter(fieldName);
			}
		} else {
			return "set" + uppercaseFirstLetter(fieldName);
		}
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
			
			for (var e : annotatedElements) {
				String elementName = e.toString();
				System.out.println("Element: " + elementName + " and it's type is " + e.getKind());

				Element enclosingElement = e.getEnclosingElement();
				System.out.println("element enclosingElement: " + enclosingElement);
				System.out.println("element enclosedElements: " + e.getEnclosedElements());
				for (var enclosed : e.getEnclosedElements()) {
					System.out.println(enclosed + " kind is: " + enclosed.getKind());
				}

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
					case "com.github.mapresultset.api.OneToMany":
						processManyRelationship(elementName, e, Relationship.Type.OneToMany);
						break;
					case "com.github.mapresultset.api.ManyToOne":
						processManyRelationship(elementName, e, Relationship.Type.ManyToOne);
						break;
					case "com.github.mapresultset.api.Id":
						processId(elementName, e);
						break;
				}
			}
		}
	}

	private void processId(String elementName, Element e) {
		FullClassName fcn = new FullClassName(e.getEnclosingElement().toString());
		var fields = primaryKeys.get(fcn);
		if (fields == null) {
			fields = new ArrayList<>();
			primaryKeys.put(fcn, fields);
		}
		fields.add(new FieldName(elementName));
	}

	private void processManyRelationship(String elementName, Element e, Relationship.Type type) {
		FullClassName fcn = new FullClassName(e.getEnclosingElement().toString());
		FullClassName partner = new FullClassName(e.asType().toString());
		relationships.add(new Relationship(fcn, partner, new FieldName(elementName), type));
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
		Map<FieldName, FieldType> mapField = new HashMap<>();
		for (VariableElement field : fields) {
			String fieldType = field.asType().toString();
			mapField.put(new FieldName(field.toString()), new FieldType(fieldType));
		}
		System.out.println("mapField = " + mapField);
		JavaStructure.Type type = JavaStructure.getType(e.getKind().toString());
		RecordComponent recordComponents = new RecordComponent();
		if (type == JavaStructure.Type.RECORD) {
			for (var enclosed : e.getEnclosedElements()) {
				if (enclosed.getKind() == ElementKind.RECORD_COMPONENT) {
					recordComponents.fields.add(enclosed.toString());
				} else if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
					recordComponents.types = getTypesFromConstructor(enclosed.toString());
				}
			}
		}
		javaStructures.put(new FullClassName(elementName), new JavaStructure(elementName, type, mapField, recordComponents));
	}

	/**
	 * 
	 * @param str eg: Country(int,java.lang.String)
	 * @return
	 */
	private List<String> getTypesFromConstructor(String str) {
		System.out.println("Parsing constructor: " + str);
		int parentheses = str.indexOf("(");
		str = str.substring(parentheses + 1, str.length() - 1);
		return Arrays.asList(str.split(","));
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
