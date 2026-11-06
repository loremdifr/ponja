package ORM.Exceptions.Checked;

import ORM.Clases.Tabla;
import ORM.Exceptions.ORMException;

public class TablaSinConstructorVacioException extends ORMException{
	public <T extends Tabla> TablaSinConstructorVacioException(Class<T> tabla){
		super(
			"Tabla %s mal implementada: ".formatted(tabla) +
			"Las clases que heredan de Tabla deben implementar " +
			"un constructor sin argumentos."
		);
	}
}
