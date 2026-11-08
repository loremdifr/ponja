import ORM.Clases.Tabla;
import ORM.Relaciones.OneToMany;

public class Empresa extends Tabla{

	private String nombre;

	public Empresa(String nombre){
		this.nombre = nombre;
	}

	public Empresa(){
		this("");
	}

	public String getNombre(){
		return nombre;
	}

	public void setNombre(String nombre){
		this.nombre = nombre;
	}
}
