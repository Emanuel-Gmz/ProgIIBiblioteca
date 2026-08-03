package servlets;

import dao.MultaDAO;
import entities.Multa;
import entities.Usuario;
import enums.EstadoMulta;
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
class MultaServletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private MultaDAO multaDAO;

    @InjectMocks
    private MultaServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        Field field = MultaServlet.class.getDeclaredField("multaDAO");
        field.setAccessible(true);
        field.set(servlet, multaDAO);
    }

    private Usuario crearUsuarioConRol(RolUsuario rol, int id) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setRol(rol);
        return u;
    }

    @Test
    void doGet_DeberiaListarTodasLasMultas_CuandoUsuarioEsAdmin() throws ServletException, IOException {
        Usuario admin = crearUsuarioConRol(RolUsuario.ADMIN, 1);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioLogueado")).thenReturn(admin);

        when(request.getParameter("action")).thenReturn("listar");
        when(multaDAO.getAll()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher(eq("/listaMultas.jsp"))).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("tituloSeccion"), eq("Gestión General de Multas"));
        verify(request).setAttribute(eq("listaMultas"), any());
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void doGet_DeberiaRedirigirALogin_CuandoNoHaySesionActiva() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/usuario?action=login");
        verify(multaDAO, never()).getAll();
    }

    @Test
    void doPost_DeberiaActualizarEstadoMulta_CuandoAccionEsActualizarEstado() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("actualizarEstado");
        when(request.getParameter("idMulta")).thenReturn("5");
        when(request.getParameter("estado")).thenReturn("PAGADA");
        when(request.getContextPath()).thenReturn("");

        Multa multaMock = new Multa();
        multaMock.setIdMulta(5);
        multaMock.setEstado(EstadoMulta.PENDIENTE);
        when(multaDAO.getById(5)).thenReturn(multaMock);

        servlet.doPost(request, response);

        verify(multaDAO).update(any(Multa.class));
        verify(response).sendRedirect("/multas");
    }
}