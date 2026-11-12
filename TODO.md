# TODO

- Revisar que todos los recursos (conexiones, statements, resultsets) esten siendo cerrados correctamente.
- usar ? wildcards en el preparedstatements de la selectquery
- validar nombres de columnas en selectquery
- paginacion de resultados!!!

# URGENTE:
- case List<?> en DB::setRelacion
- DB::borrar() ta roto
- Soft-delete no filtra
- NULL ints se cargan como 0 en TipoColumna.valueFromResult
- Tabla::guardar() cambia el modificado ANTES de que DB::guardar() compruebe si esta guardado(), lo que hace que siempre se guarde! Tabla::guardar() tendria que checkear el guardado por si misma?
- Where faltan las wildcards no es muy segura
- Foreign key constraints en las tablas
- Indices para buscar



## TIPOS:
- guardar Enums como VARCHAR
  - Checkear que todos los enums guardados ne la DB sean validos despues de registrarTablas y tirar uan excpetion


## relaciones de tablas
  - ManyToOne: la fk se autocrea y la matiene el orm
  - OneToMany: lazy list en el get. Si la columna inversa no tiene la relation, tirar error.
  - ManyToMany: .. por ahora no



## transactions
