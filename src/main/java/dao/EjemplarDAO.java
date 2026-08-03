package dao;

import entities.Ejemplar;
import entities.Libro;
import enums.EstadoEjemplar;
import exceptions.EjemplarException;
import interfaces.AdmConexion;
import interfaces.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class EjemplarDAO implements DAO<Ejemplar, Integer> {

    // --- CONSULTAS SQL ---
    private static final String SQL_INSERT =
            "INSERT INTO ejemplares (idLibro, codigoInventario, estado) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE ejemplares SET idLibro = ?, codigoInventario = ?, estado = ? WHERE idEjemplar = ?";
    private static final String SQL_DELETE =
            "DELETE FROM ejemplares WHERE idEjemplar = ?";

    // INNER JOIN para traer los datos básicos del libro al que pertenece la copia
    private static final String SQL_GETALL =
            "SELECT e.idEjemplar, e.codigoInventario, e.estado, " +
                    "l.idLibro, l.ISBN, l.titulo " +
                    "FROM ejemplares e " +
                    "INNER JOIN libros l ON e.idLibro = l.idLibro";

    private static final String SQL_GETBYID = SQL_GETALL + " WHERE e.idEjemplar = ?";
    private static final String SQL_EXISTBYID = "SELECT 1 FROM ejemplares WHERE idEjemplar = ? LIMIT 1";

    // Consulta de negocio: Traer solo las copias disponibles de un libro específico
    private static final String SQL_GET_DISPONIBLES_BY_LIBRO =
            SQL_GETALL + " WHERE e.idLibro = ? AND e.estado = 'DISPONIBLE'";

    // --- SOPORTE DE CONEXIÓN (Producción y Testing) ---
    private Connection conexionExterna;

    public EjemplarDAO() {
        // Constructor por defecto para producción
    }

    public EjemplarDAO(Connection conexionExterna) {
        this.conexionExterna = conexionExterna;
    }

    protected Connection obtenerConexion() throws SQLException {
        if (conexionExterna != null) {
            return conexionExterna;
        }
        return AdmConexion.INSTANCE.obtenerConexion();
    }

    /**
     * Cierra la conexión únicamente si NO es una conexión externa de pruebas.
     */
    private void cerrarConexion(Connection conn) {
        if (conexionExterna == null && conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Silenciar o manejar error de cierre
            }
        }
    }

    // --- MÉTODO AUXILIAR DE MAPEO ---
    private Ejemplar mapearEjemplar(ResultSet rs) throws SQLException {
        Ejemplar e = new Ejemplar();
        e.setIdEjemplar(rs.getInt("idEjemplar"));
        e.setCodigoInventario(rs.getString("codigoInventario"));
        e.setEstado(EstadoEjemplar.valueOf(rs.getString("estado")));

        // Mapeamos el Libro asociado (solo los datos que trajimos en el JOIN)
        Libro l = new Libro();
        l.setIdLibro(rs.getInt("idLibro"));
        l.setIsbn(rs.getString("ISBN"));
        l.setTitulo(rs.getString("titulo"));

        e.setLibro(l);

        return e;
    }

    // --- IMPLEMENTACIÓN DE LA INTERFAZ DAO ---

    @Override
    public List<Ejemplar> getAll() {
        List<Ejemplar> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GETALL);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(mapearEjemplar(rs));
                }
            }
        } catch (SQLException e) {
            throw new EjemplarException("Error al listar los ejemplares desde la base de datos", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }

    @Override
    public void insert(Ejemplar objeto) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, RETURN_GENERATED_KEYS)) {

                ps.setInt(1, objeto.getLibro().getIdLibro());
                ps.setString(2, objeto.getCodigoInventario());
                ps.setString(3, objeto.getEstado().name());

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new EjemplarException("No se pudo insertar el ejemplar.");
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        objeto.setIdEjemplar(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new EjemplarException("Error al intentar guardar el ejemplar", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public void update(Ejemplar objeto) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

                ps.setInt(1, objeto.getLibro().getIdLibro());
                ps.setString(2, objeto.getCodigoInventario());
                ps.setString(3, objeto.getEstado().name());
                ps.setInt(4, objeto.getIdEjemplar());

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new EjemplarException("No se pudo actualizar: el ejemplar con ID " + objeto.getIdEjemplar() + " no existe.");
                }
            }
        } catch (SQLException e) {
            throw new EjemplarException("Error al intentar actualizar el ejemplar", e);
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
                    throw new EjemplarException("No se encontró el ejemplar con ID " + id + " para eliminar.");
                }
            }
        } catch (SQLException e) {
            throw new EjemplarException("Error al intentar eliminar el ejemplar. Verifica que no tenga préstamos asociados.", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Ejemplar getById(Integer id) {
        Ejemplar ejemplar = null;
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GETBYID)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ejemplar = mapearEjemplar(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new EjemplarException("Error al recuperar el ejemplar con ID: " + id, e);
        } finally {
            cerrarConexion(conn);
        }
        return ejemplar;
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
            throw new EjemplarException("Error al verificar existencia del ejemplar", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    // --- MÉTODOS ESPECÍFICOS DE NEGOCIO ---

    public List<Ejemplar> getDisponiblesByLibro(int idLibro) {
        List<Ejemplar> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GET_DISPONIBLES_BY_LIBRO)) {

                ps.setInt(1, idLibro);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearEjemplar(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new EjemplarException("Error al buscar los ejemplares disponibles para el libro con ID " + idLibro, e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }
}