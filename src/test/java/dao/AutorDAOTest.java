package dao;

import entities.Autor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AutorDAOTest {

    private AutorDAO autorDAO;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        // Usamos spy para mockear el método protegido obtenerConexion() del DAO
        autorDAO = spy(new AutorDAO());

        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Simulamos la obtención de la conexión vía AdmConexion
        doReturn(mockConnection).when(autorDAO).obtenerConexion();
    }

    @Test
    void testGetAll() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("idAutor")).thenReturn(1);
        when(mockResultSet.getString("nombreCompleto")).thenReturn("Gabriel García Márquez");
        when(mockResultSet.getString("nacionalidad")).thenReturn("Colombiana");

        // Act
        List<Autor> autores = autorDAO.getAll();

        // Assert
        assertThat(autores).hasSize(1);
        Autor autor = autores.get(0);
        assertThat(autor.getIdAutor()).isEqualTo(1);
        assertThat(autor.getNombreCompleto()).isEqualTo("Gabriel García Márquez");
        assertThat(autor.getNacionalidad()).isEqualTo("Colombiana");

        verify(mockConnection).prepareStatement(AutorDAO.SQL_GETALL);
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

        when(mockResultSet.getInt("idAutor")).thenReturn(2);
        when(mockResultSet.getString("nombreCompleto")).thenReturn("Jorge Luis Borges");
        when(mockResultSet.getString("nacionalidad")).thenReturn("Argentina");

        // Act
        Autor autor = autorDAO.getById(2);

        // Assert
        assertThat(autor).isNotNull();
        assertThat(autor.getIdAutor()).isEqualTo(2);
        assertThat(autor.getNombreCompleto()).isEqualTo("Jorge Luis Borges");
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

        Autor autor = new Autor(0, "Julio Cortázar", "Argentina");

        // Act
        autorDAO.insert(autor);

        // Assert
        assertThat(autor.getIdAutor()).isEqualTo(10);
        verify(mockPreparedStatement).setString(1, "Julio Cortázar");
        verify(mockPreparedStatement).setString(2, "Argentina");
        verify(mockPreparedStatement).executeUpdate();
        verify(mockGeneratedKeys).getInt(1);
    }

    @Test
    void testUpdate() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        Autor autor = new Autor(3, "Isabel Allende", "Chilena");

        // Act
        autorDAO.update(autor);

        // Assert
        verify(mockPreparedStatement).setString(1, "Isabel Allende");
        verify(mockPreparedStatement).setString(2, "Chilena");
        verify(mockPreparedStatement).setInt(3, 3);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testDelete() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        autorDAO.delete(1);

        // Assert
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testExistsById_True() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        // Act
        boolean exists = autorDAO.existsById(1);

        // Assert
        assertThat(exists).isTrue();
        verify(mockPreparedStatement).setInt(1, 1);
    }

    @Test
    void testSearchByNombre() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("idAutor")).thenReturn(1);
        when(mockResultSet.getString("nombreCompleto")).thenReturn("Gabriel García Márquez");
        when(mockResultSet.getString("nacionalidad")).thenReturn("Colombiana");

        // Act
        List<Autor> autores = autorDAO.searchByNombre("García");

        // Assert
        assertThat(autores).hasSize(1);
        assertThat(autores.get(0).getNombreCompleto()).contains("García");

        verify(mockPreparedStatement).setString(1, "%García%");
        verify(mockPreparedStatement).executeQuery();
    }
}