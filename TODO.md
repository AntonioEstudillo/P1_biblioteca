# Trabajo pendiente (TODO)

Total: 90 TODO. Busca `// TODO Exx` en el código.

| # | Grupo | Descripción |
|---|---|---|
| 1 | E03 | Validar que el prefijo tenga exactamente 3 letras mayúsculas y que el número sea positivo (IllegalArgumentException). |
| 2 | E03 | Obtener (o crear) la secuencia del prefijo con computeIfAbsent e incrementarla atómicamente. |
| 3 | E03 | Actualizar la secuencia del prefijo con accumulateAndGet(numero, Math::max). |
| 4 | E03 | Validar el formato con la expresión [A-Z]{3}-\d{4,} y construir la clave. |
| 5 | E03 | Ordenar primero por prefijo y después por número. |
| 6 | E02 | Aplicar TASA_IMPUESTO a tarifa(unidades) y redondear a 2 decimales. |
| 7 | E02 | Usar Comparator.comparing(Prestable::nombre, String.CASE_INSENSITIVE_ORDER). |
| 8 | E05 | Devolver "Repositorio digital: " concatenado con el valor de urlRepositorio(). |
| 9 | E05 | Devolver "Nivel de acceso: " concatenado con el valor de nivelAcceso(). |
| 10 | E06 | Restar el porcentaje de descuento al monto y redondear a 2 decimales. |
| 11 | E07 | Implementar con switch-expresión las transiciones del diagrama; los estados terminales no permiten ninguna. |
| 12 | E04 | Asignar la clave (no nula), registrarla en Signatura.registrar y validar nombre y precio mediante los setters. |
| 13 | E04 | Rechazar nombres nulos o en blanco (ReglaNegocioException); guardar el nombre sin espacios extremos. |
| 14 | E04 | Rechazar precios negativos, NaN o infinitos (ReglaNegocioException). |
| 15 | E04 | Validar unidades > 0 (ReglaNegocioException), incrementar cotizaciones y devolver calcularTarifa redondeada a 2 decimales. |
| 16 | E05 | Validar: paginas > 0 (si no se cumple: ReglaNegocioException "El número de páginas debe ser positivo"). |
| 17 | E05 | Libro: cuota lineal por día. Fórmula: precioBase() * unidades |
| 18 | E05 | Crear un nuevo Libro con el mismo nombre, precio y atributo (clave nueva). |
| 19 | E05 | Validar: antiguedadAnios >= 0 (si no se cumple: ReglaNegocioException "La antigüedad no puede ser negativa"). |
| 20 | E05 | Revista: 50 % de descuento si tiene más de 5 años. Fórmula: precioBase() * unidades * (antiguedadAnios > 5 ? 0.5 : 1.0) |
| 21 | E05 | Crear un nuevo Revista con el mismo nombre, precio y atributo (clave nueva). |
| 22 | E05 | Validar: grado != null && !grado.isBlank() (si no se cumple: ReglaNegocioException "El grado académico es obligatorio"). |
| 23 | E05 | Tesis: los primeros 3 días son gratuitos. Fórmula: unidades <= 3 ? 0.0 : precioBase() * (unidades - 3) |
| 24 | E05 | Crear un nuevo Tesis con el mismo nombre, precio y atributo (clave nueva). |
| 25 | E05 | Combinar Digitalizable.super.descripcionCorta() y Restringido.super.descripcionCorta() separados por " | ". |
| 26 | E06 | Validar id > 0 y asignar nombre, correo y nivel mediante sus setters. |
| 27 | E06 | Rechazar nombre nulo o en blanco. |
| 28 | E06 | Validar el correo con el patrón CORREO y guardarlo en minúsculas. |
| 29 | E14 | Escribir id, nombre, correo y el nombre del nivel (writeInt / writeUTF). |
| 30 | E14 | Leer en el mismo orden en que se escribió y reconstruir el nivel con Nivel.valueOf. |
| 31 | E07 | Validar id > 0, actor y fecha no nulos; asignar y sincronizar SECUENCIA con accumulateAndGet(id, Math::max). |
| 32 | E07 | Exigir estado ABIERTA, rechazar elementos repetidos (stream anyMatch), crear la Linea interna y añadirla. |
| 33 | E07 | Exigir estado ABIERTA y eliminar con removeIf la línea cuyo elemento tenga esa clave. |
| 34 | E07 | Validar la transición con permiteTransicionA y exigir al menos una línea para pasar a EN_CURSO. |
| 35 | E08 | Sumar los subtotales de las líneas con mapToDouble y redondear a 2 decimales. |
| 36 | E08 | Sumar los importes de las líneas (ya con descuento) y redondear a 2 decimales. |
| 37 | E07 | Validar unidades > 0 y asignar los campos. |
| 38 | E08 | Usar Prestamo.this.socio.nivel().aplicar(subtotal()). |
| 39 | E09 | Recorrer los argumentos; rechazar nulos y claves duplicadas (ReglaNegocioException); usar putIfAbsent. |
| 40 | E09 | Stream de los valores filtrado con regla::evaluar y recolectado con toList(). |
| 41 | E09 | Usar filter(tipo::isInstance) y map(tipo::cast). |
| 42 | E10 | Devolver new Comparator<>() { compare(a, b) → Double.compare(a.tarifa(unidades), b.tarifa(unidades)) }. |
| 43 | E10 | Declarar dentro del método la clase local Acumulador (cuenta, suma, mínimo, máximo) y recorrer el catálogo. |
| 44 | E10 | Lanzar NoSuchElementException si no hay siguiente; devolver el elemento y avanzar el cursor. |
| 45 | E11 | Devolver la lambda t -> evaluar(t) && otra.evaluar(t) (validar otra no nula). |
| 46 | E11 | Devolver la lambda t -> evaluar(t) || otra.evaluar(t). |
| 47 | E11 | Devolver la lambda t -> !evaluar(t). |
| 48 | E11 | Lambda que acepta los elementos cuyo precioBase() sea menor o igual que maximo. |
| 49 | E11 | Lambda insensible a mayúsculas/minúsculas sobre nombre(). |
| 50 | E11 | Referencia a método de instancia particular: capacidad::isInstance. |
| 51 | E11 | Devolver una lambda que notifique primero a this y después a siguiente. |
| 52 | E12 | Si ya está activa lanzar IllegalStateException; crear new Thread(this, nombreHilo), marcarlo demonio, activar y start(). |
| 53 | E12 | Bajar la bandera, interrumpir el hilo y esperar con join() (si existe). |
| 54 | E12 | Mientras activa: ejecutarCiclo(), incrementar ciclos y dormir periodoMs; ante InterruptedException restaurar la interrupción y salir. |
| 55 | E13 | Abrir jdbc:sqlite:<ruta>, activar PRAGMA foreign_keys = ON y crear el esquema. |
| 56 | E13 | setAutoCommit(false); ejecutar; commit; ante SQLException rollback y PersistenciaException; finalmente setAutoCommit(true). |
| 57 | E13 | switch (e) { case Material.Libro x -> ...; ... } sin default (la jerarquía es sellada). |
| 58 | E13 | INSERT ... ON CONFLICT(clave) DO UPDATE ... con PreparedStatement. |
| 59 | E13 | SELECT por clave; mapear con mapeador si hay fila. |
| 60 | E13 | SELECT ordenado por clave; mapear cada fila. |
| 61 | E13 | DELETE por clave; devolver si se afectó alguna fila. |
| 62 | E13 | Upsert por id con PreparedStatement. |
| 63 | E13 | SELECT por id. |
| 64 | E13 | SELECT ordenado por id. |
| 65 | E13 | DELETE por id. |
| 66 | E13 | En bd.enTransaccion: upsert del encabezado, borrar sus líneas e insertarlas en lote (addBatch/executeBatch). |
| 67 | E13 | Leer el encabezado, resolver el actor y los elementos con sus repositorios, agregar líneas y restaurar el estado. |
| 68 | E13 | Construir el objeto en estado ABIERTA, agregar las líneas y al final restaurarEstado(estado leído). |
| 69 | E13 | Recorrer los ids ordenados y reconstruir cada registro. |
| 70 | E13 | DELETE del encabezado (las líneas se eliminan en cascada). |
| 71 | E14 | ObjectOutputStream(BufferedOutputStream(Files.newOutputStream)): encabezado, fecha, catálogo, listas. |
| 72 | E14 | Leer en el mismo orden; validar el encabezado (IOException si no coincide). |
| 73 | E15 | Crear los tres repositorios SQLite, asignar el reloj y cargar los datos existentes en memoria. |
| 74 | E15 | Usar referencias a método: repoElementos.todos().forEach(catalogo::agregar), etc. |
| 75 | E15 | Crear con la fábrica, persistir, agregar al catálogo, publicar ALTA y devolver el elemento. |
| 76 | E15 | Impedir retirar elementos referenciados por algún registro; eliminar de la BD y del catálogo. |
| 77 | E15 | Rechazar correos repetidos; id = máximo actual + 1 (stream mapToInt/max); persistir y publicar ALTA. |
| 78 | E15 | Crear el registro con la fecha del reloj, persistir, guardar en el mapa y publicar CAMBIO_ESTADO. |
| 79 | E15 | Localizar registro y elemento, agregar la línea y persistir. |
| 80 | E15 | Aplicar la transición, persistir y publicar CAMBIO_ESTADO. |
| 81 | E16 | flatMap de líneas; groupingBy(tipo, TreeMap::new, summingDouble(importe)). |
| 82 | E16 | groupingBy(elemento, summingInt(unidades)) y máximo por valor con Map.Entry.comparingByValue(). |
| 83 | E16 | groupingBy(actor, summingDouble(total)); ordenar descendentemente por valor; limit(n). |
| 84 | E16 | mapToDouble(Prestamo::total).summaryStatistics() sobre los cerrados. |
| 85 | E16 | partitioningBy(e -> e.precioBase() >= umbral). |
| 86 | E16 | Recorrer el catálogo ordenado por clave y aplicar map(funcion). |
| 87 | E16 | switch (e) con un caso por subtipo y al menos un caso con guarda "when". |
| 88 | E12 | Recorrer los registros y aplicar las dos reglas con un switch sobre el estado. |
| 89 | E12 | Crear el monitor interno (new MonitorVencimientos(periodoMs)) e iniciarlo con un nombre de hilo descriptivo. |
| 90 | E12 | Delegar en la envolvente: Biblioteca.this.revisar(). |
