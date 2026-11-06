package ORM.Queries;

import ORM.Clases.Tabla;
import ORM.DB;
import ORM.Exceptions.Checked.ErrorEnQueryException;
import ORM.Exceptions.ORMException;
import ORM.SQL.Schema;
import ORM.SQL.TipoColumna;

import java.sql.*;
import java.util.ArrayList;

public record InsertIntoQuery(
	Schema schema,
	Tabla fila
) implements Query{

	@Override
	public PreparedStatement toPreparedStatement(Connection conexion)
	throws ORMException{
		//TODO: only update dirty fields

		try{

			final var query_str =
			"""
			INSERT INTO %s (%s) VALUES (%s) AS %s
			ON DUPLICATE KEY UPDATE %s;
			""";

			final var alias_fila = "NUEVA_FILA";
			final var columnas   = new ArrayList<String>();
			final var valores    = new ArrayList<>();
			final var update     = new ArrayList<String>();

			for(final var col : schema.columnas().values()){
				final var valor = DB.valorDeColumna(fila, col);

				// salteamos la PK si es null, porque estamos guardando
				// algo nuevo y queremos que la DB la inserte
				if(col.pk() && valor == null) continue;

				columnas.add(col.nombre());
				valores.add(valor);
				update.add("%s = %s.%s".formatted(
					 col.nombre(), alias_fila, col.nombre()
				));
			}

			final var statement = conexion.prepareStatement(
				query_str.formatted(
					schema.tabla(),
					String.join(", ", columnas),
					"?, ".repeat(valores.size() - 1) + " ?",
					alias_fila,
					String.join(", ", update)
				),
				Statement.RETURN_GENERATED_KEYS //necesario para poder hidratar la PK
			);


			for(int i = 0; i < valores.size(); i++){
				TipoColumna.valueIntoStatement(
					statement,
					i + 1, //el statement is 1-based quien sabe PORQUE!!!!
					valores.get(i)
				);

			}
			return statement;

		}catch(SQLException e){ throw new ErrorEnQueryException(e); }
	}
}
