package entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LibroTest {

  private Libro libro;

  @BeforeEach
  void setUp() {
    libro = new Libro();
  }

  @Test
  void testGetIdLibro() {
    Libro libroConId = new Libro();
    libroConId.setIdLibro(1);

    assertThat(libroConId.getIdLibro()).isEqualTo(1);
  }

  @Test
  void testSetIdLibro() {
    libro.setIdLibro(10);
    assertThat(libro.getIdLibro()).isEqualTo(10);
  }

  @Test
  void testGetIsbn() {
    Libro libroConIsbn = new Libro();
    libroConIsbn.setIsbn("978-3-16-148410-0");

    assertThat(libroConIsbn.getIsbn()).isEqualTo("978-3-16-148410-0");
  }

  @Test
  void testSetIsbn() {
    libro.setIsbn("123-456-789");
    assertThat(libro.getIsbn()).isEqualTo("123-456-789");
  }

  @Test
  void testGetTitulo() {
    Libro libroConTitulo = new Libro();
    libroConTitulo.setTitulo("Don Quijote de la Mancha");

    assertThat(libroConTitulo.getTitulo()).isEqualTo("Don Quijote de la Mancha");
  }

  @Test
  void testSetTitulo() {
    libro.setTitulo("La Odisea");
    assertThat(libro.getTitulo()).isEqualTo("La Odisea");
  }

  @Test
  void testGetDescripcion() {
    libro.setDescripcion("Una obra clásica de la literatura.");
    assertThat(libro.getDescripcion()).isEqualTo("Una obra clásica de la literatura.");
  }

  @Test
  void testGetCategoria() {
    Categoria categoria = new Categoria(1, "Ficción", "Libros de ficción");
    libro.setCategoria(categoria);

    assertThat(libro.getCategoria()).isNotNull();
    assertThat(libro.getCategoria().getNombre()).isEqualTo("Ficción");
  }

  @Test
  void testGetAutores() {
    List<Autor> autores = new ArrayList<>();
    Autor autor = new Autor();
    autor.setNombreCompleto("Miguel de Cervantes");
    autores.add(autor);

    libro.setAutores(autores);

    assertThat(libro.getAutores()).isNotEmpty();
    assertThat(libro.getAutores().get(0).getNombreCompleto()).isEqualTo("Miguel de Cervantes");
  }

  @Test
  void testConstructorDefault() {
    Libro libroDefault = new Libro();
    assertThat(libroDefault.getIdLibro()).isEqualTo(0);
    assertThat(libroDefault.getTitulo()).isNull();
    assertThat(libroDefault.getCategoria()).isNull();
    assertThat(libroDefault.getAutores()).isNull();
  }
}