package mx.tecnm.orizaba.tp.biblioteca.modelo;

/**
 * Interfaz de capacidad (clasificación múltiple, véase {@code java_4_modelo/t4_jerarquia/t1_clasificacion}).
 * Declara un método por defecto {@code descripcionCorta()} que colisiona deliberadamente con el de la otra
 * capacidad; quien implemente ambas debe resolver la colisión con {@code Interfaz.super.descripcionCorta()}
 * (véase {@code java_5_lambdas/a_interfaces_default/Default3}).
 */
public interface Digitalizable {

    String urlRepositorio();
    /**
     * Retorna la cadena concatenada con lo que obtenga el método urlRepositorio();
     * */
    default String descripcionCorta() {
        // TODO E05: Devolver "Repositorio digital: " concatenado con el valor de urlRepositorio().
        return "Repositorio digital: " + urlRepositorio();
    }
}
