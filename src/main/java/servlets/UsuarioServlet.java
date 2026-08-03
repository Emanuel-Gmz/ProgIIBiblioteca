package servlets;

import dao.UsuarioDAO;
import entities.Usuario;
import enums.RolUsuario;
import exceptions.UsuarioException;
import utils.PasswordUtil; // Tu utilidad de encriptación existente

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "UsuarioServlet", urlPatterns = {"/usuario"})
public class UsuarioServlet extends HttpServlet {

  private UsuarioDAO usuarioDAO;

  @Override
  public void init() throws ServletException {
    this.usuarioDAO = new UsuarioDAO();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {

    String action = request.getParameter("action");
    if (action == null) {
      action = "login";
    }

    switch (action) {
      case "login":
        request.getRequestDispatcher("/formLogin.jsp").forward(request, response);
        break;
      case "registro":
        request.getRequestDispatcher("/formRegistro.jsp").forward(request, response);
        break;
      case "logout":
        logout(request, response);
        break;
      case "registroAdmin":
        HttpSession session = request.getSession(false);
        Usuario uLogueado = (session != null) ? (Usuario) session.getAttribute("usuarioLogueado") : null;

        if (uLogueado == null || (uLogueado.getRol() != RolUsuario.ADMIN && uLogueado.getRol() != RolUsuario.BIBLIOTECARIO)) {
          response.sendRedirect(request.getContextPath() + "/usuario?action=login");
          return;
        }

        request.getRequestDispatcher("/formRegistro.jsp").forward(request, response);
        break;
      default:
        response.sendRedirect(request.getContextPath() + "/index");
        break;
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {

    request.setCharacterEncoding("UTF-8");

    String action = request.getParameter("action");

    try {
      if ("registrar".equals(action)) {
        registrarUsuario(request, response);
      } else if ("registrarAdmin".equals(action)) {
        registrarUsuarioAdmin(request, response);
      } else if ("iniciarSesion".equals(action)) {
        iniciarSesion(request, response);
      } else {
        response.sendRedirect(request.getContextPath() + "/index");
      }
    } catch (UsuarioException e) {
      request.setAttribute("error", e.getMessage());

      if ("registrarAdmin".equals(action) || "registrar".equals(action)) {
        request.getRequestDispatcher("/formRegistro.jsp").forward(request, response);
      } else {
        request.getRequestDispatcher("/formLogin.jsp").forward(request, response);
      }
    } catch (Exception e) {
      e.printStackTrace();
      request.setAttribute("error", "Ocurrió un error inesperado en el servidor.");
      request.getRequestDispatcher("/error_pantalla.jsp").forward(request, response);
    }
  }

  private void registrarUsuario(HttpServletRequest request, HttpServletResponse response)
          throws Exception {

    String nombre = request.getParameter("nombre");
    String apellido = request.getParameter("apellido");
    String email = request.getParameter("email");
    String telefono = request.getParameter("telefono");
    String password = request.getParameter("password");

    if (nombre == null || email == null || password == null || nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
      throw new UsuarioException("Todos los campos obligatorios deben estar completos.");
    }

    if (usuarioDAO.existsByEmail(email)) {
      throw new UsuarioException("El correo ingresado ya está registrado en la biblioteca.");
    }

    Usuario nuevoUsuario = new Usuario();
    nuevoUsuario.setNombre(nombre);
    nuevoUsuario.setApellido(apellido);
    nuevoUsuario.setEmail(email);
    nuevoUsuario.setTelefono(telefono);

    String hash = PasswordUtil.hashPassword(password);
    nuevoUsuario.setContrasenia(hash);

    nuevoUsuario.setRol(RolUsuario.USUARIO);

    usuarioDAO.insert(nuevoUsuario);

    HttpSession session = request.getSession();
    session.setAttribute("usuarioLogueado", nuevoUsuario);

    response.sendRedirect(request.getContextPath() + "/index");
  }

  private void registrarUsuarioAdmin(HttpServletRequest request, HttpServletResponse response)
          throws Exception {

    HttpSession session = request.getSession(false);
    Usuario uLogueado = (session != null) ? (Usuario) session.getAttribute("usuarioLogueado") : null;

    if (uLogueado == null || (uLogueado.getRol() != RolUsuario.ADMIN && uLogueado.getRol() != RolUsuario.BIBLIOTECARIO)) {
      response.sendRedirect(request.getContextPath() + "/usuario?action=login");
      return;
    }

    String nombre = request.getParameter("nombre");
    String apellido = request.getParameter("apellido");
    String email = request.getParameter("email");
    String telefono = request.getParameter("telefono");
    String password = request.getParameter("password");

    if (nombre == null || email == null || password == null || nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
      throw new UsuarioException("Todos los campos obligatorios deben estar completos.");
    }

    if (usuarioDAO.existsByEmail(email)) {
      throw new UsuarioException("El correo ingresado ya está registrado en la biblioteca.");
    }

    Usuario nuevoUsuario = new Usuario();
    nuevoUsuario.setNombre(nombre);
    nuevoUsuario.setApellido(apellido);
    nuevoUsuario.setEmail(email);
    nuevoUsuario.setTelefono(telefono);
    nuevoUsuario.setContrasenia(PasswordUtil.hashPassword(password));
    nuevoUsuario.setRol(RolUsuario.USUARIO);

    usuarioDAO.insert(nuevoUsuario);

    response.sendRedirect(request.getContextPath() + "/libros?action=listar");
  }

  private void iniciarSesion(HttpServletRequest request, HttpServletResponse response)
          throws Exception {

    String email = request.getParameter("email");
    String password = request.getParameter("password");

    if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
      throw new UsuarioException("Debes ingresar correo y contraseña.");
    }

    Usuario usuario = usuarioDAO.getByEmail(email);

    if (usuario == null) {
      throw new UsuarioException("Credenciales incorrectas. Verifica tu email y contraseña.");
    }

    boolean coincide = PasswordUtil.verifyPassword(password, usuario.getContrasenia());

    if (!coincide) {
      throw new UsuarioException("Credenciales incorrectas. Verifica tu email y contraseña.");
    }

    HttpSession session = request.getSession();
    session.setAttribute("usuarioLogueado", usuario);

    response.sendRedirect(request.getContextPath() + "/index");
  }

  private void logout(HttpServletRequest request, HttpServletResponse response)
          throws IOException {

    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }

    response.sendRedirect(request.getContextPath() + "/usuario?action=login");
  }
}