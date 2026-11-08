package ORM.Relaciones;

import ORM.Clases.Tabla;
import ORM.DB;
import ORM.Exceptions.Checked.FatalORMException;
import ORM.Exceptions.ORMException;

//TODO: hacer que el ORM detecte estos tipos de campos!

public final class ManyToOne<T extends Tabla>
implements RelacionEntreTablas<T>{

	private T fila_asociada;
	private boolean guardada = false; // cambio persistido en la DB
	private boolean cargada  = false; // lazy-loaded?

	//estos datos los inyecta el ORM
	private String   fk_nombre = null;
	private Integer  fk_valor  = null;
	private Class<T> tabla     = null;

	public ManyToOne(){
		this.fila_asociada = null;
	}

	public T get() throws ORMException{
		if(fk_nombre == null || tabla == null){
			throw new FatalORMException();
		}

		if(!cargada){
			fila_asociada = DB.buscarPorId(tabla, fk_valor);
			guardada = true;
			cargada  = true;
		}

		return fila_asociada;
	}

	public void set(T pertenece_a){
		if(cargada
		&& fila_asociada != null
		&& fila_asociada.equals(pertenece_a)){
			return; //sin cambios
		}

		this.fila_asociada = pertenece_a;
		this.guardada = false;
		this.cargada  = true;
	}

	public boolean __guardarAsociada() throws ORMException{
		if(guardada) return true;
		if(fila_asociada.guardar()){
			guardada = true;
			cargada  = true;
			return true;
		}
		return false;
	}

}
