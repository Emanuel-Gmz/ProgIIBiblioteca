package servlets;

import dao.EjemplarDAO;
import dao.MultaDAO;
import dao.PrestamoDAO;
import dao.UsuarioDAO;
import entities.Ejemplar;
import entities.Multa;
import entities.Prestamo;
import entities.Usuario;
import enums.EstadoEjemplar;
import enums.EstadoPrestamo;
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
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrestamoServletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private PrestamoDAO prestamoDAO;
    @Mock
    private EjemplarDAO ejemplarDAO;
    @Mock
    private UsuarioDAO usuarioDAO;
    @Mock
    private MultaDAO multaDAO;

    @InjectMocks
    private PrestamoServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        injectMockField("prestamoDAO", prestamoDAO);
        injectMockField("ejemplarDAO", ejemplarDAO);
        injectMockField("usuarioDAO", usuarioDAO);
        injectMockField("multaDAO", multaDAO);
    }

    private void injectMockField(String fieldName, Object mockInstance) throws Exception {
        Field field = PrestamoServlet.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(servlet, mockInstance);
    }

    private Usuario crearUsuarioConRol(RolUsuario rol, int id) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setRol(rol);
        return u;
    }

    @Test
    void doGet_DeberiaMostrarHistorialPersonal_CuandoUsuarioEstaLogueado() throws ServletException, IOException {
        Usuario user = crearUsuarioConRol(RolUsuario.USUARIO, 1);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioLogueado")).thenReturn(user);

        when(request.getParameter("action")).thenReturn("historial");
        when(prestamoDAO.getByUsuario(1)).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher(eq("/historialPrestamos.jsp"))).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("listaPrestamos"), any());
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void doGet_DeberiaRedirigirALogin_CuandoNoHaySesionActiva() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/usuario?action=login");
        verify(prestamoDAO, never()).getByUsuario(anyInt());
    }

    @Test
    void doGet_DeberiaMostrarPrestamosVencidos_CuandoAccionEsVencidos() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("vencidos");
        when(prestamoDAO.getVencidos()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher(eq("/historialPrestamos.jsp"))).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("filtroVencidos"), eq(true));
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void doPost_DeberiaSolicitarPrestamo_CuandoDatosSonValidos() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("solicitar");
        when(request.getParameter("idUsuario")).thenReturn("1");
        when(request.getParameter("idEjemplar")).thenReturn("5");
        when(request.getParameter("diasPrestamo")).thenReturn("10");
        when(request.getContextPath()).thenReturn("");

        Ejemplar ejemplarMock = new Ejemplar();
        ejemplarMock.setIdEjemplar(5);
        ejemplarMock.setEstado(EstadoEjemplar.DISPONIBLE);
        when(ejemplarDAO.getById(5)).thenReturn(ejemplarMock);

        Usuario usuarioMock = new Usuario();
        usuarioMock.setIdUsuario(1);
        when(usuarioDAO.getById(1)).thenReturn(usuarioMock);

        servlet.doPost(request, response);

        verify(prestamoDAO).insert(any(Prestamo.class));
        verify(response).sendRedirect("/prestamos?action=historial");
    }

    @Test
    void doPost_DeberiaRegistrarDevolucionATiempo_CuandoAccionEsDevolver() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("devolver");
        when(request.getParameter("idPrestamo")).thenReturn("3");
        when(request.getContextPath()).thenReturn("");

        Prestamo prestamoMock = new Prestamo();
        prestamoMock.setIdPrestamo(3);
        prestamoMock.setEstado(EstadoPrestamo.ACTIVO);
        prestamoMock.setFechaLimite(LocalDate.now().plusDays(5)); // Vence en el futuro (a tiempo)
        when(prestamoDAO.getById(3)).thenReturn(prestamoMock);

        servlet.doPost(request, response);

        verify(multaDAO, never()).insert(any(Multa.class)); // No genera multa
        verify(prestamoDAO).update(any(Prestamo.class));
        verify(response).sendRedirect("/prestamos?action=historial");
    }
}