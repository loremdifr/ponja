package ORM.Exceptions.Checked;

import ORM.Clases.Tabla;
import ORM.Exceptions.ORMException;

import java.lang.reflect.Field;

public class PKsMultiplesException extends ORMException{

	public PKsMultiplesException(
		Class<? extends Tabla> tabla,
		Field pk_anterior,
		Field pk_nueva
	){
		final var template = "La clase %s no puede tener más de una @ClavePrimaria (%s, %s)";
		super(template.formatted(
			tabla.getSimpleName(), pk_anterior.getName(), pk_nueva.getName()
		));
	}

}
