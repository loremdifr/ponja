import ORM.Clases.Tabla;
import ORM.DB;
import ORM.Exceptions.ORMException;
import ORM.Relaciones.OneToMany;

import java.util.ArrayList;
import java.util.List;

public class Empresa extends Tabla{

	private String nombre;

	private OneToMany<Usuario> usuarios = new OneToMany<>(Usuario.class);

	public Empresa(String nombre){
		this.nombre = nombre;
	}

	public Empresa(){
		this("");
	}

	// getters

	public String getNombre(){
		return nombre;
	}

	public ArrayList<Empresa> getUsuarios()
	throws ORMException{
		return DB.getRelacion(usuarios);
	}

	// setters

	public void setNombre(String nombre){
		this.nombre = nombre;
	}

	public void setUsuarios(List<Usuario> usuarios){
		DB.setRelacion(this.usuarios, usuarios);

	}
	@Override
	public String toString(){ return nombre; }
}
