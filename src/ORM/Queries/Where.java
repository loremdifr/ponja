package ORM.Queries;
import java.util.ArrayList;
import java.util.List;

public sealed interface Where permits Where.BasicWhere, Where.NestedWhere{
	enum Conector{ AND, OR }
	Conector conector(); //NOTE: esto está aca para requerirlo en las impls!
	String toString(int index);

	record BasicWhere(
		String     columna,
		Comparador comparador,
		Object     valor,
		Conector   conector
	) implements Where{

		public BasicWhere(String columna, String comparador, Object valor){
			this(columna, Comparador.fromString(comparador), valor, Conector.AND);
		}

		public BasicWhere(String columna, String comparador, Object valor, Conector conector){
			this(columna, Comparador.fromString(comparador), valor, conector);
		}

		@Override
		public String toString(int index){
			final var str = new StringBuilder();
			if(index > 0) str.append(" ").append(conector).append(" ");
			final var valor_str = switch(valor){
				case null      -> "NULL";
				case String s  -> "'%s'".formatted(s);
//				case List<?> l -> "(%s)".formatted(
//					String.join(", ", l) // TODO: esto no anda asi
//				);
				default -> valor.toString();
			};
			return str.append(columna)
					  .append(" ")
					  .append(comparador)
					  .append(" ")
					  .append(valor_str)
				   	  .toString();
		}
	}

	record NestedWhere(
		ArrayList<Where> wheres,
		Conector         conector
	) implements Where{
		@Override
		public String toString(int index){
			final var str = new StringBuilder();
			if(index > 0) str.append(" ").append(conector);
			str.append("(");
			for(int i = 0; i < wheres.size(); i++){
				str.append(wheres.get(i).toString(i));
			}
			str.append(")");
			return str.toString();
		}
	}
}
