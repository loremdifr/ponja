import ORM.Annotations.GuardarComo;
import ORM.Annotations.NoSeGuarda;
import ORM.Exceptions.ORMException;
import ORM.Relaciones.ManyToOne;
import ORM.SQL.TipoColumna;
import ORM.Clases.Tabla;

import java.util.ArrayList;

public class Usuario extends Tabla{

	private String  nombre;
	private int     edad;

	// se puede especificar el tipo de una columna manualmente
	// (ya que los strings son por defecto varchar, y en este caso queremos
	// que sea de tipo TEXT):
	@GuardarComo(tipo = TipoColumna.TEXT)
	private String biografia;

	// si un campo no se guarda en la DB, se puede marcar asi:
	@NoSeGuarda
	final private ArrayList<String> tareas;

	// las relaciones entre tablas deben expresarse usando las
	// clases auxiliares provistas por el ORM:
	final private ManyToOne<Empresa> empresa = new ManyToOne<>();

	// el ORM requiere que exista un constructor sin parametros,
	// para poder instanciar las clases al devolver resultados.
	// (el contenido de este constructor no importa, puede tener lo que sea
	// adentro, mientras no tenga parámetros)
	public Usuario(){
		this("", 0, "");
	}

	public Usuario(
		String nombre,
		int    edad,
		String biografia
	){
		this.nombre    = nombre;
		this.edad      = edad;
		this.biografia = biografia;
		this.tareas    = new ArrayList<>();
	}

	//getters
	public String  getNombre()   { return nombre;        }
	public int     getEdad()     { return edad;          }
	public String  getBiografia(){ return biografia;     }

	public Empresa getEmpresa() throws ORMException{
		return empresa.get();
	}

	//setters
	public void setNombre   (String  nombre)   { this.nombre = nombre; }
	public void setEdad     (int     edad)     { this.edad   = edad;   }
	public void setEmpresa  (Empresa empresa)  { this.empresa.set(empresa); }
	public void setBiografia(String  biografia){ this.biografia = biografia; }

	@Override
	public String toString(){
		var nombre_empresa = "<sin cargar>";
		try{
		 nombre_empresa = empresa.cargada()
			? getEmpresa().getNombre()
			: nombre_empresa;
		}catch(ORMException _){}
		return "[id=%d, nombre=%s, edad=%d, bio=%s, empresa=%s]".formatted(
			getId(), nombre, edad, biografia, nombre_empresa
		);
	}
}
