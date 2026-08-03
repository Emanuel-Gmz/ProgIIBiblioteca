package entities;

import enums.EstadoMulta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MultaTest {

    private Multa multa;

    @BeforeEach
    void setUp() {
        multa = new Multa();
    }

    @Test
    void testGetIdMulta() {
        Multa multaConId = new Multa();
        multaConId.setIdMulta(1);

        assertThat(multaConId.getIdMulta()).isEqualTo(1);
    }

    @Test
    void testSetIdMulta() {
        multa.setIdMulta(10);
        assertThat(multa.getIdMulta()).isEqualTo(10);
    }

    @Test
    void testGetMonto() {
        multa.setMonto(1500.50);
        assertThat(multa.getMonto()).isEqualTo(1500.50);
    }

    @Test
    void testGetFechaGeneracion() {
        LocalDate fecha = LocalDate.of(2026, 6, 10);
        multa.setFechaGeneracion(fecha);

        assertThat(multa.getFechaGeneracion()).isEqualTo(fecha);
    }

    @Test
    void testGetEstado() {
        multa.setEstado(EstadoMulta.PENDIENTE);
        assertThat(multa.getEstado()).isEqualTo(EstadoMulta.PENDIENTE);
    }

    @Test
    void testGetPrestamo() {
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(3);
        multa.setPrestamo(prestamo);

        assertThat(multa.getPrestamo()).isNotNull();
        assertThat(multa.getPrestamo().getIdPrestamo()).isEqualTo(3);
    }

    @Test
    void testGetUsuario() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(2);
        multa.setUsuario(usuario);

        assertThat(multa.getUsuario()).isNotNull();
        assertThat(multa.getUsuario().getIdUsuario()).isEqualTo(2);
    }

    @Test
    void testConstructorDefault() {
        Multa multaDefault = new Multa();
        assertThat(multaDefault.getIdMulta()).isEqualTo(0);
        assertThat(multaDefault.getMonto()).isEqualTo(0.0);
        assertThat(multaDefault.getFechaGeneracion()).isNull();
        assertThat(multaDefault.getEstado()).isNull();
        assertThat(multaDefault.getPrestamo()).isNull();
        assertThat(multaDefault.getUsuario()).isNull();
    }

    @Test
    void testConstructorConParametros() {
        LocalDate fecha = LocalDate.of(2026, 6, 5);
        Prestamo prestamo = new Prestamo();
        Usuario usuario = new Usuario();

        Multa multaCompleta = new Multa(1, 2500.0, fecha, EstadoMulta.PAGADA, prestamo, usuario);

        assertThat(multaCompleta.getIdMulta()).isEqualTo(1);
        assertThat(multaCompleta.getMonto()).isEqualTo(2500.0);
        assertThat(multaCompleta.getFechaGeneracion()).isEqualTo(fecha);
        assertThat(multaCompleta.getEstado()).isEqualTo(EstadoMulta.PAGADA);
        assertThat(multaCompleta.getPrestamo()).isEqualTo(prestamo);
        assertThat(multaCompleta.getUsuario()).isEqualTo(usuario);
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDate fecha = LocalDate.of(2026, 6, 5);
        Prestamo prestamo = new Prestamo();
        Usuario usuario = new Usuario();

        Multa m1 = new Multa(1, 1000.0, fecha, EstadoMulta.PENDIENTE, prestamo, usuario);
        Multa m2 = new Multa(1, 1000.0, fecha, EstadoMulta.PENDIENTE, prestamo, usuario);

        assertThat(m1).isEqualTo(m2);
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
    }
}