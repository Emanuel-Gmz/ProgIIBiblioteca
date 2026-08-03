package entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AutorTest {

    private Autor autor;

    @BeforeEach
    void setUp() {
        autor = new Autor();
    }

    @Test
    void testGetIdAutor() {
        Autor autorConId = new Autor();
        autorConId.setIdAutor(1);

        assertThat(autorConId.getIdAutor()).isEqualTo(1);
    }

    @Test
    void testSetIdAutor() {
        autor.setIdAutor(10);
        assertThat(autor.getIdAutor()).isEqualTo(10);
    }

    @Test
    void testGetNombreCompleto() {
        Autor autorConNombre = new Autor();
        autorConNombre.setNombreCompleto("J. K. Rowling");

        assertThat(autorConNombre.getNombreCompleto()).isEqualTo("J. K. Rowling");
    }

    @Test
    void testSetNombreCompleto() {
        autor.setNombreCompleto("Miguel de Cervantes");
        assertThat(autor.getNombreCompleto()).isEqualTo("Miguel de Cervantes");
    }

    @Test
    void testGetNacionalidad() {
        autor.setNacionalidad("Británica");
        assertThat(autor.getNacionalidad()).isEqualTo("Británica");
    }

    @Test
    void testConstructorDefault() {
        Autor autorDefault = new Autor();
        assertThat(autorDefault.getIdAutor()).isEqualTo(0);
        assertThat(autorDefault.getNombreCompleto()).isNull();
        assertThat(autorDefault.getNacionalidad()).isNull();
    }
}