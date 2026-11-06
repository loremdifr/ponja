package ORM.Exceptions;

import ORM.Clases.Tabla;

public class SchemaModificadoException extends ORMException{
	public SchemaModificadoException(Tabla fila){
		var str = "El schema de la tabla %s parece haber sido modificado " +
			      "desde el inicio de la aplicacion.";
		super(str.formatted(fila.getClass()));
	}
}
