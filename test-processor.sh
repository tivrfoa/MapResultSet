rm -fR classes/* &&
  rm -fR generatedSources/* &&
  find api/src/main/java/com/github/tivrfoa/mapresultset/ -name "*.java" > mapsources.txt &&
  echo '--- Compiling API:' &&
  echo '--------------' && cat mapsources.txt && echo '---------------' &&
  javac -d classes/ @mapsources.txt &&

  find processor/src/main/java/com/github/tivrfoa/mapresultset/ -name "*.java" > mapsources.txt &&
  echo '--- Compiling Processor:' &&
  echo '--------------' && cat mapsources.txt && echo '---------------' &&
  javac -d classes/ @mapsources.txt &&

  echo '--- Processing Annotations:' &&
  find integrationtest/src/main/java/org/ -name "*.java" > sources.txt &&
  echo '--------------' && cat sources.txt && echo '---------------' &&
## -proc:only does not work. It tries to compile anyways :(
  javac -verbose  -cp classes/ -Xlint:processing -proc:only -implicit:none  -processor com.github.tivrfoa.mapresultset.MappingProcessor \
    -s generatedSources @sources.txt

## Previous step fails because "-proc:only" is not respected.
## https://stackoverflow.com/questions/72795137/first-mvn-compile-fails-to-find-generated-sources
  echo '--- Compiling Integration Test:' &&
  find generatedSources -name "*.java" > sources.txt &&
  find integrationtest/src/main/java/org/ -name "*.java" >> sources.txt &&
  echo '--------------' && cat sources.txt && echo '---------------' &&
  javac -cp classes/:lib/junit-jupiter-api-5.6.2.jar:lib/apiguardian-api-1.1.0.jar \
    -d classes/ @sources.txt integrationtest/src/test/java/com/github/tivrfoa/mapresultset/TestProcessor.java &&
    
  echo '--- Running TestProcessor' &&
  java -cp classes/:lib/mysql-connector-java-8.0.29.jar:lib/junit-jupiter-5.6.2.jar:lib/junit-jupiter-api-5.6.2.jar:lib/opentest4j-1.2.0.jar \
    -ea com.github.tivrfoa.mapresultset.TestProcessor

rm mapsources.txt
rm sources.txt

