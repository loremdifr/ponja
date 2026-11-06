package ORM.Exceptions.Checked;

import ORM.Exceptions.ORMException;

import java.lang.reflect.Field;

public class TipoDeColumnaInvalidaException extends ORMException{

	public TipoDeColumnaInvalidaException(Field columna){
		final var template = "Tipo del campo `%s` no soportado. (%s)";
		super(template.formatted(columna.getName(), columna.getType()));

	}
}
