package ORM.Queries;

import ORM.Exceptions.Checked.ErrorEnQueryException;
import ORM.Exceptions.ORMException;
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
		Connection conexion
	) throws ORMException{

		final var query = new ArrayList<String>();
		final var query_str      = "ALTER TABLE %s %s %s;";
		final var add_col_str    = "ADD COLUMN IF NOT EXISTS %s";
		final var drop_col_str   = "DROP COLUMN IF EXISTS %s";
		final var change_col_str = "CHANGE COLUMN %s %s";

		for(var col : add_columns){
			query.add(add_col_str.formatted(col.toSQLDefinition()));
		}

		for(var col : drop_columns){
			query.add(drop_col_str.formatted(col.nombre()));
		}

		for(var entry : change_columns.entrySet()){
			final var nombre_anterior = entry.getKey();
			final var col = entry.getValue();
			query.add(change_col_str.formatted(
				nombre_anterior,
				col.toSQLDefinition()
			));
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
