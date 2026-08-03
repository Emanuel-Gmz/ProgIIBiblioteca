package servlets;

import dao.UsuarioDAO;
import entities.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.PasswordUtil;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/sesion")
public class SesionServlet extends HttpServlet {

  private UsuarioDAO usuarioDAO = new UsuarioDAO();
  private static Map<String, HttpSession> sesionesActivas = new ConcurrentHashMap<>();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    String cerrarSesionParam = req.getParameter("cerrarSesion");

    if ("true".equals(cerrarSesionParam)) {
      cerrarSesion(req, res);
    } else {
      res.sendRedirect(req.getContextPath() + "/index");
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    iniciarSesion(req, res);
  }

  private void cerrarSesion(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    HttpSession session = req.getSession(false);

    if (session != null && session.getAttribute("usuarioLogueado") != null) {
      session.invalidate();
      res.sendRedirect(req.getContextPath() + "/formLogin.jsp?mensajeExito=Se+ha+cerrado+la+sesion+correctamente.");
    } else {
      res.sendRedirect(req.getContextPath() + "/formLogin.jsp?mensajeError=No+tienes+ninguna+sesion+activa.");
    }
  }

  private void iniciarSesion(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    String email = req.getParameter("email");
    String password = req.getParameter("password");

    String destinoRedirect = validarCredenciales(email, password, req);

    if (destinoRedirect.contains("formLogin.jsp")) {
      res.sendRedirect(req.getContextPath() + "/" + destinoRedirect);
    } else {
      res.sendRedirect(destinoRedirect);
    }
  }

  private String validarCredenciales(String email, String password, HttpServletRequest req) {
    if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
      req.getSession().setAttribute("mensajeError", "Email y contraseña son obligatorios.");
      return "formLogin.jsp";
    }

    email = email.trim();

    if (sesionesActivas.containsKey(email)) {
      try {
        sesionesActivas.get(email).invalidate();
      } catch (IllegalStateException e) {}
    }

    Usuario usuario = usuarioDAO.getByEmail(email);

    if (usuario == null) {
      req.getSession().setAttribute("mensajeError", "Email y/o contraseña incorrecta.");
      return "formLogin.jsp";
    }

    try {
      if (!PasswordUtil.verifyPassword(password, usuario.getContrasenia())) {
        req.getSession().setAttribute("mensajeError", "Email y/o contraseña incorrecta.");
        return "formLogin.jsp";
      }
    } catch (Exception e) {
      System.err.println("Error verificando contraseña: " + e.getMessage());
      req.getSession().setAttribute("mensajeError", "Error en el sistema de autenticación.");
      return "formLogin.jsp";
    }

    HttpSession sesionVieja = req.getSession(false);
    if (sesionVieja != null) {
      sesionVieja.invalidate();
    }

    HttpSession sesion = req.getSession(true);
    sesion.setAttribute("usuarioLogueado", usuario);
    sesionesActivas.put(email, sesion);

    return req.getContextPath() + "/index";
  }
}