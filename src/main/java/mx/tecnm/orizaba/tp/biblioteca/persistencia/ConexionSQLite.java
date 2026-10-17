package mx.tecnm.orizaba.tp.biblioteca.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Administra la conexión JDBC con SQLite y el esquema de la base de datos.
 * <p>Proceso: el bloque estático carga el controlador la primera vez que la clase se inicializa
 * (véase {@code java_2_proceso/Proceso07}: uso activo de una clase).</p>
 */
public final class ConexionSQLite implements AutoCloseable {

    private static final String[] ESQUEMA = {
        """
        CREATE TABLE IF NOT EXISTS material (
            clave        TEXT PRIMARY KEY,
            tipo         TEXT NOT NULL,
            nombre       TEXT NOT NULL,
            precio_base  REAL NOT NULL CHECK (precio_base >= 0),
            atributo     TEXT NOT NULL
        )""",
        """
        CREATE TABLE IF NOT EXISTS socio (
            id      INTEGER PRIMARY KEY,
            nombre  TEXT NOT NULL,
            correo  TEXT NOT NULL UNIQUE,
            nivel   TEXT NOT NULL
        )""",
        """
        CREATE TABLE IF NOT EXISTS prestamo (
            id        INTEGER PRIMARY KEY,
            socio_id  INTEGER NOT NULL REFERENCES socio(id),
            fecha     TEXT NOT NULL,
            estado    TEXT NOT NULL
        )""",
        """
        CREATE TABLE IF NOT EXISTS prestamo_linea (
            prestamo_id   INTEGER NOT NULL REFERENCES prestamo(id) ON DELETE CASCADE,
            clave_material TEXT NOT NULL REFERENCES material(clave),
            unidades        INTEGER NOT NULL CHECK (unidades > 0),
            PRIMARY KEY (prestamo_id, clave_material)
        )"""
    };

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("No se encontró el controlador sqlite-jdbc en el classpath");
        }
    }

    private final Connection conexion;

    /** @param rutaArchivo archivo .db (se crea si no existe) */
    public ConexionSQLite(String rutaArchivo) {
        // TODO E13: Abrir jdbc:sqlite:<ruta>, activar PRAGMA foreign_keys = ON y crear el esquema.
        throw new UnsupportedOperationException("TODO E13");
    }

    public Connection conexion() {
        return conexion;
    }

    /**
     * Ejecuta un trabajo dentro de una transacción: confirma si termina bien y revierte ante error.
     */
    public <R> R enTransaccion(TrabajoSQL<R> trabajo) {
        // TODO E13: setAutoCommit(false); ejecutar; commit; ante SQLException rollback y PersistenciaException; finalmente setAutoCommit(true).
        throw new UnsupportedOperationException("TODO E13");
    }

    /** Interfaz funcional miembro: trabajo que puede lanzar {@link SQLException}. */
    @FunctionalInterface
    public interface TrabajoSQL<R> {
        R ejecutar(Connection c) throws SQLException;
    }

    @Override
    public void close() {
        try {
            conexion.close();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al cerrar la conexión", e);
        }
    }
}
