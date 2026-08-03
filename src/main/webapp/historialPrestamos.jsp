<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <link rel="icon" href="${pageContext.request.contextPath}/imagenes/FaviconW.png" type="image/png">
    <title>
        <c:choose>
            <c:when test="${sessionScope.usuarioLogueado.rol == 'ADMIN' || sessionScope.usuarioLogueado.rol == 'BIBLIOTECARIO'}">
                Auditoría de Préstamos - Biblioteca WillBook
            </c:when>
            <c:otherwise>
                Mis Préstamos - Biblioteca WillBook
            </c:otherwise>
        </c:choose>
    </title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css?v=2"/>
</head>
<body>

    <jsp:include page="header.jsp" />

    <main class="container my-5">
        <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
            <h2 class="fw-bold mb-0">
                <c:choose>
                    <c:when test="${sessionScope.usuarioLogueado.rol == 'ADMIN' || sessionScope.usuarioLogueado.rol == 'BIBLIOTECARIO'}">
                        <i class="bi bi-shield-lock-fill me-2"></i> Auditoría General de Préstamos
                    </c:when>
                    <c:otherwise>
                        <i class="bi bi-book-fill me-2"></i> Historial de Mis Préstamos
                    </c:otherwise>
                </c:choose>
            </h2>

            <c:if test="${sessionScope.usuarioLogueado.rol == 'ADMIN' || sessionScope.usuarioLogueado.rol == 'BIBLIOTECARIO'}">
                <form action="${pageContext.request.contextPath}/prestamos" method="GET" class="d-flex gap-2">
                    <input type="hidden" name="action" value="historial">
                    <input type="text" name="busquedaUsuario" class="form-control" placeholder="Buscar por usuario..." value="${param.busquedaUsuario}" style="width: 250px;">
                    <button type="submit" class="btn btn-outline-primary fw-bold"><i class="bi bi-search"></i> Buscar</button>
                </form>
            </c:if>
        </div>

        <c:choose>
            <c:when test="${not empty listaPrestamos}">
                <div class="card shadow-sm border-0 overflow-hidden">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0 styled-table">
                            <thead>
                                <tr>
                                    <th>Libro</th>
                                    <th>Código Ejemplar</th>
                                    <c:if test="${sessionScope.usuarioLogueado.rol == 'ADMIN' || sessionScope.usuarioLogueado.rol == 'BIBLIOTECARIO'}">
                                        <th>Usuario</th>
                                    </c:if>
                                    <th>Fecha Préstamo</th>
                                    <th>Fecha Límite</th>
                                    <th>Estado</th>
                                    <c:if test="${sessionScope.usuarioLogueado.rol == 'ADMIN' || sessionScope.usuarioLogueado.rol == 'BIBLIOTECARIO'}">
                                        <th class="text-center">Acciones</th>
                                    </c:if>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${listaPrestamos}">
                                    <tr>
                                        <td class="fw-bold">${p.ejemplar.libro.titulo}</td>
                                        <td><span class="badge bg-secondary">${p.ejemplar.codigoInventario}</span></td>

                                        <c:if test="${sessionScope.usuarioLogueado.rol == 'ADMIN' || sessionScope.usuarioLogueado.rol == 'BIBLIOTECARIO'}">
                                            <td>${p.usuario.nombre} ${p.usuario.apellido} <small class="text-muted">(${p.usuario.email})</small></td>
                                        </c:if>

                                        <td>${p.fechaPrestamo}</td>
                                        <td>${p.fechaLimite}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.estado == 'ACTIVO'}">
                                                    <span class="badge bg-success">Activo</span>
                                                </c:when>
                                                <c:when test="${p.estado == 'DEVUELTO'}">
                                                    <span class="badge bg-primary">Devuelto</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-danger">${p.estado}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <c:if test="${sessionScope.usuarioLogueado.rol == 'ADMIN' || sessionScope.usuarioLogueado.rol == 'BIBLIOTECARIO'}">
                                            <td class="text-center">
                                                <c:if test="${p.estado == 'ACTIVO'}">
                                                    <form action="${pageContext.request.contextPath}/prestamos" method="POST" class="d-inline">
                                                        <input type="hidden" name="action" value="devolver">
                                                        <input type="hidden" name="idPrestamo" value="${p.idPrestamo}">
                                                        <button type="submit" class="btn btn-sm btn-outline-warning fw-bold" title="Finalizar préstamo y registrar devolución">
                                                            <i class="bi bi-check2-circle"></i> Finalizar
                                                        </button>
                                                    </form>
                                                </c:if>
                                                <c:if test="${p.estado != 'ACTIVO'}">
                                                    <span class="text-muted small">Cerrado</span>
                                                </c:if>
                                            </td>
                                        </c:if>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-info text-center py-4 shadow-sm" role="alert">
                    <i class="bi bi-info-circle fs-3 d-block mb-2"></i>
                    <h5>No hay préstamos registrados que coincidan con la búsqueda.</h5>
                    <a href="${pageContext.request.contextPath}/prestamos?action=historial" class="btn btn-primary btn-sm mt-2 fw-bold">Ver Todos</a>
                </div>
            </c:otherwise>
        </c:choose>
    </main>

    <jsp:include page="footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>