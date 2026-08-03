package servlets;

import dao.CategoriaDAO;
import entities.Categoria;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private CategoriaDAO categoriaDAO;

    @InjectMocks
    private CategoriaServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        Field field = CategoriaServlet.class.getDeclaredField("categoriaDAO");
        field.setAccessible(true);
        field.set(servlet, categoriaDAO);
    }

    private Usuario crearAdminSeguro() {
        Usuario u = new Usuario();
        u.setIdUsuario(1);
        u.setRol(RolUsuario.ADMIN);
        return u;
    }

    @Test
    void doGet_DeberiaListarCategorias_PorDefecto() throws ServletException, IOException {
        List<Categoria> lista = Collections.singletonList(new Categoria(1, "Ficción", "Novelas"));
        when(categoriaDAO.getAll()).thenReturn(lista);
        when(request.getRequestDispatcher(eq("/listaCategorias.jsp"))).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("listaCategorias"), eq(lista));
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void doGet_DeberiaMostrarFormularioNuevo_CuandoUsuarioEsAdmin() throws ServletException, IOException {
        Usuario admin = crearAdminSeguro();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioLogueado")).thenReturn(admin);

        when(request.getParameter("action")).thenReturn("nuevo");
        when(request.getRequestDispatcher(eq("/formCategoria.jsp"))).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void doPost_DeberiaInsertarCategoria_CuandoDatosSonValidos() throws ServletException, IOException {
        Usuario admin = crearAdminSeguro();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioLogueado")).thenReturn(admin);

        when(request.getParameter("action")).thenReturn("insertar");
        when(request.getParameter("nombre")).thenReturn("Terror");
        when(request.getContextPath()).thenReturn("");

        servlet.doPost(request, response);

        verify(categoriaDAO).insert(any(Categoria.class));
        verify(response).sendRedirect("/libros?action=nuevo");
    }

    @Test
    void doPost_DeberiaMostrarError_CuandoNombreEsVacio() throws ServletException, IOException {
        Usuario admin = crearAdminSeguro();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioLogueado")).thenReturn(admin);

        when(request.getParameter("action")).thenReturn("insertar");
        when(request.getParameter("nombre")).thenReturn("");
        when(request.getRequestDispatcher(eq("/formCategoria.jsp"))).thenReturn(requestDispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("error"), eq("El nombre de la categoría es obligatorio."));
        verify(requestDispatcher).forward(request, response);
        verify(categoriaDAO, never()).insert(any());
    }

    @Test
    void doPost_DeberiaLanzarSecurityException_CuandoNoHayPermisos() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getParameter("action")).thenReturn("insertar");

        servlet.doPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(categoriaDAO, never()).insert(any());
    }
}