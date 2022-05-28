javac -d classes/ com/github/mapresultset/Query.java &&
  javac -d classes/ com/github/mapresultset/Table.java &&
  javac -d classes/ com/github/mapresultset/MappingProcessor.java &&
  javac -cp classes/ -processor MappingProcessor -d classes/ org/acme/Notebook.java &&
  javac -cp classes/ -processor MappingProcessor -d classes/ somepackage/TestProcessor.java &&
  java -cp classes/ -ea somepackage.TestProcessor

