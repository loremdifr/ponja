package ORM.Relaciones;

import ORM.Clases.Tabla;

import java.util.ArrayList;

public final class OneToMany<T extends Tabla>
extends Relacion<T>{

	public OneToMany(Class<T> tabla_asociada, Integer fk){
		super(tabla_asociada, fk);
	}
}
