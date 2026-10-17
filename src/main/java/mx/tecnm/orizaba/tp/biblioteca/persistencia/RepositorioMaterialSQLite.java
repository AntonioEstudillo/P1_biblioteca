package mx.tecnm.orizaba.tp.biblioteca.persistencia;

import mx.tecnm.orizaba.tp.biblioteca.modelo.Material;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Signatura;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio JDBC de materiales (tabla de herencia única: columna {@code tipo}).
 * <ul>
 *   <li>Escritura: {@code switch} con <em>pattern matching</em> exhaustivo sobre la jerarquía sellada.</li>
 *   <li>Lectura: mapa de reconstructores (lambdas) indexado por el nombre del subtipo.</li>
 * </ul>
 */
public class RepositorioMaterialSQLite implements Repositorio<Material, Signatura> {

    /** Interfaz funcional privada anidada. */
    @FunctionalInterface
    private interface Reconstructor {
        Material crear(Signatura clave, String nombre, double precio, String atributo);
    }

    private static final Map<String, Reconstructor> RECONSTRUCTORES = Map.of(
            "Libro", (c, n, p, a) -> new Material.Libro(c, n, p, Integer.parseInt(a)),
            "Revista", (c, n, p, a) -> new Material.Revista(c, n, p, Integer.parseInt(a)),
            "Tesis", (c, n, p, a) -> new Material.Tesis(c, n, p, a)
    );

    private final ConexionSQLite bd;
    private final MapeadorFila<Material> mapeador = rs -> {
        Reconstructor r = RECONSTRUCTORES.get(rs.getString("tipo"));
        if (r == null) {
            throw new SQLException("Tipo desconocido: " + rs.getString("tipo"));
        }
        return r.crear(Signatura.parse(rs.getString("clave")), rs.getString("nombre"),
                rs.getDouble("precio_base"), rs.getString("atributo"));
    };

    public RepositorioMaterialSQLite(ConexionSQLite bd) {
        this.bd = bd;
    }

    /** Atributo específico del subtipo, obtenido con pattern matching. */
    static String atributoDe(Material e) {
        // TODO E13: switch (e) { case Material.Libro x -> ...; ... } sin default (la jerarquía es sellada).
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public void guardar(Material e) {
        // TODO E13: INSERT ... ON CONFLICT(clave) DO UPDATE ... con PreparedStatement.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<Material> buscar(Signatura clave) {
        // TODO E13: SELECT por clave; mapear con mapeador si hay fila.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<Material> todos() {
        // TODO E13: SELECT ordenado por clave; mapear cada fila.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public boolean eliminar(Signatura clave) {
        // TODO E13: DELETE por clave; devolver si se afectó alguna fila.
        throw new UnsupportedOperationException("TODO E13");
    }
}
