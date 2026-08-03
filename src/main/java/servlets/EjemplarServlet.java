package servlets;

import dao.EjemplarDAO;
import dao.LibroDAO;
import entities.Ejemplar;
import entities.Libro;
import enums.EstadoEjemplar;
import entities.Usuario;
import enums.RolUsuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "EjemplarServlet", urlPatterns = {"/ejemplares"})
public class EjemplarServlet extends HttpServlet {

    private EjemplarDAO ejemplarDAO;
    private LibroDAO libroDAO;

    @Override
    public void init() throws ServletException {
        this.ejemplarDAO = new EjemplarDAO();
        this.libroDAO = new LibroDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Validar seguridad: Solo administradores o bibliotecarios pueden gestionar ejemplares
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogueado") : null;

        if (usuario == null || (usuario.getRol() != RolUsuario.ADMIN && usuario.getRol() != RolUsuario.BIBLIOTECARIO)) {
            response.sendRedirect(request.getContextPath() + "/usuario?action=login");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "listar";
        }

        if ("nuevo".equals(action)) {
            // Muestra el formulario para registrar un ejemplar asociado al idLibro recibido por parámetro
            String idLibroStr = request.getParameter("idLibro");
            if (idLibroStr != null && !idLibroStr.isEmpty()) {
                request.setAttribute("idLibro", idLibroStr);
            }
            request.getRequestDispatcher("/formEjemplar.jsp").forward(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/libros?action=listar");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try {
            if ("registrar".equals(action)) {
                String idLibroStr = request.getParameter("idLibro");
                String codigoInventario = request.getParameter("codigoInventario");

                if (idLibroStr == null || codigoInventario == null || idLibroStr.isEmpty() || codigoInventario.isEmpty()) {
                    request.setAttribute("error", "Todos los campos son obligatorios.");
                    request.getRequestDispatcher("/formEjemplar.jsp").forward(request, response);
                    return;
                }

                int idLibro = Integer.parseInt(idLibroStr);
                Libro libro = libroDAO.getById(idLibro);

                if (libro == null) {
                    request.setAttribute("error", "El libro asociado no existe.");
                    request.getRequestDispatcher("/formEjemplar.jsp").forward(request, response);
                    return;
                }

                // Crear el objeto Ejemplar
                Ejemplar nuevoEjemplar = new Ejemplar();
                nuevoEjemplar.setLibro(libro);
                nuevoEjemplar.setCodigoInventario(codigoInventario);
                nuevoEjemplar.setEstado(EstadoEjemplar.DISPONIBLE); // Por defecto nace disponible

                // Guardar en la base de datos usando el DAO
                ejemplarDAO.insert(nuevoEjemplar);

                // Redirigir de vuelta al catálogo con éxito
                response.sendRedirect(request.getContextPath() + "/libros?action=listar");
            } else {
                response.sendRedirect(request.getContextPath() + "/libros?action=listar");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error interno al registrar el ejemplar físico.");
            request.getRequestDispatcher("/formEjemplar.jsp").forward(request, response);
        }
    }
}