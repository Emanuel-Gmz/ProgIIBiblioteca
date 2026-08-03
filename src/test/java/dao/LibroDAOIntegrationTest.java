package dao;

import entities.Autor;
import entities.Categoria;
import entities.Libro;
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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class LibroDAOIntegrationTest {

    @Container
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("probiblioteca_test")
                    .withUsername("root")
                    .withPassword("root")
                    .withInitScript("init-test.sql");

    private LibroDAO libroDAO;
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
            stmt.execute("DELETE FROM libros_autores;");
            stmt.execute("DELETE FROM libros;");
            stmt.execute("DELETE FROM categorias;");
            stmt.execute("DELETE FROM autores;");
            stmt.execute("ALTER TABLE libros AUTO_INCREMENT = 1;");
            stmt.execute("ALTER TABLE categorias AUTO_INCREMENT = 1;");
            stmt.execute("ALTER TABLE autores AUTO_INCREMENT = 1;");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");

            // Insertamos datos base previos para que el libro pueda referenciarlos
            stmt.execute("INSERT INTO categorias (idCategoria, nombre, descripcion) VALUES (1, 'Ficción', 'Novelas y cuentos');");
            stmt.execute("INSERT INTO autores (idAutor, nombreCompleto, nacionalidad) VALUES (1, 'Gabriel García Márquez', 'Colombiana');");
        }

        libroDAO = new LibroDAO(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testInsertarYGetById() {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(1);

        Autor autor = new Autor();
        autor.setIdAutor(1);
        List<Autor> autores = new ArrayList<>();
        autores.add(autor);

        Libro libro = new Libro();
        libro.setIsbn("978-987-1234-56-7");
        libro.setTitulo("Cien años de soledad");
        libro.setDescripcion("Una obra maestra de la literatura.");
        libro.setImagen("portada.jpg");
        libro.setCategoria(categoria);
        libro.setAutores(autores);

        libroDAO.insert(libro);

        assertThat(libro.getIdLibro()).isGreaterThan(0);

        Libro recuperado = libroDAO.getById(libro.getIdLibro());
        assertThat(recuperado).isNotNull();
        assertThat(recuperado.getTitulo()).isEqualTo("Cien años de soledad");
        assertThat(recuperado.getIsbn()).isEqualTo("978-987-1234-56-7");
        assertThat(recuperado.getCategoria()).isNotNull();
        assertThat(recuperado.getCategoria().getNombre()).isEqualTo("Ficción");
        assertThat(recuperado.getAutores()).hasSize(1);
        assertThat(recuperado.getAutores().get(0).getNombreCompleto()).isEqualTo("Gabriel García Márquez");
    }

    @Test
    void testGetAll() {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(1);

        Libro l1 = new Libro();
        l1.setIsbn("111-111");
        l1.setTitulo("Libro A");
        l1.setCategoria(categoria);

        Libro l2 = new Libro();
        l2.setIsbn("222-222");
        l2.setTitulo("Libro B");
        l2.setCategoria(categoria);

        libroDAO.insert(l1);
        libroDAO.insert(l2);

        List<Libro> libros = libroDAO.getAll();

        assertThat(libros).hasSize(2);
        assertThat(libros).extracting(Libro::getTitulo)
                .containsExactlyInAnyOrder("Libro A", "Libro B");
    }

    @Test
    void testUpdate() {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(1);

        Libro libro = new Libro();
        libro.setIsbn("333-333");
        libro.setTitulo("Título Original");
        libro.setCategoria(categoria);

        libroDAO.insert(libro);

        libro.setTitulo("Título Modificado");
        libro.setIsbn("444-444");
        libroDAO.update(libro);

        Libro actualizado = libroDAO.getById(libro.getIdLibro());
        assertThat(actualizado.getTitulo()).isEqualTo("Título Modificado");
        assertThat(actualizado.getIsbn()).isEqualTo("444-444");
    }

    @Test
    void testDelete() {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(1);

        Libro libro = new Libro();
        libro.setIsbn("555-555");
        libro.setTitulo("Libro a borrar");
        libro.setCategoria(categoria);

        libroDAO.insert(libro);
        int idBorrable = libro.getIdLibro();

        libroDAO.delete(idBorrable);

        Libro eliminado = libroDAO.getById(idBorrable);
        assertThat(eliminado).isNull();
    }

    @Test
    void testExistsById() {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(1);

        Libro libro = new Libro();
        libro.setIsbn("666-666");
        libro.setTitulo("Libro Existente");
        libro.setCategoria(categoria);

        libroDAO.insert(libro);

        assertThat(libroDAO.existsById(libro.getIdLibro())).isTrue();
        assertThat(libroDAO.existsById(99999)).isFalse();
    }

    @Test
    void testBuscarPorTitulo() {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(1);

        Libro libro = new Libro();
        libro.setIsbn("777-777");
        libro.setTitulo("Programación en Java Avanzada");
        libro.setCategoria(categoria);

        libroDAO.insert(libro);

        List<Libro> resultados = libroDAO.buscarPorTitulo("Java");

        assertThat(resultados).isNotEmpty();
        assertThat(resultados.get(0).getTitulo()).contains("Java");
    }
}