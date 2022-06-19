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

  find integrationtest/src/test/java/org/ -name "*.java" > sources.txt &&
  echo '--- Compiling Integration Test:' &&
  echo '--------------' && cat sources.txt && echo '---------------' &&
  javac -cp classes/ -processor com.github.tivrfoa.mapresultset.MappingProcessor -s generatedSources \
    -d classes/ @sources.txt integrationtest/src/test/java/com/github/tivrfoa/mapresultset/TestProcessor.java &&
    
  echo '--- Running TestProcessor' &&
  java -cp classes/:lib/mysql-connector-java-8.0.29.jar -ea com.github.tivrfoa.mapresultset.TestProcessor

rm mapsources.txt
rm sources.txt

