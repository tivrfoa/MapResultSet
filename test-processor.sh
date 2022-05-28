rm -fR classes/* &&
  javac -d classes/ com/github/mapresultset/Query.java &&
  javac -d classes/ com/github/mapresultset/Table.java &&
  javac -d classes/ com/github/mapresultset/MappingProcessor.java &&
  javac -cp classes/ -processor com.github.mapresultset.MappingProcessor -s generatedSources -d classes/ org/acme/Notebook.java &&
  javac -cp classes/ -processor com.github.mapresultset.MappingProcessor -s generatedSources -d classes/ somepackage/TestProcessor.java &&
  java -cp classes/ -ea somepackage.TestProcessor

