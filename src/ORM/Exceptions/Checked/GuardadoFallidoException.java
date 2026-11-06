package ORM.Exceptions.Checked;

import ORM.Clases.Tabla;
import ORM.Exceptions.ORMException;

public class GuardadoFallidoException extends ORMException{
	public GuardadoFallidoException(Tabla fila){
		super("No se pudo guardar la fila para la tabla %s.".formatted(
			fila.getClass()
		));
	}
}
