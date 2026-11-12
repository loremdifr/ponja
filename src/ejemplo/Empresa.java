package ejemplo;

import ORM.Clases.Tabla;
import ORM.DB;
import ORM.Exceptions.ORMException;
import ORM.Relaciones.OneToMany;

import java.util.ArrayList;
import java.util.List;

public class Empresa extends Tabla{

	private String nombre;
	private final OneToMany<Usuario> usuarios;

	public Empresa(String nombre){
		this.nombre = nombre;
		this.usuarios = new OneToMany<>();
	}

	public Empresa(){
		this("");
	}

	// getters

	public String getNombre(){
		return nombre;
	}

	public ArrayList<Usuario> getUsuarios()
	throws ORMException{
		return DB.getRelacion(this, usuarios);
	}

	// setters

	public void setNombre(String nombre){
		this.nombre = nombre;
	}

	public void setUsuarios(List<Usuario> usuarios) throws ORMException{
		DB.setRelacion(this.usuarios, (ArrayList<Usuario>) usuarios);
	}

	@Override
	public String toString(){ return nombre; }
}
