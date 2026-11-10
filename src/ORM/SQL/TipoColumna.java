package ORM.SQL;

import ORM.Exceptions.*;
import ORM.Exceptions.Checked.*;
import ORM.Relaciones.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public enum TipoColumna{
	AUTO    (""),
	BOOL    ("BOOL"),
	INT     ("INT"),
	BIGINT  ("BIGINT"),
	FLOAT   ("FLOAT"),
	DOUBLE  ("DOUBLE"),
	DECIMAL ("DECIMAL"),
	VARCHAR ("VARCHAR", 255), //TODO: parametrizar?
	TEXT    ("TEXT"),
	DATE    ("DATE"),
	DATETIME("DATETIME"),
	;

	final private String sqlString;
	final private Integer column_size;

	TipoColumna(String sqlString, Integer column_size){
		this.sqlString 	 = sqlString;
		this.column_size = column_size;
	}

	TipoColumna(String sqlString){ this(sqlString, null); }

	@Override
	public String toString(){ return sqlString; }

	public String toStringWithSize(){
		if(column_size == null) return toString();
		return "%s(%d)".formatted(sqlString, column_size);
	}

	// static ..
	static final private Map<Integer, String> sql_type_names;

	static {
		sql_type_names = new HashMap<>();
		for(var field : Types.class.getFields()){
			try{
				sql_type_names.put(
					field.getInt(null), //son static por eso null
					field.getName()
				);
			}catch(IllegalAccessException _){}
		}

	}

	static public TipoColumna fromString(String str)
	throws ORMException{
		for(var tipo_columna : values()){
			if(tipo_columna.toString().equals(str)) return tipo_columna;
		}
		throw new TipoDeColumnaInesperadoException(str);
	}

	//TODO: todo esto tendria que salir de un mapeo en lugar de tantos switches

	static public TipoColumna fromSqlType(int sql_type)
	throws ORMException{
		return switch(sql_type){
			case Types.TINYINT     -> BOOL;
			case Types.BIT         -> BOOL;
			case Types.BOOLEAN     -> BOOL;
			case Types.INTEGER     -> INT;
			case Types.BIGINT      -> BIGINT;
			case Types.FLOAT       -> FLOAT;
			case Types.DOUBLE      -> DOUBLE;
			case Types.DECIMAL     -> DECIMAL;
			case Types.VARCHAR     -> VARCHAR;
			case Types.LONGVARCHAR -> TEXT;
			case Types.TIMESTAMP   -> DATETIME;
			case Types.DATE        -> DATE;
			default -> throw new TipoDeColumnaInesperadoException(
				sql_type_names.get(sql_type)
			);
		};
	}

	static public TipoColumna fromField(Field field)
	throws ORMException{
		final var tipo = field.getType();
		final var tipo_fkey = TipoColumna.INT;
		if(tipo == ManyToOne.class)     return tipo_fkey;
		if(tipo == OneToOne.class)      return tipo_fkey;
//		if(tipo == OneToMany.class)     return null;
//		if(tipo == ManyToMany.class)    return null;
		if(tipo == Boolean.class)       return TipoColumna.BOOL;
		if(tipo == boolean.class)       return TipoColumna.BOOL;
		if(tipo == int.class)           return TipoColumna.INT;
		if(tipo == Integer.class)       return TipoColumna.INT;
		if(tipo == long.class)          return TipoColumna.BIGINT;
		if(tipo == Long.class)          return TipoColumna.BIGINT;
		if(tipo == float.class)         return TipoColumna.FLOAT;
		if(tipo == Float.class)         return TipoColumna.FLOAT;
		if(tipo == double.class)        return TipoColumna.DOUBLE;
		if(tipo == Double.class)        return TipoColumna.DOUBLE;
		if(tipo == BigDecimal.class)    return TipoColumna.DECIMAL;
		if(tipo == String.class)        return TipoColumna.VARCHAR;
		if(tipo == LocalDate.class)     return TipoColumna.DATE;
		if(tipo == LocalDateTime.class) return TipoColumna.DATETIME;

		//TODO: imagenes? binary blob?

		throw new TipoDeColumnaInvalidaException(field);
	}

	static public Object valueFromResult(
		TipoColumna tipo_columna,
		ResultSet   result,
		String      label
	)
	throws ORMException{
		try{
			return switch(tipo_columna){
				case BOOL     -> result.getBoolean   (label);
				case INT      -> result.getInt       (label);
				case BIGINT   -> result.getLong      (label);
				case FLOAT    -> result.getFloat     (label);
				case DOUBLE   -> result.getDouble    (label);
				case DECIMAL  -> result.getBigDecimal(label);
				case VARCHAR  -> result.getString    (label);
				case TEXT     -> result.getString    (label);
				case DATE     -> {
					var date = result.getDate(label);
					yield date == null ? null : date.toLocalDate();
				}
				case DATETIME -> {
					var ts = result.getTimestamp(label);
					yield ts == null ? null : ts.toLocalDateTime();
				}
				//NOTE: esto no deberia pasar nunca porque
				//      tipoDeColumna cambia los AUTO a lo que le corresponden.
				case AUTO -> throw new FatalORMException();
			};
		}catch(SQLException e){
			throw new TipoDeColumnaIncongruenteException(tipo_columna, label);
		}
	}

	static public Object valueFromResult(
		Columna   col,
		ResultSet result,
		String    label
	) throws ORMException {
		return valueFromResult(col.tipo(), result, label);
	}

	//TODO: esto no deberia estar aca...
	static public void valueIntoStatement(
		PreparedStatement statement,
		int index,
		Object value
	) throws ORMException{
		try{
			switch(value){
				case Integer in      -> statement.setInt       (index, in);
				case String s        -> statement.setString    (index, s);
				case BigDecimal b    -> statement.setBigDecimal(index, b);
				case Boolean b       -> statement.setBoolean   (index, b);
				case Byte b          -> statement.setByte      (index, b);
				case LocalDate d     -> statement.setDate      (index, Date.valueOf(d));
				case LocalDateTime t -> statement.setTimestamp (index, Timestamp.valueOf(t));
				case Double d        -> statement.setDouble    (index, d);
				case Float f         -> statement.setFloat     (index, f);
				case Long l          -> statement.setLong      (index, l);
				case null            -> statement.setNull      (index, Types.NULL);
				default -> throw new TipoDeValorInesperadoException(value);
			}
		}catch(SQLException e){
			//NOTE: esto no deberia ocurrir nunca. Sucedería si el statement
			//      no tiene el indice esperado, pero el ORM se encarga de eso.
			throw new FatalORMException();
		}
	}

	static public Object valueFromResult(Columna col, ResultSet result)
	throws ORMException{
		return valueFromResult(col, result, col.nombre());
	}

}
