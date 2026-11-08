package ORM.Exceptions.Checked;

import ORM.Exceptions.ORMException;
import ORM.SQL.TipoColumna;

public class TipoDeColumnaIncongruenteException extends ORMException{
	public TipoDeColumnaIncongruenteException(TipoColumna tipo_columna, String label){
		var str = "El tipo de columna para el campo %s en la base de datos " +
			      "no corresponde al asignado por el ORM (%s)";
		super(str.formatted(label, tipo_columna));
	}
}
