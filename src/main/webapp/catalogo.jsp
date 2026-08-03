<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Catálogo de Libros - Biblioteca WillBook</title>
    <link rel="icon" href="${pageContext.request.contextPath}/imagenes/FaviconW.png" type="image/png">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css"/>
    <%-- Forzamos la recarga del CSS sumándole ?v=3 para evitar problemas de caché --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css?v=3"/>
</head>
<body>

    <jsp:include page="header.jsp" />

    <!-- Usamos nuestra clase personalizada seccion-catalogo -->
    <main class="container my-4 seccion-catalogo p-4">
        <h2 class="mb-4 fw-bold" style="color: var(--azul-oscuro);"><i class="bi bi-journal-richtext me-2 text-warning"></i> Catálogo de Libros</h2>

        <!-- Barra de búsqueda, filtro automático y acciones -->
        <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">

            <form id="formFiltro" action="${pageContext.request.contextPath}/libros" method="GET" class="d-flex gap-2 flex-wrap align-items-center">
                <input type="hidden" name="action" value="buscar">

                <input type="text" name="q" class="form-control" placeholder="Buscar por título..." value="${param.q}" style="width: 220px;">

                <select name="idCategoria" id="selectCategoria" class="form-select" style="width: 200px;" onchange="document.getElementById('formFiltro').submit();">
                    <option value="">Todas las categorías</option>
                    <c:forEach var="cat" items="${listaCategorias}">
                        <option value="${cat.idCategoria}" ${param.idCategoria == cat.idCategoria ? 'selected' : ''}>
                            ${cat.nombre}
                        </option>
                    </c:forEach>
                </select>

                <button type="submit" class="btn btn-primary fw-bold"><i class="bi bi-search"></i> Filtrar</button>

                <c:if test="${not empty param.q or not empty param.idCategoria}">
                    <a href="${pageContext.request.contextPath}/libros?action=listar" class="btn btn-outline-secondary fw-bold" title="Limpiar filtros">
                        <i class="bi bi-x-circle"></i> Limpiar
                    </a>
                </c:if>
            </form>

            <div class="d-flex gap-2">
                <c:if test="${not empty sessionScope.usuarioLogueado and (sessionScope.usuarioLogueado.rol == 'ADMIN' or sessionScope.usuarioLogueado.rol == 'BIBLIOTECARIO')}">
                    <a href="${pageContext.request.contextPath}/libros?action=nuevo" class="btn btn-success fw-bold">
                        <i class="bi bi-plus-circle me-1"></i> Agregar Nuevo Libro
                    </a>
                </c:if>

                <c:if test="${not empty sessionScope.usuarioLogueado}">
                    <a href="${pageContext.request.contextPath}/prestamos?action=historial" class="btn btn-outline-primary fw-bold">
                        <i class="bi bi-journal-bookmark me-1"></i> Ver Mis Préstamos
                    </a>
                </c:if>
            </div>
        </div>

        <!-- Listado de libros con la clase personalizada tarjeta-libro -->
        <div class="row row-cols-1 row-cols-md-3 g-4">
            <c:forEach var="libro" items="${listaLibros}">
                <div class="col">
                    <div class="card h-100 tarjeta-libro overflow-hidden">

                        <!-- Imagen de Portada con clase personalizada encabezado-portada -->
                        <div class="encabezado-portada text-center py-4" style="height: 220px; overflow: hidden;">
                            <c:choose>
                                <c:when test="${not empty libro.imagen}">
                                    <img src="${libro.imagen}" alt="Portada de ${libro.titulo}" class="img-fluid h-100 object-fit-cover" />
                                </c:when>
                                <c:otherwise>
                                    <div class="d-flex align-items-center justify-content-center h-100 text-secondary">
                                        <i class="bi bi-book display-1"></i>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title fw-bold text-primary">${libro.titulo}</h5>

                            <h6 class="card-subtitle mb-2 text-secondary small">
                                <i class="bi bi-person-fill me-1"></i>
                                <c:choose>
                                    <c:when test="${not empty libro.autores}">
                                        <c:forEach var="aut" items="${libro.autores}" varStatus="status">
                                            ${aut.nombreCompleto}${!status.last ? ', ' : ''}
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        Autor desconocido
                                    </c:otherwise>
                                </c:choose>
                            </h6>

                            <div class="mb-2">
                                <c:if test="${not empty libro.categoria}">
                                    <span class="badge bg-secondary">${libro.categoria.nombre}</span>
                                </c:if>
                                <span class="text-muted small ms-2">ISBN: ${libro.isbn}</span>
                            </div>

                            <p class="card-text text-muted small flex-grow-1">${libro.descripcion}</p>

                            <div class="mt-auto pt-3 border-top">
                                <c:if test="${not empty sessionScope.usuarioLogueado}">
                                    <a href="${pageContext.request.contextPath}/prestamos?action=nuevo&idLibro=${libro.idLibro}" class="btn btn-primary btn-sm w-100 mb-2 fw-bold">
                                        <i class="bi bi-hand-index me-1"></i> Solicitar Préstamo
                                    </a>
                                </c:if>

                                <c:if test="${not empty sessionScope.usuarioLogueado and (sessionScope.usuarioLogueado.rol == 'ADMIN' or sessionScope.usuarioLogueado.rol == 'BIBLIOTECARIO')}">
                                    <a href="${pageContext.request.contextPath}/ejemplares?action=nuevo&idLibro=${libro.idLibro}" class="btn btn-outline-success btn-sm w-100 fw-bold">
                                        <i class="bi bi-plus-square me-1"></i> + Agregar Ejemplar
                                    </a>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </main>

    <jsp:include page="footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>