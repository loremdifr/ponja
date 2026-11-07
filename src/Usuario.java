import ORM.Annotations.GuardarComo;
import ORM.Annotations.NoSeGuarda;
import ORM.Relaciones.ManyToOne;
import ORM.SQL.TipoColumna;
import ORM.Clases.Tabla;

public class Usuario extends Tabla{

	private String  nombre;
	private int     edad;

	//para que no sea varchar, se puede especificar:
	@GuardarComo(tipo = TipoColumna.TEXT)
	private String biografia;

	//ciertos campos no deben ser guardados:
	@NoSeGuarda
	private ManyToOne<Empresa> empresa;

	//esto es requerido por el ORM, un constructor sin parametros
	public Usuario(){}

	public Usuario(
		String nombre,
		int    edad,
		String biografia
	){
		this.nombre    = nombre;
		this.edad      = edad;
		this.biografia = biografia;
		this.empresa   = new ManyToOne<>();
	}

	//getters
	public String  getNombre()   { return nombre;        }
	public int     getEdad()     { return edad;          }
	public String  getBiografia(){ return biografia;     }
	public Empresa getEmpresa()  { return empresa.get(); }

	//setters
	public void setNombre   (String  nombre)   { this.nombre = nombre; }
	public void setEdad     (int     edad)     { this.edad   = edad;   }
//	public void setEmpresa  (Empresa empresa)  { this.empresa = empresa; }
	public void setBiografia(String  biografia){ this.biografia = biografia; }

	@Override
	public String toString(){
		return "[id=%d, nombre=%s, edad=%d, bio=%s]".formatted(
			getId(), nombre, edad, biografia
		);
	}
}
