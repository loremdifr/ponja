package ORM.Exceptions.Checked;

import ORM.Exceptions.ORMException;

public class TipoDeValorInesperadoException extends ORMException{
	public TipoDeValorInesperadoException(Object valor){
		var str = "Valor de tipo '%s' inesperado al intentar crear la Query (%s)";
		super(str.formatted(valor.getClass(), valor));
	}
}
