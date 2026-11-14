package ORM.Queries;


import ORM.Exceptions.ORMException;
import ORM.MySQL;

import java.sql.PreparedStatement;

sealed public interface Query permits
	SelectQuery,
	AlterTableQuery,
	CreateTableQuery,
	InsertIntoQuery
{
	PreparedStatement toPreparedStatement(MySQL conexion) throws ORMException;
}
