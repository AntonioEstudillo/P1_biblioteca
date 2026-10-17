package mx.tecnm.orizaba.tp.biblioteca.funcional;

import mx.tecnm.orizaba.tp.biblioteca.modelo.Material;

/**
 * Biblioteca de reglas reutilizables (clase utilitaria no instanciable).
 */
public final class Reglas {

    private Reglas() {
        throw new AssertionError("No instanciable");
    }
    /**
     * Se crea la lambda que permite evaluar si el material que se le pasa son menores o iguales al maximo
     * */
    public static Regla<Material> precioHasta(double maximo) {
        // TODO E11: Lambda que acepta los elementos cuyo precioBase() sea menor o igual que maximo.
        return material -> material.precioBase() <= maximo;
    }
    /**
     * Pasamos el nombre a minúscula y pasamos la variable texto del método a minuscula a la hora de ser evaluada
     * */
    public static Regla<Material> nombreContiene(String texto) {
        // TODO E11: Lambda insensible a mayúsculas/minúsculas sobre nombre().
        return material -> material.nombre().toLowerCase().contains(texto.toLowerCase());
    }


    /**
     * Se utiliza la referencia al método isINstance para comprobar que sea una instancia de Digitalizable.
     * */
    /** Elementos que implementan una capacidad (p. ej. {@code Digitalizable.class}). */
    public static Regla<Material> conCapacidad(Class<?> capacidad) {
        // TODO E11: Referencia a método de instancia particular: capacidad::isInstance.
        return capacidad::isInstance;
    }
}
