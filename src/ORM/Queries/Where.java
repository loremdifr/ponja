package ORM.Queries;
import ORM.Exceptions.ORMException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public sealed interface Where permits Where.BasicWhere, Where.NestedWhere{
	enum Conector{ AND, OR }
	Conector conector(); //NOTE: esto está aca para requerirlo en las impls!
	String toString(int index);

	static String valorString(Object val){
		return switch(val){
			case null      -> "NULL";
			case String s  -> "'%s'".formatted(s);
			case List<?> l -> {
				List<String> str_list = l.stream().map(
					Where::valorString
				).toList();
				yield "(%s)".formatted(
					String.join(", ", str_list)
				);
			}
			default -> val.toString();
		};
	}

	record BasicWhere(
		String     columna,
		Comparador comparador,
		Object     valor,
		Conector   conector
	) implements Where{

		public BasicWhere(String columna, String comparador, Object valor, Conector conector) throws ORMException{
			this(columna, Comparador.fromString(comparador), valor, conector);
		}

		public BasicWhere(String columna, String comparador, Object valor) throws ORMException{
			this(columna, comparador, valor, Conector.AND);
		}


		@Override
		public String toString(int index){
			final var str = new StringBuilder();
			if(index > 0) str.append(" ").append(conector).append(" ");
			final var valor_str = Where.valorString(valor);
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

		@FunctionalInterface
		public interface NestedWhereFunction {
			NestedWhere apply(NestedWhere w) throws ORMException;
		}

		public NestedWhere(){ this(new ArrayList<>(), Conector.AND); }

		//and
		public NestedWhere and(String columna, String comparador, Object valor)
		throws ORMException{
			wheres.add(new Where.BasicWhere(
				columna, comparador, valor, Conector.AND
			));
			return this;
		}

		public NestedWhere and(NestedWhereFunction anidado)
		throws ORMException{
			wheres.add(anidado.apply(new Where.NestedWhere(
				new ArrayList<>(), Conector.AND
			)));
			return this;
		}

		//or
		public NestedWhere or(String columna, String comparador, Object valor)
		throws ORMException{
			wheres.add(new Where.BasicWhere(
				columna, comparador, valor, Conector.OR
			));
			return this;
		}

		public NestedWhere or(NestedWhereFunction anidado)
		throws ORMException{
			wheres.add(anidado.apply(new Where.NestedWhere(
				new ArrayList<>(), Conector.OR
			)));
			return this;
		}

		@Override
		public String toString(int index){
			if(wheres.isEmpty()) return "";
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
