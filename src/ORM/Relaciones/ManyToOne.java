package ORM.Relaciones;
import ORM.Clases.Tabla;
import ORM.DB;
import ORM.Exceptions.ORMException;
import ORM.SQL.ClaveForanea;

public final class ManyToOne<T extends Tabla>
extends Relacion<T, T>{

	public ManyToOne(){ super(); }
	public ManyToOne(Integer fk){ super(fk); }

	@Override
	@SuppressWarnings("unchecked")
	public T __buscar(ClaveForanea fk_data) throws ORMException{
		return DB.buscarPorId(
			(Class<T>) fk_data.tabla_asociada(), //segurisimo papa
			fk()
		);
	}

}
