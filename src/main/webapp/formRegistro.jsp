<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Registro de Usuario - Biblioteca WillBook</title>
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
                        <h4 class="mb-0">
                            <i class="bi bi-person-plus-fill me-2"></i> REGISTRAR NUEVO USUARIO
                        </h4>
                    </div>

                    <div class="card-body">
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger" role="alert">
                                <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
                            </div>
                        </c:if>

                        <form action="${pageContext.request.contextPath}/usuario" method="POST">

                            <input type="hidden" name="action" value="${not empty sessionScope.usuarioLogueado and (sessionScope.usuarioLogueado.rol == 'ADMIN' || sessionScope.usuarioLogueado.rol == 'BIBLIOTECARIO') ? 'registrarAdmin' : 'registrar'}">

                            <div class="mb-3">
                                <label for="nombre" class="form-label fw-bold">Nombre</label>
                                <input type="text" name="nombre" id="nombre" class="form-control" placeholder="Nombre" required />
                            </div>

                            <div class="mb-3">
                                <label for="apellido" class="form-label fw-bold">Apellido</label>
                                <input type="text" name="apellido" id="apellido" class="form-control" placeholder="Apellido" required />
                            </div>

                            <div class="mb-3">
                                <label for="email" class="form-label fw-bold">Correo Electrónico</label>
                                <input type="email" name="email" id="email" class="form-control" placeholder="ejemplo@correo.com" required />
                            </div>

                            <div class="mb-3">
                                <label for="telefono" class="form-label fw-bold">Teléfono</label>
                                <input type="text" name="telefono" id="telefono" class="form-control" placeholder="Teléfono" />
                            </div>

                            <div class="mb-3">
                                <label for="password" class="form-label fw-bold">Contraseña</label>
                                <input type="password" name="password" id="password" class="form-control" placeholder="Contraseña" required />
                            </div>

                            <div class="d-grid gap-2 mt-4">
                                <button type="submit" class="btn btn-success btn-lg fw-bold">Registrar Usuario</button>
                                <a href="${pageContext.request.contextPath}/index" class="btn btn-outline-secondary fw-bold">Cancelar</a>
                            </div>
                        </form>
                    </div>
                </div>

            </div>
        </div>
    </div>

    <jsp:include page="footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>