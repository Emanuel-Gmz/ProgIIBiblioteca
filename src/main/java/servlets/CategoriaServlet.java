package servlets;

import dao.CategoriaDAO;
import entities.Categoria;
import enums.RolUsuario;
import entities.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "CategoriaServlet", urlPatterns = {"/categorias"})
public class CategoriaServlet extends HttpServlet {

    private CategoriaDAO categoriaDAO;

    @Override
    public void init() throws ServletException {
        this.categoriaDAO = new CategoriaDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) {
            action = "listar";
        }

        switch (action) {
            case "listar":
                listarCategorias(request, response);
                break;
            case "nuevo":
                verificarPermisosAdmin(request);
                request.getRequestDispatcher("/formCategoria.jsp").forward(request, response);
                break;
            default:
                listarCategorias(request, response);
                break;
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

                String nombre = request.getParameter("nombre");
                if (nombre == null || nombre.trim().isEmpty()) {
                    request.setAttribute("error", "El nombre de la categoría es obligatorio.");
                    request.getRequestDispatcher("/formCategoria.jsp").forward(request, response);
                    return;
                }

                Categoria nuevaCategoria = new Categoria();
                nuevaCategoria.setNombre(nombre.trim());

                categoriaDAO.insert(nuevaCategoria);

                // Redirigir al listado o volver al formulario de libros
                response.sendRedirect(request.getContextPath() + "/libros?action=nuevo");
            } else {
                response.sendRedirect(request.getContextPath() + "/index.jsp");
            }
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error al registrar la categoría.");
            request.getRequestDispatcher("/formCategoria.jsp").forward(request, response);
        }
    }

    private void listarCategorias(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Categoria> lista = categoriaDAO.getAll();
        request.setAttribute("listaCategorias", lista);
        request.getRequestDispatcher("/listaCategorias.jsp").forward(request, response);
    }

    private void verificarPermisosAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogueado") : null;

        if (usuario == null || (usuario.getRol() != RolUsuario.ADMIN && usuario.getRol() != RolUsuario.BIBLIOTECARIO)) {
            throw new SecurityException("Acceso denegado. No tienes permisos para gestionar categorías.");
        }
    }
}