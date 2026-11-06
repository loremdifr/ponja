package ORM.Queries;


import ORM.Exceptions.ORMException;

import java.sql.Connection;
import java.sql.PreparedStatement;

sealed public interface Query permits
	SelectQuery,
	AlterTableQuery,
	CreateTableQuery,
	InsertIntoQuery
{
	PreparedStatement toPreparedStatement(Connection conexion) throws ORMException;
}
