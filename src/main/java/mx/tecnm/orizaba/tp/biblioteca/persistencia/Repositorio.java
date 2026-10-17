package mx.tecnm.orizaba.tp.biblioteca.persistencia;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico de repositorio (patrón Repository).
 * Contiene un <strong>tipo miembro de interfaz</strong> ({@link MapeadorFila}), implícitamente
 * {@code public static} (véase {@code java_3_tipos_internos/t2_jerarquias/J05_Tipos_Miembro_Interfaz}).
 *
 * @param <T> entidad
 * @param <K> clave
 */
public interface Repositorio<T, K> {

    void guardar(T entidad);

    Optional<T> buscar(K clave);

    List<T> todos();

    boolean eliminar(K clave);

    default boolean existe(K clave) {
        return buscar(clave).isPresent();
    }

    default void guardarTodos(Iterable<? extends T> entidades) {
        entidades.forEach(this::guardar);
    }

    /** Convierte la fila actual de un {@link ResultSet} en un objeto. */
    @FunctionalInterface
    interface MapeadorFila<R> {
        R mapear(ResultSet rs) throws SQLException;
    }
}
