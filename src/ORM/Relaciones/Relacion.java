package ORM.Relaciones;

import ORM.Clases.Tabla;

public abstract sealed class Relacion<T extends Tabla>
permits ManyToOne, OneToOne, OneToMany{

	public final Class<T> tabla_asociada;

	//NOTE: el tipo de la FK tiene que  coincidir con
	//      el tipo del id definido en Tabla.
	Integer fk      = null;
	boolean cargada = false;
	T db_cache      = null;

	public Relacion(Class<T> tabla_asociada, Integer fk){
		this.tabla_asociada = tabla_asociada;
		this.fk = fk;
	}

	public Relacion(Class<T> tabla_asociada){
		this(tabla_asociada, null);
	}

	public Integer fk()             { return fk;             }
	public boolean cargada()        { return cargada;        }
	public Class<T> tabla_asociada(){ return tabla_asociada; }
	public T db_cache()             { return db_cache;       }

	//TODO: no me gusta el nombre del param
	public void __cargar(T fila_asociada){
		db_cache = fila_asociada;
		cargada  = true;
	}

	public void setFk(Integer fk){ this.fk = fk; }
}
