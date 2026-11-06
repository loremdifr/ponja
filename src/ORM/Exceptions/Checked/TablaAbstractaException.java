package ORM.Exceptions.Checked;

import ORM.Exceptions.ORMException;

public class TablaAbstractaException extends ORMException{

	public TablaAbstractaException(Class<?> tabla){
		super("La Tabla %s es abstracta y no pude ser instanciada.".formatted(
			tabla.toString()
		));
	}
}
