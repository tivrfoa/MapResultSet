package somepackage;

import org.acme.Notebook;
import com.github.mapresultset.Query;
import com.github.mapresultset.Table;

public class TestProcessor {
	public static void main(String[] args) {
		var phone = new Phone();
		var notebook = new Notebook();
	}

	@Query
	// String sql = "select id from person";
	// final String sql = null;
	final String sql = "select id from person";

	@Table
	static class Phone {
	}
}

