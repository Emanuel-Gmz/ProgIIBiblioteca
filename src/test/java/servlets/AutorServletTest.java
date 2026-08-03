package servlets;

import dao.AutorDAO;
import entities.Autor;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutorServletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private AutorDAO autorDAO;

    @InjectMocks
    private AutorServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        injectMockField("autorDAO", autorDAO);
    }

    private void injectMockField(String fieldName, Object mockInstance) throws Exception {
        Field field = AutorServlet.class.getDeclaredField(fieldName);
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
    void doGet_DeberiaListarAutores_PorDefecto() throws ServletException, IOException {
        List<Autor> listaAutores = Collections.singletonList(new Autor(1, "Gabriel García Márquez", "Colombiana"));
        when(autorDAO.getAll()).thenReturn(listaAutores);
        when(request.getRequestDispatcher(eq("/listaAutores.jsp"))).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("listaAutores"), eq(listaAutores));
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void doPost_DeberiaInsertarAutor_CuandoAccionEsInsertar() throws ServletException, IOException {
        Usuario admin = crearAdminSeguro();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioLogueado")).thenReturn(admin);

        when(request.getParameter("action")).thenReturn("insertar");
        when(request.getParameter("nombreCompleto")).thenReturn("Isabel Allende");
        when(request.getParameter("nacionalidad")).thenReturn("Chilena");
        when(request.getContextPath()).thenReturn("");

        servlet.doPost(request, response);

        verify(autorDAO).insert(any(Autor.class));
        verify(response).sendRedirect("/libros?action=nuevo");
    }

    @Test
    void doPost_DeberiaLanzarSecurityException_CuandoUsuarioNoTienePermisos() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null); // Sin sesión
        when(request.getParameter("action")).thenReturn("insertar");

        servlet.doPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(autorDAO, never()).insert(any());
    }

    @Test
    void doGet_DeberiaMostrarFormularioNuevo_CuandoAccionEsNuevo() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("nuevo");
        when(request.getRequestDispatcher(eq("/formAutor.jsp"))).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(requestDispatcher).forward(request, response);
    }
}