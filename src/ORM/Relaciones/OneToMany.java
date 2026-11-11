package ORM.Relaciones;

import ORM.Clases.Tabla;
import ORM.DB;
import ORM.Exceptions.ORMException;

import java.util.ArrayList;

public final class OneToMany<T extends Tabla>
extends Relacion<T, ArrayList<T>>{

	public OneToMany(Class<T> tabla_asociada, String nombre, Integer fk){
		super(tabla_asociada, nombre, fk);
	}

	public OneToMany(Class<T> tabla_asociada, String nombre){
		super(tabla_asociada, nombre);
	}

	@Override
	public ArrayList<T> __buscar(Class<T> desde) throws ORMException{
		final var fk_nombre = DB.fk(desde, nombre());
		return (ArrayList<T>) DB.buscarPorFk(tabla_asociada(), fk_nombre, fk());
	}

}
