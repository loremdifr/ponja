package ORM.Relaciones;

import ORM.Clases.Tabla;
import ORM.DB;
import ORM.Exceptions.ORMException;

public final class OneToOne<T extends Tabla>
extends Relacion<T, T>{

	public OneToOne(Class<T> tabla_asociada, String nombre, Integer fk){
		super(tabla_asociada, nombre, fk);
	}

	public OneToOne(Class<T> tabla_asociada, String nombre){
		super(tabla_asociada, nombre);
	}

	@Override
	public T __buscar(Class<T> from) throws ORMException{
		return DB.buscarPorId(tabla_asociada(), fk());
	}
}
