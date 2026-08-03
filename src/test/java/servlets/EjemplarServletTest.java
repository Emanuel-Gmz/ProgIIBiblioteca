package servlets;

import dao.EjemplarDAO;
import dao.LibroDAO;
import entities.Ejemplar;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EjemplarServletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private EjemplarDAO ejemplarDAO;
    @Mock
    private LibroDAO libroDAO;

    @InjectMocks
    private EjemplarServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        injectMockField("ejemplarDAO", ejemplarDAO);
        injectMockField("libroDAO", libroDAO);
    }

    private void injectMockField(String fieldName, Object mockInstance) throws Exception {
        Field field = EjemplarServlet.class.getDeclaredField(fieldName);
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
    void doGet_DeberiaMostrarFormularioNuevo_CuandoUsuarioEsAdminYAccionEsNuevo() throws ServletException, IOException {
        Usuario admin = crearAdminSeguro();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioLogueado")).thenReturn(admin);

        when(request.getParameter("action")).thenReturn("nuevo");
        when(request.getParameter("idLibro")).thenReturn("1");
        when(request.getRequestDispatcher(eq("/formEjemplar.jsp"))).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("idLibro"), eq("1"));
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void doGet_DeberiaRedirigirALogin_CuandoUsuarioNoTienePermisos() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/usuario?action=login");
    }

    @Test
    void doPost_DeberiaRegistrarEjemplar_CuandoDatosSonValidos() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("registrar");
        when(request.getParameter("idLibro")).thenReturn("1");
        when(request.getParameter("codigoInventario")).thenReturn("INV-001");
        when(request.getContextPath()).thenReturn("");

        Libro libroMock = new Libro();
        libroMock.setIdLibro(1);
        when(libroDAO.getById(1)).thenReturn(libroMock);

        servlet.doPost(request, response);

        verify(ejemplarDAO).insert(any(Ejemplar.class));
        verify(response).sendRedirect("/libros?action=listar");
    }

    @Test
    void doPost_DeberiaMostrarError_CuandoFaltanCamposObligatorios() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("registrar");
        when(request.getParameter("idLibro")).thenReturn("");
        when(request.getParameter("codigoInventario")).thenReturn("");
        when(request.getRequestDispatcher(eq("/formEjemplar.jsp"))).thenReturn(requestDispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("error"), eq("Todos los campos son obligatorios."));
        verify(requestDispatcher).forward(request, response);
        verify(ejemplarDAO, never()).insert(any());
    }
}