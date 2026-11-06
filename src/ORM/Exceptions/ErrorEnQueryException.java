package ORM.Exceptions;

import java.sql.SQLException;

public class ErrorEnQueryException extends ORMException{
	public ErrorEnQueryException(SQLException e){
		super(
			"Ocurrió un error al intentar ejecutar la query: %s".formatted(
			e.getMessage()));
	}
}
