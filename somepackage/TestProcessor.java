package somepackage;

import org.acme.Notebook;
import com.github.mapresultset.Table;

public class TestProcessor {
	public static void main(String[] args) {
		var phone = new Phone();
		var notebook = new Notebook();
	}

	@Table
	static class Phone {
	}
}

