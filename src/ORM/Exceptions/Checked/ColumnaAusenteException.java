package ORM.Exceptions.Checked;

import ORM.Exceptions.ORMException;

public class ColumnaAusenteException extends ORMException{

	public ColumnaAusenteException(
		String nombre_columna,
		String nombre_tabla
	){
		final var template = "La columna %s de la Tabla %s no existe en la DB con el nombre esperado";
		super(template.formatted(nombre_columna, nombre_tabla));
	}
}
