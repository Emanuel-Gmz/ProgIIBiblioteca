package entities;

import enums.RolUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
    }

    @Test
    void testGetIdUsuario() {
        Usuario usuarioConId = new Usuario();
        usuarioConId.setIdUsuario(1);

        assertThat(usuarioConId.getIdUsuario()).isEqualTo(1);
    }

    @Test
    void testSetIdUsuario() {
        usuario.setIdUsuario(15);
        assertThat(usuario.getIdUsuario()).isEqualTo(15);
    }

    @Test
    void testGetNombre() {
        usuario.setNombre("Héctor");
        assertThat(usuario.getNombre()).isEqualTo("Héctor");
    }

    @Test
    void testGetEmail() {
        usuario.setEmail("hector@email.com");
        assertThat(usuario.getEmail()).isEqualTo("hector@email.com");
    }

    @Test
    void testGetContrasenia() {
        usuario.setContrasenia("secreto123");
        assertThat(usuario.getContrasenia()).isEqualTo("secreto123");
    }

    @Test
    void testGetRol() {
        usuario.setRol(RolUsuario.ADMIN);
        assertThat(usuario.getRol()).isEqualTo(RolUsuario.ADMIN);
    }

    @Test
    void testConstructorDefault() {
        Usuario usuarioDefault = new Usuario();
        assertThat(usuarioDefault.getIdUsuario()).isEqualTo(0);
        assertThat(usuarioDefault.getNombre()).isNull();
        assertThat(usuarioDefault.getEmail()).isNull();
        assertThat(usuarioDefault.getContrasenia()).isNull();
        assertThat(usuarioDefault.getRol()).isNull();
    }
}