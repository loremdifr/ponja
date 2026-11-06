package ORM;

import ORM.Annotations.*;
import ORM.Clases.*;
import ORM.Exceptions.*;
import ORM.Exceptions.Checked.*;
import ORM.Exceptions.Unchecked.DBConexionException;
import ORM.Exceptions.Unchecked.DBConfigInexistenteException;
import ORM.Exceptions.Unchecked.NoLeisteElReadmeException;
import ORM.Queries.*;
import ORM.SQL.*;

import javax.naming.OperationNotSupportedException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
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

	private static final Connection conexion;
	private static final Map<Class<? extends Tabla>, Schema> tablas;
	private static final String nombre_db;


	static {
		var config_file = DB.class.getResourceAsStream("db.properties");
		if(config_file == null) throw new DBConfigInexistenteException();

		var db_config = new Properties();

		try{
			db_config.load(config_file);
		}catch(IOException e){
			throw new DBConfigInexistenteException();
		}

		var host = db_config.getProperty("host");
		var port = db_config.getProperty("port");
		var db   = db_config.getProperty("db");
		var usr  = db_config.getProperty("usr");
		var pwd  = db_config.getProperty("pwd");

		try{
			final var url_template = "jdbc:mysql://%s:%s/%s";
			conexion = DriverManager.getConnection(
				url_template.formatted(host, port, db), usr, pwd
			);
		}catch(SQLException e){
			throw new DBConexionException(e);
		}

		nombre_db = db;
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


	static public boolean guardar(Tabla fila)
	throws ORMException{
		try{
			if(fila.guardado()) return true;
			final var tabla     = tablas.get(fila.getClass());
			final var query     = new InsertIntoQuery(tabla, fila);
			final var statement = query.toPreparedStatement(conexion);
			final var result    = statement.executeUpdate() > 0;

			//rellenar PK si se creo
			if(result && fila.getId() == null){
				final var generated_keys = statement.getGeneratedKeys();
				final var schema   = tablas.get(fila.getClass());
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
		}catch(SQLException e){
			throw new GuardadoFallidoException(fila);
		}catch(IllegalAccessException e){
			throw new SchemaModificadoException(fila);
		}
	}

	static public <T extends Tabla> ArrayList<T> ejecutarQuery(
		Query query, Class<T> tabla
	) throws ORMException {
		try{
			final var statement = query.toPreparedStatement(conexion);
			final var results   = statement.executeQuery();
			return resultSetAFilas(results, tabla);
		}catch(SQLTimeoutException e){
			throw new DBTimeoutException();
		}catch(SQLException e){
			throw new ErrorEnQueryException(e);
		}
	}

	static private <T extends Tabla> ArrayList<T> resultSetAFilas(
		ResultSet results, Class<T> tabla
	) throws ORMException {
		try{
			final var filas = new ArrayList<T>();
			final var schema = tablas.get(tabla);
			while(results.next()){
				final var constructor = tabla.getConstructor();
				constructor.setAccessible(true);

				final var instancia = constructor.newInstance();

				//rellenar ojeto
				for(var entry : schema.columnas().entrySet()){
					final var field = entry.getKey();
					final var col   = entry.getValue();
					final var valor = TipoColumna.valueFromResult(col, results);
					field.setAccessible(true);
					field.set(instancia, valor);
				}
				filas.add(tabla.cast(instancia));
			}
			return filas;

		}catch(NoSuchMethodException e){
			throw new TablaSinConstructorVacioException(tabla);
		}catch(SQLException e){
			throw new ErrorEnQueryException(e);
		}catch(InvocationTargetException e){
			//NOTE: esto tampoco debería pasar, salvo que
			//      el constructor tire una exception tambien
			throw new RuntimeException(e);
		}catch(InstantiationException e){
			throw new TablaAbstractaException(tabla);
		}catch(IllegalAccessException e){
			//NOTE: esto no deberia pasar nunca porque usamos setAccessible
			throw new FatalORMException();
		}
	}

	static public SelectQuery buscar(Class<? extends Tabla> tabla){
		return new SelectQuery(tabla);
	}

	static public Tabla buscarPorId(
		Class<? extends Tabla> tabla,
		Integer id
	) throws ORMException {
		final var schema = tablas.get(tabla);
		if(schema == null) throw new TablaNoRegistradaException(tabla);
		return buscar(tabla).where("id", "=", id).first();
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

	private static void actualizarTabla(
		Schema schema_esperado,
		HashMap<String, Columna> cols_en_db
	) throws ORMException{
		//TODO: checkear bien esto
//		if(cols_en_db..equals(schema_esperado)) return; //pero si estas igual

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

		try{
			final var statement = super_query.toPreparedStatement(conexion);
			final var result    = statement.executeUpdate() > 0;
			//TODO: falta algo aca?
		}catch(SQLException e){ throw new ErrorEnQueryException(e); }

	}

	private static HashMap<String, Columna> extraerColumnasDB(String tabla_nombre)
	throws ORMException{
		try{
			final var meta_data = conexion.getMetaData();
			final var cols    = meta_data.getColumns(null, null, tabla_nombre, "%");
			final var pks     = meta_data.getPrimaryKeys(null, null, tabla_nombre);
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
	) throws ORMException{
		final var columnas = columnasDeTabla(tabla);

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
			final var sql_str = sql.formatted(nombre_db, schema.tabla());
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

	public static Columna getColumna(String nombre_tabla, String nombre_columna)
	throws ORMException{
		//TODO: probablemente necesito un mapa de string a tabla
		//      para que esto sea mas rapido, porque se llama desde las queries
		for(var schema : tablas.values()){
			if(!schema.tabla().equals(nombre_tabla)) continue;
			return getColumna(schema, nombre_columna);
		}
		throw new ColumnaAusenteException(nombre_columna, nombre_tabla);
	}

	public static Schema schemaDeTabla(Class<? extends Tabla> tabla){
		return tablas.get(tabla);
	}

	public static long hashearFila(Tabla fila) throws ORMException{
		final var cols = columnasDeTabla(fila.getClass());
		final var cols_en_orden = cols.values().stream().sorted().toList();
		long hashcode = 6767676767676767L; //NOTE: six seven

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
		final var columnas = columnasDeTabla(tabla);
		final var pk = columnas
			.entrySet()
			.stream()
			.filter(es -> es.getValue().pk())
			.toList()
			.getFirst()
			.getKey();
		return new Schema(camelToSnake(tabla.getSimpleName()), columnas, pk);
	}

	static private HashMap<Field, Columna> columnasDeTabla(
		Class<? extends Tabla> tabla
	) throws ORMException{
		final var columnas = new HashMap<Field, Columna>();
		try{
			final var clave_primaria = Tabla.class.getDeclaredField("id");

			Class<?> curr_class = tabla;
			while(curr_class != null && curr_class != Object.class){
				for(var field : curr_class.getDeclaredFields()){

					//saltear static
					if(Modifier.isStatic(field.getModifiers())){
						continue;
					}

					//saltear no guardables
					if(field.getAnnotation(NoSeGuarda.class) != null){
						continue;
					}

					//ver si no se intento pisar la id
					//TODO: esto es legacy de cuando la PK se podia configurar
					//      por tabla
					if(field.getName().equals("id")
					&& curr_class != Tabla.class){
						throw new PKRedefinidaException(tabla);
					}

					final var es_pk    = field.equals(clave_primaria);
					final var not_null = field.getType().isPrimitive() || es_pk;
					columnas.put(field, new Columna(
						nombreDeColumna(field),
						tipoDeColumna(field),
						not_null,
						es_pk
					));

					//TODO: resolver el tema de las claves foraneas
				}
				curr_class = curr_class.getSuperclass();
			}

		}catch(NoSuchFieldException e){
			// jamas se tira esta exception porque el parametro es
			// Class<? extends Tabla> y Tabla tiene el campo definido
		}

		return columnas;
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
		return camelToSnake(field.getName());
	}

	static private String camelToSnake(String str){
		return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
	}


}
