package mx.tecnm.orizaba.tp.biblioteca.modelo;

import java.util.Comparator;

/**
 * Abstracción raíz del dominio: todo lo que puede prestarse expone una clave,
 * un nombre, un precio base y una tarifa por unidad (días).
 * <p>Abstracción (Booch): define el <em>qué</em>, no el <em>cómo</em>
 * (véase {@code java_4_modelo/t1_abstraccion/Abstraccion03_interfaces}).</p>
 */
public interface Prestable {

    /** Constante de interfaz: implícitamente {@code public static final}. */
    double TASA_IMPUESTO = 0.16;

    Signatura clave();

    String nombre();

    double precioBase();

    /** Tarifa (sin impuestos) para la cantidad de días indicada. */
    double tarifa(int unidades);


    /**
     * Calcula el impuesto sumado uno al mismo (para obtener la operación multiplicada por 1.16)
     * */
    /** Método por defecto (extensión de la interfaz sin romper implementaciones). */
    default double tarifaConImpuesto(int unidades) {
        // TODO E02: Aplicar TASA_IMPUESTO a tarifa(unidades) y redondear a 2 decimales.
        double total =
                tarifa(unidades) * (1 + TASA_IMPUESTO);

        return Math.round(total * 100.0) / 100.0;
    }
    /**
     * Utiliza el método estático de comparing de la clase comparator y accede al nombre con la referencia de método
     * de Prestable
     * Indica que se ignore si es mayúscula o minúscula a la hora de ordenar
     * */
    /** Método estático de interfaz: comparador por nombre (referencia a método de objeto arbitrario). */
    static Comparator<Prestable> porNombre() {
        // TODO E02: Usar Comparator.comparing(Prestable::nombre, String.CASE_INSENSITIVE_ORDER).
        return Comparator.comparing(Prestable::nombre, String.CASE_INSENSITIVE_ORDER);
    }
}
