# MapResultSet

The goal is to automate the manual process of setting the object's properties from a ResultSet.

MapResultSet is not a query validator, so make sure your
query actually works before you use it in your Java project.


## Example of Generated Sources

https://github.com/tivrfoa/MapResultSet/tree/main/generatedSources/org/acme/dao

## Annotations

The two required annotations for using MapResultSet are `Table` and `Query`.

The `@Column` annotation is required if the column name is different from the field name.

There are also the following annotations to map relationships:

1. Id ([necessary to group the relationships](https://github.com/tivrfoa/MapResultSet/blob/main/generatedSources/org/acme/dao/ListPersonCountryRecords.java#L36))
2. OneToOne
3. OneToMany
4. ManyToOne
5. ManyToMany

The variable annotated with `@Query` must be final, eg:
```java
@Query
final String listPeople;
```

And due to [Java Annotation Processor limitation](https://stackoverflow.com/questions/3285652/how-can-i-create-an-annotation-processor-that-processes-a-local-variable), the queries must not be local variables.


## MapResultSet Query Restrictions

MapResultSet has some restrictions regarding your queries.
These restrictions could be handled, but I think they make
your query more readable too. xD

1. Join must be done using JOIN, not in WHERE clause;
2. Values returned from SELECT that are not a simple column name must
   have an alias and be preceded with `AS`, eg: select 1 as one; select age + 18 as something;
3. Columns in `select` must be preceded by the table name (or alias) if the `from` clause contains
more than one table;
4. Table alias must be preceded by `AS`


## Generated Classes Structure

### Package

Generated classes will be in the same package that contains the `@Query`.

### Class Name

There will be one MapResultSet class created per package that contains a `@Query`.
Each `@Query` also creates a class with the name of the query with the first letter in uppercase,
followed by Records, eg: the query below will create a class called ListPeopleRecords.

```java
@Query
final String listPeople;
```

**ps:** if the query contains only one table and there's no temporary columns,
then this class is not created and MapResultSet returns a list of the only class
in the query.


## Using different collections in your relationships

By default MapResultSet assumes you are using a `List` and it creates it with `ArrayList`,
but you can use any collection you want, as long as you tell which method to use to create
the collection, and which method should be used to add elements to it.

The create methods must be `static` and not accept any parameters.

Example using HashSet and LinkedList:

```java
@Table (name = "country")
public record Country(@Id int id, float density, String name,
        double squareMeters, @Column (name = "phone_code") int phoneCode,
        long someBigNumber, BigInteger evenBigger,
        // It doesn't make sense for Country to have a list of Person ...
        // It's just for testing.
        @OneToMany (createWith = "newHashSet()", addWith = "add") Set<Person> listPerson,
        
        // Just for tests. This is actually a OneToMany
        @ManyToMany (createWith = "newLinkedList()", addWith = "add") List<State> states) {
    
        public static List<State> newLinkedList() {
                return new LinkedList<State>();
        }
    
        public static Set<Person> newHashSet() {
                return new HashSet<Person>();
        }
}
```

https://github.com/tivrfoa/MapResultSet/blob/main/generatedSources/org/acme/dao/ListPersonCountryRecords.java#L56

## Using in your project

### pom.xml

Add these to `dependencies`:

```xml
        <dependency>
            <groupId>io.github.tivrfoa</groupId>
            <artifactId>mapresultset</artifactId>
            <version>0.1.0</version>
        </dependency>
        <dependency>
            <groupId>io.github.tivrfoa</groupId>
            <artifactId>mapresultset-processor</artifactId>
            <version>0.1.0</version>
        </dependency>
```

And these to `plugins`:

```xml
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.10.1</version>
        <executions>
            <execution>
            <id>process-annotations</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>compile</goal>
            </goals>
            <configuration>
                <failOnError>false</failOnError>
                <compilerArgs>
                    <arg>-proc:only</arg>
                    <arg>-implicit:none</arg>
                    <arg>-processor</arg>
                    <arg>io.github.tivrfoa.mapresultset.MappingProcessor</arg>
                </compilerArgs>
            </configuration>
            </execution>
            <execution>
                <id>default-compile</id>
                <phase>compile</phase>
                <goals>
                    <goal>compile</goal>
                </goals>
                <configuration>
                    <compilerArgs>
                        <arg>-proc:none</arg>
                    </compilerArgs>
                </configuration>
            </execution>
        </executions>
    </plugin>
```

### mvn clean compile

Every time you use one of the MapResultSet annotations, you should run `mvn clean compile`
in order for the changes to become available for you.


Then you can just call `MapResultSet.queryName(resultSet)`, eg:

https://github.com/tivrfoa/MapResultSet/blob/main/integrationtest/src/main/java/org/acme/dao/BookDao.java#L41

```java
    @Query
    private static final String listBooksOnly = """
            select b.author_name, b.name
            from book as b
            order by b.author_name
            """;
    
    public static List<Book> listBooksOnly() {
        try {
            ResultSet resultSet = executeQuery(listBooksOnly);
            return MapResultSet.listBooksOnly(resultSet);
        } catch (SQLException ex) {
            // ...
        }
    }
```

## Examples

[Integration Test](https://github.com/tivrfoa/MapResultSet/tree/main/integrationtest)

[Micronaut](https://github.com/tivrfoa/micronaut-data-access-tests/blob/main/micronaut-data-jdbc-repository-maven-java/src/main/java/com/example/GenreDao.java#L18)

[Spring Boot](https://github.com/tivrfoa/spring-jdbc-with-MapResultSet/blob/main/src/main/java/com/example/demo/RelationalDataAccessApplication.java)
