package mx.tecnm.orizaba.tp.biblioteca.modelo;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Objeto valor inmutable que identifica a un material (formato {@code ABC-0001}).
 * <ul>
 *   <li>Encapsulamiento: {@code record} con constructor compacto que valida invariantes.</li>
 *   <li>Proceso: bloque de inicialización estática (véase {@code java_2_proceso/Proceso03}).</li>
 *   <li>Concurrencia: secuencias atómicas seguras entre hilos.</li>
 * </ul>
 */
public record Signatura(String prefijo, int numero) implements Comparable<Signatura>, Serializable {

    private static final Map<String, AtomicInteger> SECUENCIAS;

    static {
        // Inicialización estática: se ejecuta una sola vez, al primer uso activo de la clase.
        SECUENCIAS = new ConcurrentHashMap<>();
    }
    /**
     * Válida que no sea nulo
     * Válida con el método matches que el formato sea igual que el esperado.
     * Válida que el número ingresado sea mayor a 0
     * */
    public Signatura {
        // TODO E03: Validar que el prefijo tenga exactamente 3 letras mayúsculas y que el número sea positivo (IllegalArgumentException).
        if(prefijo == null || !prefijo.matches("[A-Z]{3}")){
            throw new IllegalArgumentException("Formato no valido");
        }

        if(numero <= 0){
            throw new IllegalArgumentException("Formato no valido");
        }

    }

    /** Genera la siguiente clave del prefijo indicado (operación atómica). */

    /**
     * Comprobamos con computeIfAbsent que aún no exista el prefijo, de exixtir devuelve AtomicInteger que ya existe, de lo contrario lo crea
     *aplicamos el método incrementAndGet para aumentar el uno y obtner el número que se incremento
     * */
    public static Signatura siguiente(String prefijo) {
        // TODO E03: Obtener (o crear) la secuencia del prefijo con computeIfAbsent e incrementarla atómicamente.
       AtomicInteger secuencia = SECUENCIAS.computeIfAbsent(prefijo, p->new AtomicInteger(0));

       int numero = secuencia.incrementAndGet();

       return new Signatura(prefijo, numero);
    }

    /**
     * Comprobamos con computeIfAbsent que aún no exista el prefijo, de exixtir devuelve AtomicInteger que ya existe, de lo contrario lo crea
     *aplicamos acumulateAndGet con la clave del número para acumular el número actual y con la referencia la método obtenemos el número mayor.
     * */
    /** Ajusta la secuencia para que nunca genere una clave ya existente (p. ej., tras leer la BD). */
    public static void registrar(Signatura clave) {
        // TODO E03: Actualizar la secuencia del prefijo con accumulateAndGet(numero, Math::max).
        AtomicInteger secuencia = SECUENCIAS.computeIfAbsent(clave.prefijo, p->new AtomicInteger(0));

        secuencia.accumulateAndGet(clave.numero(), Math::max);
    }

    /**
     * Válida que no sea nulo
     * Válida el formato para que coincida con la expresión dada
     * Divide el prefijo y el número convirtiendo este de String a Int
     * */
    /** Convierte un texto {@code ABC-0001} en una clave. */
    public static Signatura parse(String texto) {
        // TODO E03: Validar el formato con la expresión [A-Z]{3}-\d{4,} y construir la clave.
        if (texto == null || !texto.matches("[A-Z]{3}-\\d{4,}")){
            throw new IllegalArgumentException("Formato Invalido");
        }

        String prefijo = texto.substring(0,3);
        int numero = Integer.parseInt(texto.substring(4));

        return new Signatura(prefijo, numero);
    }
    /**
     * Compara tanto el prefijo como el número con otro
     * Si los prefijos coinciden, compara por número
     * */
    @Override
    public int compareTo(Signatura otra) {
        // TODO E03: Ordenar primero por prefijo y después por número.
        int comparacionPrefijo = prefijo.compareTo(otra.prefijo);

        if (comparacionPrefijo != 0){
            return comparacionPrefijo;
        }

        return Integer.compare(numero, otra.numero());
    }

    @Override
    public String toString() {
        return String.format("%s-%04d", prefijo, numero);
    }
}
