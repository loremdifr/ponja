# ponja

pequenho ORM na Java


## TODOs

- Revisar que todos los recursos (conexiones, statements, resultsets) esten siendo cerrados correctamente.
- usar ? wildcards en el preparedstatements de la selectquery
- validar nombres de columnas en selectquery
- paginacion de resultados!!!

#### TIPOS:
- guardar Enums como VARCHAR
  - Checkear que todos los enums guardados ne la DB sean validos despues deregistrarTablas y tirar uan excpetion


#### QUERIES:
- SelectQuery hacer generic cosa que DB.buscar(clase) devuelva SelectQuery<T> y no haya que castear por todos lados
  - toList() tambien tendria que ser generica?
- agregar DB.query("") para poder hacer queries manuales


####  relaciones de tablas
  - ManyToOne: la fk se autocrea y la matiene el orm
  - OneToMany: lazy list en el get. Si la columna inversa no tiene la relation, tirar error.
  - ManyToMany: .. por ahora no
