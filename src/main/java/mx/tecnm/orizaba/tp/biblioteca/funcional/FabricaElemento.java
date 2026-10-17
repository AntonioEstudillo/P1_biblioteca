package mx.tecnm.orizaba.tp.biblioteca.funcional;

import mx.tecnm.orizaba.tp.biblioteca.modelo.Material;

/**
 * Fábrica genérica acotada, diseñada para recibir <strong>referencias a constructor</strong>:
 * {@code FabricaElemento<Material.Libro, Integer> f = Material.Libro::new;}
 * (véase {@code java_5_lambdas/c_referencias_metodos/Referencias4}).
 *
 * @param <E> subtipo concreto de Material
 * @param <A> tipo del atributo específico del subtipo
 */
@FunctionalInterface
public interface FabricaElemento<E extends Material, A> {

    E crear(String nombre, double precioBase, A atributo);
}
