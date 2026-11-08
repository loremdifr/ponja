package ORM.Relaciones;

import ORM.Clases.Tabla;
import jdk.jshell.spi.ExecutionControl;

import java.util.ArrayList;

public final class ManyToMany<T extends Tabla, P extends Tabla>
implements RelacionEntreTablas<ArrayList<T>>{

	//TODO: no estoy del todo seguro de esto
	final private ArrayList<T> tabla;
	final private ArrayList<P> pivot;

	public ManyToMany() throws ExecutionControl.NotImplementedException{
		this.tabla = new ArrayList<>();
		this.pivot = new ArrayList<>();
		throw new ExecutionControl.NotImplementedException("para un poco loco");
	}

	public ArrayList<T> get(){ return tabla; }

	@Override
	public void set(ArrayList<T> tabla_relacionada){


	}

	public boolean guardar(){ return false; } //TODO:
}
