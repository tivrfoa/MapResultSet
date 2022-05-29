rm -fR classes/* &&
  javac -d classes/ com/github/mapresultset/*.java &&
  find org/ -name "*.java" > sources.txt &&
  javac -cp classes/ -processor com.github.mapresultset.MappingProcessor -s generatedSources \
    -d classes/ @sources.txt tests/TestProcessor.java &&
  java -cp classes/ -ea tests.TestProcessor

