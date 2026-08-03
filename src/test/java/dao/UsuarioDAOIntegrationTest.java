package dao;

import entities.Usuario;
import enums.RolUsuario;
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
class UsuarioDAOIntegrationTest {

    @Container
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("probiblioteca_test")
                    .withUsername("root")
                    .withPassword("root")
                    .withInitScript("init-test.sql");

    private UsuarioDAO usuarioDAO;
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
            stmt.execute("DELETE FROM usuarios;");
            stmt.execute("ALTER TABLE usuarios AUTO_INCREMENT = 1;");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");
        }

        usuarioDAO = new UsuarioDAO(connection);
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
        usuario.setNombre("Carlos");
        usuario.setApellido("López");
        usuario.setEmail("carlos.lopez@mail.com");
        usuario.setTelefono("3482123456");
        usuario.setContrasenia("hashedpass123");
        usuario.setRol(RolUsuario.USUARIO);

        usuarioDAO.insert(usuario);

        assertThat(usuario.getIdUsuario()).isGreaterThan(0);

        Usuario recuperado = usuarioDAO.getById(usuario.getIdUsuario());
        assertThat(recuperado).isNotNull();
        assertThat(recuperado.getNombre()).isEqualTo("Carlos");
        assertThat(recuperado.getEmail()).isEqualTo("carlos.lopez@mail.com");
        assertThat(recuperado.getRol()).isEqualTo(RolUsuario.USUARIO);
    }

    @Test
    void testGetAll() {
        Usuario u1 = new Usuario();
        u1.setNombre("María");
        u1.setApellido("Gómez");
        u1.setEmail("maria@mail.com");
        u1.setContrasenia("1234");
        u1.setRol(RolUsuario.ADMIN);

        Usuario u2 = new Usuario();
        u2.setNombre("Pedro");
        u2.setApellido("Pérez");
        u2.setEmail("pedro@mail.com");
        u2.setContrasenia("1234");
        u2.setRol(RolUsuario.BIBLIOTECARIO);

        usuarioDAO.insert(u1);
        usuarioDAO.insert(u2);

        List<Usuario> lista = usuarioDAO.getAll();
        assertThat(lista).hasSize(2);
    }

    @Test
    void testGetByEmail() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Lucía");
        usuario.setApellido("Benítez");
        usuario.setContrasenia("1234");
        usuario.setEmail("lucia@mail.com");
        usuario.setRol(RolUsuario.USUARIO);

        usuarioDAO.insert(usuario);

        Usuario encontrado = usuarioDAO.getByEmail("lucia@mail.com");
        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getNombre()).isEqualTo("Lucía");
    }

    @Test
    void testExistsByEmail() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Esteban");
        usuario.setApellido("Quito");
        usuario.setContrasenia("1234");
        usuario.setEmail("esteban@mail.com");
        usuario.setRol(RolUsuario.USUARIO);

        usuarioDAO.insert(usuario);

        assertThat(usuarioDAO.existsByEmail("esteban@mail.com")).isTrue();
        assertThat(usuarioDAO.existsByEmail("noexiste@mail.com")).isFalse();
    }
}