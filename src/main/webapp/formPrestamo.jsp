<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Solicitar Préstamo - Biblioteca WillBook</title>
    <link rel="icon" href="${pageContext.request.contextPath}/imagenes/FaviconW.png" type="image/png">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css?v=2"/>
</head>
<body>

    <jsp:include page="header.jsp" />

    <main class="container my-5" style="max-width: 600px;">
        <div class="card shadow border-0 p-4 rounded-4">
            <h2 class="text-center fw-bold mb-4 text-primary">Solicitar Nuevo Préstamo</h2>

            <c:if test="${not empty error}">
                <div class="alert alert-danger" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/prestamos" method="POST">
                <input type="hidden" name="action" value="solicitar">

                <input type="hidden" name="idUsuario" value="${sessionScope.usuarioLogueado.idUsuario}">

                <div class="mb-3">
                    <label for="idEjemplar" class="form-label fw-bold">Seleccionar Ejemplar Físico (Disponible):</label>
                    <select name="idEjemplar" id="idEjemplar" class="form-select" required>
                        <option value="">-- Seleccione un ejemplar --</option>
                        <c:forEach var="ej" items="${ejemplares}">
                            <c:if test="${ej.estado == 'DISPONIBLE' and ej.libro.idLibro == idLibroSeleccionado}">
                                <option value="${ej.idEjemplar}">
                                    ${ej.libro.titulo} - Código: [${ej.codigoInventario}]
                                </option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>

                <div class="mb-4">
                    <label for="diasPrestamo" class="form-label fw-bold">Plazo de Préstamo (Días):</label>
                    <input type="number" name="diasPrestamo" id="diasPrestamo" class="form-control" value="14" min="1" max="30" required>
                </div>

                <div class="d-grid gap-2">
                    <button type="submit" class="btn btn-primary btn-lg fw-bold">Confirmar Solicitud</button>
                    <a href="${pageContext.request.contextPath}/prestamos?action=historial" class="btn btn-outline-secondary fw-bold">Cancelar</a>
                </div>
            </form>
        </div>
    </main>

    <jsp:include page="footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>