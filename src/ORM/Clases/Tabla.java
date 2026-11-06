package ORM.Clases;

import ORM.Annotations.NoSeGuarda;
import ORM.DB;
import ORM.Exceptions.ORMException;

import java.time.LocalDateTime;


public abstract class Tabla{

	private Integer       id         = null;
	private LocalDateTime modificado = null;
	private LocalDateTime creado     = null;
	private Boolean       borrado    = null;

	@NoSeGuarda
	private long snapshot; //TODO: null?


	public boolean guardado() throws ORMException{
		return snapshot != DB.hashearFila(this);
	}

	public boolean guardar()
	throws ORMException {
		final var result    = DB.guardar(this);
		if(result) snapshot = DB.hashearFila(this);
		return result;
	}

	//getters
	public final Integer       getId()         { return id;         }
	public final LocalDateTime getModificado() { return modificado; }
	public final LocalDateTime getCreado()     { return creado;     }
	public final Boolean       getBorrado()    { return borrado;    }

}
