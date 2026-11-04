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

    /**
     * <p>abre el archivo .db</p>
     * @param rutaArchivo archivo .db (se crea si no existe)
     * @throws PersistenciaException en caso de que no se pueda abrir de manera correcta la conexión*/
    public ConexionSQLite(String rutaArchivo) {
        // TODO E13: Abrir jdbc:sqlite:<ruta>, activar PRAGMA foreign_keys = ON y crear el esquema.
        try {
            conexion = DriverManager.getConnection( "jdbc:sqlite:" + rutaArchivo);

        try (Statement st = conexion.createStatement()){
            st.execute("PRAGMA foreign_keys = ON");

            for(String sql : ESQUEMA){
                st.execute(sql);
            }
        }
        }catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al iniciar la BD"+ e.getMessage(),
                    e
            );
        }
    }

    public Connection conexion() {
        return conexion;
    }

    /**
     * <p>Ejecuta un trabajo dentro de una transacción: confirma si termina bien y revierte ante error.
     *
     * Desactiva temporalmente el modo autocommit, ejecuta el trabajo
     * recibido y confirma la transacción mediante {@code commit()} si
     * finaliza correctamente.</p>
     *
     * @param <R> tipo del resultado producido por el trabajo
     * @param trabajo operación SQL que se ejecutará dentro de la transacción
     * @return el resultado producido por {@code trabajo}
     * @throws PersistenciaException si ocurre un error durante la ejecución, la reversión o la restauración del modo autocommit
     */
    public <R> R enTransaccion(TrabajoSQL<R> trabajo) {
        // TODO E13: setAutoCommit(false); ejecutar; commit; ante SQLException rollback y PersistenciaException; finalmente setAutoCommit(true).
        try {
            conexion.setAutoCommit(false);

            R resultado = trabajo.ejecutar(conexion);

            conexion.commit();
            return resultado;
        } catch (SQLException e) {
            try {
                conexion.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }

            throw new PersistenciaException("Error durante la transaccion" +e.getMessage(), e);
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException e) {
                throw new PersistenciaException("Error al restaurar autocommit", e);
            }
        }
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
