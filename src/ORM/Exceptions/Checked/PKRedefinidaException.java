package ORM.Exceptions.Checked;

import ORM.Clases.Tabla;
import ORM.Exceptions.ORMException;

public class PKRedefinidaException extends ORMException{
	public PKRedefinidaException(Class<? extends Tabla> tabla){
		super("Se intentó redefinir la clave primaria en '%s'".formatted(
			tabla.toString()
		));
	}
}
