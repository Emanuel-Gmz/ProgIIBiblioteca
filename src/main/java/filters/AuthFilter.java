package filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebFilter(urlPatterns = {"/*"})
public class AuthFilter implements Filter {

  // LISTA BLANCA: Rutas públicas accesibles sin iniciar sesión
  private static final List<String> RUTAS_PUBLICAS = Arrays.asList(
          "/",
          "/index",
          "/sesion",
          "/libros",
          "/usuario",
          "/formLogin.jsp",
          "/error_pantalla.jsp"
  );

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    HttpSession session = httpRequest.getSession(false);

    // Verificamos el atributo de sesión correcto que usas en tus servlets y headers
    boolean isLoggedIn = (session != null && session.getAttribute("usuarioLogueado") != null);

    String requestURI = httpRequest.getRequestURI();
    String contextPath = httpRequest.getContextPath();

    // Limpiamos el contextPath de la URI para compararla con la lista blanca
    String uriSinContexto = requestURI.substring(contextPath.length());

    // Verificamos si la ruta exacta está en la lista blanca
    boolean isPublicRoute = RUTAS_PUBLICAS.contains(uriSinContexto);

    // Verificamos recursos estáticos (CSS, imágenes como WillBook.png o FaviconW.png, JS, etc.)
    boolean isStaticResource = uriSinContexto.matches(".*\\.(css|jpg|png|gif|js|ico|svg)$");

    // LÓGICA DE CONTROL DE ACCESO
    if (isLoggedIn || isPublicRoute || isStaticResource) {
      // Permitir el paso
      chain.doFilter(request, response);
    } else {
      // Redirigir al formulario de login si intenta acceder a zonas protegidas (como multas o préstamos)
      httpResponse.sendRedirect(contextPath + "/usuario?action=login");
    }
  }

  @Override
  public void destroy() {}
}