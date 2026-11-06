package ORM.Queries;

enum Comparador{
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

	public static Comparador fromString(String str){
		//TODO:
		return Comparador.EQ;
	};
}
