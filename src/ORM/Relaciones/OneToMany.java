package ORM.Relaciones;

import ORM.Clases.Tabla;
import ORM.DB;
import ORM.Exceptions.ORMException;
import ORM.SQL.ClaveForanea;

import java.util.ArrayList;

public final class OneToMany<T extends Tabla>
extends Relacion<T, ArrayList<T>>{

	public OneToMany(){ super(); }
	public OneToMany(Integer fk){ super(fk); }

	@Override
	@SuppressWarnings("unchecked")
	public ArrayList<T> __buscar(ClaveForanea fk_data) throws ORMException{
		return (ArrayList<T>) DB.buscarPorFk(
			fk_data.tabla_asociada(),
			fk_data.fk_nombre(),
			fk()
		);
	}

}
