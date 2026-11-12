package ORM.Relaciones;

import ORM.Clases.Tabla;
import ORM.Exceptions.ORMException;
import ORM.SQL.ClaveForanea;

//import java.lang.reflect.Field;

//TODO: ya no hace falta el T extends tabla..?
public abstract sealed class Relacion<T extends Tabla, V>
permits ManyToOne, OneToOne, OneToMany{

	//NOTE: el tipo de la FK tiene que coincidir con
	//      el tipo del id definido en Tabla.
	Integer fk        = null;
	boolean cargada   = false;
	V cache_db        = null;
//	V cache_prev      = null;
	boolean pendiente = false; //TODO: renombrar a guardada?

	public Relacion(Integer fk){ this.fk = fk; }
	public Relacion(){ this(null); }

	// lamentablemente necesario porque java
	// tiene type erasure
	public abstract V __buscar(ClaveForanea fk_data) throws ORMException;

	//TODO: no me gusta el nombre del param
	public void __cargar(V fila_asociada){
//		if(cargada) cache_prev = cache_db;
		cache_db = fila_asociada;
		cargada  = true;
	}

	public void __invalidarCache(){
		cache_db = null;
		cargada  = false;
	}

	// getteers
	public Integer fk()         { return fk;         }
	public boolean cargada()    { return cargada;    }
	public V       cache_db()   { return cache_db;   }
//	public V       cache_prev() { return cache_prev; }
	public boolean pendiente()  { return pendiente;  }

	// setters
	public void setFk(Integer fk){ this.fk = fk; }
	public void setPendiente(boolean pendiente) { this.pendiente = pendiente; }

}
