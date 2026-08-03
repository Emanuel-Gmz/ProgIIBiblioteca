package dao;

import entities.*;
import enums.EstadoEjemplar;
import enums.EstadoPrestamo;
import exceptions.PrestamoException;
import interfaces.AdmConexion;
import interfaces.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class PrestamoDAO implements DAO<Prestamo, Integer> {


    private static final String SQL_INSERT =
            "INSERT INTO prestamos (idUsuario, idEjemplar, fechaPrestamo, fechaLimite, estado) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE prestamos SET fechaDevolucion = ?, estado = ? WHERE idPrestamo = ?";
    private static final String SQL_DELETE =
            "DELETE FROM prestamos WHERE idPrestamo = ?";
    private static final String SQL_UPDATE_ESTADO_EJEMPLAR =
            "UPDATE ejemplares SET estado = ? WHERE idEjemplar = ?";
    private static final String SQL_GETALL =
            "SELECT p.idPrestamo, p.fechaPrestamo, p.fechaLimite, p.fechaDevolucion, p.estado, " +
                    "u.idUsuario, u.nombre, u.apellido, " +
                    "e.idEjemplar, e.codigoInventario, " +
                    "l.titulo AS tituloLibro " +
                    "FROM prestamos p " +
                    "INNER JOIN usuarios u ON p.idUsuario = u.idUsuario " +
                    "INNER JOIN ejemplares e ON p.idEjemplar = e.idEjemplar " +
                    "INNER JOIN libros l ON e.idLibro = l.idLibro";
    private static final String SQL_GETBYID = SQL_GETALL + " WHERE p.idPrestamo = ?";
    private static final String SQL_EXISTBYID = "SELECT 1 FROM prestamos WHERE idPrestamo = ? LIMIT 1";
    private static final String SQL_GET_BY_USUARIO = SQL_GETALL + " WHERE p.idUsuario = ?";
    private static final String SQL_GET_VENCIDOS = SQL_GETALL + " WHERE p.estado = 'ACTIVO' AND p.fechaLimite < CURDATE()";
    private static final String SQL_GET_RANKING = "SELECT u.idUsuario, u.nombre, u.apellido, COUNT(p.idPrestamo) AS totalPrestamos " +
            "FROM prestamos p " +
            "INNER JOIN usuarios u ON p.idUsuario = u.idUsuario " +
            "GROUP BY u.idUsuario, u.nombre, u.apellido " +
            "ORDER BY totalPrestamos DESC LIMIT 3";

    private Connection conexionExterna;
    public PrestamoDAO() {}
    public PrestamoDAO(Connection conexionExterna) {this.conexionExterna = conexionExterna;}
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
    @Override
    public List<Prestamo> getAll() {
        List<Prestamo> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GETALL);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(mapearPrestamo(rs));
                }
            }
        } catch (SQLException e) {
            throw new PrestamoException("Error al listar los préstamos desde la base de datos", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }

    @Override
    public void insert(Prestamo objeto) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, RETURN_GENERATED_KEYS)) {
                ps.setInt(1, objeto.getUsuario().getIdUsuario());
                ps.setInt(2, objeto.getEjemplar().getIdEjemplar());
                ps.setDate(3, Date.valueOf(objeto.getFechaPrestamo()));
                ps.setDate(4, Date.valueOf(objeto.getFechaLimite()));
                ps.setString(5, objeto.getEstado().name());

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new PrestamoException("No se pudo insertar el préstamo.");
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        objeto.setIdPrestamo(rs.getInt(1));
                    }
                }
            }

            try (PreparedStatement psEjemplar = conn.prepareStatement(SQL_UPDATE_ESTADO_EJEMPLAR)) {
                psEjemplar.setString(1, EstadoEjemplar.PRESTADO.name());
                psEjemplar.setInt(2, objeto.getEjemplar().getIdEjemplar());
                psEjemplar.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw new PrestamoException("Error transaccional al intentar guardar el préstamo", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
                cerrarConexion(conn);
            }
        }
    }

    @Override
    public void update(Prestamo objeto) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
                if (objeto.getFechaDevolucion() != null) {
                    ps.setDate(1, Date.valueOf(objeto.getFechaDevolucion()));
                } else {
                    ps.setNull(1, Types.DATE);
                }

                ps.setString(2, objeto.getEstado().name());
                ps.setInt(3, objeto.getIdPrestamo());

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new PrestamoException("No se pudo actualizar: el préstamo con ID " + objeto.getIdPrestamo() + " no existe.");
                }
            }

            if (objeto.getEstado() == EstadoPrestamo.DEVUELTO) {
                try (PreparedStatement psEjemplar = conn.prepareStatement(SQL_UPDATE_ESTADO_EJEMPLAR)) {
                    psEjemplar.setString(1, EstadoEjemplar.DISPONIBLE.name());
                    psEjemplar.setInt(2, objeto.getEjemplar().getIdEjemplar());
                    psEjemplar.executeUpdate();
                }
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw new PrestamoException("Error transaccional al intentar actualizar el préstamo", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
                cerrarConexion(conn);
            }
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
                    throw new PrestamoException("No se encontró el préstamo con ID " + id + " para eliminar.");
                }
            }
        } catch (SQLException e) {
            throw new PrestamoException("Error al intentar eliminar el préstamo", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Prestamo getById(Integer id) {
        Prestamo prestamo = null;
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GETBYID)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        prestamo = mapearPrestamo(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new PrestamoException("Error al recuperar el préstamo con ID: " + id, e);
        } finally {
            cerrarConexion(conn);
        }
        return prestamo;
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
            throw new PrestamoException("Error al verificar existencia del préstamo", e);
        } finally {
            cerrarConexion(conn);
        }
    }
    public List<Prestamo> getByUsuario(int idUsuario) {
        List<Prestamo> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GET_BY_USUARIO)) {

                ps.setInt(1, idUsuario);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearPrestamo(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new PrestamoException("Error al listar los préstamos del usuario", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }

    public List<Prestamo> getVencidos() {
        List<Prestamo> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GET_VENCIDOS);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(mapearPrestamo(rs));
                }
            }
        } catch (SQLException e) {
            throw new PrestamoException("Error al listar los préstamos vencidos", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }

    public List<Usuario> getTop3UsuariosConMasPrestamos() {
        List<Usuario> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GET_RANKING);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("idUsuario"));
                    u.setNombre(rs.getString("nombre"));
                    u.setApellido(rs.getString("apellido"));
                    lista.add(u);
                }
            }
        } catch (SQLException e) {
            throw new PrestamoException("Error al obtener el top de usuarios con más préstamos", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }

    private Prestamo mapearPrestamo(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo();
        p.setIdPrestamo(rs.getInt("idPrestamo"));

        p.setFechaPrestamo(rs.getDate("fechaPrestamo").toLocalDate());
        p.setFechaLimite(rs.getDate("fechaLimite").toLocalDate());

        if (rs.getDate("fechaDevolucion") != null) {
            p.setFechaDevolucion(rs.getDate("fechaDevolucion").toLocalDate());
        }

        p.setEstado(EstadoPrestamo.valueOf(rs.getString("estado")));

        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("idUsuario"));
        u.setNombre(rs.getString("nombre"));
        u.setApellido(rs.getString("apellido"));
        p.setUsuario(u);

        Ejemplar e = new Ejemplar();
        e.setIdEjemplar(rs.getInt("idEjemplar"));
        e.setCodigoInventario(rs.getString("codigoInventario"));

        Libro l = new Libro();
        l.setTitulo(rs.getString("tituloLibro"));
        e.setLibro(l);

        p.setEjemplar(e);

        return p;
    }
}