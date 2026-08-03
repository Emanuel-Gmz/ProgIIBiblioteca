package entities;

import enums.EstadoEjemplar;

import java.util.Objects;

public class Ejemplar {
    private int idEjemplar;
    private String codigoInventario;
    private EstadoEjemplar estado;
    private Libro libro;

    public Ejemplar() {}

    public Ejemplar(int idEjemplar, String codigoInventario, EstadoEjemplar estado, Libro libro) {
        this.idEjemplar = idEjemplar;
        this.codigoInventario = codigoInventario;
        this.estado = estado;
        this.libro = libro;
    }

    public int getIdEjemplar() {
        return idEjemplar;
    }

    public void setIdEjemplar(int idEjemplar) {
        this.idEjemplar = idEjemplar;
    }

    public String getCodigoInventario() {
        return codigoInventario;
    }

    public void setCodigoInventario(String codigoInventario) {
        this.codigoInventario = codigoInventario;
    }

    public EstadoEjemplar getEstado() {
        return estado;
    }

    public void setEstado(EstadoEjemplar estado) {
        this.estado = estado;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ejemplar ejemplar = (Ejemplar) o;
        return idEjemplar == ejemplar.idEjemplar && Objects.equals(codigoInventario, ejemplar.codigoInventario) && estado == ejemplar.estado && Objects.equals(libro, ejemplar.libro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEjemplar, codigoInventario, estado, libro);
    }

    @Override
    public String toString() {
        return "Ejemplar{" +
                "idEjemplar=" + idEjemplar +
                ", codigoInventario='" + codigoInventario + '\'' +
                ", estado=" + estado +
                ", libro=" + libro +
                '}';
    }
}
