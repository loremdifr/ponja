package tests;

import ORM.DB;
import ORM.Exceptions.ORMException;
import ejemplo.Empresa;
import ejemplo.Usuario;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class Tests{

	// para agregar tests: agregar funciones que empiecen con "test"
	// y que NO sean estaticas!

	public void testRegistrarTablas() throws ORMException{
		DB.registrarTablas(
			"db.test.properties",
			Usuario.class,
			Empresa.class
		);

		//TODO: inspeccionar que las tablas tenga la forma esperada..
	}

	//TODO: hacer mas tests..

	// ---

	static void main(){

		final var tests = new Tests();

		System.out.println("Ejecutando tests...");
		int exitos = 0;
		int fallos = 0;

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

}
