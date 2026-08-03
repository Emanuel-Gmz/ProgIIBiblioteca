package dao;

import entities.Categoria;
import entities.Libro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LibroDAOTest {

    private LibroDAO libroDAO;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private ResultSet mockAutoresResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        libroDAO = spy(new LibroDAO());

        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockAutoresResultSet = mock(ResultSet.class);

        doReturn(mockConnection).when(libroDAO).obtenerConexion();

        // Mock genérico para consultas secundarias de autores dentro del mapeo
        when(mockConnection.prepareStatement(eq("SELECT a.idAutor, a.nombreCompleto, a.nacionalidad FROM autores a JOIN libros_autores la ON a.idAutor = la.idAutor WHERE la.idLibro = ?")))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockAutoresResultSet);
        when(mockAutoresResultSet.next()).thenReturn(false); // Sin autores por defecto
    }

    @Test
    void testGetAll() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(LibroDAO.SQL_GETALL)).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Simular datos en el ResultSet principal
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("idLibro")).thenReturn(1);
        when(mockResultSet.getString("ISBN")).thenReturn("978-3-16-148410-0");
        when(mockResultSet.getString("titulo")).thenReturn("El Quijote");
        when(mockResultSet.getString("descripcion")).thenReturn("Libro de caballería");
        when(mockResultSet.getString("imagen")).thenReturn("quijote.jpg");
        when(mockResultSet.getInt("idCategoria")).thenReturn(1);
        when(mockResultSet.getString("nombreCategoria")).thenReturn("Ficción");
        when(mockResultSet.getString("descCategoria")).thenReturn("Novelas");

        // Act
        List<Libro> libros = libroDAO.getAll();

        // Assert
        assertThat(libros).hasSize(1);
        Libro libro = libros.get(0);
        assertThat(libro.getIdLibro()).isEqualTo(1);
        assertThat(libro.getTitulo()).isEqualTo("El Quijote");
        assertThat(libro.getIsbn()).isEqualTo("978-3-16-148410-0");
        assertThat(libro.getCategoria().getNombre()).isEqualTo("Ficción");

        // Verificar interacciones (sin forzar el cierre exacto del resultset que ocurre por duplicado)
        verify(mockConnection).prepareStatement(LibroDAO.SQL_GETALL);
        verify(mockPreparedStatement, atLeastOnce()).executeQuery();
        verify(mockPreparedStatement, atLeastOnce()).close();
        verify(mockConnection, atLeastOnce()).close();
    }

    @Test
    void testGetById() throws SQLException {
        // Arrange
        String sqlGetById = LibroDAO.SQL_GETALL + " WHERE l.idLibro = ?";
        when(mockConnection.prepareStatement(sqlGetById)).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        when(mockResultSet.getInt("idLibro")).thenReturn(1);
        when(mockResultSet.getString("ISBN")).thenReturn("123-456");
        when(mockResultSet.getString("titulo")).thenReturn("La Odisea");
        when(mockResultSet.getString("descripcion")).thenReturn("Epopeya griega");
        when(mockResultSet.getString("imagen")).thenReturn("odisea.jpg");
        when(mockResultSet.getInt("idCategoria")).thenReturn(2);
        when(mockResultSet.getString("nombreCategoria")).thenReturn("Educativo");
        when(mockResultSet.getString("descCategoria")).thenReturn("Estudio");

        // Act
        Libro libro = libroDAO.getById(1);

        // Assert
        assertThat(libro).isNotNull();
        assertThat(libro.getIdLibro()).isEqualTo(1);
        assertThat(libro.getTitulo()).isEqualTo("La Odisea");

        // Corregido: se ejecuta 2 veces (una para el libro y otra para sus autores)
        verify(mockPreparedStatement, times(2)).setInt(1, 1);
    }

    @Test
    void testInsert() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(45);

        Categoria categoria = new Categoria(1, "Ficción", "Desc");

        Libro libro = new Libro();
        libro.setIdLibro(0);
        libro.setIsbn("987-654");
        libro.setTitulo("Harry Potter");
        libro.setDescripcion("Magia y aventuras");
        libro.setImagen("hp.jpg");
        libro.setCategoria(categoria);
        libro.setAutores(new ArrayList<>());

        // Act
        libroDAO.insert(libro);

        // Assert
        assertThat(libro.getIdLibro()).isEqualTo(45);
        verify(mockPreparedStatement).setString(1, "987-654");
        verify(mockPreparedStatement).setString(2, "Harry Potter");
        verify(mockPreparedStatement).setInt(5, 1); // idCategoria
        verify(mockPreparedStatement).executeUpdate();
        verify(mockGeneratedKeys).getInt(1);
    }

    @Test
    void testExistsById_True() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement("SELECT 1 FROM libros WHERE idLibro = ? LIMIT 1")).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        // Act
        boolean exists = libroDAO.existsById(1);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void testDelete() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement("DELETE FROM libros WHERE idLibro = ?")).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        libroDAO.delete(1);

        // Assert
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeUpdate();
    }
}