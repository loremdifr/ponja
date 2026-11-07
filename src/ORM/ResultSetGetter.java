package ORM;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetGetter<T> {
    T apply(ResultSet result_set, int index) throws SQLException;
}
