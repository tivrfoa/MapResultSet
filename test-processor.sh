rm -fR classes/* &&
  rm -fR generatedSources/* &&
  find src/main/java/com/github/tivrfoa/mapresultset/ -name "*.java" > mapsources.txt &&
  echo '--- Compiling annotation processor sources:' &&
  echo '--------------' && cat mapsources.txt && echo '---------------' &&
  javac -d classes/ @mapsources.txt &&
  find src/test/java/org/ -name "*.java" > sources.txt &&
  javac -cp classes/ -processor com.github.tivrfoa.mapresultset.MappingProcessor -s generatedSources \
    -d classes/ @sources.txt src/test/java/com/github/tivrfoa/mapresultset/TestProcessor.java &&
  echo '--- Running TestProcessor' &&
  java -cp classes/:lib/mysql-connector-java-8.0.29.jar -ea com.github.tivrfoa.mapresultset.TestProcessor
