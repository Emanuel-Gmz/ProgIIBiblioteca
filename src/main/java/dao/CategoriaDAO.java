package dao;

import entities.Categoria;
import exceptions.CategoriaException;
import interfaces.AdmConexion;
import interfaces.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class CategoriaDAO implements DAO<Categoria, Integer> {
    private static final String SQL_INSERT =
            "INSERT INTO categorias (nombre, descripcion) VALUES (?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE categorias SET nombre = ?, descripcion = ? WHERE idCategoria = ?";
    private static final String SQL_DELETE =
            "DELETE FROM categorias WHERE idCategoria = ?";
    public static final String SQL_GETALL =
            "SELECT idCategoria, nombre, descripcion FROM categorias";
    private static final String SQL_GETBYID = SQL_GETALL + " WHERE idCategoria = ?";
    private static final String SQL_EXISTBYID = "SELECT 1 FROM categorias WHERE idCategoria = ? LIMIT 1";
    private static final String SQL_GETBYNOMBRE = SQL_GETALL + " WHERE nombre = ?";
    private Connection conexionExterna;

    public CategoriaDAO() {}

    public CategoriaDAO(Connection conexionExterna) {this.conexionExterna = conexionExterna;}
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
            }
        }
    }
    private Categoria mapearCategoria(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setIdCategoria(rs.getInt("idCategoria"));
        c.setNombre(rs.getString("nombre"));
        c.setDescripcion(rs.getString("descripcion"));
        return c;
    }
    @Override
    public List<Categoria> getAll() {
        List<Categoria> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GETALL);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(mapearCategoria(rs));
                }
            }
        } catch (SQLException e) {
            throw new CategoriaException("Error al listar las categorías desde la base de datos", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }

    @Override
    public void insert(Categoria objeto) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, RETURN_GENERATED_KEYS)) {

                ps.setString(1, objeto.getNombre());
                ps.setString(2, objeto.getDescripcion());

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new CategoriaException("No se pudo insertar la categoría.");
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        objeto.setIdCategoria(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new CategoriaException("La categoría '" + objeto.getNombre() + "' ya existe.", e);
            }
            throw new CategoriaException("Error al intentar guardar la categoría", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public void update(Categoria objeto) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

                ps.setString(1, objeto.getNombre());
                ps.setString(2, objeto.getDescripcion());
                ps.setInt(3, objeto.getIdCategoria());

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new CategoriaException("No se pudo actualizar: la categoría con ID " + objeto.getIdCategoria() + " no existe.");
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new CategoriaException("Ya existe otra categoría con el nombre '" + objeto.getNombre() + "'.", e);
            }
            throw new CategoriaException("Error al intentar actualizar la categoría", e);
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
                    throw new CategoriaException("No se encontró la categoría con ID " + id + " para eliminar.");
                }
            }
        } catch (SQLException e) {
            throw new CategoriaException("No se puede eliminar la categoría porque tiene libros asociados.", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Categoria getById(Integer id) {
        Categoria categoria = null;
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GETBYID)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        categoria = mapearCategoria(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new CategoriaException("Error al recuperar la categoría con ID: " + id, e);
        } finally {
            cerrarConexion(conn);
        }
        return categoria;
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
            throw new CategoriaException("Error al verificar existencia de la categoría", e);
        } finally {
            cerrarConexion(conn);
        }
    }
    public Categoria getByNombre(String nombre) {
        Categoria categoria = null;
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GETBYNOMBRE)) {

                ps.setString(1, nombre);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        categoria = mapearCategoria(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new CategoriaException("Error al recuperar la categoría por nombre", e);
        } finally {
            cerrarConexion(conn);
        }
        return categoria;
    }
}