package ORM.Exceptions.Checked;

import ORM.Exceptions.ORMException;

public class FatalORMException extends ORMException{

	public FatalORMException(){
		super("Error fatal inés perado, consulte con un abogado.");
	}
}
