package ORM.Exceptions.Checked;

import ORM.Clases.Tabla;
import ORM.Exceptions.ORMException;

import java.lang.reflect.Field;

public class PKNoGuardableException extends ORMException{

	public PKNoGuardableException(
		Class<? extends Tabla> tabla, Field field
	){
		final var template = "La clase %s no puede tener una @ClavePrimaria que @NoSeGuarda (%s)";
		super(template.formatted(
			tabla.getSimpleName(), field.getName()
		));
	}

}
