package dao;

import entities.Ejemplar;
import entities.Prestamo;
import entities.Usuario;
import enums.EstadoPrestamo;
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
class PrestamoDAOIntegrationTest {

    @Container
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("probiblioteca_test")
                    .withUsername("root")
                    .withPassword("root")
                    .withInitScript("init-test.sql");

    private PrestamoDAO prestamoDAO;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(
                mysql.getJdbcUrl(),
                mysql.getUsername(),
                mysql.getPassword()
        );

        // Limpieza profunda asegurando integridad referencial
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            stmt.execute("DELETE FROM prestamos;");
            stmt.execute("DELETE FROM ejemplares;");
            stmt.execute("DELETE FROM libros;");
            stmt.execute("DELETE FROM categorias;");
            stmt.execute("DELETE FROM usuarios;");
            stmt.execute("ALTER TABLE prestamos AUTO_INCREMENT = 1;");
            stmt.execute("ALTER TABLE ejemplares AUTO_INCREMENT = 1;");
            stmt.execute("ALTER TABLE libros AUTO_INCREMENT = 1;");
            stmt.execute("ALTER TABLE categorias AUTO_INCREMENT = 1;");
            stmt.execute("ALTER TABLE usuarios AUTO_INCREMENT = 1;");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");

            // Datos base requeridos para asociar el préstamo
            stmt.execute("INSERT INTO usuarios (idUsuario, nombre, apellido, email, telefono, contrasenia, rol) VALUES (1, 'Ana', 'Gómez', 'ana@mail.com', '555123', '1234', 'USUARIO');");
            stmt.execute("INSERT INTO categorias (idCategoria, nombre, descripcion) VALUES (1, 'Ficción', 'Novelas');");
            stmt.execute("INSERT INTO libros (idLibro, ISBN, titulo, descripcion, idCategoria) VALUES (1, '978-3-16-148410-0', 'El principito', 'Clásico', 1);");
            stmt.execute("INSERT INTO ejemplares (idEjemplar, idLibro, codigoInventario, estado) VALUES (1, 1, 'BIB-100', 'DISPONIBLE');");
        }

        prestamoDAO = new PrestamoDAO(connection);
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

        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setIdEjemplar(1);

        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setEjemplar(ejemplar);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaLimite(LocalDate.now().plusDays(7));
        prestamo.setEstado(EstadoPrestamo.ACTIVO);

        prestamoDAO.insert(prestamo);

        assertThat(prestamo.getIdPrestamo()).isGreaterThan(0);

        Prestamo recuperado = prestamoDAO.getById(prestamo.getIdPrestamo());
        assertThat(recuperado).isNotNull();
        assertThat(recuperado.getEstado()).isEqualTo(EstadoPrestamo.ACTIVO);
        assertThat(recuperado.getUsuario().getNombre()).isEqualTo("Ana");
        assertThat(recuperado.getEjemplar().getCodigoInventario()).isEqualTo("BIB-100");
    }

    @Test
    void testGetAll() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);

        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setIdEjemplar(1);

        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setEjemplar(ejemplar);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaLimite(LocalDate.now().plusDays(7));
        prestamo.setEstado(EstadoPrestamo.ACTIVO);

        prestamoDAO.insert(prestamo);

        List<Prestamo> lista = prestamoDAO.getAll();
        assertThat(lista).hasSize(1);
    }

    @Test
    void testUpdateDevolucion() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);

        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setIdEjemplar(1);

        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setEjemplar(ejemplar);
        prestamo.setFechaPrestamo(LocalDate.now().minusDays(5));
        prestamo.setFechaLimite(LocalDate.now().plusDays(2));
        prestamo.setEstado(EstadoPrestamo.ACTIVO);

        prestamoDAO.insert(prestamo);

        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        prestamo.setFechaDevolucion(LocalDate.now());
        prestamoDAO.update(prestamo);

        Prestamo actualizado = prestamoDAO.getById(prestamo.getIdPrestamo());
        assertThat(actualizado.getEstado()).isEqualTo(EstadoPrestamo.DEVUELTO);
        assertThat(actualizado.getFechaDevolucion()).isNotNull();
    }
}