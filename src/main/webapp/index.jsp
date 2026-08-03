<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Inicio - Biblioteca Virtual WillBook</title>
    <link rel="icon" href="${pageContext.request.contextPath}/imagenes/FaviconW.png" type="image/png">

    <!-- Bootstrap 5 y Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css?v=2"/>
</head>
<body>

    <!-- Incluimos el Header unificado -->
    <jsp:include page="header.jsp" />

    <main class="container my-5" style="max-width: 1100px;">
        <!-- Contenedor central principal que se adapta automáticamente al tema claro u oscuro -->
        <div class="card shadow-lg border-0 p-4 p-md-5 rounded-4">

            <!-- 1. HERO / BIENVENIDA -->
            <section class="p-4 p-md-5 mb-5 rounded-4 text-center border shadow-sm bg-body-tertiary">
                <h1 class="display-5 fw-bold mb-3 text-primary">¡Bienvenido a WillBook!</h1>
                <p class="col-md-9 fs-5 mx-auto text-secondary mb-4">
                    Explora nuestro catálogo de libros, gestiona tus préstamos de forma rápida y descubre las últimas novedades literarias.
                </p>
                <div class="d-flex justify-content-center gap-3">
                    <a href="${pageContext.request.contextPath}/libros?action=listar" class="btn btn-primary btn-lg px-4 fw-bold">
                        <i class="bi bi-book me-2"></i> Ver Catálogo Completo
                    </a>
                    <c:if test="${empty sessionScope.usuarioLogueado}">
                        <a href="${pageContext.request.contextPath}/usuario?action=login" class="btn btn-outline-secondary btn-lg px-4 fw-bold">
                            Iniciar Sesión
                        </a>
                    </c:if>
                </div>
            </section>

            <!-- 3. ÚLTIMOS LIBROS AGREGADOS / NOVEDADES -->
            <div class="mb-5">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h3 class="fw-bold m-0">
                        <i class="bi bi-stars me-2 text-warning"></i> Últimos Libros Agregados
                    </h3>
                    <a href="${pageContext.request.contextPath}/libros?action=listar" class="text-decoration-none fw-bold">Ver todos &rarr;</a>
                </div>

                <div class="row row-cols-1 row-cols-md-4 g-4">
                    <c:choose>
                        <c:when test="${not empty ultimosLibros}">
                            <c:forEach var="libro" items="${ultimosLibros}">
                                <div class="col">
                                    <div class="card h-100 shadow-sm rounded-3 overflow-hidden border">
                                        <div class="bg-secondary-subtle text-center py-3" style="height: 180px; overflow: hidden;">
                                            <c:choose>
                                                <c:when test="${not empty libro.imagen}">
                                                    <img src="${libro.imagen}" alt="Portada de ${libro.titulo}" class="img-fluid h-100 object-fit-cover" />
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="d-flex align-items-center justify-content-center h-100 text-muted">
                                                        <i class="bi bi-book display-4"></i>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="card-body d-flex flex-column">
                                            <h6 class="card-title fw-bold text-truncate">${libro.titulo}</h6>
                                            <p class="card-text text-secondary small text-truncate mb-3">${libro.descripcion}</p>
                                            <a href="${pageContext.request.contextPath}/prestamos?action=nuevo&idLibro=${libro.idLibro}" class="btn btn-primary btn-sm mt-auto fw-bold">
                                                Solicitar Préstamo
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="col-12">
                                <div class="alert alert-info text-center">No hay novedades registradas recientemente.</div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- 4. PASOS PARA TU PRÉSTAMO -->
            <div class="card shadow-sm p-4 text-center rounded-4 mb-5">
                <h3 class="fw-bold mb-4">
                    <i class="bi bi-info-circle-fill me-2 text-primary"></i> PASOS PARA TU PRÉSTAMO
                </h3>

                <div class="row row-cols-1 row-cols-md-3 g-4">
                    <div class="col">
                        <div class="p-4 rounded-4 text-white shadow h-100 d-flex flex-column justify-content-center align-items-center" style="background-color: #265a88; min-height: 180px;">
                            <h5 class="fw-bold mb-2">PASO 1</h5>
                            <p class="small mb-0 text-center">Acercate a la biblioteca WillBook para obtener tu cuenta.</p>
                        </div>
                    </div>
                    <div class="col">
                        <div class="p-4 rounded-4 text-white shadow h-100 d-flex flex-column justify-content-center align-items-center" style="background-color: #265a88; min-height: 180px;">
                            <h5 class="fw-bold mb-2">PASO 2</h5>
                            <p class="small mb-0 text-center">Ingresá al catálogo y solicitá el préstamo del ejemplar que quieras.</p>
                        </div>
                    </div>
                    <div class="col">
                        <div class="p-4 rounded-4 text-white shadow h-100 d-flex flex-column justify-content-center align-items-center" style="background-color: #265a88; min-height: 180px;">
                            <h5 class="fw-bold mb-2">PASO 3</h5>
                            <p class="small mb-0 text-center">Una vez creado tu préstamo, retiralo presencialmente en el plazo indicado.</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 5. TOP 3 LECTORES DESTACADOS Y BUSCADOR -->
            <div class="row g-4 mb-3">
                <div class="col-lg-6">
                    <div class="card h-100 shadow-sm border">
                        <div class="card-header bg-primary text-white fw-bold">
                            <i class="bi bi-trophy-fill me-2"></i> Top 3 Lectores Destacados
                        </div>
                        <ul class="list-group list-group-flush">
                            <c:choose>
                                <c:when test="${not empty topUsuarios}">
                                    <c:forEach var="usu" items="${topUsuarios}" varStatus="status">
                                        <li class="list-group-item d-flex justify-content-between align-items-center py-3">
                                            <div>
                                                <span class="badge bg-warning text-dark me-2">${status.index + 1}°</span>
                                                <strong>${usu.nombre} ${usu.apellido}</strong>
                                            </div>
                                            <span class="badge bg-secondary rounded-pill">Lector activo</span>
                                        </li>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <li class="list-group-item text-center text-muted py-3">
                                        Aún no hay registros suficientes de préstamos.
                                    </li>
                                </c:otherwise>
                            </c:choose>
                        </ul>
                    </div>
                </div>

                <!-- Accesos al Buscador -->
                <div class="col-lg-6">
                    <div class="card h-100 shadow-sm border bg-body-secondary">
                        <div class="card-body d-flex flex-column justify-content-center text-center p-4">
                            <h3 class="card-title fw-bold mb-3">¿Buscas un libro en específico?</h3>
                            <p class="text-secondary mb-4">Utiliza nuestro buscador avanzado integrado en el catálogo para filtrar por título o categoría en segundos.</p>
                            <a href="${pageContext.request.contextPath}/libros?action=listar" class="btn btn-primary w-50 mx-auto fw-bold">
                                Ir al Buscador
                            </a>
                        </div>
                    </div>
                </div>

        </div>
    </main>

    <!-- Incluimos el Footer -->
    <jsp:include page="footer.jsp" />

    <!-- Scripts de Bootstrap -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>