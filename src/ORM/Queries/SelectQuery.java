package ORM.Queries;
import ORM.Clases.Tabla;
import ORM.DB;
import ORM.Exceptions.Checked.DireccionInvalidaException;
import ORM.Exceptions.Checked.ErrorEnQueryException;
import ORM.Exceptions.Checked.OrderByImparesException;
import ORM.Exceptions.ORMException;
import ORM.MySQL;
import ORM.SQL.Schema;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

import static ORM.Queries.Where.*;
import static ORM.Queries.Where.NestedWhere.NestedWhereFunction;

public final class SelectQuery<T extends Tabla> implements Query{

	private enum Dir {DESC, ASC}

	final private Class<T> from;
	final private Schema schema;
	final private NestedWhere base_filter; //el where final es (base_filter) AND (where)

	final private ArrayList<String>    select;
	final private NestedWhere          where;
	final private ArrayList<String>    groupBy;
	final private NestedWhere          having;
	final private HashMap<String, Dir> orderBy;
	private Integer limit;
	private Integer offset;

	public SelectQuery(Class<T> from, NestedWhere base_filter){
		this.from        = from;
		this.base_filter = base_filter;

		this.schema  = DB.schemaDeTabla(from);
		this.select  = new ArrayList<>();
		this.where   = new NestedWhere();
		this.groupBy = new ArrayList<>();
		this.having  = new NestedWhere();
		this.orderBy = new HashMap<>();
		this.limit   = null;
		this.offset  = null;

		//el select por defecto tiene todas las columnas
		for(final var col : schema.columnas().values()){
			select.add(col.nombre());
		}
	}

	public SelectQuery(Class<T> from){ this(from, new NestedWhere()); }

	//where
	public SelectQuery<T> where(String columna, String comparador, Object valor)
	throws ORMException{
		final var _ = DB.getColumna(schema, columna);
		where.and(columna, comparador, valor);
		return this;
	}

	public SelectQuery<T> where(String columna, Object valor)
	throws ORMException{ return where(columna, igualA(valor), valor); }

	public SelectQuery<T> where(String columna, String comparador, Object valor, Object ...valores)
	throws ORMException{
		final var vals = new ArrayList<>();
		vals.add(valor);
		vals.addAll(List.of(valores));
		return where(columna, comparador, vals);
	}

	public SelectQuery<T> where(NestedWhereFunction anidado)
	throws ORMException{
		where.and(anidado);
		return this;
	}

	//andWhere == where
	public SelectQuery<T> andWhere(String columna, String comparador, Object valor)
	throws ORMException{ return where(columna, comparador, valor); }

	public SelectQuery<T> andWhere(String columna, Object valor)
	throws ORMException{ return where(columna, igualA(valor), valor); }

	public SelectQuery<T> andWhere(String columna, String comparador, Object valor, Object ...valores)
	throws ORMException{ return where(columna, comparador, valor, valores); }

	public SelectQuery<T> andWhere(NestedWhereFunction anidado)
	throws ORMException{ return where(anidado); }

	//orWhere
	public SelectQuery<T> orWhere(String columna, String comparador, Object valor)
	throws ORMException{
		final var _ = DB.getColumna(schema, columna);
		where.or(columna, comparador, valor);
		return this;
	}

	public SelectQuery<T> orWhere(String columna, Object valor)
	throws ORMException{ return orWhere(columna, igualA(valor), valor); }

	public SelectQuery<T> orWhere(String columna, String comparador, Object valor, Object ...valores)
	throws ORMException{
		final var vals = new ArrayList<>();
		vals.add(valor);
		vals.addAll(List.of(valores));
		return orWhere(columna, comparador, vals);
	}

	public SelectQuery<T> orWhere(NestedWhereFunction anidado)
	throws ORMException{
		where.or(anidado);
		return this;
	}

	//groupBy
	public SelectQuery<T> groupBy(String ...columnas){
		groupBy.clear();
		groupBy.addAll(Arrays.asList(columnas));
		return this;
	}

	public SelectQuery<T> limit(int cantidad){
		limit = cantidad;
		return this;
	}

	public SelectQuery<T> offset(int cantidad){
		offset = cantidad;
		return this;
	}

	//orderBy
	public SelectQuery<T> orderBy(String ...col_dir_pairs) throws ORMException{
		if(col_dir_pairs.length % 2 != 0){
			throw new OrderByImparesException();
		}

		orderBy.clear();
		for(int i = 0; i < col_dir_pairs.length; i+=2){
			final var columna   = col_dir_pairs[i];
			final var direccion = col_dir_pairs[i+1];

			final var dir = switch(direccion.toLowerCase()){
				case "asc"  -> Dir.ASC;
				case "desc" -> Dir.DESC;
				default     -> throw new DireccionInvalidaException(direccion);
			};
			orderBy.put(columna, dir);
		}

		return this;
	}

	//results
	public List<T> toList() throws ORMException{
		final var results = DB.ejecutarQuery(this);
		final var filas   = DB.resultSetAFilas(results);
		return DB.instanciarFilas(filas, from);

	}

	public T first() throws ORMException{
		return limit(1).toList().getFirst();
	}

	public List<T> paginar(int resultados_por_pagina, int pagina)
	throws ORMException{
		if (pagina < 1) pagina = 1;
		return limit(resultados_por_pagina).offset(
			resultados_por_pagina * (pagina - 1)
		).toList();
	}

	public Long count() throws ORMException{
		return DB.ejecutarFuncion(select("COUNT(*)"), ResultSet::getLong);
	}

	public Double avg(String columna) throws ORMException{
		return DB.ejecutarFuncion(
			select("AVG(%s)".formatted(columna)),
			ResultSet::getDouble
		);
	}

	public Double sum(String columna) throws ORMException{
		return DB.ejecutarFuncion(
			select("SUM(%s)".formatted(columna)),
			ResultSet::getDouble
		);
	}

	//TODO: estas son mas complejas asi que pa despues
//	public void min(){ return; }
//	public void max(){ return; }
//	public void distinct(){ return; }

	public SelectQuery<T> select(String ...columnas){
		select.clear();
		select.addAll(Arrays.asList(columnas));
		return this;
	}

	@Override
	public PreparedStatement toPreparedStatement(MySQL conexion)
	throws ORMException{
		final var sql_query = new StringBuilder();
		sql_query.append("SELECT %s FROM %s".formatted(
			String.join(", ", select), schema.tabla()
		));

		final var filter_str = base_filter.toString(0);
		final var where_str  = where.toString(0);
		final var filtered_where = Stream.of(filter_str, where_str)
			.filter(s -> !s.isBlank())
			.toList();
		final var filtered_where_str = String.join(" AND ", filtered_where);
		if(!filtered_where_str.isBlank()){
			sql_query.append(" WHERE %s".formatted(filtered_where_str));
		}

		//TODO: me esta faltando usar ? wildcards con el Where!

		//GROUP BY
		if(!groupBy.isEmpty()){
			sql_query.append(" GROUP BY %s".formatted(
				String.join(", ", groupBy)
			));
		}

		//HAVING
		final var having_str = having.toString(0);
		if(!having_str.isBlank()){
			sql_query.append(" HAVING %s".formatted(having_str));
		}

		//ORDER BY
		if(!orderBy.isEmpty()){
			final var order_by_str = orderBy.entrySet().stream().map(
				entry -> "%s %s".formatted(
					entry.getKey(),
					entry.getValue().toString()
				)).toList();
			sql_query.append(" ORDER BY %s".formatted(String.join(", ", order_by_str)));
		}

		//LIMIT
		if(limit != null){
			sql_query.append(" LIMIT %s".formatted(limit));
		}

		//OFFSET
		if(limit != null && offset != null){
			sql_query.append(" OFFSET %s".formatted(offset));
		}

		sql_query.append(";");

		try{
			return conexion.prepareStatement(
				sql_query.toString()
			);
		}catch(SQLException e){
			throw new ErrorEnQueryException(e);
		}
	}

	private static String igualA(Object valor){
		final var comparador = switch(valor){
			case null -> Comparador.IS;
			case String s when s.equals("NULL") -> Comparador.IS;
			case String s when s.equals("null") -> Comparador.IS;
			default -> Comparador.EQ;
		};
		return comparador.toString();
	}

}
