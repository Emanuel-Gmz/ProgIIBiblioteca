<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Agregar Ejemplar - Biblioteca WillBook</title>
    <link rel="icon" href="${pageContext.request.contextPath}/imagenes/FaviconW.png" type="image/png">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css?v=2"/>
</head>
<body>

    <jsp:include page="header.jsp" />

    <main class="container my-5" style="max-width: 600px;">
        <div class="card shadow border-0 p-4 rounded-4 bg-dark text-white">
            <h2 class="text-center fw-bold mb-4 text-success"><i class="bi bi-plus-square me-2"></i> Nuevo Ejemplar Físico</h2>

            <c:if test="${not empty error}">
                <div class="alert alert-danger" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
                </div>
            </c:if>

            <!-- El formulario enviará los datos al EjemplarServlet con la acción registrar -->
            <form action="${pageContext.request.contextPath}/ejemplares" method="POST">
                <input type="hidden" name="action" value="registrar">

                <!-- ID del libro que viene por parámetro desde el catálogo -->
                <input type="hidden" name="idLibro" value="${param.idLibro}">

                <div class="mb-3">
                    <label class="form-label fw-bold">Libro Seleccionado (ID):</label>
                    <input type="text" class="form-control bg-secondary text-white border-0" value="${param.idLibro}" readonly>
                </div>

                <div class="mb-4">
                    <label for="codigoInventario" class="form-label fw-bold">Código de Inventario / Código de Barras:</label>
                    <input type="text" name="codigoInventario" id="codigoInventario" class="form-control" placeholder="Ej: EJEM-2026-001" required>
                    <div class="form-text text-muted">Identificador único físico para este ejemplar en la biblioteca.</div>
                </div>

                <div class="d-grid gap-2">
                    <button type="submit" class="btn btn-success btn-lg fw-bold">Guardar Ejemplar</button>
                    <a href="${pageContext.request.contextPath}/libros?action=listar" class="btn btn-outline-light">Cancelar</a>
                </div>
            </form>
        </div>
    </main>

    <jsp:include page="footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>