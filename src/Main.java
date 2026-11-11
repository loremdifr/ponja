import ORM.Exceptions.ORMException;
import ORM.DB;

void main() throws ORMException{

	// TUTORIAL
	// ========

	// requisitos:
	// 1. copiar el archivo db.properties.default a db.properties
	// 2. modificarlo con los datos de acceso a su db
	// 3. la db no se crea sola, asi que hay que crearla en blanco

	//registro clases en SQL y se auto-crean las tablas en la DB:
	DB.registrarTablas(
		Usuario.class,
		Empresa.class
	);

	// si le cambias los datos a la clase Usuario, se van a mantener
	// actualizados en la DB solitos.

	//como guardar datos y buscarlos:
	var mi_usuario = new Usuario(
		"Jared",
		19,
		"What up, I'm Jared, I'm 19, and I never fucking learned how to read"
	);

	//guardar en la db es asi:
	mi_usuario.guardar();

	System.out.println("usuario: " + mi_usuario);
	System.out.println("guardado?: " + mi_usuario.guardado());
	// el metodo guardado() -> ¿esta persistido y al dia en la DB?

	mi_usuario.setEdad(67); //al modificarlo, guardado() regresa false
	System.out.println("con cambios: " + mi_usuario);
	System.out.println("guardado?: " + mi_usuario.guardado());

	// es decir, guardado toma los siguientes valores:
	// - para un objeto nuevo: false (nunca se guardo!)
	// - guardado en la DB y sin cambiarle nada desde entonces: true
	// - editado algo despues del ultimo guardar(): false

	// Si guardar() se llama cuando guardado() ya es true, no hace nada!
	// (ya que asi no pisa el timestamp de modificacion).
	// (osea que no hace falta encerrar guardar() con un if(guardado))

	var id = mi_usuario.getId(); //una vez guardado, el ID se actualiza solo
	System.out.println("ID: " + id);

	System.out.println("-----------------------------------------------------");

	//Despues puedo buscarlo directamente por ID:
	var usuario_especifico = DB.buscarPorId(Usuario.class, id);
	System.out.println("desde la db: " + usuario_especifico);
	System.out.println("guardado: " + usuario_especifico.guardado());

	//O puedo buscar lo que quiera:
	var jared = DB.buscar(Usuario.class)
		.where("nombre", "LIKE", "J%")
		.andWhere("edad", ">", 18)
		.first(); //le mete limit 1, y te devuelve solo el primer resultado

	System.out.println("jared: " + jared);

	System.out.println("-----------------------------------------------------");

	//llamando a toList() se obtiene un ArrayList
	var usuarios = DB.buscar(Usuario.class)
		.where("nombre", "LIKE", "J%")
		.orWhere("nombre", "IN", "Pedro", "Pablo")
		.orderBy("nombre", "ASC")
		.limit(12)
		.toList();

	System.out.println("usuarios: " + usuarios);

	// las funciones de SQL tienen su propio metodo:
	var total_usuarios = DB.buscar(Usuario.class).count();
	System.out.println("total usuarios: " + total_usuarios);

	// y se componen de la misma manera:
	var menores = DB.buscar(Usuario.class)
		.where("edad", "<", 18)
		.count();
	System.out.println("menores: " + menores);

	var jared_promedio = DB.buscar(Usuario.class)
		.where("nombre", "jared")
		.orWhere("nombre", "Jared")
		.avg("edad");
	System.out.println("edad promedio: " + jared_promedio);

	System.out.println("-----------------------------------------------------");

	// queries manuales (es un wrapper de JDBC con sintaxis menos horrible)
	var filas = DB.query("SELECT * FROM usuario WHERE nombre LIKE ?", "Jared");
	System.out.println("Usuarios con query:");
	for(var fila : filas){
		System.out.println(fila);
	}
	// el resultado de las queries manuales es un ArrayList<Fila> y cada fila es
	// basicamente un HashMap donde la clave es el nombre de la columna (String)
	// y el valor es... el valor (de la columna para esa fila)

	// es decir:
	var primer_fila = filas.getFirst();
	System.out.println("nombre del primer resultado: " + primer_fila.get("nombre"));

	System.out.println("-----------------------------------------------------");

	// PAGINACION
	// cuando uno espera recibir demasiados resultados, puede llamar en su lugar
	// al metodo paginar de la query:

	var usuarios_pag2 = DB.buscar(Usuario.class)
		//.where() ... etc
		.paginar(5, 2); //5 por pagina, pagina 2
	System.out.println("pagina 2: " + usuarios_pag2);

	// funciona igual que toList() pero devuelve solamente como máximo
	// la cantidad por pagina. internamente se asegura que pagina este
	// siempre dentro de los valores validos (0 < pagina <= total/paginas)

	System.out.println("-----------------------------------------------------");

	// relaciones entre tablas

	// la relaciones entre tablas se definen usando las clases:
	// "OneToMany", "ManyToMany", "ManyToOne" y "OneToOne".

	// Por ejemplo, en la clase Usuario:
	// private ManyToOne<Empresa> empresa;
	// y en la clase Empresa:
	// private OneToMany<Usuario> usuarios;

	// al definir las relaciones de esta manera, el ORM
	// se encarga de que las claves foraneas existan
	// y permite vincular/desvincular filas.

	var usr = DB.buscarPorId(Usuario.class, id);
	var globant = new Empresa("Globant");
	System.out.println("Globant: " + globant);

	//setEmpresa llama a usr.empresa.set(globant);
	usr.setEmpresa(globant);

	// para guardar, se llama directamente al guardar
	// del usuario, que se encarga tambien de persisitir
	// las relaciones.
	usr.guardar();
	System.out.println("globber: " + usr);

	//para obtener la relacionada:
	var empresa = usr.getEmpresa(); //internamente: DB.getRelacion(empresa);
	System.out.println("empresa desde el usr: " + empresa);

	//para remover la relacion simplemente se usa null:
	usr.setEmpresa(null);
	usr.guardar();

	//MUY IMPORTANTE: internamente es -> DB.setRelacion(empresa, null);
	//                nunca debe ser  -> usr.empresa = null;

	//en un OneToMany, sería así: y el resultado se puede iterar
	globant.setUsuarios(usuarios);
	var globbers = globant.getUsuarios(); // DB.getRelacion(usuarios);
	for(var globber : globbers){
		//TODO:

	}
//
//	//ya que la clave foranea la administra el ORM,
//	//podemos buscar por Fk asi:
//	var usrs = DB.buscarPorFk(Usuario.class, "empresa", 1);
//
//	//y si necesitamos algo más específico, le podemos pedir al ORM que nos diga
//	//el nombre de la clave foranea para armar nuestras propias queries:
//	var fk = DB.claveForanea(Usuario.class, "empresa");
//	var empleados = DB.buscar(Usuario.class)
//		.where(fk, "IN", 1, 2, 3, 4)
//		.toList();
//
//	// para ManyToMany se trabaja con una tabla pivot, que debe pertenecer
//	// a ambas tablas. Por ejemplo:
//
//	var ind_pesquera   = new Industria("Pesquera");
//	var ind_automotriz = new Industria("Automotriz");
//	var ind_software   = new Industria("Software");
//
//	globant.addIndustria(ind_software); // internamente: globant.industrias.add()
//	globant.guardar();
//
//	//si tenemos que agregar cosas en la tabla pivot, tambien podemos:
//	//(por ejemplo, si la tabla pivot tuviera "rol")
////	globant.addIndustria(new EmpresaIndustria(ind_pesquera, "lider"));
//
//	System.out.println("-----------------------------------------------------");
//
//	//transacciones
//
////	DB.transaccion();
////	// ..... todo en el medio
////	DB.ejecutarTransaccion();
//
//	System.out.println("-----------------------------------------------------");

}
