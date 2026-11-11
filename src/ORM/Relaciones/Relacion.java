package ORM.Relaciones;

import ORM.Clases.Tabla;
import ORM.Exceptions.ORMException;

//import java.lang.reflect.Field;

public abstract sealed class Relacion<T extends Tabla, V>
permits ManyToOne, OneToOne, OneToMany{

	private final Class<T> tabla_asociada;
	private final String nombre;

	//NOTE: el tipo de la FK tiene que coincidir con
	//      el tipo del id definido en Tabla.
	Integer fk      = null;
	boolean cargada = false;
	V db_cache      = null;

	public Relacion(Class<T> tabla_asociada, String nombre, Integer fk){
		this.tabla_asociada = tabla_asociada;
		this.nombre = nombre;
		this.fk = fk;
	}

	public Relacion(Class<T> tabla_asociada, String nombre){
		this(tabla_asociada, nombre, null);
	}

	// lamentablemente necesario porque java
	// tiene type erasure
	public abstract V __buscar(Class<T> from) throws ORMException;

	//TODO: no me gusta el nombre del param
	public void __cargar(V fila_asociada){
		db_cache = fila_asociada;
		cargada  = true;
	}


	// getteers

	public Integer fk()             { return fk;             }
	public boolean cargada()        { return cargada;        }
	public Class<T> tabla_asociada(){ return tabla_asociada; }
	public V db_cache()             { return db_cache;       }
	public String nombre()          { return nombre;         }

	// setter (singular)
	public void setFk(Integer fk){ this.fk = fk; }


}
