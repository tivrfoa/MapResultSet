rm -fR classes/* &&
  javac -d classes/ com/github/mapresultset/*.java &&
  javac -cp classes/ -processor com.github.mapresultset.MappingProcessor -s generatedSources \
    -d classes/ org/acme/Notebook.java somepackage/TestProcessor.java &&
  java -cp classes/ -ea somepackage.TestProcessor

