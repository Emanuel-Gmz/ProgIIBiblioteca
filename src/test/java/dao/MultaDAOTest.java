package dao;

import entities.Multa;
import entities.Prestamo;
import entities.Usuario;
import enums.EstadoMulta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MultaDAOTest {

    private MultaDAO multaDAO;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        multaDAO = spy(new MultaDAO());

        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        doReturn(mockConnection).when(multaDAO).obtenerConexion();
    }

    @Test
    void testGetAll() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("idMulta")).thenReturn(1);
        when(mockResultSet.getDouble("monto")).thenReturn(1200.0);
        when(mockResultSet.getDate("fechaGeneracion")).thenReturn(Date.valueOf(LocalDate.of(2026, 6, 12)));
        when(mockResultSet.getString("estado")).thenReturn("PENDIENTE");
        when(mockResultSet.getInt("idPrestamo")).thenReturn(3);
        when(mockResultSet.getInt("idUsuario")).thenReturn(1);
        when(mockResultSet.getString("nombre")).thenReturn("Héctor");
        when(mockResultSet.getString("apellido")).thenReturn("Gómez");
        when(mockResultSet.getString("email")).thenReturn("hector@mail.com");
        when(mockResultSet.getDate("fechaLimite")).thenReturn(Date.valueOf(LocalDate.now()));

        List<Multa> multas = multaDAO.getAll();

        assertThat(multas).hasSize(1);
        Multa multa = multas.get(0);
        assertThat(multa.getIdMulta()).isEqualTo(1);
        assertThat(multa.getMonto()).isEqualTo(1200.0);
        assertThat(multa.getEstado()).isEqualTo(EstadoMulta.PENDIENTE);
        assertThat(multa.getUsuario().getNombre()).isEqualTo("Héctor");

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

        when(mockResultSet.getInt("idMulta")).thenReturn(2);
        when(mockResultSet.getDouble("monto")).thenReturn(500.0);
        when(mockResultSet.getDate("fechaGeneracion")).thenReturn(Date.valueOf(LocalDate.of(2026, 6, 14)));
        when(mockResultSet.getString("estado")).thenReturn("PAGADA");
        when(mockResultSet.getInt("idPrestamo")).thenReturn(4);
        when(mockResultSet.getInt("idUsuario")).thenReturn(2);
        when(mockResultSet.getString("nombre")).thenReturn("Juan");
        when(mockResultSet.getString("apellido")).thenReturn("Pérez");
        when(mockResultSet.getString("email")).thenReturn("juan@mail.com");
        when(mockResultSet.getDate("fechaLimite")).thenReturn(Date.valueOf(LocalDate.now()));

        Multa multa = multaDAO.getById(2);

        assertThat(multa).isNotNull();
        assertThat(multa.getIdMulta()).isEqualTo(2);
        assertThat(multa.getMonto()).isEqualTo(500.0);
        assertThat(multa.getEstado()).isEqualTo(EstadoMulta.PAGADA);
        verify(mockPreparedStatement).setInt(1, 2);
    }

    @Test
    void testInsert() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(15);

        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(3);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);

        Multa multa = new Multa();
        multa.setIdMulta(0);
        multa.setMonto(1500.0);
        multa.setFechaGeneracion(LocalDate.of(2026, 6, 15));
        multa.setEstado(EstadoMulta.PENDIENTE);
        multa.setPrestamo(prestamo);
        multa.setUsuario(usuario);

        multaDAO.insert(multa);

        assertThat(multa.getIdMulta()).isEqualTo(15);
        verify(mockPreparedStatement).setInt(1, 3); // idPrestamo
        verify(mockPreparedStatement).setInt(2, 1); // idUsuario
        verify(mockPreparedStatement).setDouble(3, 1500.0); // monto
        verify(mockPreparedStatement).setDate(4, Date.valueOf(LocalDate.of(2026, 6, 15))); // fechaGeneracion
        verify(mockPreparedStatement).setString(5, "PENDIENTE"); // estado
        verify(mockPreparedStatement).executeUpdate();
        verify(mockGeneratedKeys).getInt(1);
    }

    @Test
    void testDelete() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        multaDAO.delete(1);

        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeUpdate();
    }
}