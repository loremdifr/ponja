package ORM.SQL;

import ORM.Clases.Tabla;

import java.lang.reflect.Field;

public record ClaveForanea(
//	Field field,
	Class<? extends Tabla> tabla_asociada,
	String fk_nombre
){}
