package ORM.Exceptions.Checked;

import ORM.Exceptions.ORMException;

public class TablaModificadaException extends ORMException{
	public TablaModificadaException(
		String tabla_nombre,
		String campo_nombre
	){
		final var template = "La Tabla %s parece haber sido modificada. " +
			                 "El campo %s no pudo ser accedido.";
		super(template.formatted(tabla_nombre, campo_nombre));
	}
}
