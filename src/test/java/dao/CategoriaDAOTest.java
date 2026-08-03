package dao;

import entities.Categoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CategoriaDAOTest {

    private CategoriaDAO categoriaDAO;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        // Usamos spy para mockear el método de conexión protegido en el DAO
        categoriaDAO = spy(new CategoriaDAO());

        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Simulamos la obtención de la conexión hacia HikariCP vía AdmConexion
        doReturn(mockConnection).when(categoriaDAO).obtenerConexion();
    }

    @Test
    void testGetAll() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Simulamos un registro en la base de datos
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("idCategoria")).thenReturn(1);
        when(mockResultSet.getString("nombre")).thenReturn("Ficción");
        when(mockResultSet.getString("descripcion")).thenReturn("Libros de ficción y novelas");

        // Act
        List<Categoria> categorias = categoriaDAO.getAll();

        // Assert
        assertThat(categorias).hasSize(1);
        Categoria cat = categorias.get(0);
        assertThat(cat.getIdCategoria()).isEqualTo(1);
        assertThat(cat.getNombre()).isEqualTo("Ficción");
        assertThat(cat.getDescripcion()).isEqualTo("Libros de ficción y novelas");

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

        when(mockResultSet.getInt("idCategoria")).thenReturn(2);
        when(mockResultSet.getString("nombre")).thenReturn("Educativo");
        when(mockResultSet.getString("descripcion")).thenReturn("Libros de estudio");

        // Act
        Categoria cat = categoriaDAO.getById(2);

        // Assert
        assertThat(cat).isNotNull();
        assertThat(cat.getIdCategoria()).isEqualTo(2);
        assertThat(cat.getNombre()).isEqualTo("Educativo");
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
        when(mockGeneratedKeys.getInt(1)).thenReturn(5);

        Categoria categoria = new Categoria(0, "Terror", "Libros de miedo");

        // Act
        categoriaDAO.insert(categoria);

        // Assert
        assertThat(categoria.getIdCategoria()).isEqualTo(5);
        verify(mockPreparedStatement).setString(1, "Terror");
        verify(mockPreparedStatement).setString(2, "Libros de miedo");
        verify(mockPreparedStatement).executeUpdate();
        verify(mockGeneratedKeys).getInt(1);
    }

    @Test
    void testDelete() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        categoriaDAO.delete(1);

        // Assert
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeUpdate();
    }
}