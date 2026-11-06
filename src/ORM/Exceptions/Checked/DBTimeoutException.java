package ORM.Exceptions.Checked;

import ORM.Exceptions.ORMException;

public class DBTimeoutException extends ORMException{
	public DBTimeoutException(){
		super("La petición a la DB se pasó del Time-out.");
	}
}
