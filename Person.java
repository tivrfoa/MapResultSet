import java.util.*;

//@Table("person")
class Person {
	private int id;
	private String name;

	public int getId() { return id; }
	public String getName() { return name; }
	public void setId(int id) { this.id = id; }
	public void setName(String name) { this.name = name; }

}

class TestPerson {
	public static void main(String[] args) {
		// some logic to find classes that have @Table annotation
		Map<String, Class> mapTableToClass = Map.of(
				"person", Person.class
				);

		@Query
		String sql = """
			select id, name
			from person as person
			""";

		var p = new ParseQuery(sql);
		p.parse();

		var listOfQueries = List.of(p);

		for (var query : listOfQueries()) {

		}
	}
}
