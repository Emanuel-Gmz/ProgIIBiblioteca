package dao;

import entities.Autor;
import entities.Categoria;
import entities.Libro;
import exceptions.LibroException;
import interfaces.AdmConexion;
import interfaces.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class LibroDAO implements DAO<Libro, Integer> {

    // --- CONSULTAS SQL ---
    private static final String SQL_INSERT =
            "INSERT INTO libros (ISBN, titulo, descripcion, imagen, idCategoria) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE libros SET ISBN = ?, titulo = ?, descripcion = ?, imagen = ?, idCategoria = ? WHERE idLibro = ?";
    private static final String SQL_DELETE =
            "DELETE FROM libros WHERE idLibro = ?";

    public static final String SQL_GETALL =
            "SELECT l.idLibro, l.ISBN, l.titulo, l.descripcion, l.imagen, " +
                    "c.idCategoria, c.nombre AS nombreCategoria, c.descripcion AS descCategoria " +
                    "FROM libros l " +
                    "LEFT JOIN categorias c ON l.idCategoria = c.idCategoria";

    private static final String SQL_GETBYID = SQL_GETALL + " WHERE l.idLibro = ?";
    private static final String SQL_EXISTBYID = "SELECT 1 FROM libros WHERE idLibro = ? LIMIT 1";
    private static final String SQL_SEARCH_BY_TITULO = SQL_GETALL + " WHERE l.titulo LIKE ?";

    private static final String SQL_SEARCH_BY_CATEGORIA = SQL_GETALL + " WHERE l.idCategoria = ?";
    private static final String SQL_SEARCH_BY_TITULO_Y_CATEGORIA = SQL_GETALL + " WHERE l.titulo LIKE ? AND l.idCategoria = ?";

    private static final String SQL_GET_ULTIMOS = SQL_GETALL + " ORDER BY l.idLibro DESC LIMIT ?";

    private static final String SQL_GET_AUTORES_BY_LIBRO =
            "SELECT a.idAutor, a.nombreCompleto, a.nacionalidad " +
                    "FROM autores a " +
                    "JOIN libros_autores la ON a.idAutor = la.idAutor " +
                    "WHERE la.idLibro = ?";

    private static final String SQL_DELETE_AUTORES_LIBRO = "DELETE FROM libros_autores WHERE idLibro = ?";

    // --- SOPORTE DE CONEXIÓN (Producción y Testing) ---
    private Connection conexionExterna;

    public LibroDAO() {
        // Constructor por defecto para producción
    }

    public LibroDAO(Connection conexionExterna) {
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
    private Libro mapearLibro(ResultSet rs, Connection conn) throws SQLException {
        Libro l = new Libro();
        l.setIdLibro(rs.getInt("idLibro"));
        l.setIsbn(rs.getString("ISBN"));
        l.setTitulo(rs.getString("titulo"));
        l.setDescripcion(rs.getString("descripcion"));
        l.setImagen(rs.getString("imagen"));

        int idCat = rs.getInt("idCategoria");
        if (!rs.wasNull()) {
            Categoria c = new Categoria();
            c.setIdCategoria(idCat);
            c.setNombre(rs.getString("nombreCategoria"));
            c.setDescripcion(rs.getString("descCategoria"));
            l.setCategoria(c);
        }

        l.setAutores(obtenerAutoresPorLibro(l.getIdLibro(), conn));

        return l;
    }

    private List<Autor> obtenerAutoresPorLibro(int idLibro, Connection conn) {
        List<Autor> autores = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SQL_GET_AUTORES_BY_LIBRO)) {
            ps.setInt(1, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Autor a = new Autor();
                    a.setIdAutor(rs.getInt("idAutor"));
                    a.setNombreCompleto(rs.getString("nombreCompleto"));
                    a.setNacionalidad(rs.getString("nacionalidad"));
                    autores.add(a);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return autores;
    }

    // --- IMPLEMENTACIÓN DE LA INTERFAZ DAO ---

    @Override
    public List<Libro> getAll() {
        List<Libro> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GETALL);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(mapearLibro(rs, conn));
                }
            }
        } catch (SQLException e) {
            throw new LibroException("Error al listar los libros del catálogo", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }

    @Override
    public void insert(Libro objeto) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, RETURN_GENERATED_KEYS)) {
                ps.setString(1, objeto.getIsbn());
                ps.setString(2, objeto.getTitulo());
                ps.setString(3, objeto.getDescripcion());
                ps.setString(4, objeto.getImagen());

                if (objeto.getCategoria() != null && objeto.getCategoria().getIdCategoria() > 0) {
                    ps.setInt(5, objeto.getCategoria().getIdCategoria());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new LibroException("No se pudo insertar el libro.");
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        objeto.setIdLibro(rs.getInt(1));
                    }
                }
            }

            if (objeto.getAutores() != null && !objeto.getAutores().isEmpty()) {
                insertarLibroAutores(objeto.getIdLibro(), objeto.getAutores(), conn);
            }

            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw new LibroException("Error al intentar guardar el libro y sus autores", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restauramos el autocommit original por buena práctica
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                cerrarConexion(conn);
            }
        }
    }

    @Override
    public void update(Libro objeto) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
                ps.setString(1, objeto.getIsbn());
                ps.setString(2, objeto.getTitulo());
                ps.setString(3, objeto.getDescripcion());
                ps.setString(4, objeto.getImagen());

                if (objeto.getCategoria() != null && objeto.getCategoria().getIdCategoria() > 0) {
                    ps.setInt(5, objeto.getCategoria().getIdCategoria());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }

                ps.setInt(6, objeto.getIdLibro());

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new LibroException("No se pudo actualizar: el libro con ID " + objeto.getIdLibro() + " no existe.");
                }
            }

            try (PreparedStatement psDel = conn.prepareStatement(SQL_DELETE_AUTORES_LIBRO)) {
                psDel.setInt(1, objeto.getIdLibro());
                psDel.executeUpdate();
            }

            if (objeto.getAutores() != null && !objeto.getAutores().isEmpty()) {
                insertarLibroAutores(objeto.getIdLibro(), objeto.getAutores(), conn);
            }

            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw new LibroException("Error al intentar actualizar el libro", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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
                    throw new LibroException("No se encontró el libro con ID " + id + " para eliminar.");
                }
            }
        } catch (SQLException e) {
            throw new LibroException("Error al intentar eliminar el libro", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Libro getById(Integer id) {
        Libro libro = null;
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GETBYID)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        libro = mapearLibro(rs, conn);
                    }
                }
            }
        } catch (SQLException e) {
            throw new LibroException("Error al recuperar el libro con ID: " + id, e);
        } finally {
            cerrarConexion(conn);
        }
        return libro;
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
            throw new LibroException("Error al verificar existencia del libro", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    // --- MÉTODOS ESPECÍFICOS DE NEGOCIO ---

    public List<Libro> buscarPorTitulo(String keyword) {
        List<Libro> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_SEARCH_BY_TITULO)) {

                ps.setString(1, "%" + keyword + "%");

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearLibro(rs, conn));
                    }
                }
            }
        } catch (SQLException e) {
            throw new LibroException("Error al buscar libros por título", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }

    public List<Libro> buscarPorCategoria(int idCategoria) {
        List<Libro> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_SEARCH_BY_CATEGORIA)) {

                ps.setInt(1, idCategoria);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearLibro(rs, conn));
                    }
                }
            }
        } catch (SQLException e) {
            throw new LibroException("Error al buscar libros por categoría", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }

    public List<Libro> buscarPorTituloYCategoria(String keyword, int idCategoria) {
        List<Libro> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_SEARCH_BY_TITULO_Y_CATEGORIA)) {

                ps.setString(1, "%" + keyword + "%");
                ps.setInt(2, idCategoria);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearLibro(rs, conn));
                    }
                }
            }
        } catch (SQLException e) {
            throw new LibroException("Error al buscar libros por título y categoría", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }

    public List<Libro> obtenerUltimosAgregados(int limite) {
        List<Libro> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GET_ULTIMOS)) {

                ps.setInt(1, limite);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearLibro(rs, conn));
                    }
                }
            }
        } catch (SQLException e) {
            throw new LibroException("Error al obtener los últimos libros agregados", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }

    public void insertarLibroAutores(int idLibro, List<Autor> autores, Connection conn) throws SQLException {
        String sql = "INSERT INTO libros_autores (idLibro, idAutor) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Autor autor : autores) {
                ps.setInt(1, idLibro);
                ps.setInt(2, autor.getIdAutor());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}