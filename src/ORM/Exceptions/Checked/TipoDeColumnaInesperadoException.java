package ORM.Exceptions.Checked;

import ORM.Exceptions.ORMException;

public class TipoDeColumnaInesperadoException extends ORMException{
	public TipoDeColumnaInesperadoException(String tipo_str){
		super("Tipo de Columna en MySQL Inés Perado (%s)".formatted(tipo_str));
	}
}
