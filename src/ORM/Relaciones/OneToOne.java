package ORM.Relaciones;

import ORM.Clases.Tabla;

public final class OneToOne<T extends Tabla>
implements RelacionEntreTablas<T>{

	final private T tabla;

	public OneToOne(){
		this.tabla = null;
	}

	public T get(){ return tabla; }

	@Override
	public void set(T tabla_relacionada){
		//TODO:
	}

	public boolean guardar(){ return false; } //TODO:
}
