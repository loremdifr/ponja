package ORM;

import ORM.Annotations.*;
import ORM.Clases.*;
import ORM.Exceptions.*;
import ORM.Exceptions.Checked.*;
import ORM.Exceptions.Unchecked.*;
import ORM.Queries.*;
import ORM.Relaciones.*;
import ORM.SQL.*;

import javax.naming.OperationNotSupportedException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

//TODO: indices

public class DB{

	private DB() throws OperationNotSupportedException{
		throw new OperationNotSupportedException(
			"Esta clase es estática, no hace falta instanciarla."
		);
	}

	private static MySQL conexion = null;
	private static final Map<Class<? extends Tabla>, Schema> tablas;

	static {
		tablas = new HashMap<>();
	}

	/**
	 * agrega las clases que se usan en la DB. el ORM se encarga
	 * de que las tablas existan y sus campos estén actualizados.
	 * <p>
	 * <b>Cómo se usa:</b>
	 * </p>
	 * <pre>
	 * //las clases deben extender Tabla:
	 * class Pedido extends Tabla{}
	 * class Usuario extends Tabla{
	 *     private OneToMany<Pedido> pedidos;
	 * }
	 *
	 * //en main:
	 * DB.registrarTablas(Usuario.class, Pedido.class);
	 * </pre>
	 */
	@SafeVarargs
	public static void registrarTablas(Class<? extends Tabla> ...clases)
	throws ORMException{
		registrarTablas(null, clases);
	}

	@SafeVarargs
	public static void registrarTablas(
		String archivo_config,
		Class<? extends Tabla> ...clases
	) throws ORMException{

		conexion = archivo_config == null
			? new MySQL()
			: new MySQL(archivo_config);

		if(!tablas.isEmpty()) throw new NoLeisteElReadmeException();

		for(final var clase : clases){
			if(tablas.containsKey(clase)) return;

			var schema = generarSchema(clase);
			tablas.put(clase, schema);

	//		Utils.prettyPrint(tablas);
		}

		for(var schema : tablas.values()){
			crearOActualizarTabla(schema);
		}

	}

	public static Object valorDeColumna(Tabla fila, Columna columna)
	throws ORMException{
		final var field = DB.buscarFieldParaColumna(fila.getClass(), columna);
		Objects.requireNonNull(field).setAccessible(true);
		try{
			return field.get(fila);
		} catch(IllegalAccessException e){
			// NOTE: esto no debería pasar nunca salvo que se haya
			//       modificado la DB mientras la app está funcionando.
			final var schema = tablas.get(fila.getClass());
			if(schema == null) throw new TablaNoRegistradaException(fila.getClass());
			throw new TablaModificadaException(schema.tabla(), field.getName());
		}
	}

	//NOTE: este auxiliar debe existir para poder establecer
	//      la relacion entre los tipos que recibe getRelacion
	//      ya que en el guardar no son lo suficientemente especificos
	//      (culpa del wildcard... no encontre otra forma mejor de resolverlo)
	//
	@SuppressWarnings({"unchecked"})
	static private <T extends Tabla> void guardarRelacion(T fila, Field field)
	throws ORMException, IllegalAccessException{
		field.setAccessible(true);

		final var fila_class = fila.getClass();
		final var rel = (Relacion<T, ?>) field.get(fila);
		final var fila_relacionada = getRelacion(fila_class, rel);

		if(fila_relacionada instanceof Tabla fila_rel){
			guardar(fila_rel);

			//asignar la fkey si es necesaria (ManyToOne, OneToOne)
			final var rel_class = field.getType();
			if(rel_class == ManyToOne.class
			|| rel_class == OneToOne.class){
				rel.setFk(fila_rel.getId());
			}
		}
	}

	/**
	 * TODO: Documentar..
	 * */
	static public boolean guardar(Tabla fila)
	throws ORMException{
		try{

			final var schema = tablas.get(fila.getClass());

			// antes de guardar esta instancia,
			// hay que fijarse que todas las relaciones esten
			// guardadas, ya que es posible que al guardarlas,
			// se modifiquen campos dentro de esta fila
			// (ej: usuario.id_empresa si se le creo una nueva)

			for(var field : schema.rels().keySet()){

				guardarRelacion(fila, field);


			}

			if(fila.guardado()) return true;

			//guardar
			final var query     = new InsertIntoQuery(schema, fila);
			try(var statement = query.toPreparedStatement(conexion)){
				final var result = statement.executeUpdate() > 0;
				//rellenar PK si se creo
				if(result && fila.getId() == null){
					final var generated_keys = statement.getGeneratedKeys();
					final var pk_field = schema.pk();
					final var col      = schema.columnas().get(pk_field);

					generated_keys.next(); //TODO: assert

					final var nueva_id = TipoColumna.valueFromResult(
						col, generated_keys, "GENERATED_KEY"
					);
					pk_field.setAccessible(true);
					pk_field.set(fila, nueva_id);
				}

				return result;
			}

		}catch(SQLException e){
			throw new GuardadoFallidoException(fila);
		}catch(IllegalAccessException e){
			throw new SchemaModificadoException(fila);
		}
	}

	static public boolean borrar(Tabla fila)
	throws ORMException{
		try{
			if(fila.borrado())       return true;
			if(!fila.guardado())     return false; // nunca se guardo
			if(fila.getId() == null) return false;

			final var field = Tabla.class.getDeclaredField("borrado");
			field.setAccessible(true);
			field.set(fila, true);

			final var result = guardar(fila);

			//si fallo el guardado, revertir el campo "borrado"
			if(!result) field.set(fila, false);

			return result;
		}catch(NoSuchFieldException e){
			//NOTE: esto no deberia pasar nunca! si sucediera es
			//      porque la tabla no tiene mas el campo borrado
			throw new FatalORMException();
		}catch(IllegalAccessException e){
			//NOTE: esto no deberia pasar nunca porque usamos setAccessible
			throw new FatalORMException();
		}
	}

	static public ArrayList<Fila> query(String query_str, Object ...query_params)
	throws ORMException {
		try{
			final var statement = conexion.prepareStatement(query_str);
			for(int i = 0; i < query_params.length; i++){
				final var param = query_params[i];
				TipoColumna.valueIntoStatement(statement, i + 1, param);
			}

			final var results = statement.executeQuery();
			return resultSetAFilas(results);
		}catch(SQLTimeoutException e){
			throw new DBTimeoutException();
		}catch(SQLException e){
			throw new ErrorEnQueryException(e);
		}
	}

	static public int ejecutarUpdate(String query_str, Object ...query_params)
	throws ORMException {
		try{
			final var statement = conexion.prepareStatement(query_str);
			for(int i = 0; i < query_params.length; i++){
				final var param = query_params[i];
				TipoColumna.valueIntoStatement(statement, i + 1, param);
			}

			return statement.executeUpdate();
		}catch(SQLTimeoutException e){
			throw new DBTimeoutException();
		}catch(SQLException e){
			throw new ErrorEnQueryException(e);
		}
	}

	static public ResultSet ejecutarQuery(Query query)
	throws ORMException {
		try{
			final var statement = query.toPreparedStatement(conexion);
			return statement.executeQuery();
		}catch(SQLTimeoutException e){
			throw new DBTimeoutException();
		}catch(SQLException e){
			throw new ErrorEnQueryException(e);
		}
	}

	static public <T, R extends Tabla> T ejecutarFuncion(
		SelectQuery<R> query,
		ResultSetGetter<T> result_getter
	) throws ORMException {
		try(final var result = DB.ejecutarQuery(query)){
			result.next(); //TODO: assert
			return result_getter.apply(result, 1);
		}catch(SQLException e){
			throw new ErrorEnQueryException(e);
		}
	}

	static public ArrayList<Fila> resultSetAFilas(ResultSet results)
	throws ORMException {
		try{
			final var meta_data = results.getMetaData();
			final var filas = new ArrayList<Fila>();

			while(results.next()){
				final var fila = new Fila();
				for(int i = 0; i < meta_data.getColumnCount(); i++){
					final var label    = meta_data.getColumnLabel(i+1);
					final var sql_type = meta_data.getColumnType(i+1);
					final var tipo_col = TipoColumna.fromSqlType(sql_type);
					final var valor    = TipoColumna.valueFromResult(
						tipo_col, results, label
					);
					fila.put(label, valor);
				}
				filas.add(fila);
			}
			return filas;

		}catch(SQLException e){
			throw new ErrorEnQueryException(e);
		}
	}

	//hydration
	@SuppressWarnings({"unchecked"})
	static public <T extends Tabla> ArrayList<T> instanciarFilas(
		ArrayList<Fila> filas, Class<T> tabla
	) throws ORMException {
		try{
			final var snapshot   = Tabla.class.getDeclaredField("_snapshot");
			final var instancias = new ArrayList<T>();
			final var schema     = tablas.get(tabla);

			for(final var fila : filas){
				final var constructor = tabla.getConstructor();
				constructor.setAccessible(true);

				final var instancia = constructor.newInstance();

				//settear campos de instancia
				for(final var entry : fila.entrySet()){
					final var label = entry.getKey();
					final var valor = entry.getValue();

					//identificar field/col correspondiente
					final var columna = getColumna(schema, label);
					final var field   = buscarFieldParaColumna(tabla, columna);

					//saltear fkeys
					if(schema.rels().containsKey(field)){
						//no rellenamos aca porque nos faltaria por ej OneToMany,
						//que no tendria campo
						continue;
					}

					//TODO: validar que el tipo de la Fila sea el
					//      esperado por la columna..?

					//NOTE: esto no deberia pasar nunca porque la variable
					//      columna viene de getColumna que tira exception antes.
					if(field == null) throw new FatalORMException();

					field.setAccessible(true);
					field.set(instancia, valor);
				}

				//TODO: aca es donde hay que rellenar los campos de las
				//      relaciones dentro de la fila usando schema.rels
				for(final var entry : schema.rels().entrySet()){
					final var field = entry.getKey();
					final var fk_data = entry.getValue();
					final var fk_value = fila.get(fk_data.fk_nombre());

					final Relacion<?, ?> rel = fk_value == null
						? null
						: crearRelacion(
							(Class<? extends Relacion>) field.getType(),
							field.getName(),
							fk_data.tabla_asociada(),
							(Integer) fk_value
						);

					field.setAccessible(true);
					field.set(instancia, rel);
				}

				var casted = tabla.cast(instancia);

				//como viene de la DB, setteamos el hash de guardado:
				snapshot.setAccessible(true);
				snapshot.set(instancia, DB.hashearFila(instancia));

				// agregar a la lista
				instancias.add(casted);
			}

			return instancias;

		}catch(NoSuchMethodException e){
			throw new TablaSinConstructorVacioException(tabla);
		}catch(InvocationTargetException e){
			//NOTE: esto tampoco debería pasar, salvo que
			//      el constructor tire una exception tambien
			throw new RuntimeException(e);
		}catch(InstantiationException e){
			throw new TablaAbstractaException(tabla);
		}catch(IllegalAccessException e){
			//NOTE: esto no deberia pasar nunca porque usamos setAccessible
			throw new FatalORMException();
		}catch(NoSuchFieldException e){
			//NOTE: esto no deberia pasar nunca! si sucediera es
			//      porque la tabla no tiene mas el campo _snapshot
			throw new FatalORMException();
		}
	}

	@SuppressWarnings({"rawtypes"})
	static private Relacion<?, ?> crearRelacion(
		Class<? extends Relacion> rel_clase,
		String rel_nombre,
		Class<? extends Tabla> tabla, //tabla asociada
		Integer fk
	) throws FatalORMException{
		if(rel_clase == ManyToOne.class) return new ManyToOne<>(tabla, rel_nombre, fk);
		if(rel_clase == OneToOne.class)  return new OneToOne<> (tabla, rel_nombre, fk);
		if(rel_clase == OneToMany.class) return new OneToMany<>(tabla, rel_nombre, fk);
		throw new FatalORMException();
	}

	static public <T extends Tabla> SelectQuery<T> buscar(Class<T> tabla){
		return new SelectQuery<>(tabla);
	}

	static public <T extends Tabla> T buscarPorId(
		Class<T> tabla,
		Integer id
	) throws ORMException {
		final var schema = tablas.get(tabla);
		if(schema == null) throw new TablaNoRegistradaException(tabla);
		return tabla.cast(buscar(tabla).where("id", id).first());
	}

	static public void crearOActualizarTabla(Schema schema)
	throws ORMException{
		if(!existeTabla(schema)){
			crearTabla(schema);
		}else{
			final var db_cols = extraerColumnasDB(schema.tabla());
			actualizarTabla(schema, db_cols);
		}

	}

	static public <T extends Tabla> List<T> buscarPorFk(
		Class<T> tabla,
		String campo,
		Integer fk
	) throws ORMException {
		final var schema = tablas.get(tabla);
		if(schema == null) throw new TablaNoRegistradaException(tabla);
		return buscar(tabla).where(campo, fk).toList();
	}

	static public <T extends Tabla, V> V getRelacion(
		Class<? extends Tabla> desde, Relacion<T, V> rel
	){
		if(rel.cargada())    return rel.db_cache();
		if(rel.fk() == null) return null;
		try{
			rel.__cargar(rel.__buscar(desde));
			return rel.db_cache();
		}catch(ORMException e){
			return null;
		}
	}

	public static String fk(
		Class<? extends Tabla> desde,
		String nombre_field
	) throws ORMException{

		//cual de las rels de la tabla es la rel que le pido?
		for(var entry : tablas.get(desde).rels().entrySet()){
			final var fk_field = entry.getKey();
			final var fk_data  = entry.getValue();
			if(fk_field.getName().equals(nombre_field)){
				return fk_data.fk_nombre();
			}
		}
		// creo que la unica explicacion de esto es que
		// algo no este bien inicializado, o que de alguna
		// forma la relacion no sea para con una clase que
		// extiende Tabla correctamente..?
		throw new FatalORMException();
	}


	static public <T extends Tabla, V> void setRelacion(
		Relacion<T, V> rel, V fila_relacionada
	) throws ORMException{
		switch(fila_relacionada){
			case Tabla f -> rel.setFk(f.getId());
			case null    -> rel.setFk(null);
			case List<?> arr -> {
				for(final var fila_rel : arr){
					// buscar el campo dependiente
					// busco en tablas la fila.class
					// y ahi busco en sus rels el campo que sea del tipo
					// como resolver ambiguedades?
					// ambiguedad ejemplo:
					// empresa.usuarios_vigentes
					// usuario.empresa_actual
					// usuario.empresa_anterior
					//

//					final var rel_dependiente = DB.getCampoDependiente();
//					setRelacion(rel_dependiente, rel.);
				}
			}
			default -> throw new FatalORMException(); //no pasa nunca
		}

		rel.__cargar(fila_relacionada);
	}

	private static void actualizarTabla(
		Schema schema_esperado,
		HashMap<String, Columna> cols_en_db
	) throws ORMException{
		//TODO: checkear bien esto
//		if(cols_en_db..equals(schema_esperado)) return; //pero si estas igual

		//TODO: usar schema.rels para crear las fkeys en la db

		final var cols_esperadas = (HashMap<String, Columna>) schema_esperado
			.columnas()
			.entrySet()
			.stream()
			.collect(Collectors.toMap(
				x -> x.getValue().nombre(),
				Map.Entry::getValue
			));

		//diff columnas
		final var cols_faltantes   = Utils.diffColumnas(cols_esperadas, cols_en_db);
		final var cols_sobrantes   = Utils.diffColumnas(cols_en_db, cols_esperadas);
		final var cols_a_modificar = new HashMap<String, Columna>();

		boolean pk_cambiada = false;
		for(final var entry : cols_esperadas.entrySet()){
			final var nombre  = entry.getKey();
			final var columna = entry.getValue();

			if(!cols_en_db.containsKey(nombre))    continue;
			if(cols_faltantes.containsKey(nombre)) continue;
			if(cols_sobrantes.containsKey(nombre)) continue;

			final var col_en_db   = cols_en_db.get(nombre);
			final var nombre_diff = !col_en_db.nombre().equals(columna.nombre());
			final var tipo_diff   = !col_en_db.tipo().equals(columna.tipo());
			final var pk_diff     = col_en_db.pk() != columna.pk();
			final var null_diff   = col_en_db.not_null() != columna.not_null();

			if(nombre_diff || tipo_diff || pk_diff || null_diff){
				cols_a_modificar.put(col_en_db.nombre(), columna);
			}

			if(pk_diff) pk_cambiada = true;

		}

		final var super_query = new AlterTableQuery(
			schema_esperado,
			pk_cambiada,
			new ArrayList<>(cols_faltantes.values().stream().toList()),
			new ArrayList<>(cols_sobrantes.values().stream().toList()),
			cols_a_modificar
		);

		try(var statement = super_query.toPreparedStatement(conexion)){
			final var result = statement.executeUpdate() > 0;
			//TODO: falta algo aca?

		}catch(SQLException e){ throw new ErrorEnQueryException(e); }
	}


	private static HashMap<String, Columna> extraerColumnasDB(String tabla_nombre)
	throws ORMException{
		try{
			final var catalog = conexion.getCatalog();
			final var meta_data = conexion.getMetaData();
			final var cols    = meta_data.getColumns(catalog, null, tabla_nombre, "%");
			final var pks     = meta_data.getPrimaryKeys(catalog, null, tabla_nombre);
			final var pk_name = pks.next() ? pks.getString("COLUMN_NAME") : null;

			final var columnas = new HashMap<String, Columna>();

			while(cols.next()){
				//cols.getString("IS_AUTOINCREMENT") //TODO: usarlo
				//TODO: extraer estos strings a un enum porque es un asco esto asi carlitos
				final var column_name = cols.getString("COLUMN_NAME");
				final var es_pk       = column_name.equals(pk_name);
				final var tipo_col    = TipoColumna.fromSqlType(cols.getInt("DATA_TYPE"));
				final var columna     = new Columna(
					column_name,
					tipo_col,
					cols.getString("IS_NULLABLE").equals("NO"),
					es_pk
				);
				columnas.put(column_name, columna);
			}
			return columnas;
		}catch(SQLException e){
			throw new ErrorEnQueryException(e);
		}
	}

	private static Field buscarFieldParaColumna(
		Class<? extends Tabla> tabla,
		Columna aguja
	){
		final var columnas = tablas.get(tabla).columnas();

		for(var entry : columnas.entrySet()){
			final var columna = entry.getValue();
			final var field   = entry.getKey();

			if(columna.nombre().equals(aguja.nombre())){
				return field;
			}

		}

		return null;
	}

	private static void crearTabla(Schema schema)
	throws ORMException{
		try{
			final var statement = conexion.createStatement();
			final var query = new CreateTableQuery(schema);
			statement.executeUpdate(query.toString());
			statement.close();
		}catch(SQLException e){ throw new ErrorEnQueryException(e); }
	}

	static public boolean existeTabla(Schema schema) throws ORMException{
		try{
			final var statement = conexion.createStatement();
			final var sql = """
							SELECT COUNT(*) FROM information_schema.tables
							WHERE table_schema = '%s'
							AND table_name = '%s';
							""";
			final var sql_str = sql.formatted(conexion.nombreDB(), schema.tabla());
			final var result  = statement.executeQuery(sql_str);
			final var success = result.next() && result.getInt(1) > 0;
			statement.close();
			return success;
		}catch(SQLException e){ throw new ErrorEnQueryException(e); }
	}

	public static Columna getColumna(Schema schema, String nombre_columna)
	throws ORMException{
		for(var columna : schema.columnas().values()){
			if(columna.nombre().equals(nombre_columna)) return columna;
		}
		throw new ColumnaAusenteException(nombre_columna, schema.tabla());
	}

	public static Schema schemaDeTabla(Class<? extends Tabla> tabla){
		return tablas.get(tabla);
	}

	public static long hashearFila(Tabla fila) throws ORMException{
		final var cols = tablas.get(fila.getClass()).columnas();
		final var cols_en_orden = cols.values().stream().sorted().toList();
		long hashcode = 6767676767676757L; //NOTE: six seven

		for(var col : cols_en_orden){
			final var valor = valorDeColumna(fila, col);
			hashcode *= 31;
			hashcode += Objects.hashCode(valor);
		}
		return hashcode;
	}

	//priv

	static private Schema generarSchema(
		Class<? extends Tabla> tabla
	) throws ORMException{
		try{
			final var nombre_tabla   = camelToSnake(tabla.getSimpleName());
			final var clave_primaria = Tabla.class.getDeclaredField("id");
			final var columnas       = new HashMap<Field, Columna>();
			final var rels           = new HashMap<Field, ClaveForanea>();

			//walk up the inheritance tree
			Class<?> curr_class = tabla;
			while(curr_class != null && curr_class != Object.class){
				for(var field : curr_class.getDeclaredFields()){

					final var field_type = field.getType();

					//saltear static
					if(Modifier.isStatic(field.getModifiers())){
						continue;
					}

					//saltear no guardables
					if(field.getAnnotation(NoSeGuarda.class) != null){
						continue;
					}

					//ver si no se intento pisar la id
					//NOTE: esto es legacy de cuando la PK se podia configurar
					//      por tabla
					if(field.getName().equals("id")
					&& curr_class != Tabla.class){
						throw new PKRedefinidaException(tabla);
					}

					final var es_pk    = field.equals(clave_primaria);
					final var not_null = field_type.isPrimitive() || es_pk;
					final var nombre   = nombreDeColumna(field);
					final var tipo     = tipoDeColumna(field);

					// las relaciones OneToMany y ManyToMany no conllevan fkeys
					// en la tabla que las declara
					if(field_type != OneToMany.class
					/*|| field_type != ManyToMany.class*/){
						columnas.put(field, new Columna(nombre, tipo, not_null, es_pk));
					}

					// si el campo es una relacion, guardamos la clase de la
					// tabla asociada para ese campo
					if(Relacion.class.isAssignableFrom(field_type)){
						rels.put(field, new ClaveForanea(
							tablaRelacionada(field), nombre
						));
					}
				}
				curr_class = curr_class.getSuperclass();
			}
			return new Schema(nombre_tabla, columnas, clave_primaria, rels);

		}catch(NoSuchFieldException e){
			// jamas se tira esta exception porque el parametro es
			// Class<? extends Tabla> y Tabla tiene el campo definido
			throw new FatalORMException();
		}
	}


	//TODO: cambiar nombre a este metodo
	@SuppressWarnings({"unchecked"})
	static private Class<? extends Tabla> tablaRelacionada(Field field)
	throws FatalORMException{
		final var field_type      = field.getType();
		final var generic_type    = field.getGenericType();
		final var declaring_class = field.getDeclaringClass();


		if(!Tabla.class.isAssignableFrom(declaring_class)){
			//la relacion debe ser siempre hacia una clase que extienda Tabla
			throw new FatalORMException();
		}

		if(generic_type instanceof ParameterizedType tipo_generico){
			final var args_del_tipo = tipo_generico.getActualTypeArguments();

			if(field_type == OneToMany.class
			|| field_type == OneToOne.class
			|| field_type == ManyToOne.class){
				return (Class<? extends Tabla>) args_del_tipo[0];
			}

			//TODO: ManyToMany

		}

		// que hiciste carlitos
		throw new FatalORMException();
	}

	static private TipoColumna tipoDeColumna(Field field)
	throws ORMException{
		final var nota = field.getAnnotation(GuardarComo.class);

		if(nota != null
		&& nota.tipo() != null
		&& nota.tipo() != TipoColumna.AUTO){
			return nota.tipo();
		}

		return TipoColumna.fromField(field);
	}

	static private String nombreDeColumna(Field field){
		final var tipo = field.getType();
		if(tipo == ManyToOne.class)  return camelToFkey(field.getName());
		if(tipo == OneToOne.class)   return camelToFkey(field.getName());
		if(tipo == OneToMany.class)  return null;
//		if(tipo == ManyToMany.class) return null;
		return camelToSnake(field.getName());
	}

	//TODO: move these to utils?
	static private String camelToFkey(String str){
		return "id_" + camelToSnake(str);
	}

	static private String camelToSnake(String str){
		return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
	}


}
