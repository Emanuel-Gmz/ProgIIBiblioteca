package servlets;

import dao.LibroDAO;
import dao.PrestamoDAO;
import entities.Libro;
import entities.Usuario;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "IndexServlet", urlPatterns = {"/index"})
public class IndexServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Obtener el Top 3 de lectores destacados
            PrestamoDAO prestamoDAO = new PrestamoDAO();
            List<Usuario> topUsuarios = prestamoDAO.getTop3UsuariosConMasPrestamos();
            request.setAttribute("topUsuarios", topUsuarios);

            // Obtener los últimos libros agregados para la sección de novedades
            LibroDAO libroDAO = new LibroDAO();
            List<Libro> ultimosLibros = libroDAO.obtenerUltimosAgregados(4);
            request.setAttribute("ultimosLibros", ultimosLibros);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Redirige al archivo físico de la vista de inicio
        RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}