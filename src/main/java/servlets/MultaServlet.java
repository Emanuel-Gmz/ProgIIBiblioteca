package servlets;

import dao.MultaDAO;
import entities.Multa;
import entities.Usuario;
import enums.EstadoMulta;
import enums.RolUsuario;
import exceptions.MultaException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "MultaServlet", urlPatterns = {"/multas"})
public class MultaServlet extends HttpServlet {

    private MultaDAO multaDAO;

    @Override
    public void init() throws ServletException {
        this.multaDAO = new MultaDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogueado") : null;

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/usuario?action=login");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "listar";
        }

        try {
            List<Multa> listaMultas;

            switch (action) {
                case "misMultas":
                    listaMultas = multaDAO.getPendientesByUsuario(usuario.getIdUsuario());
                    request.setAttribute("tituloSeccion", "Mis Multas Pendientes");
                    break;

                case "listar":
                default:
                    if (usuario.getRol() == RolUsuario.ADMIN || usuario.getRol() == RolUsuario.BIBLIOTECARIO) {
                        listaMultas = multaDAO.getAll();
                        request.setAttribute("tituloSeccion", "Gestión General de Multas");
                    } else {
                        listaMultas = multaDAO.getByUsuario(usuario.getIdUsuario());
                        request.setAttribute("tituloSeccion", "Mi Historial de Multas");
                    }
                    break;
            }

            request.setAttribute("listaMultas", listaMultas);
            request.getRequestDispatcher("/listaMultas.jsp").forward(request, response);

        } catch (MultaException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/error_pantalla.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error inesperado al cargar las multas.");
            request.getRequestDispatcher("/error_pantalla.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try {
            if ("actualizarEstado".equals(action)) {
                int idMulta = Integer.parseInt(request.getParameter("idMulta"));
                String nuevoEstadoStr = request.getParameter("estado");

                Multa multa = multaDAO.getById(idMulta);
                if (multa != null) {
                    multa.setEstado(EstadoMulta.valueOf(nuevoEstadoStr));
                    multaDAO.update(multa);
                }
            }
            response.sendRedirect(request.getContextPath() + "/multas");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error al actualizar el estado de la multa.");
            request.getRequestDispatcher("/error_pantalla.jsp").forward(request, response);
        }
    }
}