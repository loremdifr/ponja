package ORM.Queries;

import java.util.HashMap;

public enum Comparador{
	EQ       ("="),
	LT       ("<"),
	LTE      ("<="),
	GT       (">"),
	GTE      (">="),
	NEQ      ("<>"),
	LIKE     ("LIKE"),
	NOT_LIKE ("NOT LIKE"),
	ILIKE    ("ILIKE"),
	NOT_ILIKE("NOT ILIKE"),
	IS       ("IS"),
	IS_NOT   ("IS NOT"),
	;

	private final String sql_str;
	Comparador(String sql_str){ this.sql_str = sql_str; }

	@Override
	public String toString(){ return sql_str; }

	private final static HashMap<String, Comparador> from_string;

	static {
		final var map = new HashMap<String, Comparador>();
		for(var c : values()){
			map.put(c.toString().toLowerCase(), c);
		}

		from_string = map;
	}

	public static Comparador fromString(String str){
		return from_string.get(str.toLowerCase());
	};
}
