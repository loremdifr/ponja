package ORM.Relaciones;

//NOTE: esta interfaz usa <T> en lugar de <T extends Tabla>
//      porque algunas relaciones necesitan wrappear la
//      Tabla en una coleccion.

import ORM.Exceptions.ORMException;

public interface RelacionEntreTablas<T>{
	T get() throws ORMException;
	void set(T fila_asociada);
}
