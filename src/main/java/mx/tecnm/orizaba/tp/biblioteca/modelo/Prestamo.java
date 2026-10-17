package mx.tecnm.orizaba.tp.biblioteca.modelo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Prestamo: agregado con ciclo de vida ({@link Estado}) compuesto por líneas.
 * <ul>
 *   <li>Composición: {@link Linea} es una clase <strong>interna</strong> (no estática) con constructor
 *       privado; sólo existe dentro de un(a) préstamo y accede a su envolvente con
 *       {@code Prestamo.this} (véase {@code java_3_tipos_internos/t1_fundamentos/Parcel01..04}).</li>
 *   <li>Concurrencia: los métodos que cambian estado están sincronizados porque el monitor
 *       los consulta desde otro hilo.</li>
 *   <li>Persistencia: serializar un objeto de clase interna exige que la envolvente sea serializable
 *       (véase {@code SerializaClasesInternas_08}).</li>
 * </ul>
 */
public class Prestamo implements Iterable<Prestamo.Linea>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final AtomicInteger SECUENCIA = new AtomicInteger();

    private final int id;
    private final Socio socio;
    private final LocalDate fecha;
    private Estado estado;
    private final List<Linea> lineas = new ArrayList<>();

    {
        estado = Estado.ABIERTA;
    }

    /** Nuevo(a) préstamo con identificador consecutivo y fecha de hoy. */
    public Prestamo(Socio socio) {
        this(SECUENCIA.incrementAndGet(), socio, LocalDate.now());
    }

    /** Constructor completo (también usado al reconstruir desde la base de datos). */
    /**
     * Validamos que el id sea mayor que 0
     * asignamos id
     * asignamos fecha y socio comprobando que no sean nulos con el método de la clase Object*/
    public Prestamo(int id, Socio socio, LocalDate fecha) {
        // TODO E07: Validar id > 0, actor y fecha no nulos; asignar y sincronizar SECUENCIA con accumulateAndGet(id, Math::max).
        if(id < 0){
            throw new ReglaNegocioException("Id debe ser mayor a 0");
        }

        this.id = id;
        this.fecha = Objects.requireNonNull(fecha, "fecha");
        this.socio = Objects.requireNonNull(socio, "socio");

        SECUENCIA.accumulateAndGet(id, Math::max);
    }

    public int id() {
        return id;
    }

    public Socio socio() {
        return socio;
    }

    public LocalDate fecha() {
        return fecha;
    }

    public synchronized Estado estado() {
        return estado;
    }

    /** Agrega una línea; sólo es posible mientras está ABIERTA y sin repetir material. */
    /**
     * Comprobamos que la transaccion sea abierta de lo contrario se devuelve una execpcion
     * Guardamos el resultado (t o f) de la comparacion del stream anyMatch(Que comprueba que exista un elemento igual)
     * en caso de que existan coincidencias mandamos error
     * Se crea una unueva linea con los parametros recibidos en el metodo y se agrega al arreglo de lineas*/

    public synchronized Linea agregar(Material elemento, int unidades) {
        // TODO E07: Exigir estado ABIERTA, rechazar elementos repetidos (stream anyMatch), crear la Linea interna y añadirla.
        if (estado != Estado.ABIERTA){
            throw new ReglaNegocioException("Se necesita estado Abierta");
        }

        boolean repetido = lineas.stream().anyMatch( linea -> linea.elemento().clave().equals(elemento.clave()));

        if(repetido){
            throw new ReglaNegocioException("El material ya ha sido agregado");
        }

        Linea linea = new Linea(elemento,unidades);
        lineas.add(linea);

        return linea;
    }

    /** Quita la línea del material indicado (sólo en estado ABIERTA). */
    /**
     * Comprobamos que la transaccion sea abierta de lo contrario se devuelve una execpcion
     * Usamos removeIf sobre la clave de la linea para remover la que coincida con la misma.
     *
     * */
    public synchronized boolean quitar(Signatura clave) {
        // TODO E07: Exigir estado ABIERTA y eliminar con removeIf la línea cuyo elemento tenga esa clave.
        if (estado != Estado.ABIERTA){
            throw new ReglaNegocioException("Se necesita estado Abierta");
        }

        return lineas.removeIf( linea -> linea.elemento().clave().equals(clave));
    }

    /**
     * Comprobamos que se permita la transaccion de lo contrario se devuelve una execpcion
     * Comprobamos que la transaccion corresponda a en curso y que no este vacia
     * */
    public synchronized void cambiarEstado(Estado destino) {
        // TODO E07: Validar la transición con permiteTransicionA y exigir al menos una línea para pasar a EN_CURSO.
        if (!estado.permiteTransicionA(destino)){
            throw new ReglaNegocioException("Transaccion de estado no permitida");
        }

        if (destino == Estado.EN_CURSO && lineas.isEmpty()){
            throw new ReglaNegocioException("Se debe de contener al menos una línea");
        }

        estado = destino;
    }

    public void iniciar() {
        cambiarEstado(Estado.EN_CURSO);
    }

    public void cerrar() {
        cambiarEstado(Estado.CERRADA);
    }

    public void cancelar() {
        cambiarEstado(Estado.CANCELADA);
    }

    /** Uso exclusivo de la capa de persistencia al reconstruir el objeto. */
    public synchronized void restaurarEstado(Estado estado) {
        this.estado = Objects.requireNonNull(estado);
    }

    public synchronized List<Linea> lineas() {
        return Collections.unmodifiableList(new ArrayList<>(lineas));
    }

    /** Suma de subtotales sin descuento. */
    /**
     * Con el uso de streams, hacemos uso del mapeo para sumar cada linea del arreglo de Linea.
     * Hacemos referncia al método subtotal (que nos devuelve el total antes del descuento) de Linea
     * Redondeamos el total con el metodo redonder que se encuentra en esta clase
     * */
    public synchronized double subtotal() {
        // TODO E08: Sumar los subtotales de las líneas con mapToDouble y redondear a 2 decimales.
        double subtotal = lineas.stream().mapToDouble(Linea::subtotal).sum();

        return redondear(subtotal);
    }

    /** Total con el descuento del nivel del socio. */

    /**
     * Con el uso de streams, hacemos uso del mapeo para sumar cada linea del arreglo de Linea.
     * Hacemos referncia al método importe (que calcula el descuento de socio) de Linea
     * Redondeamos el total con el metodo redonder que se encuentra en esta clase
     * */
    public synchronized double total() {
        // TODO E08: Sumar los importes de las líneas (ya con descuento) y redondear a 2 decimales.
        double total = lineas.stream().mapToDouble(Linea::importe).sum();

        return redondear(total);
    }

    public long diasDesde(LocalDate hoy) {
        return ChronoUnit.DAYS.between(fecha, hoy);
    }

    @Override
    public Iterator<Linea> iterator() {
        return lineas().iterator();
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return String.format("Prestamo #%d [%s] %s, %d línea(s), total $%.2f",
                id, estado(), socio.nombre(), lineas().size(), total());
    }

    /**
     * Línea de préstamo: clase interna (composición). Su constructor es privado, de modo que
     * sólo la envolvente puede crearla; accede al estado de la envolvente con {@code Prestamo.this}.
     */
    public final class Linea implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final Material elemento;
        private final int unidades;


        /**
         * Evaluamos el que las unidades sean mayor a 0
         * Asignamos elementos (en el cual comprobamos que que no sea null)
         * Asignamos unidades*/
        private Linea(Material elemento, int unidades) {
            // TODO E07: Validar unidades > 0 y asignar los campos.
            if (unidades <= 0) {
                throw new ReglaNegocioException(
                        "Las unidades deben ser positivas"
                );
            }

            this.elemento = Objects.requireNonNull(elemento, "elemento");
            this.unidades = unidades;
        }

        public Material elemento() {
            return elemento;
        }

        public int unidades() {
            return unidades;
        }

        public double subtotal() {
            return elemento.tarifa(unidades);
        }


        /**
         * Usamos this para llamar al presatamo, obtenemos la variable sicio, el método nivel
         * y el método aplicar, en el cual pasamos el subtotal (el cual calcula el subtotal antes de aplicar el descuento)
         *
         * */
        /** Importe con el descuento del nivel del socio de la envolvente. */
        public double importe() {
            // TODO E08: Usar Prestamo.this.socio.nivel().aplicar(subtotal()).
            return Prestamo.this.socio.nivel().aplicar(subtotal());
        }

        public Prestamo contenedor() {
            return Prestamo.this;
        }

        @Override
        public String toString() {
            return String.format("%s x %d días = $%.2f", elemento.nombre(), unidades, importe());
        }
    }
}
