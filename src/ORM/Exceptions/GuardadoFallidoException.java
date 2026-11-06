package ORM.Exceptions;

import ORM.Clases.Tabla;

public class GuardadoFallidoException extends ORMException{
	public GuardadoFallidoException(Tabla fila){
		super("No se pudo guardar la fila para la tabla %s.".formatted(
			fila.getClass()
		));
	}
}
