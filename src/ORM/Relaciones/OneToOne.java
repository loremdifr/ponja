package ORM.Relaciones;

import ORM.Clases.Tabla;

public final class OneToOne<T extends Tabla>
extends Relacion<T>{
	public OneToOne(Class<T> tabla_asociada, Integer fk){
		super(tabla_asociada, fk);
	}
}
