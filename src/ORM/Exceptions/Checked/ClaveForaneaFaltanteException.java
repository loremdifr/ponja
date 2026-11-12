package ORM.Exceptions.Checked;

import ORM.Exceptions.ORMException;
import ORM.SQL.Schema;

import java.lang.reflect.Field;

public class ClaveForaneaFaltanteException extends ORMException{

	public ClaveForaneaFaltanteException(
		Field rel_field,
		Schema desde,
		String fkey_esperada,
		Schema hasta
	){
		final var template =
			"La relacion %s en la clase %s espera que exista el " +
			"campo %s en la tabla %s";
		super(template.formatted(
			rel_field.getType(),
			desde.tabla(),
			fkey_esperada,
			hasta.tabla()
		));
	}
}
