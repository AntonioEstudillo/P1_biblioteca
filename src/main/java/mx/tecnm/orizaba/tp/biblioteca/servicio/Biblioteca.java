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

    /** @param reloj proveedor de la fecha actual (inyectable para pruebas y simulación) */
    public Biblioteca(ConexionSQLite bd, Supplier<LocalDate> reloj) {
        // TODO E15: Crear los tres repositorios SQLite, asignar el reloj y cargar los datos existentes en memoria.
        throw new UnsupportedOperationException("TODO E15");
    }

    private void cargar() {
        // TODO E15: Usar referencias a método: repoElementos.todos().forEach(catalogo::agregar), etc.
        throw new UnsupportedOperationException("TODO E15");
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
     * Alta genérica de cualquier subtipo mediante una fábrica (función de orden superior).
     * Ejemplo: {@code registrar(Material.Libro::new, "...", 100.0, 420)}.
     */
    public <E extends Material, A> E registrar(FabricaElemento<E, A> fabrica, String nombre, double precio, A atributo) {
        // TODO E15: Crear con la fábrica, persistir, agregar al catálogo, publicar ALTA y devolver el elemento.
        throw new UnsupportedOperationException("TODO E15");
    }

    public void actualizar(Material elemento, String nombre, double precio) {
        elemento.setNombre(nombre);
        elemento.setPrecioBase(precio);
        repoElementos.guardar(elemento);
    }

    public boolean retirar(Signatura clave) {
        // TODO E15: Impedir retirar elementos referenciados por algún registro; eliminar de la BD y del catálogo.
        throw new UnsupportedOperationException("TODO E15");
    }

    public Acervo catalogo() {
        return catalogo;
    }

    public Material elemento(String clave) {
        return catalogo.buscar(Signatura.parse(clave))
                .orElseThrow(() -> new ReglaNegocioException("No existe " + clave));
    }

    // ------------------------------------------------------------------ socios

    public Socio registrarSocio(String nombre, String correo, Nivel nivel) {
        // TODO E15: Rechazar correos repetidos; id = máximo actual + 1 (stream mapToInt/max); persistir y publicar ALTA.
        throw new UnsupportedOperationException("TODO E15");
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

    public Prestamo abrir(int idSocio) {
        // TODO E15: Crear el registro con la fecha del reloj, persistir, guardar en el mapa y publicar CAMBIO_ESTADO.
        throw new UnsupportedOperationException("TODO E15");
    }

    public Prestamo.Linea agregarLinea(int idPrestamo, String clave, int unidades) {
        // TODO E15: Localizar registro y elemento, agregar la línea y persistir.
        throw new UnsupportedOperationException("TODO E15");
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

    /** Orden superior: la transición concreta llega como {@link Consumer} (referencia a método). */
    private void transitar(int id, Consumer<Prestamo> transicion) {
        // TODO E15: Aplicar la transición, persistir y publicar CAMBIO_ESTADO.
        throw new UnsupportedOperationException("TODO E15");
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

    /** Ingresos por subtipo (sólo registros cerrados). */
    public Map<String, Double> ingresosPorTipo() {
        // TODO E16: flatMap de líneas; groupingBy(tipo, TreeMap::new, summingDouble(importe)).
        throw new UnsupportedOperationException("TODO E16");
    }

    /** El material con más unidades en registros cerrados. */
    public Optional<Material> masDemandado() {
        // TODO E16: groupingBy(elemento, summingInt(unidades)) y máximo por valor con Map.Entry.comparingByValue().
        throw new UnsupportedOperationException("TODO E16");
    }

    /** Los {@code n} socios con mayor importe acumulado. */
    public List<Socio> mejores(int n) {
        // TODO E16: groupingBy(actor, summingDouble(total)); ordenar descendentemente por valor; limit(n).
        throw new UnsupportedOperationException("TODO E16");
    }

    public DoubleSummaryStatistics estadisticas() {
        // TODO E16: mapToDouble(Prestamo::total).summaryStatistics() sobre los cerrados.
        throw new UnsupportedOperationException("TODO E16");
    }

    public Map<Boolean, List<Material>> particionarPorPrecio(double umbral) {
        // TODO E16: partitioningBy(e -> e.precioBase() >= umbral).
        throw new UnsupportedOperationException("TODO E16");
    }

    public Map<Estado, Long> conteoPorEstado() {
        return registros.values().stream()
                .collect(Collectors.groupingBy(Prestamo::estado, () -> new TreeMap<>(), Collectors.counting()));
    }

    public List<Material> buscar(Regla<? super Material> regla) {
        return catalogo.filtrar(regla);
    }

    /** Proyección genérica (orden superior). */
    public <R> List<R> proyectar(Function<? super Material, ? extends R> funcion) {
        // TODO E16: Recorrer el catálogo ordenado por clave y aplicar map(funcion).
        throw new UnsupportedOperationException("TODO E16");
    }

    /** Descripción con <em>pattern matching</em> exhaustivo sobre la jerarquía sellada (sin default). */
    public String describir(Material e) {
        // TODO E16: switch (e) con un caso por subtipo y al menos un caso con guarda "when".
        throw new UnsupportedOperationException("TODO E16");
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
        throw new UnsupportedOperationException("TODO E12");
    }

    public synchronized void iniciarMonitor(long periodoMs) {
        // TODO E12: Crear el monitor interno (new MonitorVencimientos(periodoMs)) e iniciarlo con un nombre de hilo descriptivo.
        throw new UnsupportedOperationException("TODO E12");
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
            throw new UnsupportedOperationException("TODO E12");
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
