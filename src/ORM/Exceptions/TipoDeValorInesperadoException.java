package ORM.Exceptions;

public class TipoDeValorInesperadoException extends ORMException{
	public TipoDeValorInesperadoException(Object valor){
		var str = "Valor de tipo '%s' inesperado al intentar crear la Query (%s)";
		super(str.formatted(valor.getClass(), valor));
	}
}
