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
		final var guardado = DB.guardar(this);
		if(guardado) _snapshot = DB.hashearFila(this);
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


	// audit fields black magic
	// es un arreglo y el orden es importante, lo usa solamente DB
	public final LocalDateTime[] __getAuditFields(){
		return new LocalDateTime[]{modificado, creado};
	}

	public final void __setAuditFields(LocalDateTime[] audit_fields){
		//TODO: assert que sean dos
		this.modificado = audit_fields[0];
		this.creado     = audit_fields[1];
	}

	public final void __setAuditFields(LocalDateTime timestamp){
		if(creado == null) creado = timestamp;
		modificado = timestamp;
	}


}
