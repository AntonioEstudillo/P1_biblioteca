package mx.tecnm.orizaba.tp.biblioteca.servicio;

import mx.tecnm.orizaba.tp.biblioteca.concurrencia.TareaPeriodica;
import mx.tecnm.orizaba.tp.biblioteca.eventos.Evento;
import mx.tecnm.orizaba.tp.biblioteca.eventos.OyenteEvento;
import mx.tecnm.orizaba.tp.biblioteca.eventos.TipoEvento;
import mx.tecnm.orizaba.tp.biblioteca.funcional.FabricaElemento;
import mx.tecnm.orizaba.tp.biblioteca.funcional.Regla;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Socio;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Material;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Acervo;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Signatura;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Estado;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Nivel;
import mx.tecnm.orizaba.tp.biblioteca.modelo.ReglaNegocioException;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Prestamo;
import mx.tecnm.orizaba.tp.biblioteca.persistencia.ConexionSQLite;
import mx.tecnm.orizaba.tp.biblioteca.persistencia.Repositorio;
import mx.tecnm.orizaba.tp.biblioteca.persistencia.RepositorioSocioSQLite;
import mx.tecnm.orizaba.tp.biblioteca.persistencia.RepositorioMaterialSQLite;
import mx.tecnm.orizaba.tp.biblioteca.persistencia.RepositorioPrestamoSQLite;
import mx.tecnm.orizaba.tp.biblioteca.persistencia.Respaldo;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Fachada del dominio (capa de servicio). <strong>La UI sólo debe depender de esta clase</strong>
 * y de los tipos del paquete {@code modelo}/{@code eventos}; nunca de JDBC.
 * <ul>
 *   <li>Concurrencia: {@link MonitorVencimientos} es una clase interna que <em>es una</em>
 *       {@link TareaPeriodica} y <em>tiene acceso</em> a {@code Biblioteca.this}: herencia múltiple de
 *       implementación mediante clases internas (véase {@code J04_Multiple_De_Implementacion}).</li>
 *   <li>Programación funcional: reportes con streams y funciones de orden superior.</li>
 * </ul>
 */
public class Biblioteca implements AutoCloseable {

    /** Días tras los cuales un(a) préstamo en curso genera una alerta. */
    public static final int PLAZO_DIAS = 7;

    private final Acervo catalogo = new Acervo();
    private final Map<Integer, Socio> socios = new ConcurrentHashMap<>();
    private final Map<Integer, Prestamo> registros = new ConcurrentHashMap<>();
    private final Set<Integer> alertados = ConcurrentHashMap.newKeySet();
    private final List<OyenteEvento> oyentes = new CopyOnWriteArrayList<>();

    private final Repositorio<Material, Signatura> repoElementos;
    private final Repositorio<Socio, Integer> repoSocios;
    private final Repositorio<Prestamo, Integer> repoRegistros;
    private final Supplier<LocalDate> reloj;
    private MonitorVencimientos monitor;

    public Biblioteca(ConexionSQLite bd) {
        this(bd, LocalDate::now);
    }

    /**
     * Se crean los tres repositorios con las variables ya declaradas pasandoles la conexion a DB
     * se asigina la variable reloj a través del paranetro que recibe el método
     * */

    /** @param reloj proveedor de la fecha actual (inyectable para pruebas y simulación) */
    public Biblioteca(ConexionSQLite bd, Supplier<LocalDate> reloj) {
        // TODO E15: Crear los tres repositorios SQLite, asignar el reloj y cargar los datos existentes en memoria.
        this.repoElementos = new RepositorioMaterialSQLite(bd);
        this.repoSocios = new RepositorioSocioSQLite(bd);
        this.repoRegistros = new RepositorioPrestamoSQLite(bd, repoSocios, repoElementos);

        this.reloj = reloj;

        cargar();
    }

    /**
     * Usamos la refrencia al métodoa agregar a través de la instancia de catalogo
     * El método cambia en socio y registro ya que ambos son Map y necesitan guardar 2 cosas
     * */

    private void cargar() {
        // TODO E15: Usar referencias a método: repoElementos.todos().forEach(catalogo::agregar), etc.
        repoElementos.todos().forEach((catalogo::agregar));

        repoSocios.todos().forEach(socio -> socios.put(socio.id(),socio));

        repoRegistros.todos().forEach(prestamo -> registros.put(prestamo.id(), prestamo));
    }

    // ------------------------------------------------------------------ eventos

    public void suscribir(OyenteEvento oyente) {
        oyentes.add(oyente);
    }

    public void desuscribir(OyenteEvento oyente) {
        oyentes.remove(oyente);
    }

    private void publicar(TipoEvento tipo, String mensaje) {
        Evento e = Evento.de(tipo, mensaje);
        oyentes.forEach(o -> o.notificar(e));
    }

    // ------------------------------------------------------------------ catálogo

    /**
     * Creamos con la fabirca la plantilla para familias de elementos
     *guardamos el elemento en su repositorio
     * Se agrega al catalogo
     * Se publica a través de su método
     * */

    /**
     * Alta genérica de cualquier subtipo mediante una fábrica (función de orden superior).
     * Ejemplo: {@code registrar(Material.Libro::new, "...", 100.0, 420)}.
     */
    public <E extends Material, A> E registrar(FabricaElemento<E, A> fabrica, String nombre, double precio, A atributo) {
        // TODO E15: Crear con la fábrica, persistir, agregar al catálogo, publicar ALTA y devolver el elemento.
        E elemento = fabrica.crear(nombre,precio,atributo);

        repoElementos.guardar(elemento);
        catalogo.agregar(elemento);

        publicar(TipoEvento.ALTA, "Material registrado: " + elemento.clave());

        return elemento;
    }

    public void actualizar(Material elemento, String nombre, double precio) {
        elemento.setNombre(nombre);
        elemento.setPrecioBase(precio);
        repoElementos.guardar(elemento);
    }
    /***
     * Recuperamos un boleano que comprueba el estado a traves de un stream com FlatMap(permite transformar el map en un solo flujo)
     *donde la clave coincida con la clave deseada
     * Si devuelve verdadero, no se podría eliminar y se lanzará una excepción
     * Se eliminará solo si este se encuentra primero en el sistema
     */

    public boolean retirar(Signatura clave) {
        // TODO E15: Impedir retirar elementos referenciados por algún registro; eliminar de la BD y del catálogo.
        boolean referenciado = registros.values().stream().flatMap(prestamo -> prestamo.lineas().stream()).anyMatch(
                linea -> linea.elemento().clave().equals(clave));

        if (referenciado){
            throw new ReglaNegocioException("No se puede retirar un libro en prestamo");
        }

        boolean eliminado = repoElementos.eliminar(clave);
        if (eliminado){
            catalogo.retirar(clave);
        }

        return eliminado;
    }

    public Acervo catalogo() {
        return catalogo;
    }

    public Material elemento(String clave) {
        return catalogo.buscar(Signatura.parse(clave))
                .orElseThrow(() -> new ReglaNegocioException("No existe " + clave));
    }

    // ------------------------------------------------------------------ socios
    /**
     *Se evalua si el correo ya existe, de ser así se lanza un excepcion.
     * Aumenta el id utilizando un stream, con optinnal devuelve cero en caso de no haber registros
     * aumenta en 1 el id
     * Crea el objeto de socio y lo guarda en el repositorio
     * publica el alta del socio
     * */
    public Socio registrarSocio(String nombre, String correo, Nivel nivel) {
        // TODO E15: Rechazar correos repetidos; id = máximo actual + 1 (stream mapToInt/max); persistir y publicar ALTA.
        boolean repetido = socios.values().stream().anyMatch(s -> s.correo().equalsIgnoreCase(correo));

        if (repetido) {
            throw new ReglaNegocioException("Ya existe un socio con ese correo");
        }

        int id = socios.values().stream().mapToInt(Socio::id).max().orElse(0)+1;
        Socio socio = new Socio(id, nombre,correo,nivel);

        repoSocios.guardar(socio);
        socios.put(id,socio);
        publicar(TipoEvento.ALTA, "Socio registro" +socio.nombre());

        return socio;
    }

    public void cambiarNivel(int idSocio, Nivel nivel) {
        Socio a = socio(idSocio);
        a.setNivel(nivel);
        repoSocios.guardar(a);
    }

    public Socio socio(int id) {
        return Optional.ofNullable(socios.get(id))
                .orElseThrow(() -> new ReglaNegocioException("No existe el registro " + id));
    }

    public List<Socio> socios() {
        return socios.values().stream().sorted(Comparator.comparingInt(Socio::id)).toList();
    }

    // ------------------------------------------------------------------ préstamos
    /**
     * Se recupera el socio
     * Aumenta el id utilizando un stream, con optional devuelve cero en caso de no haber registros
     * aumenta en 1 el id
     * Crea el objeto de prestamo y lo guarda en el repositorio
     * publica el pretamo cambiando el estado.
     * */

    public Prestamo abrir(int idSocio) {
        // TODO E15: Crear el registro con la fecha del reloj, persistir, guardar en el mapa y publicar CAMBIO_ESTADO.
            Socio socio = socio(idSocio);

            int id = registros.values().stream().mapToInt(Prestamo::id).max().orElse(0)+1;

            Prestamo prestamo = new Prestamo(id, socio,reloj.get());

            repoRegistros.guardar(prestamo);
            registros.put(id,prestamo);

            publicar(TipoEvento.CAMBIO_ESTADO, "prestamo abierto" + id);

            return prestamo;
    }
    /**
     * Refcuperamos el prestamo y el material
     * Agregamos la linea
     * Guardamos el prestamo en el repositorio
     * */

    public Prestamo.Linea agregarLinea(int idPrestamo, String clave, int unidades) {
        // TODO E15: Localizar registro y elemento, agregar la línea y persistir.
        Prestamo prestamo = registro(idPrestamo);

        Material material = elemento(clave);

        Prestamo.Linea linea = prestamo.agregar(material, unidades);

        repoRegistros.guardar(prestamo);

        return linea;
    }

    public boolean quitarLinea(int idPrestamo, String clave) {
        Prestamo t = registro(idPrestamo);
        boolean ok = t.quitar(Signatura.parse(clave));
        repoRegistros.guardar(t);
        return ok;
    }

    public void iniciar(int idPrestamo) {
        transitar(idPrestamo, Prestamo::iniciar);
    }

    public void cerrar(int idPrestamo) {
        transitar(idPrestamo, Prestamo::cerrar);
    }

    public void cancelar(int idPrestamo) {
        transitar(idPrestamo, Prestamo::cancelar);
    }

    /**
     * Localizamos el prestamo
     * Posterior aplicamos la transicion recibida con accept del metodo de Consumer
     * guardamos en el repositorio
     * publicamos el prestamo cambiando el estado
     * */

    /** Orden superior: la transición concreta llega como {@link Consumer} (referencia a método). */
    private void transitar(int id, Consumer<Prestamo> transicion) {
        // TODO E15: Aplicar la transición, persistir y publicar CAMBIO_ESTADO.
        Prestamo prestamo = registro(id);

        transicion.accept(prestamo);

        repoRegistros.guardar(prestamo);

        publicar(TipoEvento.CAMBIO_ESTADO, "Prestamo" + id +"cambio a " + prestamo.estado());
    }

    public Prestamo registro(int id) {
        return Optional.ofNullable(registros.get(id))
                .orElseThrow(() -> new ReglaNegocioException("No existe el registro " + id));
    }

    public List<Prestamo> registros() {
        return registros.values().stream().sorted(Comparator.comparingInt(Prestamo::id)).toList();
    }

    public List<Prestamo> registrosEn(Estado estado) {
        return registros().stream().filter(t -> t.estado() == estado).toList();
    }

    // ------------------------------------------------------------------ reportes (streams)

    private Stream<Prestamo> cerrados() {
        return registros.values().stream().filter(t -> t.estado() == Estado.CERRADA);
    }

    /**
     * Utilizamos flatMap para agrupar las lineas de los prestamos
     * Agrupamos las lineas con su tipo de material utilizando groupingBy, Treemap pora ordenar el resultado
     * y utilizamos Collectors.summingDouble para sumar los importes
     * a los que accedemos a traves de la referencia al método de importe
     * */
    /** Ingresos por subtipo (sólo registros cerrados). */
    public Map<String, Double> ingresosPorTipo() {
        // TODO E16: flatMap de líneas; groupingBy(tipo, TreeMap::new, summingDouble(importe)).
        return cerrados().flatMap(p-> p.lineas().stream())
                .collect(Collectors.groupingBy(linea -> linea.elemento().tipo(), TreeMap::new, Collectors.summingDouble(Prestamo.Linea::importe)));

    }
    /**
     * Accedemos a las lineas de los métodos que ya han sido cerrados
     * Agrupamos las lineas por mnaterial y sumamos las unidades de estas
     * */
    /** El material con más unidades en registros cerrados. */
    public Optional<Material> masDemandado() {
        // TODO E16: groupingBy(elemento, summingInt(unidades)) y máximo por valor con Map.Entry.comparingByValue().
        return cerrados().flatMap(p->p.lineas().stream())
                .collect(Collectors.groupingBy(Prestamo.Linea::elemento, Collectors.summingInt(Prestamo.Linea::unidades)))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
    }
    /**
     * Recuperamos los cerrados y los agrupamos por socio
     * sumamos los totales
     * convertimos las entradas del map en un stream
     * utilizamos Map.Entry.<Socio, Double>comparingByValue() para ordenar de mayor a menor
     * y reversed() para hacer lo contrario
     * limitamos el numero de lineas segun el parametro ingresado
     *
     *
     * */
    /** Los {@code n} socios con mayor importe acumulado. */
    public List<Socio> mejores(int n) {
        // TODO E16: groupingBy(actor, summingDouble(total)); ordenar descendentemente por valor; limit(n).
        return cerrados().collect(Collectors.groupingBy(Prestamo::socio,Collectors.summingDouble(Prestamo::total)))
                .entrySet().stream().sorted(Map.Entry.<Socio,Double> comparingByValue().reversed() )
                .limit(n).map(Map.Entry::getKey).toList();
    }
    /**
     * Obtiene las estadisticas con summary a traves del stream
     *
     * */

    public DoubleSummaryStatistics estadisticas() {
        // TODO E16: mapToDouble(Prestamo::total).summaryStatistics() sobre los cerrados.
        return cerrados().mapToDouble(Prestamo::total).summaryStatistics();
    }
    /**
     * Usamos StreamSupport para poder recorrer el catalogo
     * recolectamos los datos utlizando la lambda proporcionada
     *
     * */
    public Map<Boolean, List<Material>> particionarPorPrecio(double umbral) {
        // TODO E16: partitioningBy(e -> e.precioBase() >= umbral).
        return StreamSupport.stream(catalogo.spliterator(),false)
                .collect(Collectors.partitioningBy(e->e.precioBase() >= umbral));
    }

    public Map<Estado, Long> conteoPorEstado() {
        return registros.values().stream()
                .collect(Collectors.groupingBy(Prestamo::estado, () -> new TreeMap<>(), Collectors.counting()));
    }

    public List<Material> buscar(Regla<? super Material> regla) {
        return catalogo.filtrar(regla);
    }

    /** Proyección genérica (orden superior). */
    /**
     * Recorre el catalogo y guarda los resultados por clave en ua lista
     * */
    public <R> List<R> proyectar(Function<? super Material, ? extends R> funcion) {
        // TODO E16: Recorrer el catálogo ordenado por clave y aplicar map(funcion).
        return catalogo.ordenados(Comparator.comparing(Material::clave)).stream().<R>map(funcion).toList();
    }

    /**
     *Se crea un switch para describir diferentes casos de la biblioteca
     * Se usa when para crear una condición en donde se encuentre un libro mayor a 500 páginas
     * */
    /** Descripción con <em>pattern matching</em> exhaustivo sobre la jerarquía sellada (sin default). */
    public String describir(Material e) {
        // TODO E16: switch (e) con un caso por subtipo y al menos un caso con guarda "when".
       return switch (e){
           case Material.Libro libro when libro.paginas() > 500 ->
               "Libro grande: %s (%d páginas)".formatted(
                       libro.nombre(), libro.paginas()
               );

           case Material.Libro libro-> "Libro: %s (%d páginas)"
                   .formatted(
                           libro.nombre(),
                           libro.paginas()
                   );
           case Material.Revista revista -> "Revista: %s (%d años)"
                   .formatted(
                           revista.nombre(),
                           revista.antiguedadAnios()
                   );

           case Material.Tesis tesis -> "Tesis: %s (%s)"
                   .formatted(
                           tesis.nombre(),
                           tesis.grado()
                   );
       };
    }

    // ------------------------------------------------------------------ monitor

    /**
     * Un ciclo de revisión (síncrono y probado de forma determinista):
     * cancela registros ABIERTOS sin líneas con al menos un día de antigüedad y emite una
     * ALERTA (una sola vez) por cada registro EN_CURSO con más de {@link #PLAZO_DIAS} días.
     *
     * @return número de acciones realizadas
     */
    public synchronized int revisar() {
        // TODO E12: Recorrer los registros y aplicar las dos reglas con un switch sobre el estado.
        LocalDate hoy = reloj.get();
        int acciones = 0;

        for (Prestamo prestamo : registros.values()){
            long dias = prestamo.diasDesde(hoy);

            switch (prestamo.estado()){

                case ABIERTA -> {
                    if (prestamo.lineas().isEmpty() && dias >= 1) {
                        prestamo.cancelar();
                        repoRegistros.guardar(prestamo);

                        publicar(TipoEvento.CANCELACION_AUTOMATICA, "Prestamo cancelado");

                        acciones++;
                    }
                }
                case EN_CURSO -> {
                    if (dias > PLAZO_DIAS && alertados.add(prestamo.id())){
                        publicar(TipoEvento.ALERTA, "Prestamo vencido");
                        acciones++;
                    }
                }
                case CERRADA, CANCELADA ->{}


            }
        }

        return acciones;

    }

    /**
     * Se crea el nuevo monitor interno y se inicia con el método iniciar.
     * */

    public synchronized void iniciarMonitor(long periodoMs) {
        // TODO E12: Crear el monitor interno (new MonitorVencimientos(periodoMs)) e iniciarlo con un nombre de hilo descriptivo.
       this.monitor = new MonitorVencimientos(periodoMs);
       this.monitor.iniciar("Monitor vencimientos");
    }

    public void detenerMonitor() throws InterruptedException {
        MonitorVencimientos m;
        synchronized (this) {
            m = monitor;
        }
        if (m != null) {
            m.detener();
        }
    }

    public synchronized boolean monitorActivo() {
        return monitor != null && monitor.activa();
    }

    /**
     * Clase interna: ES UNA {@link TareaPeriodica} y TIENE ACCESO a la envolvente.
     */
    public final class MonitorVencimientos extends TareaPeriodica {

        private MonitorVencimientos(long periodoMs) {
            super(periodoMs);
        }

        @Override
        protected void ejecutarCiclo() {
            // TODO E12: Delegar en la envolvente: Biblioteca.this.revisar().
            Biblioteca.this.revisar();
        }
    }

    // ------------------------------------------------------------------ respaldo

    public void exportarRespaldo(Path archivo) throws IOException {
        Respaldo.exportar(archivo, catalogo, socios(), registros());
    }

    /** Detiene el monitor (si está activo). Invocar al cerrar la aplicación. */
    @Override
    public void close() {
        try {
            detenerMonitor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
