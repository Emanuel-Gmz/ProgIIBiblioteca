package dao;

import entities.Autor;
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
class AutorDAOIntegrationTest {

    @Container
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("probiblioteca_test")
                    .withUsername("root")
                    .withPassword("root")
                    .withInitScript("init-test.sql");

    private AutorDAO autorDAO;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        // 1. Primero abrimos la conexión con el contenedor de Testcontainers
        connection = DriverManager.getConnection(
                mysql.getJdbcUrl(),
                mysql.getUsername(),
                mysql.getPassword()
        );

        // 2. Luego limpiamos la tabla asegurando que 'connection' ya no es null
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            stmt.execute("DELETE FROM autores;");
            stmt.execute("ALTER TABLE autores AUTO_INCREMENT = 1;");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");
        }

        // 3. Finalmente instanciamos el DAO inyectándole la conexión limpia
        autorDAO = new AutorDAO(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testInsertarYGetById() {
        Autor autor = new Autor(0, "Gabriel García Márquez", "Colombiana");
        autorDAO.insert(autor);

        assertThat(autor.getIdAutor()).isGreaterThan(0);

        Autor recuperado = autorDAO.getById(autor.getIdAutor());
        assertThat(recuperado).isNotNull();
        assertThat(recuperado.getNombreCompleto()).isEqualTo("Gabriel García Márquez");
        assertThat(recuperado.getNacionalidad()).isEqualTo("Colombiana");
    }

    @Test
    void testGetAll() {
        Autor a1 = new Autor(0, "Jorge Luis Borges", "Argentina");
        Autor a2 = new Autor(0, "Isabel Allende", "Chilena");
        autorDAO.insert(a1);
        autorDAO.insert(a2);

        List<Autor> autores = autorDAO.getAll();

        assertThat(autores).hasSize(2);
        assertThat(autores).extracting(Autor::getNombreCompleto)
                .containsExactlyInAnyOrder("Jorge Luis Borges", "Isabel Allende");
    }

    @Test
    void testUpdate() {
        Autor autor = new Autor(0, "Autor Provisional", "Uruguaya");
        autorDAO.insert(autor);

        autor.setNombreCompleto("Autor Modificado");
        autor.setNacionalidad("Argentina");
        autorDAO.update(autor);

        Autor actualizado = autorDAO.getById(autor.getIdAutor());
        assertThat(actualizado.getNombreCompleto()).isEqualTo("Autor Modificado");
        assertThat(actualizado.getNacionalidad()).isEqualTo("Argentina");
    }

    @Test
    void testDelete() {
        Autor autor = new Autor(0, "Autor Para Borrar", "Chilena");
        autorDAO.insert(autor);
        int idBorrable = autor.getIdAutor();

        autorDAO.delete(idBorrable);

        Autor eliminado = autorDAO.getById(idBorrable);
        assertThat(eliminado).isNull();
    }

    @Test
    void testExistsById() {
        Autor autor = new Autor(0, "Julio Cortázar", "Argentina");
        autorDAO.insert(autor);

        assertThat(autorDAO.existsById(autor.getIdAutor())).isTrue();
        assertThat(autorDAO.existsById(99999)).isFalse();
    }

    @Test
    void testSearchByNombre() {
        Autor autor = new Autor(0, "Mario Vargas Llosa", "Peruana");
        autorDAO.insert(autor);

        List<Autor> resultados = autorDAO.searchByNombre("Vargas");

        assertThat(resultados).isNotEmpty();
        assertThat(resultados.get(0).getNombreCompleto()).contains("Vargas");
    }
}