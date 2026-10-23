package mx.tecnm.orizaba.tp.biblioteca.modelo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase abstracta <strong>sellada</strong> que factoriza el estado y el comportamiento común de todo
 * material. Sus tres subtipos se declaran como clases anidadas estáticas finales
 * (véase {@code java_4_modelo/t1_abstraccion/Abstraccion04_anidadas}).
 * <ul>
 *   <li>Abstracción + Jerarquía: patrón <em>Template Method</em> ({@link #tarifa(int)} es final y delega en
 *       {@link #calcularTarifa(int)}).</li>
 *   <li>Proceso: bloque estático, bloque de instancia y encadenamiento {@code this(...)}/{@code super(...)}
 *       (véase {@code java_2_proceso/Proceso03}, {@code Proceso09}).</li>
 *   <li>Persistencia: {@code Serializable} con un campo {@code transient}
 *       (véase {@code java_4_modelo/t7_persistencia/a_serializacion/NoSerializar_09}).</li>
 * </ul>
 */
public abstract sealed class Material implements Prestable, Serializable
        permits Material.Libro, Material.Revista, Material.Tesis {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final AtomicInteger INSTANCIAS_CREADAS;

    static {
        INSTANCIAS_CREADAS = new AtomicInteger();
    }

    private final Signatura clave;
    private String nombre;
    private double precioBase;
    private final LocalDateTime creadoEn;
    /** No se serializa: tras recuperar un respaldo vuelve a 0. */
    private transient int cotizaciones;

    {
        // Bloque de inicialización de instancia: se ejecuta antes del cuerpo de cualquier constructor.
        creadoEn = LocalDateTime.now();
        INSTANCIAS_CREADAS.incrementAndGet();
    }

    /** Constructor para objetos nuevos: genera la clave y encadena con {@code this(...)}. */
    protected Material(String prefijo, String nombre, double precioBase) {
        this(Signatura.siguiente(prefijo), nombre, precioBase);
    }

    /**
     * Válida que no sea nulo con el método requireNotNull de Object
     * Registra la clave a traves del método estático registra de Signature
     * Asigna nombre y precio base a traves de sus setters.
     * */
    /** Constructor para objetos reconstruidos (persistencia) con clave conocida. */
    protected Material(Signatura clave, String nombre, double precioBase) {
        // TODO E04: Asignar la clave (no nula), registrarla en Signatura.registrar y validar nombre y precio mediante los setters.
            this.clave = Objects.requireNonNull(clave, "clave");

            Signatura.registrar(clave);

            setNombre(nombre);
            setPrecioBase(precioBase);
    }

    public static int instanciasCreadas() {
        return INSTANCIAS_CREADAS.get();
    }

    @Override
    public final Signatura clave() {
        return clave;
    }

    @Override
    public final String nombre() {
        return nombre;
    }

    @Override
    public final double precioBase() {
        return precioBase;
    }

    public final LocalDateTime creadoEn() {
        return creadoEn;
    }

    public final int cotizaciones() {
        return cotizaciones;
    }

    /**
     * Válida que el nombre no sea nulo y que no sea un espacio en blanco
     * asigna el nombre recortando los espacios
     * */
    public final void setNombre(String nombre) {
        // TODO E04: Rechazar nombres nulos o en blanco (ReglaNegocioException); guardar el nombre sin espacios extremos.
        if (nombre == null || nombre.isBlank()){
            throw new ReglaNegocioException("Nombre no valido");
        }

        this.nombre = nombre.trim();
    }


    /**
     * Valida que sea mayor a 0
     * con el método isNAN de Double, validad que no sea NaN
     * Valida que no sea infinito con el método isInfinite de Double
     * De no cumplir una de los predicados devuelve una exception
     * asigna el precio base*/
    public final void setPrecioBase(double precioBase) {
        // TODO E04: Rechazar precios negativos, NaN o infinitos (ReglaNegocioException).
        if(precioBase < 0 || Double.isNaN(precioBase) || Double.isInfinite(precioBase)){
            throw new ReglaNegocioException("Precio base no valido");
        }

        this.precioBase = precioBase;
    }

    /** Método plantilla: valida, contabiliza y redondea; la variación la aporta cada subtipo. */

    /**
     * Válidad que las unidades sean mayor a 0
     * Incrementa en uno las cotizaciones
     * calcula la tarifa atraves del método calcularTarifa
     * Redondea con el método round (al redondear a el entero más cercano se multiplica primero por 100
     * para correr los decimales, posterior se divide entre 100 para ajustar el punto.)
     * */
    @Override
    public final double tarifa(int unidades) {
        // TODO E04: Validar unidades > 0 (ReglaNegocioException), incrementar cotizaciones y devolver calcularTarifa redondeada a 2 decimales.
        if(unidades <= 0){
            throw  new ReglaNegocioException("Unidades deben de ser mayor a 0");
        }

        cotizaciones++;

        double tarifaCalculada = calcularTarifa(unidades);

        return Math.round(tarifaCalculada*100)/100;
    }

    /** Punto de variación del Template Method. */
    protected abstract double calcularTarifa(int unidades);

    /** Copia con clave nueva; los subtipos la redefinen con <strong>retorno covariante</strong>. */
    public abstract Material copiar();

    /** Nombre corto del subtipo (útil para la UI y la persistencia). */
    public final String tipo() {
        return getClass().getSimpleName();
    }

    @Override
    public final boolean equals(Object o) {
        return this == o || (o instanceof Material otro && clave.equals(otro.clave));
    }

    @Override
    public final int hashCode() {
        return clave.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s[%s, %s, $%.2f]", tipo(), clave, nombre, precioBase);
    }

    /**
     * Libro: cuota lineal por día.
     * <p>Subtipo de Material declarado como clase anidada estática final; implementa Digitalizable.</p>
     */
    public static final class Libro extends Material implements Digitalizable {
        @Serial
        private static final long serialVersionUID = 1L;

        public static final String PREFIJO = "LIB";

        private final int paginas;

        public Libro(String nombre, double precioBase, int paginas) {
            super(PREFIJO, nombre, precioBase);
            this.paginas = validar(paginas);
        }

        public Libro(Signatura clave, String nombre, double precioBase, int paginas) {
            super(clave, nombre, precioBase);
            this.paginas = validar(paginas);
        }
        /**
         *Válida que las páginas sean mayor a 0
         */

        private static int validar(int paginas) {
            // TODO E05: Validar: paginas > 0 (si no se cumple: ReglaNegocioException "El número de páginas debe ser positivo").
            if(paginas <= 0){
                throw new ReglaNegocioException("El número de páginas debe ser positivo");
            }

            return paginas;
        }

        public int paginas() {
            return paginas;
        }

        /**
         * Realiza y retorna el cálculo de la cuota con la fórmula dada.
         */
        @Override
        protected double calcularTarifa(int unidades) {
            // TODO E05: Libro: cuota lineal por día. Fórmula: precioBase() * unidades
           double cuotaLinealDias = precioBase()*unidades;

           return cuotaLinealDias;
        }

        /** Retorno covariante: devuelve Libro, no Material. */

        /**
         * Retorna un objeto de Revista tomando los datos que ya se tienen
         * */
        @Override
        public Libro copiar() {
            // TODO E05: Crear un nuevo Libro con el mismo nombre, precio y atributo (clave nueva).
            return new Libro(
                nombre(),
                precioBase(),
                paginas
            );
        }

        @Override
        public String urlRepositorio() {
            return "https://repositorio.orizaba.tecnm.mx/" + clave();
        }

        @Override
        public String toString() {
            return super.toString() + " {páginas=" + paginas + "}";
        }
    }

    /**
     * Revista: 50 % de descuento si tiene más de 5 años.
     * <p>Subtipo de Material declarado como clase anidada estática final; implementa Restringido.</p>
     */
    public static final class Revista extends Material implements Restringido {
        @Serial
        private static final long serialVersionUID = 1L;

        public static final String PREFIJO = "REV";

        private final int antiguedadAnios;

        public Revista(String nombre, double precioBase, int antiguedadAnios) {
            super(PREFIJO, nombre, precioBase);
            this.antiguedadAnios = validar(antiguedadAnios);
        }

        public Revista(Signatura clave, String nombre, double precioBase, int antiguedadAnios) {
            super(clave, nombre, precioBase);
            this.antiguedadAnios = validar(antiguedadAnios);
        }
        /**
         *Válida que la antiguedad sea mayor a 0
         */
        private static int validar(int antiguedadAnios) {
            // TODO E05: Validar: antiguedadAnios >= 0 (si no se cumple: ReglaNegocioException "La antigüedad no puede ser negativa").
           if(antiguedadAnios < 0){
               throw new ReglaNegocioException("La antigüedad no puede ser negativa");
           }

           return antiguedadAnios;
        }

        public int antiguedadAnios() {
            return antiguedadAnios;
        }

        /**
         * Calcula la tarifa a través de un operador ternario, en el cual evalúa las unidades
         * para determinar si multiplicar por 1 o por 0.5 dependiendo de los años.
         * */
        @Override
        protected double calcularTarifa(int unidades) {
            // TODO E05: Revista: 50 % de descuento si tiene más de 5 años. Fórmula: precioBase() * unidades * (antiguedadAnios > 5 ? 0.5 : 1.0)
            double tarifaCalculada = precioBase()*unidades*(antiguedadAnios > 5 ? 0.5 : 1.0);

            return tarifaCalculada;
        }

        /** Retorno covariante: devuelve Revista, no Material. */

        /**
         * Retorna un objeto de Revista tomando los datos que ya se tienen
         * */
        @Override
        public Revista copiar() {
            // TODO E05: Crear un nuevo Revista con el mismo nombre, precio y atributo (clave nueva).
            return new Revista(
                    nombre(),
                    precioBase(),
                    antiguedadAnios
            );
        }

        @Override
        public int nivelAcceso() {
            return antiguedadAnios > 20 ? 3 : 1;
        }

        @Override
        public String toString() {
            return super.toString() + " {antigüedad (años)=" + antiguedadAnios + "}";
        }
    }

    /**
     * Tesis: los primeros 3 días son gratuitos.
     * <p>Subtipo de Material declarado como clase anidada estática final; implementa Digitalizable, Restringido.</p>
     */
    public static final class Tesis extends Material implements Digitalizable, Restringido {
        @Serial
        private static final long serialVersionUID = 1L;

        public static final String PREFIJO = "TES";

        private final String grado;

        public Tesis(String nombre, double precioBase, String grado) {
            super(PREFIJO, nombre, precioBase);
            this.grado = validar(grado);
        }

        public Tesis(Signatura clave, String nombre, double precioBase, String grado) {
            super(clave, nombre, precioBase);
            this.grado = validar(grado);
        }
        /**
         * Válida que no se envíen vacíos ni espacios en blanco
         * Recorta los espacios en blanco del retorno
         * */
        private static String validar(String grado) {
            // TODO E05: Validar: grado != null && !grado.isBlank() (si no se cumple: ReglaNegocioException "El grado académico es obligatorio").
            if (grado == null || grado.isBlank()){
                throw new ReglaNegocioException("El grado académico es obligatorio");
            }

            return grado.trim();
        }

        public String grado() {
            return grado;
        }

        /**
         * Calcula la tarifa a través de un operador ternario, en el cual evalúa las unidades
         * para determinar si se deja el valor en 0 o hace un descuento de 3 sobre el total de las unidades
         *
         * */

        @Override
        protected double calcularTarifa(int unidades) {
            // TODO E05: Tesis: los primeros 3 días son gratuitos. Fórmula: unidades <= 3 ? 0.0 : precioBase() * (unidades - 3)
           double tarifaCalculada = unidades <= 3 ? 0.0 : precioBase() * (unidades - 3);

           return tarifaCalculada;
        }

        /** Retorno covariante: devuelve Tesis, no Material. */
        /**
         * Retorna un objeto de Tesis tomando los datos que ya se tienen
         * */
        @Override
        public Tesis copiar() {
            // TODO E05: Crear un nuevo Tesis con el mismo nombre, precio y atributo (clave nueva).
            return new Tesis(
                    nombre(),
                    precioBase(),
                    grado
            );
        }

        @Override
        public String urlRepositorio() {
            return "https://repositorio.orizaba.tecnm.mx/tesis/" + clave();
        }

        @Override
        public int nivelAcceso() {
            return 3;
        }

        /** Resolución explícita de la colisión entre métodos por defecto. */
        /**
         * Sobreescribe el método para evitar la colisión devolviendo una cadena con lo que esta en el método de ambas clase padre
         *
         * */
        @Override
        public String descripcionCorta() {
            // TODO E05: Combinar Digitalizable.super.descripcionCorta() y Restringido.super.descripcionCorta() separados por " | ".
            return Digitalizable.super.descripcionCorta() + " | " + Restringido.super.descripcionCorta();
        }

        @Override
        public String toString() {
            return super.toString() + " {grado=" + grado + "}";
        }
    }
}
