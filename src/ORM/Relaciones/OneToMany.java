package ORM.Relaciones;

import ORM.Clases.Tabla;

import java.util.ArrayList;

public final class OneToMany<T extends Tabla>
implements RelacionEntreTablas<ArrayList<T>>{

	final private ArrayList<T> tabla;

	public OneToMany(){
		this.tabla = new ArrayList<>();
	}

	public ArrayList<T> get(){ return tabla; }

	@Override
	public void set(ArrayList<T> tabla_relacionada){
		//TODO:
	}

	public boolean guardar(){ return false; } //TODO:
}
