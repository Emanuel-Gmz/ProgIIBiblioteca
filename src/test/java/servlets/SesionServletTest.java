package servlets;

import dao.UsuarioDAO;
import entities.Usuario;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SesionServletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private SesionServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        Field field = SesionServlet.class.getDeclaredField("usuarioDAO");
        field.setAccessible(true);
        field.set(servlet, usuarioDAO);
    }

    @Test
    void doGet_DeberiaCerrarSesion_CuandoParametroEsVerdadero() throws ServletException, IOException {
        when(request.getParameter("cerrarSesion")).thenReturn("true");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioLogueado")).thenReturn(new Usuario());
        when(request.getContextPath()).thenReturn("");

        servlet.doGet(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect(contains("formLogin.jsp"));
    }

    @Test
    void doGet_DeberiaRedirigirAIndex_CuandoParametroCerrarSesionNoExiste() throws ServletException, IOException {
        when(request.getParameter("cerrarSesion")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/index");
    }

    @Test
    void doPost_DeberiaDevolverError_CuandoCredencialesEstanVacias() throws ServletException, IOException {
        when(request.getParameter("email")).thenReturn("");
        when(request.getParameter("password")).thenReturn("");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("");

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("mensajeError"), eq("Email y contraseña son obligatorios."));
        verify(response).sendRedirect("/formLogin.jsp");
    }

    @Test
    void doPost_DeberiaDevolverError_CuandoUsuarioNoExiste() throws ServletException, IOException {
        when(request.getParameter("email")).thenReturn("noexiste@mail.com");
        when(request.getParameter("password")).thenReturn("123456");
        when(request.getSession()).thenReturn(session);
        when(usuarioDAO.getByEmail("noexiste@mail.com")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("mensajeError"), eq("Email y/o contraseña incorrecta."));
        verify(response).sendRedirect("/formLogin.jsp");
    }
}