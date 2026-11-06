package ORM.Exceptions;

public class DBConfigInexistenteException extends RuntimeException{

	public DBConfigInexistenteException(){
		super(
			"""
			El archivo de configuración de la DB no existe.
			Crealo como "db.properties" en la raiz del proyecto.
			Llenalo con el contenido de "db.properties.default".
			"""
		);
	}
}
