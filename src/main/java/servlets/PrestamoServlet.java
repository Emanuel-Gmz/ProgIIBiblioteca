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
import enums.EstadoMulta;
import enums.EstadoPrestamo;
import enums.RolUsuario;
import exceptions.PrestamoException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@WebServlet(name = "PrestamoServlet", urlPatterns = {"/prestamos"})
public class PrestamoServlet extends HttpServlet {

    private PrestamoDAO prestamoDAO;
    private EjemplarDAO ejemplarDAO;
    private UsuarioDAO usuarioDAO;
    private MultaDAO multaDAO; // 👈 1. Declaramos el DAO de Multas

    @Override
    public void init() throws ServletException {
        this.prestamoDAO = new PrestamoDAO();
        this.ejemplarDAO = new EjemplarDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.multaDAO = new MultaDAO(); // 👈 2. Lo inicializamos
    }

    // --- MANEJO DE PETICIONES GET (Historial, Vencidos o Ver Solicitud) ---
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) {
            action = "historial";
        }

        try {
            switch (action) {
                case "historial":
                    verHistorialPrestamos(request, response);
                    break;
                case "vencidos":
                    verPrestamosVencidos(request, response);
                    break;
                case "nuevo":
                    mostrarFormularioPrestamo(request, response);
                    break;
                default:
                    verHistorialPrestamos(request, response);
                    break;
            }
        } catch (PrestamoException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/error_pantalla.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error inesperado al cargar los préstamos.");
            request.getRequestDispatcher("/error_pantalla.jsp").forward(request, response);
        }
    }

    // --- MANEJO DE PETICIONES POST (Registrar Préstamo o Devolución) ---
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try {
            if ("solicitar".equals(action)) {
                solicitarPrestamo(request, response);
            } else if ("devolver".equals(action)) {
                registrarDevolucion(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/prestamos?action=historial");
            }
        } catch (PrestamoException e) {
            request.setAttribute("error", e.getMessage());
            // Recargar datos necesarios si falla el formulario de préstamo
            if ("solicitar".equals(action)) {
                request.setAttribute("ejemplares", ejemplarDAO.getAll());
                request.setAttribute("usuarios", usuarioDAO.getAll());
                request.getRequestDispatcher("/formPrestamo.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/prestamos?action=historial");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error interno al procesar la operación de préstamo.");
            request.getRequestDispatcher("/error_pantalla.jsp").forward(request, response);
        }
    }

    // --- LÓGICA DE NEGOCIO Y ACCIONES ---

    private void verHistorialPrestamos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogueado") : null;

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/usuario?action=login");
            return;
        }

        List<Prestamo> listaPrestamos;
        String busqueda = request.getParameter("busquedaUsuario");
        String modo = request.getParameter("modo"); // 👈 Nuevo parámetro para distinguir vista personal vs general

        // Si es Admin/Bibliotecario PERO explícitamente pide el modo "general" (desde la barra lateral)
        if ((usuario.getRol() == RolUsuario.ADMIN || usuario.getRol() == RolUsuario.BIBLIOTECARIO) && "general".equals(modo)) {
            if (busqueda != null && !busqueda.isEmpty()) {
                listaPrestamos = prestamoDAO.getAll().stream()
                        .filter(p -> (p.getUsuario() != null && p.getUsuario().getNombre() != null && p.getUsuario().getNombre().toLowerCase().contains(busqueda.toLowerCase())) ||
                                (p.getUsuario() != null && p.getUsuario().getApellido() != null && p.getUsuario().getApellido().toLowerCase().contains(busqueda.toLowerCase())))
                        .toList();
            } else {
                listaPrestamos = prestamoDAO.getAll();
            }
            request.setAttribute("esVistaGeneral", true); // Para cambiar el título en la vista si deseas
        } else {
            // Por defecto (si entra desde "Mi Historial"), muestra estrictamente los préstamos del usuario logueado
            listaPrestamos = prestamoDAO.getByUsuario(usuario.getIdUsuario());
            request.setAttribute("esVistaGeneral", false);
        }

        request.setAttribute("listaPrestamos", listaPrestamos);
        request.getRequestDispatcher("/historialPrestamos.jsp").forward(request, response);
    }

    private void verPrestamosVencidos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Solo administradores o bibliotecarios deberían ver esto
        List<Prestamo> vencidos = prestamoDAO.getVencidos();
        request.setAttribute("listaPrestamos", vencidos);
        request.setAttribute("filtroVencidos", true);
        request.getRequestDispatcher("/historialPrestamos.jsp").forward(request, response);
    }

    private void mostrarFormularioPrestamo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idLibroStr = request.getParameter("idLibro");

        // Si viene un idLibro específico, podemos pasarlo como atributo
        if (idLibroStr != null && !idLibroStr.isEmpty()) {
            request.setAttribute("idLibroSeleccionado", Integer.parseInt(idLibroStr));
        }

        request.setAttribute("ejemplares", ejemplarDAO.getAll());
        request.setAttribute("usuarios", usuarioDAO.getAll());
        request.getRequestDispatcher("/formPrestamo.jsp").forward(request, response);
    }

    private void solicitarPrestamo(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String idUsuarioStr = request.getParameter("idUsuario");
        String idEjemplarStr = request.getParameter("idEjemplar");
        String diasPrestamoStr = request.getParameter("diasPrestamo"); // Ej: 7 o 14 días de plazo

        if (idUsuarioStr == null || idEjemplarStr == null) {
            throw new PrestamoException("Debe seleccionar un usuario y un ejemplar físico.");
        }

        int idUsuario = Integer.parseInt(idUsuarioStr);
        int idEjemplar = Integer.parseInt(idEjemplarStr);
        int dias = (diasPrestamoStr != null && !diasPrestamoStr.isEmpty()) ? Integer.parseInt(diasPrestamoStr) : 14;

        // Validar que el ejemplar exista y esté disponible
        Ejemplar ejemplar = ejemplarDAO.getById(idEjemplar);
        if (ejemplar == null || ejemplar.getEstado() != EstadoEjemplar.DISPONIBLE) {
            throw new PrestamoException("El ejemplar seleccionado no se encuentra disponible para préstamo.");
        }

        Usuario usuario = usuarioDAO.getById(idUsuario);
        if (usuario == null) {
            throw new PrestamoException("El usuario seleccionado no existe.");
        }

        // Armar el objeto Préstamo
        Prestamo nuevoPrestamo = new Prestamo();
        nuevoPrestamo.setUsuario(usuario);
        nuevoPrestamo.setEjemplar(ejemplar);
        nuevoPrestamo.setFechaPrestamo(LocalDate.now());
        nuevoPrestamo.setFechaLimite(LocalDate.now().plusDays(dias)); // Fecha tope calculada
        nuevoPrestamo.setEstado(EstadoPrestamo.ACTIVO);

        // Guardar usando la transacción que configuramos en el PrestamoDAO
        prestamoDAO.insert(nuevoPrestamo);

        response.sendRedirect(request.getContextPath() + "/prestamos?action=historial");
    }

    private void registrarDevolucion(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String idPrestamoStr = request.getParameter("idPrestamo");
        if (idPrestamoStr == null || idPrestamoStr.isEmpty()) {
            throw new PrestamoException("ID de préstamo inválido.");
        }

        int idPrestamo = Integer.parseInt(idPrestamoStr);
        Prestamo prestamo = prestamoDAO.getById(idPrestamo);

        if (prestamo == null || prestamo.getEstado() != EstadoPrestamo.ACTIVO) {
            throw new PrestamoException("El préstamo no existe o ya ha sido devuelto anteriormente.");
        }

        // Actualizar datos de devolución
        prestamo.setFechaDevolucion(LocalDate.now());

        // Comprobar si se devolvió tarde para marcarlo y generar la multa automáticamente
        if (LocalDate.now().isAfter(prestamo.getFechaLimite())) {
            prestamo.setEstado(EstadoPrestamo.VENCIDO);

            // Calcular días de retraso y monto de la multa (Ej: $500 por cada día de demora)
            long diasRetraso = ChronoUnit.DAYS.between(prestamo.getFechaLimite(), LocalDate.now());
            double montoMulta = diasRetraso * 500.0;

            // Construir y registrar la multa usando tu MultaDAO[cite: 13, 14]
            Multa nuevaMulta = new Multa();
            nuevaMulta.setPrestamo(prestamo);
            nuevaMulta.setUsuario(prestamo.getUsuario());
            nuevaMulta.setMonto(montoMulta);
            nuevaMulta.setFechaGeneracion(LocalDate.now());
            nuevaMulta.setEstado(EstadoMulta.PENDIENTE);

            multaDAO.insert(nuevaMulta);
        } else {
            prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        }

        // Ejecutar el update transaccional (que liberará el ejemplar a DISPONIBLE)[cite: 14]
        prestamoDAO.update(prestamo);

        response.sendRedirect(request.getContextPath() + "/prestamos?action=historial");
    }
}