package ORM;

import ORM.SQL.Columna;
import com.mysql.cj.xdevapi.Column;

import javax.naming.OperationNotSupportedException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils{
	private Utils() throws OperationNotSupportedException{
		throw new OperationNotSupportedException(
			"Esta clase es estática, no hace falta instanciarla."
		);
	}

	public static void prettyPrint(Object ojeto){
		final var str = ojeto.toString()
			.replace("],", "]")
			.replace("},", "}")
			.replace(", ", ",");
		final var tab = "︙\t";
		int tab_level = 0;
		for(int i = 0; i <  str.length(); i++){
			final var chr = str.charAt(i);
			switch(chr){
				case '[', '{' -> {
					System.out.println(chr);
					System.out.print(tab.repeat(++tab_level));
				}
				case ']', '}' -> {
					System.out.println();
					System.out.println(tab.repeat(--tab_level) + chr);
					System.out.print(tab.repeat(tab_level));
				}
				case ',' -> {
					System.out.println(chr);
					System.out.print(tab.repeat(tab_level));
				}
				default -> System.out.print(chr);
			}
		}
	}

	static HashMap<String, Columna> diffColumnas(
		HashMap<String, Columna> a,
		HashMap<String, Columna> b
	){
		if(a.equals(b)) return new HashMap<>();

		final var entries_a = a.entrySet();
		final var keyset_b  = b.keySet();

		return (HashMap<String, Columna>) entries_a.stream()
			.filter(x -> !keyset_b.contains(x.getKey()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}



}
