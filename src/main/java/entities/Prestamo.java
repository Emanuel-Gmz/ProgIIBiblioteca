package entities;

import enums.EstadoPrestamo;

import java.time.LocalDate;
import java.util.Objects;

public class Prestamo {
  private int idPrestamo;
  private LocalDate fechaPrestamo;
  private LocalDate fechaLimite;
  private LocalDate fechaDevolucion;
  private EstadoPrestamo estado;
  private Usuario usuario;
  private Ejemplar ejemplar;

  public Prestamo() {}

  public Prestamo(int idPrestamo, LocalDate fechaPrestamo, LocalDate fechaLimite, LocalDate fechaDevolucion, EstadoPrestamo estado, Usuario usuario, Ejemplar ejemplar) {
    this.idPrestamo = idPrestamo;
    this.fechaPrestamo = fechaPrestamo;
    this.fechaLimite = fechaLimite;
    this.fechaDevolucion = fechaDevolucion;
    this.estado = estado;
    this.usuario = usuario;
    this.ejemplar = ejemplar;
  }

  public int getIdPrestamo() {
    return idPrestamo;
  }

  public void setIdPrestamo(int idPrestamo) {
    this.idPrestamo = idPrestamo;
  }

  public LocalDate getFechaPrestamo() {
    return fechaPrestamo;
  }

  public void setFechaPrestamo(LocalDate fechaPrestamo) {
    this.fechaPrestamo = fechaPrestamo;
  }

  public LocalDate getFechaLimite() {
    return fechaLimite;
  }

  public void setFechaLimite(LocalDate fechaLimite) {
    this.fechaLimite = fechaLimite;
  }

  public LocalDate getFechaDevolucion() {
    return fechaDevolucion;
  }

  public void setFechaDevolucion(LocalDate fechaDevolucion) {
    this.fechaDevolucion = fechaDevolucion;
  }

  public EstadoPrestamo getEstado() {
    return estado;
  }

  public void setEstado(EstadoPrestamo estado) {
    this.estado = estado;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public Ejemplar getEjemplar() {
    return ejemplar;
  }

  public void setEjemplar(Ejemplar ejemplar) {
    this.ejemplar = ejemplar;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Prestamo prestamo = (Prestamo) o;
    return idPrestamo == prestamo.idPrestamo && Objects.equals(fechaPrestamo, prestamo.fechaPrestamo) && Objects.equals(fechaLimite, prestamo.fechaLimite) && Objects.equals(fechaDevolucion, prestamo.fechaDevolucion) && estado == prestamo.estado && Objects.equals(usuario, prestamo.usuario) && Objects.equals(ejemplar, prestamo.ejemplar);
  }

  @Override
  public int hashCode() {
    return Objects.hash(idPrestamo, fechaPrestamo, fechaLimite, fechaDevolucion, estado, usuario, ejemplar);
  }

  @Override
  public String toString() {
    return "Prestamo{" +
            "idPrestamo=" + idPrestamo +
            ", fechaPrestamo=" + fechaPrestamo +
            ", fechaLimite=" + fechaLimite +
            ", fechaDevolucion=" + fechaDevolucion +
            ", estado=" + estado +
            ", usuario=" + usuario +
            ", ejemplar=" + ejemplar +
            '}';
  }
}