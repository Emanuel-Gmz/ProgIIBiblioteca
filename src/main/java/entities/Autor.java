package entities;

import java.util.Objects;

public class Autor {
    private int idAutor;
    private String nombreCompleto;
    private String nacionalidad;

    public Autor() {}

    public Autor(int idAutor, String nombreCompleto, String nacionalidad) {
        this.idAutor = idAutor;
        this.nombreCompleto = nombreCompleto;
        this.nacionalidad = nacionalidad;
    }

    public int getIdAutor() {
        return idAutor;
    }

    public void setIdAutor(int idAutor) {
        this.idAutor = idAutor;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Autor autor = (Autor) o;
        return idAutor == autor.idAutor && Objects.equals(nombreCompleto, autor.nombreCompleto) && Objects.equals(nacionalidad, autor.nacionalidad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAutor, nombreCompleto, nacionalidad);
    }

    @Override
    public String toString() {
        return "Autor{" +
                "idAutor=" + idAutor +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", nacionalidad='" + nacionalidad + '\'' +
                '}';
    }
}
