package dao;

import entities.Multa;
import entities.Prestamo;
import entities.Usuario;
import enums.EstadoMulta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class MultaDAOIntegrationTest {

    @Container
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("probiblioteca_test")
                    .withUsername("root")
                    .withPassword("root")
                    .withInitScript("init-test.sql");

    private MultaDAO multaDAO;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(
                mysql.getJdbcUrl(),
                mysql.getUsername(),
                mysql.getPassword()
        );

        // Limpieza de tablas respetando el orden de las foreign keys
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            stmt.execute("DELETE FROM multas;");
            stmt.execute("DELETE FROM prestamos;");
            stmt.execute("DELETE FROM ejemplares;");
            stmt.execute("DELETE FROM libros;");
            stmt.execute("DELETE FROM categorias;");
            stmt.execute("DELETE FROM usuarios;");
            stmt.execute("ALTER TABLE multas AUTO_INCREMENT = 1;");
            stmt.execute("ALTER TABLE prestamos AUTO_INCREMENT = 1;");
            stmt.execute("ALTER TABLE ejemplares AUTO_INCREMENT = 1;");
            stmt.execute("ALTER TABLE libros AUTO_INCREMENT = 1;");
            stmt.execute("ALTER TABLE categorias AUTO_INCREMENT = 1;");
            stmt.execute("ALTER TABLE usuarios AUTO_INCREMENT = 1;");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");

            // Insertamos datos base necesarios (Usuario, Categoria, Libro, Ejemplar y Préstamo)
            stmt.execute("INSERT INTO usuarios (idUsuario, nombre, apellido, email, telefono, contrasenia, rol) VALUES (1, 'Juan', 'Pérez', 'juan@mail.com', '123456', '1234', 'USUARIO');");
            stmt.execute("INSERT INTO categorias (idCategoria, nombre, descripcion) VALUES (1, 'Ficción', 'Novelas');");
            stmt.execute("INSERT INTO libros (idLibro, ISBN, titulo, descripcion, idCategoria) VALUES (1, '978-3-16-148410-0', 'Libro Prueba', 'Desc', 1);");
            stmt.execute("INSERT INTO ejemplares (idEjemplar, idLibro, codigoInventario, estado) VALUES (1, 1, 'BIB-001', 'PRESTADO');");
            stmt.execute("INSERT INTO prestamos (idPrestamo, idUsuario, idEjemplar, fechaPrestamo, fechaLimite, estado) VALUES (1, 1, 1, '2026-05-01', '2026-05-15', 'ACTIVO');");
        }

        multaDAO = new MultaDAO(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testInsertarYGetById() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);

        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(1);

        Multa multa = new Multa();
        multa.setPrestamo(prestamo);
        multa.setUsuario(usuario);
        multa.setMonto(1500.00);
        multa.setFechaGeneracion(LocalDate.of(2026, 5, 16));
        multa.setEstado(EstadoMulta.PENDIENTE);

        multaDAO.insert(multa);

        assertThat(multa.getIdMulta()).isGreaterThan(0);

        Multa recuperada = multaDAO.getById(multa.getIdMulta());
        assertThat(recuperada).isNotNull();
        assertThat(recuperada.getMonto()).isEqualTo(1500.00);
        assertThat(recuperada.getEstado()).isEqualTo(EstadoMulta.PENDIENTE);
        assertThat(recuperada.getUsuario().getNombre()).isEqualTo("Juan");
    }

    @Test
    void testGetAll() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);

        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(1);

        Multa m1 = new Multa();
        m1.setPrestamo(prestamo);
        m1.setUsuario(usuario);
        m1.setMonto(500.00);
        m1.setFechaGeneracion(LocalDate.now());
        m1.setEstado(EstadoMulta.PENDIENTE);

        Multa m2 = new Multa();
        m2.setPrestamo(prestamo);
        m2.setUsuario(usuario);
        m2.setMonto(1000.00);
        m2.setFechaGeneracion(LocalDate.now());
        m2.setEstado(EstadoMulta.PAGADA);

        multaDAO.insert(m1);
        multaDAO.insert(m2);

        List<Multa> multas = multaDAO.getAll();

        assertThat(multas).hasSize(2);
    }

    @Test
    void testUpdate() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);

        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(1);

        Multa multa = new Multa();
        multa.setPrestamo(prestamo);
        multa.setUsuario(usuario);
        multa.setMonto(800.00);
        multa.setFechaGeneracion(LocalDate.now());
        multa.setEstado(EstadoMulta.PENDIENTE);

        multaDAO.insert(multa);

        multa.setEstado(EstadoMulta.PAGADA);
        multa.setMonto(800.00);
        multaDAO.update(multa);

        Multa actualizada = multaDAO.getById(multa.getIdMulta());
        assertThat(actualizada.getEstado()).isEqualTo(EstadoMulta.PAGADA);
    }

    @Test
    void testDelete() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);

        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(1);

        Multa multa = new Multa();
        multa.setPrestamo(prestamo);
        multa.setUsuario(usuario);
        multa.setMonto(300.00);
        multa.setFechaGeneracion(LocalDate.now());
        multa.setEstado(EstadoMulta.PENDIENTE);

        multaDAO.insert(multa);
        int idBorrable = multa.getIdMulta();

        multaDAO.delete(idBorrable);

        Multa eliminada = multaDAO.getById(idBorrable);
        assertThat(eliminada).isNull();
    }

    @Test
    void testGetPendientesByUsuario() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);

        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(1);

        Multa m1 = new Multa();
        m1.setPrestamo(prestamo);
        m1.setUsuario(usuario);
        m1.setMonto(400.00);
        m1.setFechaGeneracion(LocalDate.now());
        m1.setEstado(EstadoMulta.PENDIENTE);

        multaDAO.insert(m1);

        List<Multa> pendientes = multaDAO.getPendientesByUsuario(1);
        assertThat(pendientes).hasSize(1);
        assertThat(pendientes.get(0.0 == 0.0 ? 0 : 0).getEstado()).isEqualTo(EstadoMulta.PENDIENTE);
    }
}