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
public class
RepositorioMaterialSQLite implements Repositorio<Material, Signatura> {

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
        return switch (e){
            case Material.Libro x ->
                Integer.toString(x.paginas());
            case Material.Revista x->
                Integer.toString(x.antiguedadAnios());
            case Material.Tesis x->
                x.grado();
        };
    }

    @Override
    public void guardar(Material e) {
        // TODO E13: INSERT ... ON CONFLICT(clave) DO UPDATE ... con PreparedStatement.
        String sql = """
                    INSERT INTO material (clave, tipo, nombre, precio_base, atributo)
                    VALUES (?, ?, ?, ?, ?)
                    ON CONFLICT(clave) DO UPDATE SET
                        tipo = excluded.tipo,
                        nombre = excluded.nombre,
                        precio_base = excluded.precio_base,
                        atributo = excluded.atributo
                    """;

        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)){
            ps.setString(1, e.clave().toString());
            ps.setString(2, e.tipo());
            ps.setString(3, e.nombre());
            ps.setDouble(4, e.precioBase());
            ps.setString(5, atributoDe(e));

            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new PersistenciaException("Error al guardar", ex);
        }
    }

    @Override
    public Optional<Material> buscar(Signatura clave) {
        // TODO E13: SELECT por clave; mapear con mapeador si hay fila.
        String sql = """
                SELECT clave, tipo, nombre, precio_base, atributo
                FROM material
                WHERE clave = ?
                """;

        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)){
            ps.setString(1, clave.toString());

            try (ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(
                            mapeador.mapear(rs)
                    );
                }

                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar",e);
        }

    }

    @Override
    public List<Material> todos() {
        // TODO E13: SELECT ordenado por clave; mapear cada fila.
       String sql = """
               SELECT clave, tipo, nombre, precio_base, atributo
               FROM material 
               ORDER BY clave = ? 
       
               """;

       List<Material> materiales = new ArrayList<>();

       try (PreparedStatement ps = bd.conexion().prepareStatement(sql); ResultSet rs = ps.executeQuery()){
            while (rs.next()){
                materiales.add(mapeador.mapear(rs));
            }
            return materiales;
       } catch (SQLException e) {
           throw new PersistenciaException("Error al consultar",e);
       }
    }

    @Override
    public boolean eliminar(Signatura clave) {
        // TODO E13: DELETE por clave; devolver si se afectó alguna fila.
        String sql = """
                DELETE FROM material
                WHERE clave = ?
                """;

        try(PreparedStatement ps = bd.conexion().prepareStatement(sql)){

            ps.setString(1, clave.toString());
            int filas = ps.executeUpdate();

            return filas>0;

        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar", e);
        }
    }
}
