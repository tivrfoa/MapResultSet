package tests;

import org.acme.Notebook;
import com.github.mapresultset.Query;
import com.github.mapresultset.Table;

public class TestProcessor {

	@Query
	final String listPhones = "select id, model from phone";
	// TODO create a test making sure that the lines
	//   below throw exception
	// String sql = "select id from phone";
	// final String sql = null;
	// final String listPhones = "select id, phone.id from phone";

	// TODO it needs to create a method like:
	// List<Phone> phones = MapResultSet.listPhones(rs);

	public static void main(String[] args) {
		var phone = new Phone();
		var notebook = new Notebook();
	}

	@Table(name = "phone")
	static class Phone {
		private int id;

		public int getId() {
			return this.id;
		}
	}
}

