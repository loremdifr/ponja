# TODO

- Revisar que todos los recursos (conexiones, statements, resultsets) esten siendo cerrados correctamente.
- usar ? wildcards en el preparedstatements de la selectquery
- validar nombres de columnas en selectquery

# URGENTE:
- Tabla::guardar() cambia el modificado ANTES de que DB::guardar() compruebe si esta guardado(), lo que hace que siempre se guarde! Tabla::guardar() tendria que checkear el guardado por si misma?
- Where faltan las wildcards no es muy segura
- Foreign key constraints en las tablas
- Indices para buscar


## TIPOS:
- guardar Enums como VARCHAR
  - Checkear que todos los enums guardados ne la DB sean validos despues de registrarTablas y tirar uan excpetion


## relaciones de tablas
  - ManyToMany: .. por ahora no
  - @ConfigurarRelacion annotation para cambiarle fkey y tabla y demas a una relacion 


## transactions
