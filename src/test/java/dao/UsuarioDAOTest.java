package dao;

import entities.Usuario;
import enums.RolUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UsuarioDAOTest {

    private UsuarioDAO usuarioDAO;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        // Usamos spy para mockear el método de conexión protegido en el DAO
        usuarioDAO = spy(new UsuarioDAO());

        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Simulamos la obtención de la conexión hacia HikariCP vía AdmConexion
        doReturn(mockConnection).when(usuarioDAO).obtenerConexion();
    }

    @Test
    void testGetAll() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Simulamos un usuario en la base de datos
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("idUsuario")).thenReturn(1);
        when(mockResultSet.getString("nombre")).thenReturn("Héctor");
        when(mockResultSet.getString("email")).thenReturn("hector@email.com");
        when(mockResultSet.getString("contrasenia")).thenReturn("secreto123");
        when(mockResultSet.getString("rol")).thenReturn("ADMIN");

        // Act
        List<Usuario> usuarios = usuarioDAO.getAll();

        // Assert
        assertThat(usuarios).hasSize(1);
        Usuario user = usuarios.get(0);
        assertThat(user.getIdUsuario()).isEqualTo(1);
        assertThat(user.getNombre()).isEqualTo("Héctor");
        assertThat(user.getEmail()).isEqualTo("hector@email.com");
        assertThat(user.getRol()).isEqualTo(RolUsuario.ADMIN);

        // Verificar interacciones
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetById() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        when(mockResultSet.getInt("idUsuario")).thenReturn(2);
        when(mockResultSet.getString("nombre")).thenReturn("Juan");
        when(mockResultSet.getString("email")).thenReturn("juan@email.com");
        when(mockResultSet.getString("contrasenia")).thenReturn("pass456");
        when(mockResultSet.getString("rol")).thenReturn("USUARIO");

        // Act
        Usuario user = usuarioDAO.getById(2);

        // Assert
        assertThat(user).isNotNull();
        assertThat(user.getIdUsuario()).isEqualTo(2);
        assertThat(user.getNombre()).isEqualTo("Juan");
        verify(mockPreparedStatement).setInt(1, 2);
    }

    @Test
    void testInsert() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(10);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(0);
        usuario.setNombre("Ana");
        usuario.setApellido(null); // o un apellido si lo configuras
        usuario.setEmail("ana@email.com");
        usuario.setTelefono(null);
        usuario.setContrasenia("clave789");
        usuario.setRol(RolUsuario.BIBLIOTECARIO);

        // Act
        usuarioDAO.insert(usuario);

        // Assert
        assertThat(usuario.getIdUsuario()).isEqualTo(10);
        verify(mockPreparedStatement).setString(1, "Ana");
        verify(mockPreparedStatement).setString(2, null); // Apellido
        verify(mockPreparedStatement).setString(3, "ana@email.com");
        verify(mockPreparedStatement).setString(4, null); // Teléfono
        verify(mockPreparedStatement).setString(5, "clave789");
        verify(mockPreparedStatement).setString(6, "BIBLIOTECARIO");
        verify(mockPreparedStatement).executeUpdate();
        verify(mockGeneratedKeys).getInt(1);
    }

    @Test
    void testDelete() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        usuarioDAO.delete(1);

        // Assert
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeUpdate();
    }
}