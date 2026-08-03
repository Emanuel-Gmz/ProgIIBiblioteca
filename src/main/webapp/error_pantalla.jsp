<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- Determinar el código de error --%>
<%
    Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
    Throwable exception = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
    String requestUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");

    if (statusCode == null) statusCode = 0;

    String errorIcon = "bi-exclamation-triangle-fill";
    String errorTitulo = "Error inesperado";
    String errorMensaje = "Ocurrió un error inesperado. Por favor, volvé a intentarlo en unos minutos.";
    String errorColor = "var(--amarillo)";

    if (statusCode == 404) {
        errorIcon = "bi-compass";
        errorTitulo = "Página no encontrada";
        errorMensaje = "La página que estás buscando no existe.\nVerificá la URL e intentá de nuevo.";
        errorColor = "var(--celeste)";
    } else if (statusCode == 500) {
        errorIcon = "bi-exclamation-triangle-fill";
        errorTitulo = "Error del servidor";
        errorMensaje = "El servidor encontró un problema interno. Estamos trabajando para solucionarlo.\nPor favor, volvé a intentarlo más tarde.";
        errorColor = "var(--amarillo)";
    } else if (exception != null) {
        errorIcon = "bi-bug-fill";
        errorTitulo = "Error en la aplicación";
        errorMensaje = "Ocurrió un error inesperado en la aplicación. Por favor, volvé a intentarlo en unos minutos.";
        errorColor = "#dc3545"; // Rojo de Bootstrap (danger)
    }

    pageContext.setAttribute("errorIcon", errorIcon);
    pageContext.setAttribute("errorTitulo", errorTitulo);
    pageContext.setAttribute("errorMensaje", errorMensaje);
    pageContext.setAttribute("errorColor", errorColor);
    pageContext.setAttribute("statusCode", statusCode);
    pageContext.setAttribute("requestUri", requestUri);
%>

<c:set var="tituloPagina" value="Error ${statusCode != 0 ? statusCode : ''} | ProBiblio" scope="request" />
<%@ include file="header.jsp" %>

<main class="d-flex align-items-start justify-content-center mt-5" style="min-height: 70vh;">
  <div class="container">
    <div class="row justify-content-center mt-4">
      <div class="col-12 col-md-6 col-lg-5">

        <%-- Tarjeta de error adaptada con .capsula-prode para que soporte Modo Oscuro --%>
        <div class="capsula-prode flex-column justify-content-center text-center p-4 mx-2 w-100" style="border-top: 5px solid ${errorColor};">

          <%-- Icono --%>
          <div class="mb-3">
            <i class="bi ${errorIcon} display-4" style="color: ${errorColor};"></i>
          </div>

          <%-- Código de error --%>
          <c:if test="${statusCode != 0}">
            <span class="badge rounded-pill px-3 py-1 mb-2 fw-bold"
                  style="background-color: ${errorColor}; color: var(--bg-principal); font-size: 1rem; letter-spacing: 2px;">
              ERROR ${statusCode}
            </span>
          </c:if>

          <%-- Título (Se quitó el style="color: ..." en línea para que css tome el control en modo oscuro) --%>
          <h1 class="fw-bold mt-2 mb-2" style="font-size: 1.5rem;">
            ${errorTitulo}
          </h1>

          <%-- Mensaje --%>
          <p class="text-muted mb-3 px-1" style="font-size: 0.9rem; line-height: 1.6; white-space: pre-line;">
            ${errorMensaje}
          </p>

          <%-- URI si es 404 (Reemplazado bg-light por la variable dinámica de tu tema) --%>
          <c:if test="${statusCode == 404 && requestUri != null}">
            <div class="rounded-3 p-2 mb-3 d-inline-block" style="background-color: var(--gris-claro);">
              <code class="text-break text-muted" style="font-size: 0.8rem;">
                <i class="bi bi-link-45deg me-1"></i>${requestUri}
              </code>
            </div>
          </c:if>

          <%-- Separador --%>
          <hr class="my-3 text-muted w-100">

          <%-- Botones de acción --%>
          <div class="d-flex flex-wrap justify-content-center gap-2 w-100">
            <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-afa px-3 py-1">
              <i class="bi bi-house-door-fill me-1"></i>Ir al inicio
            </a>
            <button onclick="history.back()" class="btn btn-outline-secondary px-3 py-1 rounded-pill">
              <i class="bi bi-arrow-left me-1"></i>Volver atrás
            </button>
          </div>

        </div>

        <%-- Ayuda adicional --%>
        <div class="text-center mt-3">
          <small class="text-muted" style="font-size: 0.75rem;">
            <i class="bi bi-question-circle me-1"></i>
            Si el problema persiste, contactá al administrador del sitio.
          </small>
        </div>

      </div>
    </div>
  </div>
</main>

<%@ include file="footer.jsp" %>