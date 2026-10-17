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
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<Socio> buscar(Integer id) {
        // TODO E13: SELECT por id.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<Socio> todos() {
        // TODO E13: SELECT ordenado por id.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public boolean eliminar(Integer id) {
        // TODO E13: DELETE por id.
        throw new UnsupportedOperationException("TODO E13");
    }
}
