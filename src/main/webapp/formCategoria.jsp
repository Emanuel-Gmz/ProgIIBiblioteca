<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Registrar Categoría | Biblioteca WillBook</title>
    <link rel="icon" href="${pageContext.request.contextPath}/imagenes/FaviconW.png" type="image/png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>

    <jsp:include page="header.jsp" />

    <div class="container mt-5">
        <h2>Registrar Nueva Categoría</h2>

        <c:if test="${not empty error}">
            <div class="alert-error">${error}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/categorias" method="POST">
            <input type="hidden" name="action" value="insertar">

            <div class="form-group mb-3">
                <label for="nombre">Nombre de la Categoría:</label>
                <input type="text" id="nombre" name="nombre" class="form-control" required placeholder="Ej: Ciencia Ficción">
            </div>

            <button type="submit" class="btn btn-primary">Guardar Categoría</button>
            <a href="${pageContext.request.contextPath}/libros?action=nuevo" class="btn btn-secondary">Volver</a>
        </form>
    </div>

    <jsp:include page="footer.jsp" />

</body>
</html>