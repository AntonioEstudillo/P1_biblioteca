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

    /**
     * <p>Guarda un préstamo y todas sus líneas en la base de datos
     * dentro de una única transacción.
     * El encabezado del préstamo se inserta o actualiza, las
     * líneas almacenadas anteriormente se eliminan y las líneas
     * actuales se insertan nuevamente mediante una operación
     * por lotes.</p>
     *
     * <p>La operación es atómica: si ocurre un error, los cambios
     * realizados durante la transacción se revierten.</p>
     *
     * @param t préstamo que se desea guardar
     * @throws PersistenciaException si ocurre un error al guardar
     * el encabezado o alguna de sus líneas
     */
    @Override
    public void guardar(Prestamo t) {
        // TODO E13: En bd.enTransaccion: upsert del encabezado, borrar sus líneas e insertarlas en lote (addBatch/executeBatch).
        bd.enTransaccion(c -> {
            String sqlPrestamo = """
                INSERT INTO prestamo (id, socio_id, fecha, estado)
                VALUES (?,?,?,?)
                ON CONFLICT(id) DO UPDATE SET
                    socio_id = excluded.socio_id,
                    fecha = excluded.fecha,
                    estado = excluded.estado
                """;

            try (PreparedStatement ps = c.prepareStatement(sqlPrestamo)) {
                ps.setInt(1, t.id());
                ps.setInt(2,t.socio().id());
                ps.setString(3, t.fecha().toString());
                ps.setString(4, t.estado().name());

                ps.executeUpdate();

            }

            String sqlBorrar = "DELETE FROM prestamo_linea WHERE prestamo_id = ?";

            try (PreparedStatement ps = c.prepareStatement(sqlBorrar)) {
                ps.setInt(1, t.id());
                ps.executeUpdate();
            }

            String sqlLinea = """
                    INSERT INTO prestamo_linea(prestamo_id, clave_material, unidades)
                    VALUES (?, ?, ?)
                   
                    """;

            try (PreparedStatement ps = c.prepareStatement(sqlLinea)){
                for (Prestamo.Linea linea : t.lineas()){
                    ps.setInt(1, t.id());

                    ps.setString(2, linea.elemento().clave().toString());

                    ps.setInt(3, linea.unidades());

                    ps.addBatch();
                }
                ps.executeBatch();
            }
            return null;
        });


    }
    /**
     * <p>Busca y reconstruye un préstamo mediante su identificador.
     * Primero se obtiene el encabezado del préstamo. Después
     * se recupera el socio asociado y se reconstruyen sus líneas
     * resolviendo cada clave mediante el repositorio de materiales.</p>
     *
     * @param id identificador del préstamo que se desea buscar
     * @return un {@link Optional} con el préstamo completamente
     * reconstruido, o vacío si no existe
     * @throws PersistenciaException si ocurre un error durante
     *  la consulta o reconstrucción del préstamo
     */

    @Override
    public Optional<Prestamo> buscar(Integer id) {
        // TODO E13: Leer el encabezado, resolver el actor y los elementos con sus repositorios, agregar líneas y restaurar el estado.
        String sql = """
                SELECT (id, socio_id, fecha, estado)
                FROM prestamo
                WHERE id = ?
                """;

        Connection c = bd.conexion();

        try (PreparedStatement ps = c.prepareStatement(sql)){
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(reconstruir(c, rs));
                }

                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>Reconstruye un préstamo a partir de la fila actual del
     * resultado de una consulta.
     * El préstamo se crea inicialmente en estado
     * {@link Estado#ABIERTA}, se recuperan y agregan sus líneas
     * y, una vez reconstruido completamente, se restaura el estado
     * que estaba almacenado en la base de datos.</p>
     *
     * @param c conexión utilizada para consultar las líneas del préstamo
     * @param rs resultado posicionado en la fila que contiene
     * el encabezado del préstamo
     * @return préstamo reconstruido con su socio, líneas y estado
     * @throws SQLException si ocurre un error durante la lectura
     * de los datos o sus líneas
     */
    private Prestamo reconstruir(Connection c, ResultSet rs) throws SQLException {
        // TODO E13: Construir el objeto en estado ABIERTA, agregar las líneas y al final restaurarEstado(estado leído).
        int id = rs.getInt("id");
        int socioId = rs.getInt("socio_id");
        LocalDate fecha = LocalDate.parse(rs.getString("fecha"));
        Estado estado = Estado.valueOf(rs.getString("estado"));

        Socio socio = socios.buscar(socioId).orElseThrow(()-> new SQLException("socio no encontrado " + socioId));

        Prestamo prestamo = new Prestamo(id, socio, fecha);

        String sql = """
                SELECT clave_material, unidades
                FROM prestamo_linea
                WHERE prestamo_id = ?
                ORDER BY clave_material                
                """;

        try(PreparedStatement ps = c.prepareStatement(sql)){
            ps.setInt(1, id);

            try(ResultSet lineas = ps.executeQuery()) {
                while (lineas.next()){
                    Signatura clave = Signatura.parse(lineas.getString("clave_material"));

                    Material material = elementos.buscar(clave).orElseThrow(()-> new SQLException("socio no encontrado " + clave));

                    prestamo.agregar(material, lineas.getInt("unidades"));
                }
            }
        }

        prestamo.restaurarEstado(estado);

        return prestamo;
    }

    /**
     * <p>Recupera todos los préstamos almacenados en la base de datos.
     * Los identificadores se procesan en orden y cada préstamo
     * se reconstruye con su socio, sus líneas de materiales y
     * su estado almacenado.</p>
     *
     * @return lista con todos los préstamos reconstruidos,
     * ordenados por identificador
     * @throws PersistenciaException si ocurre un error durante
     * la consulta o reconstrucción de algún préstamo
     */
    @Override
    public List<Prestamo> todos() {
        // TODO E13: Recorrer los ids ordenados y reconstruir cada registro.
        String sql = """
                SELECT id, socio_id, fecha, estado
                FROM prestamo
                ORDER BY id
                """;

        List<Prestamo> prestamos = new ArrayList<>();

        Connection c = bd.conexion();

        try (PreparedStatement ps = c.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();

            while (rs.next()){
                prestamos.add(reconstruir(c,rs));
            }

            return prestamos;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * <p>
     * Elimina de la base de datos el préstamo asociado al
     * identificador indicado.
     * Al eliminar el encabezado, las líneas asociadas se
     * eliminan automáticamente gracias a la restricción
     * {@code ON DELETE CASCADE} definida en la base de datos.</p>
     *
     * @param id identificador del préstamo que se desea eliminar
     * @return {@code true} si se eliminó al menos un préstamo;
     * {@code false} si no existía el identificador indicado
     * @throws PersistenciaException si ocurre un error durante
     * la operación SQL
     */
    @Override
    public boolean eliminar(Integer id) {
        // TODO E13: DELETE del encabezado (las líneas se eliminan en cascada).
        String sql = """
                DELETE FROM prestamo
                WHERE id = ?
                """;

        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)){
            ps.setInt(1, id);

            int filas = ps.executeUpdate();

            return filas > 0;


        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo eliminar",e);
        }
    }
}
