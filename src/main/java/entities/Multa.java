package entities;

import enums.EstadoMulta;

import java.time.LocalDate;
import java.util.Objects;

public class Multa {
    private int idMulta;
    private double monto;
    private LocalDate fechaGeneracion;
    private EstadoMulta estado;
    private Prestamo prestamo;
    private Usuario usuario;

    public Multa() {}

    public Multa(int idMulta, double monto, LocalDate fechaGeneracion, EstadoMulta estado, Prestamo prestamo, Usuario usuario) {
        this.idMulta = idMulta;
        this.monto = monto;
        this.fechaGeneracion = fechaGeneracion;
        this.estado = estado;
        this.prestamo = prestamo;
        this.usuario = usuario;
    }

    public int getIdMulta() {
        return idMulta;
    }

    public void setIdMulta(int idMulta) {
        this.idMulta = idMulta;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDate fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public EstadoMulta getEstado() {
        return estado;
    }

    public void setEstado(EstadoMulta estado) {
        this.estado = estado;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Multa multa = (Multa) o;
        return idMulta == multa.idMulta && Double.compare(monto, multa.monto) == 0 && Objects.equals(fechaGeneracion, multa.fechaGeneracion) && estado == multa.estado && Objects.equals(prestamo, multa.prestamo) && Objects.equals(usuario, multa.usuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMulta, monto, fechaGeneracion, estado, prestamo, usuario);
    }

    @Override
    public String toString() {
        return "Multa{" +
                "idMulta=" + idMulta +
                ", monto=" + monto +
                ", fechaGeneracion=" + fechaGeneracion +
                ", estado=" + estado +
                ", prestamo=" + prestamo +
                ", usuario=" + usuario +
                '}';
    }
}
