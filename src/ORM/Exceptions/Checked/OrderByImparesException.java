package ORM.Exceptions.Checked;

import ORM.Exceptions.ORMException;

public class OrderByImparesException extends ORMException{

	public OrderByImparesException(){
		super("Los argumentos del orderBy deben ser pares (columna, ASC, columna, DESC, etc...)");
	}
}
