package dao;

import entities.Ejemplar;
import entities.Libro;
import enums.EstadoEjemplar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EjemplarDAOTest {

    private EjemplarDAO ejemplarDAO;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        // Usamos spy para mockear el método protegido obtenerConexion() del DAO
        ejemplarDAO = spy(new EjemplarDAO());

        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Simulamos la obtención de la conexión vía AdmConexion
        doReturn(mockConnection).when(ejemplarDAO).obtenerConexion();
    }

    @Test
    void testGetAll() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("idEjemplar")).thenReturn(1);
        when(mockResultSet.getString("codigoInventario")).thenReturn("INV-001");
        when(mockResultSet.getString("estado")).thenReturn("DISPONIBLE");
        when(mockResultSet.getInt("idLibro")).thenReturn(5);
        when(mockResultSet.getString("ISBN")).thenReturn("978-3-16-148410-0");
        when(mockResultSet.getString("titulo")).thenReturn("El Quijote");

        // Act
        List<Ejemplar> ejemplares = ejemplarDAO.getAll();

        // Assert
        assertThat(ejemplares).hasSize(1);
        Ejemplar ejemplar = ejemplares.get(0);
        assertThat(ejemplar.getIdEjemplar()).isEqualTo(1);
        assertThat(ejemplar.getCodigoInventario()).isEqualTo("INV-001");
        assertThat(ejemplar.getEstado()).isEqualTo(EstadoEjemplar.DISPONIBLE);
        assertThat(ejemplar.getLibro().getTitulo()).isEqualTo("El Quijote");

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

        when(mockResultSet.getInt("idEjemplar")).thenReturn(2);
        when(mockResultSet.getString("codigoInventario")).thenReturn("INV-002");
        when(mockResultSet.getString("estado")).thenReturn("PRESTADO");
        when(mockResultSet.getInt("idLibro")).thenReturn(1);
        when(mockResultSet.getString("ISBN")).thenReturn("123-456");
        when(mockResultSet.getString("titulo")).thenReturn("La Odisea");

        // Act
        Ejemplar ejemplar = ejemplarDAO.getById(2);

        // Assert
        assertThat(ejemplar).isNotNull();
        assertThat(ejemplar.getIdEjemplar()).isEqualTo(2);
        assertThat(ejemplar.getEstado()).isEqualTo(EstadoEjemplar.PRESTADO);
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
        when(mockGeneratedKeys.getInt(1)).thenReturn(12);

        Libro libro = new Libro();
        libro.setIdLibro(3);

        Ejemplar ejemplar = new Ejemplar(0, "INV-999", EstadoEjemplar.DISPONIBLE, libro);

        // Act
        ejemplarDAO.insert(ejemplar);

        // Assert
        assertThat(ejemplar.getIdEjemplar()).isEqualTo(12);
        verify(mockPreparedStatement).setInt(1, 3);
        verify(mockPreparedStatement).setString(2, "INV-999");
        verify(mockPreparedStatement).setString(3, "DISPONIBLE");
        verify(mockPreparedStatement).executeUpdate();
        verify(mockGeneratedKeys).getInt(1);
    }

    @Test
    void testUpdate() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        Libro libro = new Libro();
        libro.setIdLibro(1);

        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setIdEjemplar(1);
        ejemplar.setCodigoInventario("BIB-001");
        ejemplar.setEstado(EstadoEjemplar.DISPONIBLE);
        ejemplar.setLibro(libro);

        // Act
        ejemplarDAO.update(ejemplar);

        // Assert - Ajusta los índices numéricos según el orden de tus signos '?' en tu SQL_UPDATE de EjemplarDAO
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testDelete() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        ejemplarDAO.delete(1);

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
        boolean exists = ejemplarDAO.existsById(1);

        // Assert
        assertThat(exists).isTrue();
        verify(mockPreparedStatement).setInt(1, 1);
    }

    @Test
    void testGetDisponiblesByLibro() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("idEjemplar")).thenReturn(7);
        when(mockResultSet.getString("codigoInventario")).thenReturn("INV-007");
        when(mockResultSet.getString("estado")).thenReturn("DISPONIBLE");
        when(mockResultSet.getInt("idLibro")).thenReturn(2);
        when(mockResultSet.getString("ISBN")).thenReturn("987-654");
        when(mockResultSet.getString("titulo")).thenReturn("Física Universitaria");

        // Act
        List<Ejemplar> disponibles = ejemplarDAO.getDisponiblesByLibro(2);

        // Assert
        assertThat(disponibles).hasSize(1);
        assertThat(disponibles.get(0).getEstado()).isEqualTo(EstadoEjemplar.DISPONIBLE);
        verify(mockPreparedStatement).setInt(1, 2);
        verify(mockPreparedStatement).executeQuery();
    }
}