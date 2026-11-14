package ORM.Queries;

import ORM.Exceptions.ORMException;
import ORM.MySQL;
import ORM.SQL.Schema;

import java.sql.PreparedStatement;
import java.util.ArrayList;

public record CreateTableQuery(Schema schema) implements Query{

	//TODO: split function

	@Override
	public String toString(){
		final var cols_str = new ArrayList<String>();

		for(final var col : schema.columnas().values()){
			cols_str.add(col.toSQLDefinition());
		}

		cols_str.add("PRIMARY KEY (%s)".formatted(
			schema.columnas().get(schema.pk()).nombre()
		));

		//TODO: fkeys?

		return "CREATE TABLE IF NOT EXISTS %s (%s);".formatted(
			schema.tabla(), String.join(",\n", cols_str));
	}

	@Override
	public PreparedStatement toPreparedStatement(MySQL conexion)
	throws ORMException{
		//TODO:
		return null;
	}
}
