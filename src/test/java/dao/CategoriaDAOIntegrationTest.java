package dao;

import entities.Categoria;
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
class CategoriaDAOIntegrationTest {

    @Container
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("probiblioteca_test")
                    .withUsername("root")
                    .withPassword("root")
                    .withInitScript("init-test.sql");

    private CategoriaDAO categoriaDAO;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        // 1. Abrimos la conexión con el contenedor de Testcontainers
        connection = DriverManager.getConnection(
                mysql.getJdbcUrl(),
                mysql.getUsername(),
                mysql.getPassword()
        );

        // 2. Limpiamos la tabla y reseteamos el autoincremento antes de cada test
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            stmt.execute("DELETE FROM categorias;");
            stmt.execute("ALTER TABLE categorias AUTO_INCREMENT = 1;");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");
        }

        // 3. Instanciamos el DAO inyectándole la conexión limpia de pruebas
        categoriaDAO = new CategoriaDAO(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testInsertarYGetById() {
        Categoria categoria = new Categoria(0, "Ficción", "Libros de novelas y cuentos fantásticos");
        categoriaDAO.insert(categoria);

        assertThat(categoria.getIdCategoria()).isGreaterThan(0);

        Categoria recuperada = categoriaDAO.getById(categoria.getIdCategoria());
        assertThat(recuperada).isNotNull();
        assertThat(recuperada.getNombre()).isEqualTo("Ficción");
        assertThat(recuperada.getDescripcion()).isEqualTo("Libros de novelas y cuentos fantásticos");
    }

    @Test
    void testGetAll() {
        Categoria c1 = new Categoria(0, "Ciencia", "Libros científicos y divulgación");
        Categoria c2 = new Categoria(0, "Historia", "Libros históricos");
        categoriaDAO.insert(c1);
        categoriaDAO.insert(c2);

        List<Categoria> categorias = categoriaDAO.getAll();

        assertThat(categorias).hasSize(2);
        assertThat(categorias).extracting(Categoria::getNombre)
                .containsExactlyInAnyOrder("Ciencia", "Historia");
    }

    @Test
    void testUpdate() {
        Categoria categoria = new Categoria(0, "Provisional", "Descripción vieja");
        categoriaDAO.insert(categoria);

        categoria.setNombre("Tecnología");
        categoria.setDescripcion("Libros de programación y sistemas");
        categoriaDAO.update(categoria);

        Categoria actualizada = categoriaDAO.getById(categoria.getIdCategoria());
        assertThat(actualizada.getNombre()).isEqualTo("Tecnología");
        assertThat(actualizada.getDescripcion()).isEqualTo("Libros de programación y sistemas");
    }

    @Test
    void testDelete() {
        Categoria categoria = new Categoria(0, "Para Borrar", "Temporal");
        categoriaDAO.insert(categoria);
        int idBorrable = categoria.getIdCategoria();

        categoriaDAO.delete(idBorrable);

        Categoria eliminada = categoriaDAO.getById(idBorrable);
        assertThat(eliminada).isNull();
    }

    @Test
    void testExistsById() {
        Categoria categoria = new Categoria(0, "Filosofía", "Estudio del pensamiento");
        categoriaDAO.insert(categoria);

        assertThat(categoriaDAO.existsById(categoria.getIdCategoria())).isTrue();
        assertThat(categoriaDAO.existsById(99999)).isFalse();
    }
}