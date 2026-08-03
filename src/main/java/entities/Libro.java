package entities;

import java.util.List;
import java.util.Objects;

public class Libro{
    private int idLibro;
    private String isbn;
    private String titulo;
    private String descripcion;
    private String imagen;
    private Categoria categoria;
    private List<Autor> autores;

    public Libro() {}

    public Libro(int idLibro, String isbn, String titulo, String descripcion, Categoria categoria, List<Autor> autores) {
        this.idLibro = idLibro;
        this.isbn = isbn;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.autores = autores;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagen() {return imagen;}

    public void setImagen(String imagen) {this.imagen = imagen;}

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public List<Autor> getAutores() {
        return autores;
    }

    public void setAutores(List<Autor> autores) {
        this.autores = autores;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Libro libro = (Libro) o;
        return idLibro == libro.idLibro && Objects.equals(isbn, libro.isbn) && Objects.equals(titulo, libro.titulo) && Objects.equals(descripcion, libro.descripcion) && Objects.equals(categoria, libro.categoria) && Objects.equals(autores, libro.autores);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLibro, isbn, titulo, descripcion, categoria, autores);
    }

    @Override
    public String toString() {
        return "Libro{" +
                "idLibro=" + idLibro +
                ", isbn='" + isbn + '\'' +
                ", titulo='" + titulo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", categoria=" + categoria +
                ", autores=" + autores +
                '}';
    }
}
