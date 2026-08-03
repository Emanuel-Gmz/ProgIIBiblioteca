package servlets;

import dao.LibroDAO;
import dao.PrestamoDAO;
import entities.Libro;
import entities.Usuario;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexServletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private PrestamoDAO prestamoDAO;
    @Mock
    private LibroDAO libroDAO;

    @InjectMocks
    private IndexServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        // Como el IndexServlet instancia los DAO dentro del doGet,
        // podemos asegurarnos de que si usa variables de instancia o si las creamos, funcione.
        // O bien mockeamos los DAOs inyectándolos si estuvieran declarados como atributos.
        // Como en tu IndexServlet.java los instancia localmente dentro del doGet (new PrestamoDAO()),
        // el enfoque ideal es probar el flujo de redirección y manejo de atributos:
        when(request.getRequestDispatcher(eq("/index.jsp"))).thenReturn(requestDispatcher);
    }

    @Test
    void doGet_DeberiaCargarIndexYRedirigir_Exitosamente() throws ServletException, IOException {
        servlet.doGet(request, response);

        verify(request).getRequestDispatcher(eq("/index.jsp"));
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void doPost_DeberiaLlamarDoGet() throws ServletException, IOException {
        servlet.doPost(request, response);

        verify(request).getRequestDispatcher(eq("/index.jsp"));
        verify(requestDispatcher).forward(request, response);
    }
}