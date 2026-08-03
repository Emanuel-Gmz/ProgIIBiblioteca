package servlets;

import dao.AutorDAO;
import entities.Autor;
import enums.RolUsuario;
import exceptions.AutorException;
import entities.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "AutorServlet", urlPatterns = {"/autores"})
public class AutorServlet extends HttpServlet {

    private AutorDAO autorDAO;

    @Override
    public void init() throws ServletException {
        this.autorDAO = new AutorDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) {
            action = "listar";
        }

        try {
            switch (action) {
                case "listar":
                    listarAutores(request, response);
                    break;
                case "nuevo":
                    mostrarFormularioNuevo(request, response);
                    break;
                default:
                    listarAutores(request, response);
                    break;
            }
        } catch (AutorException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/error_pantalla.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Ocurrió un error inesperado al cargar los autores.");
            request.getRequestDispatcher("/error_pantalla.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try {
            if ("insertar".equals(action)) {
                verificarPermisosAdmin(request);
                insertarAutor(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/autores?action=listar");
            }
        } catch (AutorException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/formAutor.jsp").forward(request, response);
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error interno al procesar el autor.");
            request.getRequestDispatcher("/error_pantalla.jsp").forward(request, response);
        }
    }

    private void listarAutores(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Autor> listaAutores = autorDAO.getAll();
        request.setAttribute("listaAutores", listaAutores);
        request.getRequestDispatcher("/listaAutores.jsp").forward(request, response);
    }

    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/formAutor.jsp").forward(request, response);
    }

    private void insertarAutor(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String nombreCompleto = request.getParameter("nombreCompleto");
        String nacionalidad = request.getParameter("nacionalidad");

        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            throw new AutorException("El nombre completo del autor es obligatorio.");
        }

        Autor nuevoAutor = new Autor();
        nuevoAutor.setNombreCompleto(nombreCompleto);
        nuevoAutor.setNacionalidad(nacionalidad);

        autorDAO.insert(nuevoAutor);

        response.sendRedirect(request.getContextPath() + "/libros?action=nuevo");
    }

    private void verificarPermisosAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogueado") : null;

        if (usuario == null || (usuario.getRol() != RolUsuario.ADMIN && usuario.getRol() != RolUsuario.BIBLIOTECARIO)) {
            throw new SecurityException("Acceso denegado. No tienes permisos para realizar esta acción.");
        }
    }
}