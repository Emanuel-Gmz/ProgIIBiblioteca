<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>${not empty requestScope.tituloPagina ? requestScope.tituloPagina : 'Biblioteca Virtual WillBook'}</title>

    <%-- 📌 Favicon global --%>
    <link rel="icon" href="${pageContext.request.contextPath}/imagenes/FaviconW.png" type="image/png">

    <!-- Bootstrap 5 y Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css?v=2"/>

    <script>
        if (!document.querySelector('meta[name="viewport"]')) {
            var meta = document.createElement('meta');
            meta.name = 'viewport';
            meta.content = 'width=device-width, initial-scale=1.0';
            document.head.appendChild(meta);
        }

        (function() {
            var currentTheme = localStorage.getItem('theme') || 'light';
            document.documentElement.setAttribute('data-theme', currentTheme);
            document.documentElement.setAttribute('data-bs-theme', currentTheme);
        })();
    </script>
</head>
<body>

<nav class="navbar sticky-top shadow w-100 px-3" style="background-color: var(--azul-oscuro);" data-bs-theme="dark">
  <div class="container-fluid d-flex justify-content-between align-items-center w-100">

    <%-- Logo y Título a la izquierda --%>
    <a href="${pageContext.request.contextPath}/index" class="navbar-brand fw-bold fs-4 text-white text-decoration-none d-flex align-items-center gap-2">
        <img src="${pageContext.request.contextPath}/imagenes/WillBook.png" alt="Logo WillBook" style="height: 35px; width: auto;" />
        WillBook
    </a>

    <div class="d-flex align-items-center gap-3">
        <%-- Acciones de usuario a la derecha --%>
        <div class="d-none d-md-flex align-items-center gap-3">
            <c:choose>
                <c:when test="${sessionScope.usuarioLogueado != null}">
                    <div class="dropdown">
                        <button class="btn text-white fw-bold d-flex align-items-center border-0 shadow-none dropdown-toggle p-0" type="button" data-bs-toggle="dropdown" aria-expanded="false" style="background: transparent;">
                            <i class="bi bi-person-circle fs-5 me-2"></i>
                            ${sessionScope.usuarioLogueado.nombre} (${sessionScope.usuarioLogueado.rol})
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end shadow border-0 mt-2">
                            <%-- Enlaces personales según el rol --%>
                            <li>
                                <a class="dropdown-item fw-bold text-secondary py-2" href="${pageContext.request.contextPath}/prestamos?action=historial">
                                    <i class="bi bi-book-fill me-2"></i>
                                    <c:choose>
                                        <c:when test="${sessionScope.usuarioLogueado.rol == 'ADMIN' || sessionScope.usuarioLogueado.rol == 'BIBLIOTECARIO'}">
                                            Mi Historial de Préstamos
                                        </c:when>
                                        <c:otherwise>
                                            Mis Préstamos
                                        </c:otherwise>
                                    </c:choose>
                                </a>
                            </li>
                            <li>
                                <a class="dropdown-item fw-bold text-warning py-2" href="${pageContext.request.contextPath}/multas?action=misMultas">
                                    <i class="bi bi-exclamation-octagon-fill me-2"></i> Mis Multas Pendientes
                                </a>
                            </li>
                            <li><hr class="dropdown-divider"></li>
                            <li>
                                <a class="dropdown-item fw-bold text-danger py-2" href="${pageContext.request.contextPath}/usuario?action=logout">
                                    <i class="bi bi-box-arrow-right me-2"></i>Cerrar sesión
                                </a>
                            </li>
                        </ul>
                    </div>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/usuario?action=login" class="btn btn-sm btn-warning fw-bold text-dark">
                        <i class="bi bi-box-arrow-in-right me-1"></i> Iniciar sesión
                    </a>
                </c:otherwise>
            </c:choose>
        </div>

        <%-- Botón hamburguesa estándar nativo para asegurar su despliegue y visibilidad --%>
        <button class="navbar-toggler border-0 shadow-none" type="button" data-bs-toggle="offcanvas" data-bs-target="#menuBiblioteca" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
    </div>

  </div>
</nav>

<%-- Menú Lateral Deslizante (Offcanvas) --%>
<div class="offcanvas offcanvas-end" id="menuBiblioteca">
  <div class="offcanvas-header border-bottom">
    <h5 class="offcanvas-title fw-bold">MENÚ PRINCIPAL</h5>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>

  <div class="offcanvas-body p-0">
    <div class="list-group list-group-flush">

      <a href="${pageContext.request.contextPath}/index" class="list-group-item list-group-item-action">Inicio</a>
      <a href="${pageContext.request.contextPath}/libros?action=listar" class="list-group-item list-group-item-action">Catálogo de Libros</a>

      <%-- SECCIÓN GESTIÓN (ADMIN / BIBLIOTECARIO) --%>
      <c:if test="${sessionScope.usuarioLogueado != null && (sessionScope.usuarioLogueado.rol == 'ADMIN' || sessionScope.usuarioLogueado.rol == 'BIBLIOTECARIO')}">
        <div class="list-group-item text-secondary small fw-bold text-uppercase border-top bg-light">Gestión de Biblioteca</div>
        <a href="${pageContext.request.contextPath}/libros?action=nuevo" class="list-group-item list-group-item-action">Nuevo Libro</a>
        <a href="${pageContext.request.contextPath}/autores?action=nuevo" class="list-group-item list-group-item-action">Nuevo Autor</a>
        <a href="${pageContext.request.contextPath}/categorias?action=nuevo" class="list-group-item list-group-item-action">Nueva Categoría</a>
        <a href="${pageContext.request.contextPath}/prestamos?action=historial&modo=general" class="list-group-item list-group-item-action text-primary fw-bold">
            <i class="bi bi-journal-text me-2"></i>Ver Todos los Préstamos
        </a>
        <a href="${pageContext.request.contextPath}/multas" class="list-group-item list-group-item-action text-warning fw-bold">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>Gestión General de Multas
        </a>
        <a href="${pageContext.request.contextPath}/usuario?action=registroAdmin" class="list-group-item list-group-item-action text-success fw-bold">
            <i class="bi bi-person-plus-fill me-2"></i>Registrar Usuario
        </a>
      </c:if>

      <%-- SECCIÓN MI CUENTA --%>
      <c:if test="${sessionScope.usuarioLogueado != null}">
        <div class="list-group-item text-secondary small fw-bold text-uppercase border-top bg-light">Mi Cuenta</div>
        <c:if test="${sessionScope.usuarioLogueado.rol == 'USUARIO'}">
            <a href="${pageContext.request.contextPath}/prestamos?action=historial" class="list-group-item list-group-item-action">Mis Préstamos</a>
            <a href="${pageContext.request.contextPath}/multas?action=misMultas" class="list-group-item list-group-item-action text-warning fw-bold">
                <i class="bi bi-exclamation-octagon-fill me-2"></i>Mis Multas Pendientes
            </a>
        </c:if>
        <a href="${pageContext.request.contextPath}/usuario?action=logout" class="list-group-item list-group-item-action text-danger fw-bold">
          <i class="bi bi-box-arrow-right me-2"></i>Cerrar sesión
        </a>
      </c:if>

      <%-- SECCIÓN ACCESO ANÓNIMO --%>
      <c:if test="${sessionScope.usuarioLogueado == null}">
        <div class="list-group-item bg-light text-secondary small fw-bold text-uppercase border-top">Acceso</div>
        <a href="${pageContext.request.contextPath}/usuario?action=login" class="list-group-item list-group-item-action text-primary fw-bold">
          <i class="bi bi-box-arrow-in-right me-2"></i>Iniciar sesión
        </a>
      </c:if>

    </div>
  </div>
</div>
</body>
</html>