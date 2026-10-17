package mx.tecnm.orizaba.tp.biblioteca.persistencia;

import mx.tecnm.orizaba.tp.biblioteca.modelo.Socio;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Material;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Signatura;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Estado;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Prestamo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JDBC de préstamos y sus líneas. Guardar es <strong>atómico</strong>:
 * encabezado y líneas se escriben en una sola transacción (commit/rollback).
 */
public class RepositorioPrestamoSQLite implements Repositorio<Prestamo, Integer> {

    private final ConexionSQLite bd;
    private final Repositorio<Socio, Integer> socios;
    private final Repositorio<Material, Signatura> elementos;

    public RepositorioPrestamoSQLite(ConexionSQLite bd, Repositorio<Socio, Integer> socios,
                         Repositorio<Material, Signatura> elementos) {
        this.bd = bd;
        this.socios = socios;
        this.elementos = elementos;
    }

    @Override
    public void guardar(Prestamo t) {
        // TODO E13: En bd.enTransaccion: upsert del encabezado, borrar sus líneas e insertarlas en lote (addBatch/executeBatch).
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<Prestamo> buscar(Integer id) {
        // TODO E13: Leer el encabezado, resolver el actor y los elementos con sus repositorios, agregar líneas y restaurar el estado.
        throw new UnsupportedOperationException("TODO E13");
    }

    private Prestamo reconstruir(Connection c, ResultSet rs) throws SQLException {
        // TODO E13: Construir el objeto en estado ABIERTA, agregar las líneas y al final restaurarEstado(estado leído).
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<Prestamo> todos() {
        // TODO E13: Recorrer los ids ordenados y reconstruir cada registro.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public boolean eliminar(Integer id) {
        // TODO E13: DELETE del encabezado (las líneas se eliminan en cascada).
        throw new UnsupportedOperationException("TODO E13");
    }
}
