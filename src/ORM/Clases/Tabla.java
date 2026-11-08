package ORM.Clases;

import ORM.Annotations.NoSeGuarda;
import ORM.DB;
import ORM.Exceptions.ORMException;

import java.time.LocalDateTime;


public abstract class Tabla{

	private Integer       id         = null;
	private LocalDateTime modificado = null;
	private LocalDateTime creado     = null;
	private LocalDateTime borrado    = null; //soft-delete

	@NoSeGuarda
	private long _snapshot = -1;

	public boolean guardado() throws ORMException{
		return _snapshot == DB.hashearFila(this);
	}

	public boolean borrado(){
		return borrado != null;
	}

	public boolean guardar()
	throws ORMException {
		//campos de audit (creado y modificado)
		final var ahora           = LocalDateTime.now();
		final var prev_creado     = creado;
		final var prev_modificado = modificado;
		creado     = (creado != null) ? creado : ahora;
		modificado = ahora;

		final var guardado = DB.guardar(this);

		if(guardado){
			 _snapshot = DB.hashearFila(this);
		}else{
			// resetear creado y modificado
			// si fallo el guardado
			creado     = prev_creado;
			modificado = prev_modificado;
		}

		return guardado;
	}

	public boolean borrar()
	throws ORMException {
		// borrar es un soft-delete que lo marca como borrado de la DB,
		// y que todas las queries usan como defecto en el where
		// para filtrar los campos.
		return DB.borrar(this);
	}

	//getters
	public final Integer       getId()         { return id;         }
	public final LocalDateTime getModificado() { return modificado; }
	public final LocalDateTime getCreado()     { return creado;     }

}
