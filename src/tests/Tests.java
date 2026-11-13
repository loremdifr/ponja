package tests;

import ORM.DB;
import ORM.Exceptions.ORMException;
import ORM.MySQL;
import ORM.SQL.Fila;
import ejemplo.Empresa;
import ejemplo.Usuario;

import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tests{

	private static final String db_config = "db.test.properties";

	//esto se suar en casi toodsl os test
	private void _truncarTablas() throws ORMException{
		DB.ejecutarUpdate("TRUNCATE usuario;");
		DB.ejecutarUpdate("TRUNCATE empresa;");
	}

	// para agregar tests: agregar funciones que empiecen con "test"
	// y que NO sean estaticas!

	public void testRegistrarTablas() throws ORMException{
		DB.registrarTablas(
			db_config,
			Usuario.class,
			Empresa.class
		);

		final var usuario_cols_esperadas = List.of(
			"id",
			"nombre",
			"edad",
			"biografia",
			"creado",
			"modificado",
			"borrado",
			"id_empresa"
		);

		final var empresa_cols_esperadas = List.of(
			"id",
			"nombre",
			"creado",
			"modificado",
			"borrado"
		);

		final var usuario_cols = DB.query("SHOW COLUMNS FROM usuario;")
			.stream()
			.map(f -> f.get("Field"))
			.toList();

		final var empresa_cols = DB.query("SHOW COLUMNS FROM empresa;")
			.stream()
			.map(f -> f.get("Field"))
			.toList();

		assertCount(
			usuario_cols,
			usuario_cols_esperadas.size(),
			"Cantidad de columnas de usuario no coinciden"
		);
		assertCount(
			empresa_cols,
			empresa_cols_esperadas.size(),
			"Cantidad de columnas de empresa no coinciden"
		);

		assertContainsAll(
			usuario_cols,
			usuario_cols_esperadas,
			"Columnas de usuarios no coinciden"
		);
		assertContainsAll(
			empresa_cols,
			empresa_cols_esperadas,
			"Columnas de empresas no coinciden"
		);

	}

	public void testGuardar() throws ORMException{
		_truncarTablas();

		final var usr = new Usuario("Test User", 99, "test biografia");
		assertTrue(usr.guardar(),       "fallo al guardar entidad nueva");
		assertTrue(usr.guardado(),      "entidad guardada .guardado() != true");
		assertTrue(usr.getId() != null, "ID de entidad guardada no se asigno");

		//comparar datos en tabla
		final var filas = DB.query("SELECT * FROM usuario WHERE id = ?", usr.getId());
		assertTrue(!filas.isEmpty(), "entidad no se guardo en la DB");

		final var fila = filas.getFirst();
		assertEquals(usr.getNombre(),    fila.get("nombre"),    "campo no se guardo");
		assertEquals(usr.getEdad(),      fila.get("edad"),      "campo no se guardo");
		assertEquals(usr.getBiografia(), fila.get("biografia"), "campo no se guardo");
		assertTrue(fila.get("creado") != null,     "campos audit fallaron");
		assertTrue(fila.get("modificado") != null, "campos audit fallaron");

		//editar entidad
		usr.setEdad(676767);
		assertTrue(!usr.guardado(), "editar entidad no actualiza guardado()");
		assertTrue(usr.guardar(),   "no se guardo la entidad editada");

		final var count_query = DB.query(
			"SELECT COUNT(*) FROM usuario WHERE id = ? AND edad = ?",
			usr.getId(),
			usr.getEdad()
		);
		final var count = (Long) count_query.getFirst().get("COUNT(*)");
		assertTrue(count == 1, "editar no se guarda en la DB");
	}

	public void testBuscarPorId() throws ORMException{
		_truncarTablas();

		final var usr = new Usuario("asdfg", 12345, "qwerrtyyuiio");
		usr.guardar();

		final var db_usr = DB.buscarPorId(Usuario.class, usr.getId());
		assertTrue(db_usr != null,                        "buscarPorId no funciona");
		assertEquals(usr.getId(), db_usr.getId(),         "buscarPorId no funciona");
		assertEquals(usr.getNombre(), db_usr.getNombre(), "buscarPorId no funciona");
		assertEquals(usr.getEdad(), db_usr.getEdad(),     "buscarPorId no funciona");
		assertTrue(db_usr.guardado(), "guardado() != true para entidad desde la DB");
	}

	public void testWhereFirst() throws ORMException{
		_truncarTablas();

		final var usr1 = new Usuario("Jorge", 99, "");
		final var usr2 = new Usuario("Pedro", 17, "");

		usr1.guardar();
		usr2.guardar();

		final var db_usr = DB.buscar(Usuario.class)
			.where("nombre", "LIKE", "J%")
			.andWhere("edad", ">", 18)
			.first();

		assertEquals(usr1.getNombre(), db_usr.getNombre(), "where no wusca");
	}

	public void testToList() throws ORMException{
		_truncarTablas();

		var usr1 = new Usuario("Jorge",  32, "");
		var usr2 = new Usuario("Pablo",  17, "");
		var usr3 = new Usuario("Pedro",  20, "");
		var usr4 = new Usuario("Maria",  22, "");

		usr1.guardar();
		usr2.guardar();
		usr3.guardar();
		usr4.guardar();

		final var usuarios = DB.buscar(Usuario.class)
			.where("nombre", "LIKE", "Jorge%")
			.orWhere("nombre", "IN", "Pedro", "Pablo")
			.orderBy("nombre", "ASC")
			.limit(12)
			.toList();

		assertEquals(3, usuarios.size(), "SelectQuery::toList() fallo");

		//test order
		assertEquals(usr1.getNombre(), usuarios.get(0).getNombre(), "query order fallo");
		assertEquals(usr2.getNombre(), usuarios.get(1).getNombre(), "query order fallo");
		assertEquals(usr3.getNombre(), usuarios.get(2).getNombre(), "query order fallo");
	}

	public void testCount() throws ORMException{
		_truncarTablas();

		final var usr1 = new Usuario("A", 10, "");
		final var usr2 = new Usuario("B", 20, "");
		final var usr3 = new Usuario("C", 30, "");

		usr1.guardar();
		usr2.guardar();
		usr3.guardar();

		assertTrue(3 == DB.buscar(Usuario.class).count(), "query count fallido");
		assertTrue(1 == DB.buscar(Usuario.class).where("nombre", "A").count(), "count con where fallido");

		//count de la db

		final var count_query = DB.query("SELECT COUNT(*) FROM usuario");
		final var count = count_query.getFirst().get("COUNT(*)");
		assertEquals(DB.buscar(Usuario.class).count(), count, "count no coincide");
	}

	public void testAvg() throws ORMException{
		_truncarTablas();

		final var usr1 = new Usuario("A", 10, "");
		final var usr2 = new Usuario("B", 20, "");
		final var usr3 = new Usuario("C", 30, "");

		usr1.guardar();
		usr2.guardar();
		usr3.guardar();

		final var avg = DB.buscar(Usuario.class).avg("edad");
		assertTrue(avg == 20.0, "AVG() fallo");
	}

	public void testQueryManual() throws ORMException{
		_truncarTablas();

		final var usr1 = new Usuario("Ernesto", 999, "");
		final var usr2 = new Usuario("Guevara", 111, "");

		usr1.guardar();
		usr2.guardar();

		final var filas = DB.query("SELECT * FROM usuario WHERE nombre LIKE ?", "E%");
		assertEquals(1, filas.size(), "query manual fallo");
	}

	public void testPaginar() throws ORMException{
		_truncarTablas();

		final var total = 12;
		for(int i = 0; i < total; i++){
			final var usr = new Usuario("user" + i, 10 + i, "");
			usr.guardar();
		}

		final var pagina2 = DB.buscar(Usuario.class)
			.orderBy("nombre", "ASC")
			.paginar(5, 2);

		assertEquals(5, pagina2.size(), "paginacion fallo");
	}

	public void testManyToOne() throws ORMException{
		_truncarTablas();

		final var usr = new Usuario("Test", 99, "");
		usr.guardar();

		final var empresa = new Empresa("Globant");
		empresa.guardar();

		usr.setEmpresa(empresa);
		usr.guardar();

		final var id_empresa = DB.query(
			"SELECT id_empresa from usuario WHERE id = ?",
			usr.getId()
		).getFirst().get("id_empresa");
		assertEquals(empresa.getId(), id_empresa, "ManyToOne fallo al guardar");

		usr.setEmpresa(null);
		usr.guardar();

		final var id_empresa_null = DB.query(
			"SELECT id_empresa from usuario WHERE id = ?",
			usr.getId()
		).getFirst().get("id_empresa");
		assertTrue(id_empresa_null == null, "ManyToOne fallo al guardar null");
	}

	public void testManyToOneDesdeDB() throws ORMException{
		_truncarTablas();

		final var usr = new Usuario("Test", 99, "");
		usr.guardar();

		final var db_usr = DB.buscarPorId(Usuario.class, usr.getId());
		final var globant = new Empresa("Globant");

		db_usr.setEmpresa(globant);
		db_usr.guardar();

		final var id_empresa = DB.query(
			"SELECT id_empresa from usuario WHERE id = ?",
			db_usr.getId()
		).getFirst().get("id_empresa");

		assertEquals(globant.getId(), id_empresa, "ManyToOne fallo al guardar desde la DB");

		final var empresa = db_usr.getEmpresa();
		assertEquals("Globant", empresa.getNombre(), "getEmpresa");

		db_usr.setEmpresa(null);
		db_usr.guardar();

		final var id_empresa_null = DB.query(
			"SELECT id_empresa from usuario WHERE id = ?",
			db_usr.getId()
		).getFirst().get("id_empresa");

		assertTrue(id_empresa_null == null, "ManyToOne fallo al guardar null desde la DB");
	}

	public void testOneToMany() throws ORMException{
		_truncarTablas();

		final var a = new Usuario("A", 20, "");
		final var b = new Usuario("B", 21, "");

		a.guardar();
		b.guardar();

		final var globant = new Empresa("Globant");
		globant.guardar();

		final var lista = new ArrayList<Usuario>();

		lista.add(a);
		lista.add(b);

		globant.setUsuarios(lista);
		globant.guardar();

		final var id_empresa_a = DB.query(
			"SELECT id_empresa from usuario WHERE id = ?", a.getId()
		).getFirst().get("id_empresa");

		assertEquals(
			globant.getId(), id_empresa_a,
			"OneToMany falla al guardar la fkey en la tabla relacionada"
		);

		final var id_empresa_b = DB.query(
			"SELECT id_empresa from usuario WHERE id = ?", b.getId()
		).getFirst().get("id_empresa");

		assertEquals(
			globant.getId(), id_empresa_b,
			"OneToMany falla al guardar la fkey en la tabla relacionada"
		);

		final var guardados = globant.getUsuarios();
		assertCount(guardados, 2, "OneToMany no trae los valores esperados ");
		final var usuarios_esperados = List.of(a.getId(), b.getId());
		assertTrue(
			guardados.stream().noneMatch(u -> u == a)
			&& guardados.stream().noneMatch(u -> u == b),
			"OneToMany refresca los datos a larga distancia, que miedo"
		);
		assertContainsAll(
			guardados.stream().map(u -> u.getId()).toList(),
			usuarios_esperados,
			"los ids no coinciden");

		final var desde_db = DB.buscarPorId(Empresa.class, globant.getId());
		final var usrs = desde_db.getUsuarios();
		assertCount(usrs, 2, "OneToMany fallo al guardar");
		assertContainsAll(
			usrs.stream().map(u -> u.getId()).toList(),
			usuarios_esperados,
			"OneToMany fallo al guardar"
		);

		//borrar
		globant.setUsuarios(new ArrayList<>(List.of(a)));
		globant.guardar();

		final var uno_solo = DB.buscarPorFk(
			Usuario.class, "id_empresa", globant.getId()
		);
		assertCount(uno_solo, 1, "OneToMany no desvincula");
		assertEquals(a.getId(), uno_solo.getFirst().getId(), "OneToMany desvincula mal");

		final var globant2 = DB.buscarPorId(Empresa.class, globant.getId());
		final var globbers2 = globant2.getUsuarios();
		assertCount(globbers2, 1, "OneToMany no actualiza al borrar");
		assertEquals(a.getId(), globbers2.getFirst().getId(), "OneToMany actualiza mal");

		// todo esto es a proposito: si guardas una vinculada entidad en otra
		// variable, y la desvinculas, el ORM no te va a modificar la que guardaste
		// en otro lado porque no es intuitivo y posiblemente no lo necesites.

		b.guardar();
		final var b_id_empresa = DB.query(
			"SELECT id_empresa FROM usuario WHERE id = ?",
			b.getId()
		).getFirst().get("id_empresa");
		assertEquals(globant.getId(), b_id_empresa, "el ORM esta haciendo magia negra");

	}

	// ---

	static void main(){

		final var tests = new Tests();
		int exitos = 0;
		int fallos = 0;


		System.out.println("Ejecutando tests...");

		for(var method : Tests.class.getDeclaredMethods()){
			final var method_name = method.getName();
			final var method_mods = method.getModifiers();

			if(Modifier.isStatic(method_mods))  continue;
			if(!method_name.startsWith("test")) continue;

			try{
				method.invoke(tests);
				exitos++;
				System.out.printf("[👍] %s\n", method_name);
			}catch(AssertionError | Exception e){
				fallos++;
				System.err.printf(
					"[👎] %s <- %s%n\n", method_name, e.getCause()
				);
			}

		}
		System.out.printf(
			"%d ejecutados (%d mal, %d bien)\n\n",
			fallos + exitos, fallos, exitos
		);
	}

	//assert utils

	static private void assertTrue(boolean condicion, String mensaje){
		if(!condicion){
			throw new AssertionError(mensaje);
		}
	}

	static private void assertEquals(Object a, Object b, String mensaje){
		if(!Objects.equals(a, b)){
			throw new AssertionError(
				mensaje + "\n\t> a: %s \n\t> b: %s".formatted(a, b));
		}
	}

	static private void assertCount(List<?> array, int count, String mensaje){
		if(array.size() != count){
			throw new AssertionError(
				mensaje + "(%d != %d)".formatted(array.size(), count));
		}
	}

	static private void assertContainsAll(
		List<?> contenedor,
		List<?> elementos,
		String mensaje
	){
		if(!contenedor.containsAll(elementos)){
			throw new AssertionError(
				mensaje + "\n\t> esperadas: %s \n\t> encontradas: %s".formatted(
					contenedor, elementos
				)
			);
		}

	}


}
