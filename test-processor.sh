rm -fR classes/* &&
  rm -fR generatedSources/* &&
  javac -d classes/ com/github/mapresultset/*.java &&
  find org/ -name "*.java" > sources.txt &&
  javac -cp classes/ -processor com.github.mapresultset.MappingProcessor -s generatedSources \
    -d classes/ @sources.txt tests/TestProcessor.java &&
  java -cp classes/:lib/mysql-connector-java-8.0.29.jar -ea tests.TestProcessor

