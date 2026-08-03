package entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PrestamoTest {

    private Prestamo prestamo;

    @BeforeEach
    void setUp() {
        prestamo = new Prestamo();
    }

    @Test
    void testGetIdPrestamo() {
        Prestamo prestamoConId = new Prestamo();
        prestamoConId.setIdPrestamo(1);

        assertThat(prestamoConId.getIdPrestamo()).isEqualTo(1);
    }

    @Test
    void testSetIdPrestamo() {
        prestamo.setIdPrestamo(8);
        assertThat(prestamo.getIdPrestamo()).isEqualTo(8);
    }

    @Test
    void testGetFechaPrestamo() {
        LocalDate fecha = LocalDate.of(2026, 6, 1);
        prestamo.setFechaPrestamo(fecha);

        assertThat(prestamo.getFechaPrestamo()).isEqualTo(fecha);
    }

    @Test
    void testGetFechaDevolucion() {
        LocalDate fechaDev = LocalDate.of(2026, 6, 15);
        prestamo.setFechaDevolucion(fechaDev);

        assertThat(prestamo.getFechaDevolucion()).isEqualTo(fechaDev);
    }

    @Test
    void testGetUsuario() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNombre("Héctor");

        prestamo.setUsuario(usuario);

        assertThat(prestamo.getUsuario()).isNotNull();
        assertThat(prestamo.getUsuario().getNombre()).isEqualTo("Héctor");
    }

    @Test
    void testConstructorDefault() {
        Prestamo prestamoDefault = new Prestamo();
        assertThat(prestamoDefault.getIdPrestamo()).isEqualTo(0);
        assertThat(prestamoDefault.getFechaPrestamo()).isNull();
        assertThat(prestamoDefault.getFechaDevolucion()).isNull();
        assertThat(prestamoDefault.getUsuario()).isNull();
    }
}