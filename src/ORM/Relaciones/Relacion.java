package ORM.Relaciones;

import ORM.Clases.Tabla;
import ORM.Exceptions.ORMException;
import ORM.SQL.ClaveForanea;

//import java.lang.reflect.Field;

public abstract sealed class Relacion<T extends Tabla, V>
permits ManyToOne, OneToOne, OneToMany{

	//NOTE: el tipo de la FK tiene que coincidir con
	//      el tipo del id definido en Tabla.
	Integer fk      = null;
	boolean cargada = false;
	V db_cache      = null;

	public Relacion(Integer fk){ this.fk = fk; }
	public Relacion(){ this(null); }

	// lamentablemente necesario porque java
	// tiene type erasure
	public abstract V __buscar(ClaveForanea fk_data) throws ORMException;

	//TODO: no me gusta el nombre del param
	public void __cargar(V fila_asociada){
		db_cache = fila_asociada;
		cargada  = true;
	}

	// getteers
	public Integer fk()      { return fk;       }
	public boolean cargada() { return cargada;  }
	public V db_cache()      { return db_cache; }

	// setter (singular)
	public void setFk(Integer fk){ this.fk = fk; }


}
