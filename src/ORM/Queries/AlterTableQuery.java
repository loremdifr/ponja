package ORM.Queries;

import ORM.Exceptions.Checked.ErrorEnQueryException;
import ORM.Exceptions.ORMException;
import ORM.MySQL;
import ORM.SQL.Columna;
import ORM.SQL.Schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;


//TODO:
public record AlterTableQuery(
	Schema                   schema,
	boolean                  pk_changed,
	ArrayList<Columna>       add_columns,
	ArrayList<Columna>       drop_columns,
	HashMap<String, Columna> change_columns
) implements Query{

	@Override
	public PreparedStatement toPreparedStatement(
		MySQL conexion
	) throws ORMException{

		//NOTE: tanto add column como drop column se tienen
		//      que fijar si existe o no, porque mysql no tiene
		//      el "IF EXISTS" ni el "IF NOT EXISTS".
		//      para cuando llega aca, ya se comprobo esto en el
		//      DB::actualizarTabla

		final var query = new ArrayList<String>();
		final var query_str      = "ALTER TABLE %s %s %s;";
		final var add_col_str    = "ADD COLUMN %s";
		final var drop_col_str   = "DROP COLUMN %s";
		final var change_col_str = "CHANGE COLUMN %s %s";
		final var modify_col_str = "MODIFY COLUMN %s";

		for(var col : add_columns){
			query.add(add_col_str.formatted(col.toSQLDefinition()));
		}

		for(var col : drop_columns){
			query.add(drop_col_str.formatted(col.nombre()));
		}

		for(var entry : change_columns.entrySet()){
			final var nombre_anterior = entry.getKey();
			final var col = entry.getValue();
			if(nombre_anterior.equals(col.nombre())){
				//mismo nombre, usa modify
				query.add(modify_col_str.formatted(
					col.toSQLDefinition()
				));
			}else{
				//cambia el nombre, usa change
				query.add(change_col_str.formatted(
					nombre_anterior,
					col.toSQLDefinition()
				));
			}
		}

		var pk_str = pk_changed ? "DROP PRIMARY KEY, ADD PRIMARY (%s)".formatted(
			schema.columnas().get(schema.pk()).nombre()
		) : "";

		try{
			return conexion.prepareStatement(
				query_str.formatted(
					schema.tabla(),
					String.join(",\n", query),
					pk_str
				)
			);
		}catch(SQLException e){ throw new ErrorEnQueryException(e); }
	}
}
