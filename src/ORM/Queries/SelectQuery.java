package ORM.Queries;
import ORM.Clases.Tabla;
import ORM.DB;
import ORM.Exceptions.ColumnaAusenteException;
import ORM.Exceptions.ORMException;
import ORM.SQL.Schema;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public final class SelectQuery implements Query{

	final private Class<? extends Tabla> from;
	final private Schema schema;

	final private ArrayList<Where>  wheres;
	final private ArrayList<String> groupBy; //TODO:
	final private ArrayList<String> orderBy; //TODO:
	private Integer limit;

	public SelectQuery(Class<? extends Tabla> from){
		this.from    = from;
		this.schema  = DB.schemaDeTabla(from);
		this.wheres  = new ArrayList<>();
		this.groupBy = new ArrayList<>();
		this.orderBy = new ArrayList<>();
		this.limit   = null;
	}

	//where
	public SelectQuery where(
		String nombre_columna,
		String comparador,
		String valor
	) throws ORMException{
		final var _ = DB.getColumna(schema, nombre_columna);
		wheres.add(new Where.BasicWhere(nombre_columna, comparador, valor));
		return this;
	}

	public SelectQuery where(
		String nombre_columna,
		String comparador,
		Integer valor_int
	) throws ORMException{
		return where(nombre_columna, comparador, valor_int.toString());
	}

	public SelectQuery where(String columna, String valor)
	throws ORMException{ return where(columna, "=", valor); }

	public SelectQuery andWhere(
		String nombre_columna,
		String comparador,
		String valor
	) throws ORMException{
		final var _ = DB.getColumna(schema, nombre_columna);
		wheres.add(new Where.BasicWhere(
			nombre_columna, comparador, valor, Where.Conector.AND
		));
		return this;
	}

	public SelectQuery orWhere(
		String nombre_columna,
		String comparador,
		String valor
	) throws ORMException{
		final var _ = DB.getColumna(schema, nombre_columna);
		wheres.add(new Where.BasicWhere(
			nombre_columna, comparador, valor, Where.Conector.OR
		));
		return this;
	}

	//
	public SelectQuery groupBy(){ return this; }
	public SelectQuery limit(int cantidad){
		limit = cantidad;
		return this;
	}
	public SelectQuery orderBy(){ return this; }

	//results
	public Tabla first() throws ORMException{
		final var result = DB.ejecutarQuery(limit(1), from);
		return result.getFirst();
	}
	public void toList(){ return; }
	public void count(){ return; }
	public void avg(){ return; }
	public void min(){ return; }
	public void max(){ return; }
	public void sum(){ return; }

//	public void distinct(){ return; }

	@Override
	public PreparedStatement toPreparedStatement(Connection conexion) throws ORMException{
		final var columnas = new ArrayList<String>();
		for(final var col : schema.columnas().values()){
			columnas.add(col.nombre());
		}

		final var sql_query = new StringBuilder();
		sql_query.append("SELECT %s FROM %s".formatted(
			String.join(", ", columnas), schema.tabla()
		));

		if(!wheres.isEmpty()){
			//TODO: no podria ser un NestedWhere en lugar de esto y mas facil?
			final var wheres_str = new StringBuilder();
			for(int i = 0; i < wheres.size(); i++){
				final var w = wheres.get(i);
				wheres_str.append(w.toString(i));
			}
			sql_query.append(" WHERE %s".formatted(wheres_str));
		}
		//TODO: me esta faltando usar wildcards con el Where!

		if(limit != null) sql_query.append(" LIMIT %s".formatted(limit));
//		if(!groupBy.isEmpty()) //TODO:
//		if(!orderBy.isEmpty()) //TODO:
		sql_query.append(";");

		return conexion.prepareStatement(
			sql_query.toString()
		);
	}



}
