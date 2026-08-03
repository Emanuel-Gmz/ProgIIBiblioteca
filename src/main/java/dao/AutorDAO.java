package dao;

import entities.Autor;
import exceptions.AutorException;
import interfaces.AdmConexion;
import interfaces.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class AutorDAO implements DAO<Autor, Integer> {
    private static final String SQL_INSERT =
            "INSERT INTO autores (nombreCompleto, nacionalidad) VALUES (?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE autores SET nombreCompleto = ?, nacionalidad = ? WHERE idAutor = ?";
    private static final String SQL_DELETE =
            "DELETE FROM autores WHERE idAutor = ?";
    public static final String SQL_GETALL =
            "SELECT idAutor, nombreCompleto, nacionalidad FROM autores";
    private static final String SQL_GETBYID = SQL_GETALL + " WHERE idAutor = ?";
    private static final String SQL_EXISTBYID = "SELECT 1 FROM autores WHERE idAutor = ? LIMIT 1";
    private static final String SQL_SEARCH_BY_NOMBRE = SQL_GETALL + " WHERE nombreCompleto LIKE ?";

    private Connection conexionExterna;
    public AutorDAO() {}
    public AutorDAO(Connection conexionExterna) {this.conexionExterna = conexionExterna;}

    protected Connection obtenerConexion() throws SQLException {
        if (conexionExterna != null) {
            return conexionExterna;
        }
        return AdmConexion.INSTANCE.obtenerConexion();
    }

    private Autor mapearAutor(ResultSet rs) throws SQLException {
        Autor a = new Autor();
        a.setIdAutor(rs.getInt("idAutor"));
        a.setNombreCompleto(rs.getString("nombreCompleto"));
        a.setNacionalidad(rs.getString("nacionalidad"));
        return a;
    }

    @Override
    public List<Autor> getAll() {
        List<Autor> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GETALL);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(mapearAutor(rs));
                }
            }
        } catch (SQLException e) {
            throw new AutorException("Error al listar los autores desde la base de datos", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }

    @Override
    public void insert(Autor objeto) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, RETURN_GENERATED_KEYS)) {

                ps.setString(1, objeto.getNombreCompleto());
                ps.setString(2, objeto.getNacionalidad());

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new AutorException("No se pudo insertar el autor.");
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        objeto.setIdAutor(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new AutorException("Error al intentar guardar el autor", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public void update(Autor objeto) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

                ps.setString(1, objeto.getNombreCompleto());
                ps.setString(2, objeto.getNacionalidad());
                ps.setInt(3, objeto.getIdAutor());

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new AutorException("No se pudo actualizar: el autor con ID " + objeto.getIdAutor() + " no existe.");
                }
            }
        } catch (SQLException e) {
            throw new AutorException("Error al intentar actualizar el autor", e);
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
                    throw new AutorException("No se encontró el autor con ID " + id + " para eliminar.");
                }
            }
        } catch (SQLException e) {
            throw new AutorException("No se puede eliminar el autor porque tiene libros asociados en el catálogo.", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Autor getById(Integer id) {
        Autor autor = null;
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GETBYID)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        autor = mapearAutor(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new AutorException("Error al recuperar el autor con ID: " + id, e);
        } finally {
            cerrarConexion(conn);
        }
        return autor;
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
            throw new AutorException("Error al verificar existencia del autor", e);
        } finally {
            cerrarConexion(conn);
        }
    }
    public List<Autor> searchByNombre(String keyword) {
        List<Autor> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_SEARCH_BY_NOMBRE)) {

                ps.setString(1, "%" + keyword + "%");

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearAutor(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new AutorException("Error al buscar autores por nombre", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }
    private void cerrarConexion(Connection conn) {
        if (conexionExterna == null && conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Log opcional de error al cerrar
            }
        }
    }
}