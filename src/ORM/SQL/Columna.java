package ORM.SQL;

import java.util.ArrayList;

public record Columna(
	String nombre,
	TipoColumna tipo,
	boolean not_null,
	boolean pk
) implements Comparable<Columna> {

	public String toSQLDefinition(){
		//column_name type AUTO_INCREMENT NOT NULL UNIQUE DEFAULT X
		final var col_str = new ArrayList<String>();

		col_str.add(nombre);
		col_str.add(tipo.toStringWithSize());

		//TODO: fk?
		if(pk){
			col_str.add("AUTO_INCREMENT");
			col_str.add("NOT NULL");
			col_str.add("UNIQUE");
		}else if(not_null){
			col_str.add("NOT NULL");
		}

		//col_str.add("DEFAULT %s");

		return String.join(" ", col_str);
	}


	@Override
	public int compareTo(Columna col){
		return this.nombre.compareTo(col.nombre);
	}
}
