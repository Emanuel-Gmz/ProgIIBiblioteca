<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Iniciar Sesión - Biblioteca WillBook</title>
    <link rel="icon" href="${pageContext.request.contextPath}/imagenes/FaviconW.png" type="image/png">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css?v=2"/>
</head>
<body>

    <jsp:include page="header.jsp" />

    <main class="container my-5" style="max-width: 500px;">
        <div class="card shadow-lg p-4 rounded-4">
            <h2 class="text-center fw-bold mb-4 text-primary">
                <i class="bi bi-box-arrow-in-right me-2"></i> Iniciar Sesión
            </h2>

            <!-- Mensaje de error si el Servlet lo devuelve -->
            <c:if test="${not empty error}">
                <div class="alert alert-danger" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/sesion" method="POST">
                <input type="hidden" name="action" value="iniciarSesion">

                <div class="mb-3">
                    <label for="email" class="form-label fw-bold">Correo Electrónico:</label>
                    <input type="email" id="email" name="email" class="form-control" placeholder="ejemplo@correo.com" required>
                </div>

                <div class="mb-4">
                    <label for="password" class="form-label fw-bold">Contraseña:</label>
                    <input type="password" id="password" name="password" class="form-control" placeholder="********" required>
                </div>

                <div class="d-grid gap-2">
                    <button type="submit" class="btn btn-primary btn-lg fw-bold">Entrar</button>
                    <a href="${pageContext.request.contextPath}/index" class="btn btn-outline-secondary">Cancelar</a>
                </div>
            </form>
        </div>
    </main>

    <jsp:include page="footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>