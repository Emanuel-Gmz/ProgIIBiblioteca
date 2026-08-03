<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Registrar Autor - Biblioteca WillBook</title>
    <link rel="icon" href="${pageContext.request.contextPath}/imagenes/FaviconW.png" type="image/png">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css?v=2"/>
</head>
<body>

    <jsp:include page="header.jsp" />

    <div class="container mt-5 mb-5">
        <div class="row justify-content-center">
            <div class="col-md-8 col-lg-6">
                <div class="card shadow-lg">
                    <div class="card-header bg-success text-white">
                        <h4 class="mb-0"><i class="bi bi-person-plus-fill me-2"></i> REGISTRAR NUEVO AUTOR</h4>
                    </div>

                    <div class="card-body">
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger" role="alert">
                                <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
                            </div>
                        </c:if>

                        <form action="${pageContext.request.contextPath}/autores" method="POST">
                            <input type="hidden" name="action" value="insertar">

                            <div class="mb-3">
                                <label for="nombreCompleto" class="form-label fw-bold">Nombre Completo</label>
                                <input type="text" name="nombreCompleto" id="nombreCompleto" class="form-control" placeholder="Ej: Gabriel García Márquez" required />
                            </div>

                            <div class="mb-3">
                                <label for="nacionalidad" class="form-label fw-bold">Nacionalidad</label>
                                <input type="text" name="nacionalidad" id="nacionalidad" class="form-control" placeholder="Ej: Colombiana" />
                            </div>

                            <div class="d-grid gap-2 mt-4">
                                <button type="submit" class="btn btn-success fw-bold">Guardar Autor</button>
                            </div>
                        </form>
                    </div>

                    <div class="card-footer text-center">
                        <a href="${pageContext.request.contextPath}/libros?action=nuevo" class="btn btn-outline-secondary btn-sm fw-bold">
                            Volver al formulario de libro
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>