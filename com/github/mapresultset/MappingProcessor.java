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
		"com.github.mapresultset.api.Query", "com.github.mapresultset.api.Id",
		"com.github.mapresultset.api.OneToOne", "com.github.mapresultset.api.OneToMany",
		"com.github.mapresultset.api.ManyToOne", "com.github.mapresultset.api.ManyToMany"})
public class MappingProcessor extends AbstractProcessor {

	private static final String GENERATED_COLUMNS = "GeneratedColumns";

	private List<Element> tables = new ArrayList<>();
	private List<Element> queries = new ArrayList<>();
	private Map<FullClassName, Map<ColumnName, FieldName>> classMappedColumns = new HashMap<>();
	private Map<FullClassName, JavaStructure> javaStructures = new HashMap<>();
	private Map<FullClassName, List<Relationship>> relationships = new HashMap<>();
	private Map<FullClassName, List<Field>> primaryKeys = new HashMap<>();
	/**
	 * key: query class name
	 * value: list of grouped by methods
	 */
	final Map<String, List<String>> queryGroupedByMethodsMap = new HashMap<>();

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
			// System.out.println("\n---------- Query Grouped By Methods -----------\n" + queryGroupedByMethodsMap);
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

		// System.out.println("------ map: Table -> Class -------");
		// System.out.println(tableMap);

		// It will create one MapResultSet class in each package that
		// contains a @Query
		// map: package -> list of methods ?
		final Map<String, QueryMethodsAndImports> packageMethods = new HashMap<>();
		final Map<String, ClassToCreate> queryClassesToCreate = new HashMap<>();
		
		// Parse all queries
		for (var queryElement : queries) {
			final String queryName = queryElement.toString();
			List<String> generatedColumns = new ArrayList<>();
			Map<FullClassName, QueryClassStructure> queryStructures = new HashMap<>();
			Set<String> queryImports = new HashSet<>();
			String queryImportsStr = "";
			final String packageName = getPackageName(queryElement);
			final String queryClassName = getQueryClassName(queryName);
			final String generatedColumnsClassName = uppercaseFirstLetter(queryName) + GENERATED_COLUMNS;
			final FullClassName generatedColumnsFullClassName = new FullClassName(generatedColumnsClassName);
			String classContent = """
					package %s;

					import java.util.ArrayList;
					import java.util.List;
					// TODO the two imports below should be added conditionally
					// when there's a groupBy method
					import java.util.Map;
					import java.util.HashMap;

					#import#

					public class %s {

						
					""".formatted(packageName, queryClassName);
			var queryClassToCreate = new ClassToCreate(packageName, queryClassName, classContent);
			String query = (String) ((VariableElement) queryElement).getConstantValue();
			System.out.println("-->> Parsing query: " + query);
			var p = new ParseQuery(query);
			p.parse();
			// System.out.println("p.getTables() = " + p.getTables());
			// System.out.println(p);

			for (ColumnRecord columnRecord : p.getColumns()) {
				// System.out.println("---> Handling column: " + columnRecord);
				boolean isTemporaryTable = false;
				if (columnRecord.table() != null) {
					isTemporaryTable = p.getTables().get(columnRecord.table()).isTemporaryTable();
				}
				if (columnRecord.isGeneratedValue() || isTemporaryTable) {
					// Add to generated columns
					String alias = columnRecord.alias();
					if (alias == null) {
						if (!isTemporaryTable) {
							throw new RuntimeException("Invalid state");
						}
						alias = columnRecord.table();
					}
					generatedColumns.add(alias);
					var m = queryStructures.get(generatedColumnsFullClassName);
					if (m == null) {
						m = new QueryClassStructure(generatedColumnsFullClassName, JavaStructure.Type.CLASS);
						queryStructures.put(generatedColumnsFullClassName, m);
					}
					// TODO let user specify a type
					m.fields.put(new ColumnName(alias), new ColumnField(alias, new Field("", "Object")));
				} else {
					String table = columnRecord.table();
					if (table == null) {
						// if it's not a generated column and table is null
						// then it means the from clause must have a single table
						if (p.getTables().size() != 1) {
							throw new RuntimeException("Columns must be preceded by the table name " +
									"when there are more than one table in the 'from' clause.");
						}
						for (var es : p.getTables().entrySet()) table = es.getValue().tableName();
					} else {
						table = p.getTables().get(table).tableName();
					}
					String fullClassNameStr = tableMap.get(table);
					if (fullClassNameStr == null) {
						table = p.getTables().get(columnRecord.table()).tableName();
						throw new RuntimeException("Table '" + table + "' is not mapped to class, " +
						     	"so no value from this table can be in the 'SELECT' clause.\n" +
								"You might want to annotate it with: @Table (name = \"" + table + "\")");
					}
					// System.out.println("table = " + table + ", tableMap.get(table) = " + fullClassNameStr);
					var fullClassName = new FullClassName(fullClassNameStr);
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
						m = new QueryClassStructure(fullClassName, structureType);
						queryStructures.put(fullClassName, m);
					}
					
					var fieldName = new FieldName(columnRecord.name());
					var classFieldType = structure.fields.get(fieldName);
					if (classFieldType == null) {
						fieldName = classMappedColumns.get(fullClassName).get(new ColumnName(columnRecord.name()));
						if (fieldName == null)
							throw new RuntimeException("Class " + fullClassNameStr + " does not have field named: " + columnRecord.name());
						classFieldType = structure.fields.get(fieldName);
					}
					String alias = columnRecord.alias();
					if (alias == null) alias = columnRecord.name();
					if (columnRecord.table() != null) alias = columnRecord.table() + "." + alias;
					m.fields.put(new ColumnName(columnRecord.name()), new ColumnField(alias, new Field(fieldName.name(), classFieldType.name())));
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

			queryClassToCreate.content += "\n\t//##end##\n}";
			queryClassToCreate.content = queryClassToCreate.content.replaceAll("#import#", queryImportsStr);
			queryClassesToCreate.put(queryClassName, queryClassToCreate);

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

		for (ClassToCreate classToCreate : queryClassesToCreate.values()) {
			List<String> queryGroupedByMethods = queryGroupedByMethodsMap.get(classToCreate.className);
			if (queryGroupedByMethods != null) {
				String methods = "";
				for (String s: queryGroupedByMethods) methods += s;
				classToCreate.content = classToCreate.content.replace("//##end##\n", methods);
			}
			writeBuilderFile(classToCreate);
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
		writeBuilderFile(packageName, generatedColumnsClassName, generatedColumnsClassToCreate.content);
	}

	private String getRecordConstructor(FullClassName fullClassName, String recordName, Map<ColumnName, ColumnField> fields, int objCounter) {
		final RecordComponent recordComponents = javaStructures.get(fullClassName).recordComponents;
		final var mappedClassFields = classMappedColumns.get(fullClassName);
		final List<String> fieldsInQuery = new ArrayList<>();
		String fieldsInitialization = "";
		for (var fieldEntry : fields.entrySet()) {
			String fieldName = fieldEntry.getKey().name();
			String columnAlias = fieldEntry.getValue().columnAlias();
			Field field = fieldEntry.getValue().field();
			if (mappedClassFields != null) {
				if (mappedClassFields.get(new ColumnName(fieldName)) != null) {
					fieldName = mappedClassFields.get(new ColumnName(fieldName)).name();
				}
			}
			fieldsInQuery.add(fieldName);
			fieldsInitialization += getResultSetForFieldTypeForRecord(field.type(), columnAlias, fieldName, objCounter);
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

	private static String copyRecordObjectInitializingLists(FullClassName fullClassName, String recordName,
			Map<ColumnName, ColumnField> fields, Map<FullClassName, JavaStructure> javaStructures) {
		final RecordComponent recordComponents = javaStructures.get(fullClassName).recordComponents;
		String fieldsInitialization = "";
		
		String constructorParameters = "";
		for (int i = 0; i < recordComponents.fields.size(); i++) {
			String fieldName = recordComponents.fields.get(i);
			String fieldType = recordComponents.types.get(i);
			if (fieldType.contains("java.util.List")) {
				fieldsInitialization += """
								%s %s = new ArrayList<>();
				""".formatted(fieldType, fieldName, fieldName);
			} else {
				fieldsInitialization += """
								%s %s = curr.%s();
				""".formatted(fieldType, fieldName, fieldName);
			}
			
			constructorParameters += fieldName;
			if (i + 1 < recordComponents.fields.size()) constructorParameters += ", ";
		}
		
		return """
		%s
						obj = new %s(%s);
		""".formatted(fieldsInitialization, recordName, constructorParameters);
	}

	public static String getDefaultValueForType(String fieldType) {
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
			Map<FullClassName, QueryClassStructure> queryStructures) {

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
			QueryClassStructure queryStructure = entry.getValue();
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
					setFields += getResultSetFieldBasedOnType(field.type(), columnAlias, objCounter, fieldSetMethod);
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

		// ===== Check relationships - ManyToOne and OneToOne
		for (var obj : objects.entrySet()) {
			var owner = relationships.get(obj.getKey());
			if (owner == null) continue;
			String ownerObj = obj.getValue();
			for (var partner : owner) {
				var partnerObj = objects.get(partner.partner());
				if (partnerObj == null) continue;
				switch (partner.type()) {
					case OneToOne, ManyToOne:
						String set = "set" + uppercaseFirstLetter(partner.partnerFieldName().name());
						methodBody += """
									%s.%s(%s);
						""".formatted(ownerObj, set, partnerObj);
						break;
				}	
			}
		}

		methodBody += """
				}

				return records;
		""";
	
		String ret = """

					public static %s %s(ResultSet rs) throws SQLException {
						%s
					}

				""".formatted(queryClassName, queryName, methodBody);
		
		// If there's a OneToMany or ManyToMany relationship, then create
		// a groupedBy method, eg: groupedByPerson
		System.out.println("--------------- CHECKING MANY RELATIONSHIPS ----------------");
		for (var queryStructure : queryStructures.entrySet()) {
			FullClassName fcn = queryStructure.getKey();
			var ownerRelationships = relationships.get(fcn);
			if (ownerRelationships == null) {
				System.out.println(fcn + " does not have any relationship mapped in it's class.");
				continue;
			} else {
				System.out.println(fcn + " relationships are: " + ownerRelationships);
			}

			for (var rel : ownerRelationships) {
				if (rel.type() != Relationship.Type.OneToMany && rel.type() != Relationship.Type.ManyToMany) {
					continue;
				}
				FullClassName partnerClass = getClassInGenericDeclaration(rel.partner());
				var partnerObj = queryStructures.get(partnerClass);
				if (partnerObj == null) continue;
				List<String> groupedByMethods = queryGroupedByMethodsMap.get(queryClassName);
				if (groupedByMethods == null) {
					groupedByMethods = new ArrayList<>();
					queryGroupedByMethodsMap.put(queryClassName, groupedByMethods);
				}
				groupedByMethods.add(createManyRelationshipGrupedByMethod(fcn, ownerRelationships,
						queryStructures));
				break;
			}
		}

		return ret;
	}

	private String getResultSetFieldBasedOnType(String type, String columnAlias, int objCounter,
			String fieldSetMethod) {
		var resultSetType = ResultSetType.fromString(type);
		switch (resultSetType) {
			case CHAR:
				return """
							var str = rs.getString("%s");
							if (str != null && str.length() >= 1)
								obj%s.%s(str.charAt(0));
				""".formatted(columnAlias, objCounter, fieldSetMethod);
			case BIG_INTEGER:
				return """
							java.math.BigDecimal dec = rs.getBigDecimal("%s");
							if (dec != null)
								obj%s.%s(dec.toBigInteger());
				""".formatted(columnAlias, objCounter, fieldSetMethod);
			default:
				String resultSetGetMethod = resultSetType.getResultSetGetMethod();
				return """
							obj%s.%s(rs.%s("%s"));
				""".formatted(objCounter, fieldSetMethod, resultSetGetMethod, columnAlias);
		}
	}

	private String getResultSetForFieldTypeForRecord(String type, String columnAlias, String fieldName, int objCounter) {
		var resultSetType = ResultSetType.fromString(type);
		final String tmpField = fieldName + objCounter;
		switch (resultSetType) {
			case CHAR:
				String fieldString = fieldName + "String";
				return """
							%s %s = rs.getString("%s");
							var %s = ' ';
							if (%s != null && %s.length() >= 1)
							%s = %s.charAt(0);
				""".formatted(type, fieldString, columnAlias, fieldName, fieldString, fieldString,
						fieldName, fieldString);
			case BIG_INTEGER:
				return """
							java.math.BigDecimal %s = rs.getBigDecimal("%s");
							java.math.BigInteger %s = null;
							if (%s != null)
								%s = %s.toBigInteger();
				""".formatted(tmpField, columnAlias, fieldName, tmpField, fieldName, tmpField);
			default:
				return """
							%s %s = rs.%s("%s");
				""".formatted(type, fieldName, resultSetType.getResultSetGetMethod(), columnAlias);
		}
	}

	private static FullClassName getClassInGenericDeclaration(FullClassName genericDeclaration) {
		String str = genericDeclaration.name();
		int lessSign = str.indexOf("<");
		int greaterSign = str.indexOf(">");
		return new FullClassName(str.substring(lessSign + 1, greaterSign));
	}

	private String createManyRelationshipGrupedByMethod(FullClassName fcn, List<Relationship> ownerRelationships,
			Map<FullClassName, QueryClassStructure> queryStructures) {
		System.out.println("owner relationships: " + ownerRelationships);
		final String ownerClass = fcn.getClassName();
		final QueryClassStructure queryClassStructure = queryStructures.get(fcn);
		
		List<Field> keyFields = primaryKeys.get(fcn);
		if (keyFields == null) {
			System.err.println("WARNING!!! " + ownerClass + " does not have a field annotated with @Id");
			System.err.println("WARNING!!! Can't create groupedBy method without knowing the @Id");
			return "";
		}
		// System.out.println("-------------- PRIMARY KEYS ---------------");
		// System.out.println(keyFields);
		var queryFields = queryClassStructure.fields;
		for (var keyField : keyFields) {
			if (!queryFields.containsKey(new ColumnName(keyField.name()))) {
				System.out.println("WARNING!!! Can't create groupedBy for class " + ownerClass +
						" because query does not contain key: " + keyField.name());
				return "";
			}
		}

		var newKeyRecord = new NewKeyRecord(ownerClass, queryClassStructure, keyFields);
		var collectionCreateAndAddMethods = new CollectionCreateAndAddMethods(fcn, ownerClass,
				ownerRelationships, queryClassStructure, queryStructures, javaStructures);

		if (queryClassStructure.type == Type.CLASS) {
			return """

					private static record %sId(%s) {}
					public List<%s> groupedBy%s() {
						Map<%sId, %s> map = new HashMap<>();
						List<%s> join = new ArrayList<>();
						int len = getList%s().size();
						for (int i = 0; i < len; i++) {
							var curr = getList%s().get(i);
							%s
							var key = %s;
							var obj = map.get(key);
							if (obj == null) {
								obj = curr;
								%s
								map.put(key, obj);
								join.add(obj);
							}
							%s
						}
						return join;
					}
				""".formatted(ownerClass, newKeyRecord.params,
					ownerClass, ownerClass,
					ownerClass, ownerClass,
					ownerClass,
					ownerClass,
					ownerClass,
					newKeyRecord.getKeys,
					newKeyRecord.constructor,
					collectionCreateAndAddMethods.createPartners,
					collectionCreateAndAddMethods.addToPartners);
		} else {
			return """

					private static record %sId(%s) {}
					public List<%s> groupedBy%s() {
						Map<%sId, %s> map = new HashMap<>();
						List<%s> join = new ArrayList<>();
						int len = getList%s().size();
						for (int i = 0; i < len; i++) {
							var curr = getList%s().get(i);
							%s
							var key = %s;
							var obj = map.get(key);
							if (obj == null) {
								// copying record and initilializing lists
								%s
								map.put(key, obj);
								join.add(obj);
							}
							%s
						}
						return join;
					}
				""".formatted(ownerClass, newKeyRecord.params,
					ownerClass, ownerClass,
					ownerClass, ownerClass,
					ownerClass,
					ownerClass,
					ownerClass,
					newKeyRecord.getKeys,
					newKeyRecord.constructor,
					collectionCreateAndAddMethods.createPartners,
					collectionCreateAndAddMethods.addToPartners);
		}
	}

	private static class NewKeyRecord {
		String params = "";
		String getKeys = "";
		String constructor = "";

		NewKeyRecord(String ownerClass, QueryClassStructure queryClassStructure, List<Field> keyFields) {
			int keyCounter = 0;
			String newKeyRecord = "new " + ownerClass + "Id(";
			for (var f : keyFields) {
				params += f.type() + " " + f.name() + ", ";
				if (queryClassStructure.type == Type.CLASS) {
					String getMethod = "get" + uppercaseFirstLetter(f.name());
					getKeys += """
							var key%s = curr.%s();
							""".formatted(keyCounter, getMethod);
				} else {
					getKeys += """
							var key%s = curr.%s();
							""".formatted(keyCounter, f.name());
				}
				newKeyRecord += "key" + keyCounter + ", ";
				keyCounter++;
			}
			params = params.substring(0, params.length() - 2);
			constructor = newKeyRecord.substring(0, newKeyRecord.length() - 2) + ")";
		}
	}

	private static class CollectionCreateAndAddMethods {
		String createPartners = "";
		String addToPartners = "";
		
		public CollectionCreateAndAddMethods(FullClassName fcn, String ownerClass,
				List<Relationship> ownerRelationships, QueryClassStructure queryClassStructure,
				Map<FullClassName, QueryClassStructure> queryStructures, Map<FullClassName, JavaStructure> javaStructures) {
			for (var rel : ownerRelationships) {
				if (rel.type() != Relationship.Type.OneToMany && rel.type() != Relationship.Type.ManyToMany) {
					continue;
				}
				FullClassName partnerClass = getClassInGenericDeclaration(rel.partner());
				QueryClassStructure partnerObj = queryStructures.get(partnerClass);
				System.out.println("######### checking relationship: " + rel);
				System.out.println("rel partner: " + rel.partner() + ", partnerClass = " + partnerClass);
				if (partnerObj == null) continue;
				if (queryClassStructure.type == Type.CLASS) {
					final String PartnerFieldName = uppercaseFirstLetter(rel.partnerFieldName().name());
					createPartners += """
							obj.set%s(new ArrayList<>());
							""".formatted(PartnerFieldName);
					addToPartners += """
							obj.get%s().add(getList%s().get(i));
							""".formatted(PartnerFieldName, partnerClass.getClassName());
				} else {
					createPartners += copyRecordObjectInitializingLists(fcn, ownerClass, queryClassStructure.fields, javaStructures);
					addToPartners += """
							obj.%s().add(getList%s().get(i));
							""".formatted(rel.partnerFieldName().name(), partnerClass.getClassName());
				}
			}
		}
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

	public static String uppercaseFirstLetter(final String str) {
		// System.out.println("---> uppercaseFirstLetter. param = " + str);
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
				/*for (var enclosed : e.getEnclosedElements()) {
					System.out.println(enclosed + " kind is: " + enclosed.getKind());
				}*/

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
					case "com.github.mapresultset.api.ManyToMany":
						processManyRelationship(elementName, e, Relationship.Type.ManyToMany);
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
		fields.add(new Field(elementName, e.asType().toString()));
	}

	private void processManyRelationship(String elementName, Element e, Relationship.Type type) {
		FullClassName fcn = new FullClassName(e.getEnclosingElement().toString());
		FullClassName partner = new FullClassName(e.asType().toString());
		List<Relationship> list = relationships.get(fcn);
		if (list == null) {
			list = new ArrayList<>();
			relationships.put(fcn, list);
		}
		list.add(new Relationship(fcn, partner, new FieldName(elementName), type));
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
