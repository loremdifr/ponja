package ORM.Exceptions;

import ORM.SQL.Columna;

public class TipoDeColumnaIncongruenteException extends ORMException{
	public TipoDeColumnaIncongruenteException(Columna col, String label){
		var str = "El tipo de columna para el campo %s en la base de datos " +
			      "no corresponde al asignado por el ORM (%s)";
		super(str.formatted(label, col.tipo()));
	}
}
