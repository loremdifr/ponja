package ORM.Relaciones;
import ORM.Clases.Tabla;
import ORM.DB;
import ORM.Exceptions.ORMException;

public final class ManyToOne<T extends Tabla>
extends Relacion<T, T>{

	public ManyToOne(Class<T> tabla_asociada, String nombre, Integer fk){
		super(tabla_asociada, nombre, fk);
	}

	public ManyToOne(Class<T> tabla_asociada, String nombre){
		super(tabla_asociada, nombre);
	}

	@Override
	public T __buscar(Class<? extends Tabla> desde) throws ORMException{
		return DB.buscarPorId(tabla_asociada(), fk());
	}

}
