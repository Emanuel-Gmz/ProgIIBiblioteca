package entities;

import enums.EstadoEjemplar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EjemplarTest {

    private Ejemplar ejemplar;

    @BeforeEach
    void setUp() {
        ejemplar = new Ejemplar();
    }

    @Test
    void testGetIdEjemplar() {
        Ejemplar ejemplarConId = new Ejemplar();
        ejemplarConId.setIdEjemplar(1);

        assertThat(ejemplarConId.getIdEjemplar()).isEqualTo(1);
    }

    @Test
    void testSetIdEjemplar() {
        ejemplar.setIdEjemplar(15);
        assertThat(ejemplar.getIdEjemplar()).isEqualTo(15);
    }

    @Test
    void testGetCodigoInventario() {
        ejemplar.setCodigoInventario("INV-001");
        assertThat(ejemplar.getCodigoInventario()).isEqualTo("INV-001");
    }

    @Test
    void testGetEstado() {
        ejemplar.setEstado(EstadoEjemplar.DISPONIBLE);
        assertThat(ejemplar.getEstado()).isEqualTo(EstadoEjemplar.DISPONIBLE);
    }

    @Test
    void testGetLibro() {
        Libro libro = new Libro();
        libro.setIdLibro(1);
        libro.setTitulo("El Quijote");

        ejemplar.setLibro(libro);

        assertThat(ejemplar.getLibro()).isNotNull();
        assertThat(ejemplar.getLibro().getTitulo()).isEqualTo("El Quijote");
    }

    @Test
    void testConstructorDefault() {
        Ejemplar ejemplarDefault = new Ejemplar();
        assertThat(ejemplarDefault.getIdEjemplar()).isEqualTo(0);
        assertThat(ejemplarDefault.getCodigoInventario()).isNull();
        assertThat(ejemplarDefault.getEstado()).isNull();
        assertThat(ejemplarDefault.getLibro()).isNull();
    }

    @Test
    void testConstructorConParametros() {
        Libro libro = new Libro();
        libro.setIdLibro(2);

        Ejemplar ejemplarCompleto = new Ejemplar(5, "INV-999", EstadoEjemplar.PRESTADO, libro);

        assertThat(ejemplarCompleto.getIdEjemplar()).isEqualTo(5);
        assertThat(ejemplarCompleto.getCodigoInventario()).isEqualTo("INV-999");
        assertThat(ejemplarCompleto.getEstado()).isEqualTo(EstadoEjemplar.PRESTADO);
        assertThat(ejemplarCompleto.getLibro()).isEqualTo(libro);
    }

    @Test
    void testEqualsAndHashCode() {
        Libro libro = new Libro();
        Ejemplar e1 = new Ejemplar(1, "INV-001", EstadoEjemplar.DISPONIBLE, libro);
        Ejemplar e2 = new Ejemplar(1, "INV-001", EstadoEjemplar.DISPONIBLE, libro);

        assertThat(e1).isEqualTo(e2);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
    }
}