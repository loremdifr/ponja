package ORM.Relaciones;
import ORM.Clases.Tabla;

public final class ManyToOne<T extends Tabla>
extends Relacion<T>{

	public ManyToOne(Class<T> tabla_asociada, Integer fk){
		super(tabla_asociada, fk);
	}

	public ManyToOne(Class<T> tabla_asociada){
		super(tabla_asociada);
	}

}
