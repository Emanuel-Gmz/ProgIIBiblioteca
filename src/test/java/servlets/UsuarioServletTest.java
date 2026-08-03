package servlets;

import dao.UsuarioDAO;
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
import utils.PasswordUtil;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private UsuarioServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        Field field = UsuarioServlet.class.getDeclaredField("usuarioDAO");
        field.setAccessible(true);
        field.set(servlet, usuarioDAO);
    }

    private Usuario crearAdminSeguro() {
        Usuario u = new Usuario();
        u.setIdUsuario(1);
        u.setRol(RolUsuario.ADMIN);
        return u;
    }

    @Test
    void doGet_DeberiaMostrarLogin_PorDefecto() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("login");
        when(request.getRequestDispatcher(eq("/formLogin.jsp"))).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void doGet_DeberiaMostrarRegistro_CuandoAccionEsRegistro() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("registro");
        when(request.getRequestDispatcher(eq("/formRegistro.jsp"))).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void doGet_DeberiaInvalidarSesionYLiquidarLogout_CuandoAccionEsLogout() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("logout");
        when(request.getSession(false)).thenReturn(session);
        when(request.getContextPath()).thenReturn("");

        servlet.doGet(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("/usuario?action=login");
    }

    @Test
    void doPost_DeberiaRegistrarUsuarioPublicamente_CuandoAccionEsRegistrar() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("registrar");
        when(request.getParameter("nombre")).thenReturn("Héctor");
        when(request.getParameter("apellido")).thenReturn("Gómez");
        when(request.getParameter("email")).thenReturn("hector@mail.com");
        when(request.getParameter("telefono")).thenReturn("3794000000");
        when(request.getParameter("password")).thenReturn("123456");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("");

        when(usuarioDAO.existsByEmail("hector@mail.com")).thenReturn(false);

        servlet.doPost(request, response);

        verify(usuarioDAO).insert(any(Usuario.class));
        verify(session).setAttribute(eq("usuarioLogueado"), any(Usuario.class));
        verify(response).sendRedirect("/index");
    }

    @Test
    void doPost_DeberiaIniciarSesion_CuandoCredencialesSonCorrectas() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("iniciarSesion");
        when(request.getParameter("email")).thenReturn("hector@mail.com");
        when(request.getParameter("password")).thenReturn("123456");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("");

        Usuario usuarioMock = new Usuario();
        usuarioMock.setEmail("hector@mail.com");
        usuarioMock.setContrasenia(PasswordUtil.hashPassword("123456"));
        when(usuarioDAO.getByEmail("hector@mail.com")).thenReturn(usuarioMock);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("usuarioLogueado"), eq(usuarioMock));
        verify(response).sendRedirect("/index");
    }
}