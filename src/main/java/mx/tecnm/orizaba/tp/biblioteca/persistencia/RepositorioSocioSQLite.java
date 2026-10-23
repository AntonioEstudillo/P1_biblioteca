package mx.tecnm.orizaba.tp.biblioteca.persistencia;

import mx.tecnm.orizaba.tp.biblioteca.modelo.Socio;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Nivel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Repositorio JDBC de socios. */
public class RepositorioSocioSQLite implements Repositorio<Socio, Integer> {

    private final ConexionSQLite bd;
    private final MapeadorFila<Socio> mapeador = rs -> new Socio(rs.getInt("id"),
            rs.getString("nombre"), rs.getString("correo"), Nivel.valueOf(rs.getString("nivel")));

    public RepositorioSocioSQLite(ConexionSQLite bd) {
        this.bd = bd;
    }

    @Override
    public void guardar(Socio a) {
        // TODO E13: Upsert por id con PreparedStatement.
        String sql = """
                INSERT INTO socio(id, nombre, correo, nivel)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                nombre = excluded.nombre,
                correo = excluded.correo,
                nivel = excluded.nivel
                """;

        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)){
            ps.setInt(1,a.id());
            ps.setString(2, a.nombre());
            ps.setString(3,a.correo());
            ps.setString(4,a.nivel().name());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar socio:" + e.getMessage(),e);
        }
    }

    @Override
    public Optional<Socio> buscar(Integer id) {
        // TODO E13: SELECT por id.
        String sql = """
            SELECT id, nombre, correo, nivel
            FROM socio
            WHERE id = ?
            """;

        try (PreparedStatement ps =
                     bd.conexion().prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapeador.mapear(rs));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo realizar la consulta socio: "
                            + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public List<Socio> todos() {
        // TODO E13: SELECT ordenado por id.
        String sql = """
                SELECT id, nombre, correo, nivel
                FROM socio
                ORDER BY id
                """;

        List<Socio> socios = new ArrayList<>();


        try(PreparedStatement ps = bd.conexion().prepareStatement(sql); ResultSet rs = ps.executeQuery()){

            while (rs.next()){
                socios.add(mapeador.mapear(rs));
            }
            return socios;

        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo realizar la consulta socio" +e.getMessage(),e);
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        // TODO E13: DELETE por id.
        String sql = """
                DELETE FROM socios
                WHERE id = ?
            
                """;

        try(PreparedStatement ps = bd.conexion().prepareStatement(sql)){
            ps.setInt(1, id);

            int filas = ps.executeUpdate();

            return filas > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar",e);
        }
    }
}
