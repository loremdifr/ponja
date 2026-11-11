package tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class Tests{

	// para agregar tests: agregar funciones que empiecen con "test"
	// y que NO sean estaticas!

	public void testDeMentira1(){
		assertEquals(1,2,"pero vo so loco");
	}


	public void testDeMentira2(){
		int[] sixseven = {6, 7};
		assertTrue(sixseven.length > 1, "asisisi muy bien");
	}

	// ---

	static void main(){
		final var tests = new Tests();
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
			}catch(
				AssertionError
				| InvocationTargetException
				| IllegalAccessException e
			){
				fallos++;
				System.err.printf(
					"[X] %s <- %s%n\n", method_name, e.getCause()
				);
			}


		}
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
