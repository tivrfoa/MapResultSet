package tests;

import com.github.mapresultset.api.Query;
import com.github.mapresultset.api.Table;

import org.acme.dao.NotebookDao;

public class TestProcessor {

	@Query
	final String listPhones = """
		select phone.id, c.name
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
		System.out.println(NotebookDao.listNotebooks());
	}

	@Table(name = "phone")
	public static class Phone {
		private int id;

		public int getId() {
			return this.id;
		}

		public void setId(int id) {
			this.id = id;
		}
		
	}
}

