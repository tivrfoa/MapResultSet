import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class OneToMany {

	public static void main(String[] args)  {
		// System.out.println(nthPrime(Integer.parseInt(args[0])));

		List<Person> pp = new ArrayList<>();
		pp.add(new Person(1, "any"));
		pp.add(new Person(2, "bob"));
		pp.add(new Person(3, "leo"));
		pp.add(new Person(2, "bob"));
		pp.add(new Person(1, "any"));

		List<Address> listAddress = new ArrayList<>();
		listAddress.add(new Address(1, "c1"));
		listAddress.add(new Address(2, "c2"));
		listAddress.add(new Address(3, "c3"));
		listAddress.add(new Address(4, "c4"));
		listAddress.add(new Address(5, "c5"));

		// One Person can have many Addresses
		// The lists will have the same size
		// I'll use a map to know if that Person is already
		// in the list. The key will be the same type as the
		// primary key (maybe I can just use a String as key
		// to make the code simpler ...)
		Map<Integer, Person> map = new HashMap();
		List<Person> join = new ArrayList<>();

		int len = pp.size();
		for (int i = 0; i < len; i++) {
			Person p = map.get(pp.get(i).id);
			if (p == null) {
				map.put(pp.get(i).id, pp.get(i));
				p = pp.get(i);
				join.add(p);
			}
			p.getAddresses().add(listAddress.get(i));
		}
		System.out.println(join);
	}
}

class Person {
	int id;
	String name;
	List<Address> addresses = new ArrayList<>();
	Person(int i, String n) { id = i; name = n; }
	public List<Address> getAddresses() {
		return this.addresses;
	}
	public String toString() {
		return "Person [id = " + id + ", name = " + name +
				", addresses = " + addresses + "]";
	}
}

class Address {
	int id;
	String city;
	Address(int i, String c) { id = i; city = c; }
	public String toString() {
		return "Address [id = " + id + ", city = " + city + "]";
	}
}
