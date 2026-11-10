package ORM.SQL;

import java.lang.reflect.Field;
import java.util.HashMap;

public record Schema(
	String tabla,
	HashMap<Field, Columna> columnas,
	Field pk,
	HashMap<Field, ClaveForanea> rels
){}
