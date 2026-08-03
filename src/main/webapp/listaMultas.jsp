<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Gestión de Multas - WillBook</title>
    <link rel="icon" href="${pageContext.request.contextPath}/imagenes/FaviconW.png" type="image/png">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css?v=2"/>
</head>
<body>

    <jsp:include page="header.jsp" />

    <main class="container my-5">
        <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
            <h2 class="fw-bold">
                <i class="bi bi-exclamation-triangle-fill text-warning me-2"></i>
                ${not empty tituloSeccion ? tituloSeccion : 'Listado de Multas'}
            </h2>
            <a href="${pageContext.request.contextPath}/index" class="btn btn-outline-secondary btn-sm fw-bold">
                <i class="bi bi-arrow-left me-1"></i> Volver al Inicio
            </a>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
            </div>
        </c:if>

        <div class="card shadow-lg">
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th class="py-3"># ID</th>
                                <c:if test="${sessionScope.usuarioLogueado.rol == 'ADMIN' || sessionScope.usuarioLogueado.rol == 'BIBLIOTECARIO'}">
                                    <th class="py-3">Usuario Infractor</th>
                                </c:if>
                                <th class="py-3">Préstamo Ref.</th>
                                <th class="py-3">Fecha Generación</th>
                                <th class="py-3">Monto</th>
                                <th class="py-3">Estado</th>
                                <c:if test="${sessionScope.usuarioLogueado.rol == 'ADMIN' || sessionScope.usuarioLogueado.rol == 'BIBLIOTECARIO'}">
                                    <th class="py-3 text-center">Acciones</th>
                                </c:if>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty listaMultas}">
                                    <c:forEach var="multa" items="${listaMultas}">
                                        <tr>
                                            <td class="fw-bold">${multa.idMulta}</td>

                                            <c:if test="${sessionScope.usuarioLogueado.rol == 'ADMIN' || sessionScope.usuarioLogueado.rol == 'BIBLIOTECARIO'}">
                                                <td>
                                                    ${multa.usuario.nombre} ${multa.usuario.apellido}
                                                    <br><small class="text-muted">${multa.usuario.email}</small>
                                                </td>
                                            </c:if>

                                            <td>ID Préstamo: #${multa.prestamo.idPrestamo}</td>
                                            <td>${multa.fechaGeneracion}</td>
                                            <td class="fw-bold text-danger">$ ${multa.monto}</td>

                                            <td>
                                                <c:choose>
                                                    <c:when test="${multa.estado == 'PENDIENTE'}">
                                                        <span class="badge bg-warning text-dark">PENDIENTE</span>
                                                    </c:when>
                                                    <c:when test="${multa.estado == 'PAGADA'}">
                                                        <span class="badge bg-success">PAGADA</span>
                                                    </c:when>
                                                    <c:when test="${multa.estado == 'ANULADA'}">
                                                        <span class="badge bg-secondary">ANULADA</span>
                                                    </c:when>
                                                </c:choose>
                                            </td>

                                            <c:if test="${sessionScope.usuarioLogueado.rol == 'ADMIN' || sessionScope.usuarioLogueado.rol == 'BIBLIOTECARIO'}">
                                                <td class="text-center">
                                                    <form action="${pageContext.request.contextPath}/multas" method="POST" class="d-inline-flex gap-1 justify-content-center">
                                                        <input type="hidden" name="action" value="actualizarEstado">
                                                        <input type="hidden" name="idMulta" value="${multa.idMulta}">

                                                        <c:if test="${multa.estado == 'PENDIENTE'}">
                                                            <button type="submit" name="estado" value="PAGADA" class="btn btn-success btn-sm" title="Marcar como Pagada">
                                                                <i class="bi bi-check-lg"></i> Pagar
                                                            </button>
                                                            <button type="submit" name="estado" value="ANULADA" class="btn btn-outline-secondary btn-sm" title="Condonar Multa">
                                                                <i class="bi bi-slash-circle"></i> Anular
                                                            </button>
                                                        </c:if>
                                                        <c:if test="${multa.estado != 'PENDIENTE'}">
                                                            <span class="text-muted small">Sin acciones pendientes</span>
                                                        </c:if>
                                                    </form>
                                                </td>
                                            </c:if>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="7" class="text-center py-4 text-muted">
                                            <i class="bi bi-check-circle fs-3 d-block mb-2 text-success"></i>
                                            No se encontraron multas registradas.
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </main>


    <jsp:include page="footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>