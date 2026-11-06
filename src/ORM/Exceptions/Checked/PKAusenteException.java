package ORM.Exceptions.Checked;

import ORM.Clases.Tabla;
import ORM.Exceptions.ORMException;

public class PKAusenteException extends ORMException{
	public PKAusenteException(Class<? extends Tabla> tabla){
		final var template = "La tabla %s no tiene ningún campo anotado como @ClavePrimaria";
		super(template.formatted(tabla.getSimpleName()));
	}
}
