package dao;

import entities.Ejemplar;
import entities.Libro;
import enums.EstadoEjemplar;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class EjemplarDAOIntegrationTest {

    @Container
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("probiblioteca_test")
                    .withUsername("root")
                    .withPassword("root")
                    .withInitScript("init-test.sql");

    private EjemplarDAO ejemplarDAO;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(
                mysql.getJdbcUrl(),
                mysql.getUsername(),
                mysql.getPassword()
        );

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            stmt.execute("DELETE FROM ejemplares;");
            stmt.execute("DELETE FROM libros;");
            stmt.execute("DELETE FROM categorias;");
            stmt.execute("ALTER TABLE ejemplares AUTO_INCREMENT = 1;");
            stmt.execute("ALTER TABLE libros AUTO_INCREMENT = 1;");
            stmt.execute("ALTER TABLE categorias AUTO_INCREMENT = 1;");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");

            stmt.execute("INSERT INTO categorias (idCategoria, nombre, descripcion) VALUES (1, 'Ficción', 'Novelas');");
            stmt.execute("INSERT INTO libros (idLibro, ISBN, titulo, descripcion, idCategoria) VALUES (1, '978-3-16-148410-0', 'Cien años de soledad', 'Novela clásica', 1);");
        }

        ejemplarDAO = new EjemplarDAO(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testInsertarYGetById() {
        Libro libro = new Libro();
        libro.setIdLibro(1);

        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setIdEjemplar(0);
        ejemplar.setCodigoInventario("BIB-001");
        ejemplar.setEstado(EstadoEjemplar.DISPONIBLE);
        ejemplar.setLibro(libro);

        ejemplarDAO.insert(ejemplar);

        assertThat(ejemplar.getIdEjemplar()).isGreaterThan(0);

        Ejemplar recuperado = ejemplarDAO.getById(ejemplar.getIdEjemplar());
        assertThat(recuperado).isNotNull();
        assertThat(recuperado.getCodigoInventario()).isEqualTo("BIB-001");
        assertThat(recuperado.getEstado()).isEqualTo(EstadoEjemplar.DISPONIBLE);
    }

    @Test
    void testGetAll() {
        Libro libro = new Libro();
        libro.setIdLibro(1);

        Ejemplar e1 = new Ejemplar();
        e1.setIdEjemplar(0);
        e1.setCodigoInventario("BIB-002");
        e1.setEstado(EstadoEjemplar.DISPONIBLE);
        e1.setLibro(libro);

        Ejemplar e2 = new Ejemplar();
        e2.setIdEjemplar(0);
        e2.setCodigoInventario("BIB-003");
        e2.setEstado(EstadoEjemplar.PRESTADO);
        e2.setLibro(libro);

        ejemplarDAO.insert(e1);
        ejemplarDAO.insert(e2);

        List<Ejemplar> ejemplares = ejemplarDAO.getAll();

        assertThat(ejemplares).hasSize(2);
        assertThat(ejemplares).extracting(Ejemplar::getCodigoInventario)
                .containsExactlyInAnyOrder("BIB-002", "BIB-003");
    }

    @Test
    void testUpdate() {
        Libro libro = new Libro();
        libro.setIdLibro(1);

        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setIdEjemplar(0);
        ejemplar.setCodigoInventario("BIB-004");
        ejemplar.setEstado(EstadoEjemplar.DISPONIBLE);
        ejemplar.setLibro(libro);

        ejemplarDAO.insert(ejemplar);

        ejemplar.setEstado(EstadoEjemplar.MANTENIMIENTO);
        ejemplarDAO.update(ejemplar);

        Ejemplar actualizado = ejemplarDAO.getById(ejemplar.getIdEjemplar());
        assertThat(actualizado.getEstado()).isEqualTo(EstadoEjemplar.MANTENIMIENTO);
    }

    @Test
    void testDelete() {
        Libro libro = new Libro();
        libro.setIdLibro(1);

        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setIdEjemplar(0);
        ejemplar.setCodigoInventario("BIB-005");
        ejemplar.setEstado(EstadoEjemplar.DISPONIBLE);
        ejemplar.setLibro(libro);

        ejemplarDAO.insert(ejemplar);
        int idBorrable = ejemplar.getIdEjemplar();

        ejemplarDAO.delete(idBorrable);

        Ejemplar eliminado = ejemplarDAO.getById(idBorrable);
        assertThat(eliminado).isNull();
    }

    @Test
    void testExistsById() {
        Libro libro = new Libro();
        libro.setIdLibro(1);

        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setIdEjemplar(0);
        ejemplar.setCodigoInventario("BIB-006");
        ejemplar.setEstado(EstadoEjemplar.DISPONIBLE);
        ejemplar.setLibro(libro);

        ejemplarDAO.insert(ejemplar);

        assertThat(ejemplarDAO.existsById(ejemplar.getIdEjemplar())).isTrue();
        assertThat(ejemplarDAO.existsById(99999)).isFalse();
    }
}