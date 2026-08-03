package dao;

import entities.Usuario;
import enums.RolUsuario;
import exceptions.UsuarioException;
import interfaces.AdmConexion;
import interfaces.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class UsuarioDAO implements DAO<Usuario, Integer> {

    // --- CONSULTAS SQL ---
    private static final String SQL_INSERT =
            "INSERT INTO usuarios (nombre, apellido, email, telefono, contrasenia, rol) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE usuarios SET nombre = ?, apellido = ?, email = ?, telefono = ?, contrasenia = ?, rol = ? WHERE idUsuario = ?";
    private static final String SQL_DELETE =
            "DELETE FROM usuarios WHERE idUsuario = ?";
    private static final String SQL_GETALL =
            "SELECT idUsuario, nombre, apellido, email, telefono, contrasenia, rol FROM usuarios";

    private static final String SQL_GETBYID = SQL_GETALL + " WHERE idUsuario = ?";
    private static final String SQL_EXISTBYID = "SELECT 1 FROM usuarios WHERE idUsuario = ? LIMIT 1";

    private static final String SQL_GETBYEMAIL = SQL_GETALL + " WHERE email = ?";
    private static final String SQL_EXISTBYEMAIL = "SELECT 1 FROM usuarios WHERE email = ? LIMIT 1";

    // --- SOPORTE DE CONEXIÓN (Producción y Testing) ---
    private Connection conexionExterna;

    public UsuarioDAO() {}

    public UsuarioDAO(Connection conexionExterna) {
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
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("idUsuario"));
        u.setNombre(rs.getString("nombre"));
        u.setApellido(rs.getString("apellido"));
        u.setEmail(rs.getString("email"));
        u.setTelefono(rs.getString("telefono"));
        u.setContrasenia(rs.getString("contrasenia"));
        u.setRol(RolUsuario.valueOf(rs.getString("rol")));
        return u;
    }

    // --- IMPLEMENTACIÓN DE LA INTERFAZ DAO ---

    @Override
    public List<Usuario> getAll() {
        List<Usuario> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GETALL);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(mapearUsuario(rs));
                }
            }
        } catch (SQLException e) {
            throw new UsuarioException("Error al listar los usuarios", e);
        } finally {
            cerrarConexion(conn);
        }
        return lista;
    }

    @Override
    public void insert(Usuario objeto) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, RETURN_GENERATED_KEYS)) {

                ps.setString(1, objeto.getNombre());
                ps.setString(2, objeto.getApellido());
                ps.setString(3, objeto.getEmail());
                ps.setString(4, objeto.getTelefono());
                ps.setString(5, objeto.getContrasenia());
                ps.setString(6, objeto.getRol().name());

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new UsuarioException("No se pudo registrar el usuario.");
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        objeto.setIdUsuario(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new UsuarioException("El email ingresado ya se encuentra registrado.", e);
            }
            throw new UsuarioException("Error al intentar registrar el usuario", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public void update(Usuario objeto) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

                ps.setString(1, objeto.getNombre());
                ps.setString(2, objeto.getApellido());
                ps.setString(3, objeto.getEmail());
                ps.setString(4, objeto.getTelefono());
                ps.setString(5, objeto.getContrasenia());
                ps.setString(6, objeto.getRol().name());
                ps.setInt(7, objeto.getIdUsuario());

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new UsuarioException("No se pudo actualizar: el usuario no existe.");
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new UsuarioException("El email ingresado ya pertenece a otra cuenta.", e);
            }
            throw new UsuarioException("Error al intentar actualizar los datos del usuario", e);
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
                    throw new UsuarioException("No se encontró el usuario para eliminar.");
                }
            }
        } catch (SQLException e) {
            throw new UsuarioException("Error al intentar eliminar el usuario", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    @Override
    public Usuario getById(Integer id) {
        Usuario usuario = null;
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GETBYID)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        usuario = mapearUsuario(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new UsuarioException("Error al recuperar el usuario por ID", e);
        } finally {
            cerrarConexion(conn);
        }
        return usuario;
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
            throw new UsuarioException("Error al verificar la existencia del usuario", e);
        } finally {
            cerrarConexion(conn);
        }
    }

    // --- MÉTODOS ESPECÍFICOS DE NEGOCIO ---

    public Usuario getByEmail(String email) {
        Usuario usuario = null;
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_GETBYEMAIL)) {

                ps.setString(1, email);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        usuario = mapearUsuario(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new UsuarioException("Error al recuperar el usuario por Email", e);
        } finally {
            cerrarConexion(conn);
        }
        return usuario;
    }

    public boolean existsByEmail(String email) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            try (PreparedStatement ps = conn.prepareStatement(SQL_EXISTBYEMAIL)) {

                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            throw new UsuarioException("Error al verificar disponibilidad del email", e);
        } finally {
            cerrarConexion(conn);
        }
    }
}