package entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoriaTest {

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
    }

    @Test
    void testGetIdCategoria() {
        Categoria catConId = new Categoria();
        catConId.setIdCategoria(1);

        assertThat(catConId.getIdCategoria()).isEqualTo(1);
    }

    @Test
    void testSetIdCategoria() {
        categoria.setIdCategoria(5);
        assertThat(categoria.getIdCategoria()).isEqualTo(5);
    }

    @Test
    void testGetNombre() {
        Categoria catConNombre = new Categoria();
        catConNombre.setNombre("Educativo");

        assertThat(catConNombre.getNombre()).isEqualTo("Educativo");
    }

    @Test
    void testSetNombre() {
        categoria.setNombre("Fantasía");
        assertThat(categoria.getNombre()).isEqualTo("Fantasía");
    }

    @Test
    void testGetDescripcion() {
        categoria.setDescripcion("Libros de literatura fantástica.");
        assertThat(categoria.getDescripcion()).isEqualTo("Libros de literatura fantástica.");
    }

    @Test
    void testConstructorDefault() {
        Categoria catDefault = new Categoria();
        assertThat(catDefault.getIdCategoria()).isEqualTo(0);
        assertThat(catDefault.getNombre()).isNull();
        assertThat(catDefault.getDescripcion()).isNull();
    }

    @Test
    void testConstructorConParametros() {
        Categoria catCompleta = new Categoria(2, "Ciencia Ficción", "Libros de ciencia y futuro");

        assertThat(catCompleta.getIdCategoria()).isEqualTo(2);
        assertThat(catCompleta.getNombre()).isEqualTo("Ciencia Ficción");
        assertThat(catCompleta.getDescripcion()).isEqualTo("Libros de ciencia y futuro");
    }
}