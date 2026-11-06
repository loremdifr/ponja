package ORM.Exceptions;

import ORM.Clases.Tabla;

public class PKRedefinidaException extends ORMException{
	public PKRedefinidaException(Class<? extends Tabla> tabla){
		super("Se intentó redefinir la clave primaria en '%s'".formatted(
			tabla.toString()
		));
	}
}
