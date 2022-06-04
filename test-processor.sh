rm -fR classes/* &&
  rm -fR generatedSources/* &&
  find com/github/mapresultset/ -name "*.java" > mapsources.txt &&
  echo '--- Compiling annotation processor sources:' &&
  echo '--------------' && cat mapsources.txt && echo '---------------' &&
  javac -d classes/ @mapsources.txt &&
  find org/ -name "*.java" > sources.txt &&
  javac -cp classes/ -processor com.github.mapresultset.MappingProcessor -s generatedSources \
    -d classes/ @sources.txt tests/TestProcessor.java &&
  echo '--- Running TestProcessor' &&
  java -cp classes/:lib/mysql-connector-java-8.0.29.jar -ea tests.TestProcessor

