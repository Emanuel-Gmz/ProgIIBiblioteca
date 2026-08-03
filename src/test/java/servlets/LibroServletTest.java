package servlets;

import dao.AutorDAO;
import dao.CategoriaDAO;
import dao.LibroDAO;
import entities.Autor;
import entities.Categoria;
import entities.Libro;
import entities.Usuario;
import enums.RolUsuario;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibroServletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private LibroDAO libroDAO;
    @Mock
    private CategoriaDAO categoriaDAO;
    @Mock
    private AutorDAO autorDAO;

    @InjectMocks
    private LibroServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        injectMockField("libroDAO", libroDAO);
        injectMockField("categoriaDAO", categoriaDAO);
        injectMockField("autorDAO", autorDAO);
    }

    private void injectMockField(String fieldName, Object mockInstance) throws Exception {
        Field field = LibroServlet.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(servlet, mockInstance);
    }

    private Usuario crearAdminSeguro() {
        Usuario u = new Usuario();
        u.setIdUsuario(1);
        u.setRol(RolUsuario.ADMIN);
        return u;
    }

    @Test
    void doGet_DeberiaListarLibros_PorDefecto() throws ServletException, IOException {
        when(libroDAO.getAll()).thenReturn(Collections.emptyList());
        when(categoriaDAO.getAll()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher(eq("/catalogo.jsp"))).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("listaLibros"), any());
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void doGet_DeberiaMostrarFormularioNuevo_ConCategoriasYAutores() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("nuevo");
        when(categoriaDAO.getAll()).thenReturn(Collections.emptyList());
        when(autorDAO.getAll()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher(eq("/formNuevoLibro.jsp"))).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("categorias"), any());
        verify(request).setAttribute(eq("listaAutores"), any());
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void doPost_DeberiaInsertarLibro_CuandoDatosSonValidos() throws ServletException, IOException {
        Usuario admin = crearAdminSeguro();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioLogueado")).thenReturn(admin);

        when(request.getParameter("action")).thenReturn("insertar");
        when(request.getParameter("isbn")).thenReturn("978-3-16-148410-0");
        when(request.getParameter("titulo")).thenReturn("Clean Code");
        when(request.getParameter("descripcion")).thenReturn("Libro de buenas prácticas");
        when(request.getParameter("idCategoria")).thenReturn("1");
        when(request.getParameter("idAutor")).thenReturn("1");
        when(request.getContextPath()).thenReturn("");

        when(categoriaDAO.getById(1)).thenReturn(new Categoria(1, "Programación", "Tech"));
        when(autorDAO.getById(1)).thenReturn(new Autor(1, "Robert C. Martin", "Estadounidense"));

        servlet.doPost(request, response);

        verify(libroDAO).insert(any(Libro.class));
        verify(response).sendRedirect("/libros?action=listar");
    }

    @Test
    void doPost_DeberiaLanzarSecurityException_CuandoNoHayPermisos() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getParameter("action")).thenReturn("insertar");

        servlet.doPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(libroDAO, never()).insert(any());
    }
}