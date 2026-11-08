package ORM.Exceptions.Checked;

import ORM.Exceptions.ORMException;

public class ComparadorNoExisteException extends ORMException{
	public ComparadorNoExisteException(String comparador){
		super("El Comparador '%s' no existe.".formatted(comparador));
	}
}
