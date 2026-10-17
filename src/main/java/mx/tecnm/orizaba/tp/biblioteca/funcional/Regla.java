package mx.tecnm.orizaba.tp.biblioteca.funcional;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Interfaz funcional propia (un solo método abstracto) con métodos por defecto de composición
 * (véase {@code java_5_lambdas/b_interfaces_funcionales} y {@code d_orden_superior/Superior2}).
 *
 * @param <T> tipo evaluado
 */
@FunctionalInterface
public interface Regla<T> {

    boolean evaluar(T t);
    /**
     * Comprobamos que otra no sea nula con el método requireNotNull de Object
     * retornamos el resultado de la lambda solicitada (T O F)
     * */
    default Regla<T> y(Regla<? super T> otra) {
        // TODO E11: Devolver la lambda t -> evaluar(t) && otra.evaluar(t) (validar otra no nula).
        Objects.requireNonNull(otra);

        return t -> evaluar(t) && otra.evaluar(t);
    }
    /**
     * Comprobamos que otra no sea nula con el método requireNotNull de Object
     * retornamos el resultado de la lambda solicitada (T O F)
     * */
    default Regla<T> o(Regla<? super T> otra) {
        // TODO E11: Devolver la lambda t -> evaluar(t) || otra.evaluar(t).
        Objects.requireNonNull(otra);

        return t->evaluar(t) || otra.evaluar(t);
    }

    /**
     * Comprobamos que otra no sea nula con el método requireNotNull de Object
     * retornamos el resultado de la lambda solicitada. Negación de T O F
     * */

    default Regla<T> negar() {
        // TODO E11: Devolver la lambda t -> !evaluar(t).
        return t -> !evaluar(t);
    }

    /** Adaptador hacia {@link Predicate} mediante referencia a método de instancia particular. */
    default Predicate<T> comoPredicado() {
        return this::evaluar;
    }

    static <T> Regla<T> todas() {
        return t -> true;
    }

    /** Adaptador desde {@link Predicate}. */
    static <T> Regla<T> de(Predicate<? super T> p) {
        return p::test;
    }
}
