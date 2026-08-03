package dao;

import entities.Ejemplar;
import entities.Prestamo;
import entities.Usuario;
import enums.EstadoPrestamo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PrestamoDAOTest {

    private PrestamoDAO prestamoDAO;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        prestamoDAO = spy(new PrestamoDAO());

        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        doReturn(mockConnection).when(prestamoDAO).obtenerConexion();
    }

    @Test
    void testGetAll() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("idPrestamo")).thenReturn(1);
        when(mockResultSet.getDate("fechaPrestamo")).thenReturn(Date.valueOf(LocalDate.of(2026, 6, 1)));
        when(mockResultSet.getDate("fechaLimite")).thenReturn(Date.valueOf(LocalDate.of(2026, 6, 15)));
        when(mockResultSet.getDate("fechaDevolucion")).thenReturn(null); // Opcional
        when(mockResultSet.getString("estado")).thenReturn("ACTIVO");
        when(mockResultSet.getInt("idUsuario")).thenReturn(1);
        when(mockResultSet.getString("nombre")).thenReturn("Héctor");
        when(mockResultSet.getString("apellido")).thenReturn("Gómez");
        when(mockResultSet.getInt("idEjemplar")).thenReturn(10);
        when(mockResultSet.getString("codigoInventario")).thenReturn("BIB-001");
        when(mockResultSet.getString("tituloLibro")).thenReturn("Java Programming");

        List<Prestamo> prestamos = prestamoDAO.getAll();

        assertThat(prestamos).hasSize(1);
        Prestamo prestamo = prestamos.get(0);
        assertThat(prestamo.getIdPrestamo()).isEqualTo(1);
        assertThat(prestamo.getFechaPrestamo()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(prestamo.getUsuario().getNombre()).isEqualTo("Héctor");

        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetById() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        when(mockResultSet.getInt("idPrestamo")).thenReturn(2);
        when(mockResultSet.getDate("fechaPrestamo")).thenReturn(Date.valueOf(LocalDate.of(2026, 6, 5)));
        when(mockResultSet.getDate("fechaLimite")).thenReturn(Date.valueOf(LocalDate.of(2026, 6, 20)));
        when(mockResultSet.getDate("fechaDevolucion")).thenReturn(null);
        when(mockResultSet.getString("estado")).thenReturn("ACTIVO");
        when(mockResultSet.getInt("idUsuario")).thenReturn(2);
        when(mockResultSet.getString("nombre")).thenReturn("Juan");
        when(mockResultSet.getString("apellido")).thenReturn("Pérez");
        when(mockResultSet.getInt("idEjemplar")).thenReturn(11);
        when(mockResultSet.getString("codigoInventario")).thenReturn("BIB-002");
        when(mockResultSet.getString("tituloLibro")).thenReturn("Bases de Datos");

        Prestamo prestamo = prestamoDAO.getById(2);

        assertThat(prestamo).isNotNull();
        assertThat(prestamo.getIdPrestamo()).isEqualTo(2);
        assertThat(prestamo.getUsuario().getIdUsuario()).isEqualTo(2);
        verify(mockPreparedStatement).setInt(1, 2);
    }

    @Test
    void testInsert() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);

        // Agregamos esta línea para que devuelva el statement también cuando actualiza el estado del ejemplar
        when(mockConnection.prepareStatement(eq("UPDATE ejemplares SET estado = ? WHERE idEjemplar = ?")))
                .thenReturn(mockPreparedStatement);

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(7);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);

        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setIdEjemplar(5);

        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(0);
        prestamo.setFechaPrestamo(LocalDate.of(2026, 6, 10));
        prestamo.setFechaLimite(LocalDate.of(2026, 6, 25));
        prestamo.setEstado(EstadoPrestamo.ACTIVO);
        prestamo.setUsuario(usuario);
        prestamo.setEjemplar(ejemplar);

        // Act
        prestamoDAO.insert(prestamo);

        // Assert
        assertThat(prestamo.getIdPrestamo()).isEqualTo(7);
        verify(mockPreparedStatement).setInt(1, 1); // idUsuario
        verify(mockPreparedStatement).setDate(3, Date.valueOf(LocalDate.of(2026, 6, 10)));
        verify(mockPreparedStatement).setDate(4, Date.valueOf(LocalDate.of(2026, 6, 25)));
        verify(mockPreparedStatement).setString(5, "ACTIVO");
        verify(mockPreparedStatement, atLeastOnce()).executeUpdate();
        verify(mockPreparedStatement, times(2)).setInt(2, 5); // Verificamos las dos veces que se usa este parámetro
        verify(mockGeneratedKeys).getInt(1);
    }

    @Test
    void testDelete() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        prestamoDAO.delete(1);

        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeUpdate();
    }
}