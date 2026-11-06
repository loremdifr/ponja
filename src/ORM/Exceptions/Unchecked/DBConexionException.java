package ORM.Exceptions.Unchecked;

import java.sql.SQLException;

public class DBConexionException extends RuntimeException{
	public DBConexionException(SQLException e){
		super("Error al intentar conectar con la DB (%s)".formatted(
			e.getMessage()
		));
	}
}
