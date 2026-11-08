package ORM.Exceptions.Checked;

import ORM.Exceptions.ORMException;

public class DireccionInvalidaException extends ORMException{
	public DireccionInvalidaException(String direccion){
		super("La direccion '%s' es inválida para el ORDER BY.".formatted(direccion));
	}
}
