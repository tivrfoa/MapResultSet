package tests;

import com.github.mapresultset.Query;
import com.github.mapresultset.Table;

import org.acme.domain.Notebook;

public class TestProcessor {

	@Query
	final String listPhones = """
		select phone.id, c.model
		from phone join company as c on
		  phone.company_id = c.id
		""";
	// TODO create a test making sure that the lines
	//   below throw exception
	// String sql = "select id from phone";
	// final String sql = null;
	// final String listPhones = "select id, phone.id from phone";
	/*final String listPhones = """
		select phone, model
		from phone join company as c on
		  phone.company_id = c.id
		""";
		*/

	// TODO it needs to create a method like:
	// List<Phone> phones = MapResultSet.listPhones(rs);

	public static void main(String[] args) {
		var phone = new Phone();
		var notebook = new Notebook();
	}

	@Table(name = "phone")
	public static class Phone {
		private int id;

		public int getId() {
			return this.id;
		}
	}
}

