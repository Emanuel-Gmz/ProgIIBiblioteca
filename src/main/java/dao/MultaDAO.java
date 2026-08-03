package dao;

import entities.Multa;
import entities.Prestamo;
import entities.Usuario;
import enums.EstadoMulta;
import exceptions.MultaException;
import interfaces.AdmConexion;
import interfaces.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class MultaDAO implements DAO<Multa, Integer> {

    // --- CONSULTAS SQL ---
    private static final String SQL_INSERT =
            "INSERT INTO multas (idPrestamo, idUsuario, monto, fechaGeneracion, estado) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE multas SET estado = ?, monto = ? WHERE idMulta = ?";
    private static final String SQL_DELETE =
            "DELETE FROM multas WHERE idMulta = ?";

    // INNER JOIN para traer los datos del infractor y del préstamo asociado
    private static final String SQL_GETALL =
            "SELECT m.idMulta, m.monto, m.fechaGeneracion, m.estado, " +
                    "u.idUsuario, u.nombre, u.apellido, u.email, " +
                    "p.idPrestamo, p.fechaLimite " +
                    "FROM multas m " +
                    "INNER JOIN usuarios u ON m.idUsuario = u.idUsuario " +
                    "INNER JOIN prestamos p ON m.idPrestamo = p.idPrestamo";

    private static final String SQL_GETBYID = SQL_GETALL + " WHERE m.idMulta = ?";
    private static final String SQL_EXISTBYID = "SELECT 1 FROM multas WHERE idMulta = ? LIMIT 1";

    // Consultas específicas de negocio
    private static final String SQL_GET_BY_USUARIO = SQL_GETALL + " WHERE m.idUsuario = ?";
    private static final String SQL_GET_PENDIENTES_BY_USUARIO = SQL_GETALL + " WHERE m.idUsuario = ? AND m.estado = 'PENDIENTE'";

    // --- SOPORTE DE CONEXIÓN (Producción y Testing) ---
    private Connection conexionExterna;

    public MultaDAO() {
        // Constructor por defecto para producción
    }

    public MultaDAO(Connection conexionExterna) {
        this.conexionExterna = conexionExterna;
    }

    protected Connection obtenerConexion() throws SQLException {
        if (conexionExterna != null) {
            return conexionExterna;
        }
        return AdmConexion.INSTANCE.obtenerConexion();
    }

    private void cerrarConexion(Connection conn) {
        if (conexionExterna == null && conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // --- MÉTODO AUXILIAR DE MAPEO ---
    private Multa mapearMulta(ResultSet rs) throws SQLException {
        Multa m = new Multa();
        m.setIdMulta(rs.getInt("idMulta"));
        m.setMonto(rs.getDouble("monto"));
        m.setFechaGeneracion(rs.getDate("fechaGeneracion").toLocalDate());
        m.setEstado(EstadoMulta.valueOf(rs.getString("estado")));

        // Mapeo del Usuario
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("idUsuario"));
        u.setNombre(rs.getString("nombre"));
        u.setApellido(rs.getString("apellido"));
        u.setEmail(rs.getString("email"));
        m.setUsuario(u);

        // Mapeo del Préstamo origen
        Prestamo p = new Prestamo();
        p.setIdPrestamo(rs.getInt("idPrestamo"));
        if (rs.getDate("fechaLimite") != null) {
            p.setFechaLimite(rs.getDate("fechaLimite").toLocalDate());
        }
        m.setPrestamo(p);

        return m;
    }

    // --- IMPLEMENTACIÓN DE LA INTERFAZ DAO ---

    @Override
    public List<Multa> getAll() {
        List<Multa> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GETALL);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(mapearMulta(rs));
                }
            }
        } catch (SQLException e) {
            throw new MultaException("Error al listar las multas del sistema", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }

    @Override
    public void insert(Multa objeto) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, RETURN_GENERATED_KEYS)) {

                ps.setInt(1, objeto.getPrestamo().getIdPrestamo());
                ps.setInt(2, objeto.getUsuario().getIdUsuario());
                ps.setDouble(3, objeto.getMonto());
                ps.setDate(4, Date.valueOf(objeto.getFechaGeneracion()));
                ps.setString(5, objeto.getEstado().name());

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new MultaException("No se pudo registrar la multa.");
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        objeto.setIdMulta(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new MultaException("Error al intentar guardar la multa", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public void update(Multa objeto) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

                ps.setString(1, objeto.getEstado().name());
                ps.setDouble(2, objeto.getMonto());
                ps.setInt(3, objeto.getIdMulta());

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new MultaException("No se pudo actualizar: la multa con ID " + objeto.getIdMulta() + " no existe.");
                }
            }
        } catch (SQLException e) {
            throw new MultaException("Error al intentar actualizar la multa", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public void delete(Integer id) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

                ps.setInt(1, id);
                int filasAfectadas = ps.executeUpdate();

                if (filasAfectadas == 0) {
                    throw new MultaException("No se encontró la multa con ID " + id + " para eliminar.");
                }
            }
        } catch (SQLException e) {
            throw new MultaException("Error al intentar eliminar la multa", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Multa getById(Integer id) {
        Multa multa = null;
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GETBYID)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        multa = mapearMulta(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new MultaException("Error al recuperar la multa con ID: " + id, e);
        } finally {
            cerrarConexion(conn);
        }
        return multa;
    }

    @Override
    public boolean existsById(Integer id) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_EXISTBYID)) {

                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            throw new MultaException("Error al verificar existencia de la multa", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    // --- MÉTODOS ESPECÍFICOS DE NEGOCIO ---

    public List<Multa> getByUsuario(int idUsuario) {
        List<Multa> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GET_BY_USUARIO)) {

                ps.setInt(1, idUsuario);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearMulta(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new MultaException("Error al listar las multas del usuario", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }

    public List<Multa> getPendientesByUsuario(int idUsuario) {
        List<Multa> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GET_PENDIENTES_BY_USUARIO)) {

                ps.setInt(1, idUsuario);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearMulta(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new MultaException("Error al buscar multas pendientes del usuario", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }
}