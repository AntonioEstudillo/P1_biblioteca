package mx.tecnm.orizaba.tp.biblioteca.funcional;

import mx.tecnm.orizaba.tp.biblioteca.modelo.Material;

/**
 * Biblioteca de reglas reutilizables (clase utilitaria no instanciable).
 */
public final class Reglas {

    private Reglas() {
        throw new AssertionError("No instanciable");
    }

    public static Regla<Material> precioHasta(double maximo) {
        // TODO E11: Lambda que acepta los elementos cuyo precioBase() sea menor o igual que maximo.
        throw new UnsupportedOperationException("TODO E11");
    }

    public static Regla<Material> nombreContiene(String texto) {
        // TODO E11: Lambda insensible a mayúsculas/minúsculas sobre nombre().
        throw new UnsupportedOperationException("TODO E11");
    }

    /** Elementos que implementan una capacidad (p. ej. {@code Digitalizable.class}). */
    public static Regla<Material> conCapacidad(Class<?> capacidad) {
        // TODO E11: Referencia a método de instancia particular: capacidad::isInstance.
        throw new UnsupportedOperationException("TODO E11");
    }
}
