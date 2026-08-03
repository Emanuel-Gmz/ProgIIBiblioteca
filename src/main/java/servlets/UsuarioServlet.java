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
    // Inicializamos el DAO una sola vez cuando arranca el Servlet
    this.usuarioDAO = new UsuarioDAO();
  }

  // --- MANEJO DE PETICIONES GET (Mostrar pantallas o Logout) ---
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {

    String action = request.getParameter("action");
    if (action == null) {
      action = "login"; // Acción por defecto
    }

    switch (action) {
      case "login":
        // Redirige al formulario de inicio de sesión
        request.getRequestDispatcher("/formLogin.jsp").forward(request, response);
        break;
      case "registro":
        // Redirige al formulario de registro público
        request.getRequestDispatcher("/formRegistro.jsp").forward(request, response);
        break;
      case "logout":
        logout(request, response);
        break;
      case "registroAdmin":
        // Validar que el usuario actual tenga permisos de gestión (Admin o Bibliotecario)
        HttpSession session = request.getSession(false);
        Usuario uLogueado = (session != null) ? (Usuario) session.getAttribute("usuarioLogueado") : null;

        if (uLogueado == null || (uLogueado.getRol() != RolUsuario.ADMIN && uLogueado.getRol() != RolUsuario.BIBLIOTECARIO)) {
          response.sendRedirect(request.getContextPath() + "/usuario?action=login");
          return;
        }

        // Redirige al formulario de registro exclusivo para personal administrativo
        request.getRequestDispatcher("/formRegistro.jsp").forward(request, response);
        break;
      default:
        response.sendRedirect(request.getContextPath() + "/index");
        break;
    }
  }

  // --- MANEJO DE PETICIONES POST (Procesar formularios) ---
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {

    // Configuramos el encoding para soportar tildes y ñ
    request.setCharacterEncoding("UTF-8");

    String action = request.getParameter("action");

    try {
      if ("registrar".equals(action)) {
        registrarUsuario(request, response);
      } else if ("registrarAdmin".equals(action)) {
        registrarUsuarioAdmin(request, response); // 👈 Maneja el registro hecho por el administrador
      } else if ("iniciarSesion".equals(action)) {
        iniciarSesion(request, response);
      } else {
        response.sendRedirect(request.getContextPath() + "/index");
      }
    } catch (UsuarioException e) {
      // Si ocurre algún error de negocio (ej. email duplicado), lo enviamos a la vista
      request.setAttribute("error", e.getMessage());

      // Volvemos a la pantalla correspondiente según lo que intentaba hacer
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

  // --- LÓGICA DE REGISTRO PÚBLICO (Crea usuario e inicia sesión automáticamente) ---
  private void registrarUsuario(HttpServletRequest request, HttpServletResponse response)
          throws Exception {

    String nombre = request.getParameter("nombre");
    String apellido = request.getParameter("apellido");
    String email = request.getParameter("email");
    String telefono = request.getParameter("telefono");
    String password = request.getParameter("password");

    // 1. Validaciones básicas
    if (nombre == null || email == null || password == null || nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
      throw new UsuarioException("Todos los campos obligatorios deben estar completos.");
    }

    // 2. Verificar si el email ya existe
    if (usuarioDAO.existsByEmail(email)) {
      throw new UsuarioException("El correo ingresado ya está registrado en la biblioteca.");
    }

    // 3. Crear el usuario y encriptar contraseña
    Usuario nuevoUsuario = new Usuario();
    nuevoUsuario.setNombre(nombre);
    nuevoUsuario.setApellido(apellido);
    nuevoUsuario.setEmail(email);
    nuevoUsuario.setTelefono(telefono);

    // Usamos tu PasswordUtil para hashear la contraseña
    String hash = PasswordUtil.hashPassword(password);
    nuevoUsuario.setContrasenia(hash);

    // Todo nuevo registro público entra como USUARIO estándar
    nuevoUsuario.setRol(RolUsuario.USUARIO);

    // 4. Guardar en BD
    usuarioDAO.insert(nuevoUsuario);

    // 5. Iniciar sesión automáticamente tras el registro público
    HttpSession session = request.getSession();
    session.setAttribute("usuarioLogueado", nuevoUsuario);

    // 6. Redirigir al inicio
    response.sendRedirect(request.getContextPath() + "/index");
  }

  // --- LÓGICA DE REGISTRO ADMINISTRATIVO (Registra sin alterar la sesión del Admin) ---
  private void registrarUsuarioAdmin(HttpServletRequest request, HttpServletResponse response)
          throws Exception {

    // 1. Validar seguridad: Solo un admin o bibliotecario puede registrar usuarios de esta forma
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

    // 2. Validaciones básicas
    if (nombre == null || email == null || password == null || nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
      throw new UsuarioException("Todos los campos obligatorios deben estar completos.");
    }

    // 3. Verificar si el email ya existe
    if (usuarioDAO.existsByEmail(email)) {
      throw new UsuarioException("El correo ingresado ya está registrado en la biblioteca.");
    }

    // 4. Crear el objeto usuario
    Usuario nuevoUsuario = new Usuario();
    nuevoUsuario.setNombre(nombre);
    nuevoUsuario.setApellido(apellido);
    nuevoUsuario.setEmail(email);
    nuevoUsuario.setTelefono(telefono);
    nuevoUsuario.setContrasenia(PasswordUtil.hashPassword(password));
    nuevoUsuario.setRol(RolUsuario.USUARIO);

    // 5. Guardar en la base de datos
    usuarioDAO.insert(nuevoUsuario);

    // 6. Redirigir al catálogo o inicio (Sin tocar la sesión del Administrador)
    response.sendRedirect(request.getContextPath() + "/libros?action=listar");
  }

  // --- LÓGICA DE LOGIN ---
  private void iniciarSesion(HttpServletRequest request, HttpServletResponse response)
          throws Exception {

    String email = request.getParameter("email");
    String password = request.getParameter("password");

    if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
      throw new UsuarioException("Debes ingresar correo y contraseña.");
    }

    // 1. Buscar usuario por email
    Usuario usuario = usuarioDAO.getByEmail(email);

    if (usuario == null) {
      throw new UsuarioException("Credenciales incorrectas. Verifica tu email y contraseña.");
    }

    // 2. Verificar contraseña con tu PasswordUtil
    boolean coincide = PasswordUtil.verifyPassword(password, usuario.getContrasenia());

    if (!coincide) {
      throw new UsuarioException("Credenciales incorrectas. Verifica tu email y contraseña.");
    }

    // 3. Login exitoso -> Guardar en Sesión
    HttpSession session = request.getSession();
    session.setAttribute("usuarioLogueado", usuario);

    // 4. Redirección al index
    response.sendRedirect(request.getContextPath() + "/index");
  }

  // --- LÓGICA DE LOGOUT ---
  private void logout(HttpServletRequest request, HttpServletResponse response)
          throws IOException {

    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate(); // Destruye la sesión actual
    }

    // Redirige al login
    response.sendRedirect(request.getContextPath() + "/usuario?action=login");
  }
}