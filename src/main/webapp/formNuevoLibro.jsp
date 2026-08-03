<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Registrar Nuevo Libro - Biblioteca WillBook</title>
    <link rel="icon" href="${pageContext.request.contextPath}/imagenes/FaviconW.png" type="image/png">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css?v=2"/>
</head>
<body>

    <!-- Incluimos el Header -->
    <jsp:include page="header.jsp" />

    <div class="container mt-5 mb-5">
        <div class="row justify-content-center">
            <div class="col-md-8 col-lg-6">
                <div class="card shadow-lg">
                    <div class="card-header bg-primary text-white">
                        <h4 class="mb-0"><i class="bi bi-book-fill me-2"></i> REGISTRAR NUEVO LIBRO</h4>
                    </div>

                    <div class="card-body">
                        <!-- Mensaje de error si el Servlet lo devuelve -->
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger" role="alert">
                                <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
                            </div>
                        </c:if>

                        <!-- Apuntamos correctamente al Servlet /libros con la acción insertar -->
                        <form action="${pageContext.request.contextPath}/libros" method="POST">
                            <input type="hidden" name="action" value="insertar">

                            <div class="mb-3">
                                <label for="isbn" class="form-label fw-bold">ISBN</label>
                                <input type="text" name="isbn" id="isbn" class="form-control" placeholder="ISBN" required />
                            </div>

                            <div class="mb-3">
                                <label for="titulo" class="form-label fw-bold">Título</label>
                                <input type="text" name="titulo" id="titulo" class="form-control" placeholder="Título del Libro" required />
                            </div>

                            <div class="mb-3">
                                <label for="descripcion" class="form-label fw-bold">Descripción</label>
                                <textarea name="descripcion" id="descripcion" class="form-control" rows="3" placeholder="Ingrese la Descripción" required></textarea>
                            </div>

                            <!-- Selector de Categoría -->
                            <div class="mb-3">
                                <label for="idCategoria" class="form-label fw-bold">Categoría</label>
                                <select name="idCategoria" id="idCategoria" class="form-select" required>
                                    <option value="" disabled selected>Seleccione una Categoría</option>
                                    <c:forEach var="cat" items="${categorias}">
                                        <option value="${cat.idCategoria}">${cat.nombre}</option>
                                    </c:forEach>
                                </select>
                                <small class="mt-1 d-block"><a href="${pageContext.request.contextPath}/categorias?action=nuevo" class="text-primary text-decoration-none fw-bold">+ Agregar nueva categoría</a></small>
                            </div>

                            <div class="mb-3">
                                <label for="imagen" class="form-label fw-bold">URL de la Imagen (Portada)</label>
                                <input type="text" name="imagen" id="imagen" class="form-control" placeholder="https://ejemplo.com/portada.jpg" />
                                <small class="text-muted">Pega el enlace web de la imagen del libro.</small>
                            </div>

                            <!-- Selector de Autor (Nuevo campo integrado) -->
                            <div class="mb-3">
                                <label for="idAutor" class="form-label fw-bold">Autor</label>
                                <select name="idAutor" id="idAutor" class="form-select" required>
                                    <option value="" disabled selected>Seleccione un Autor</option>
                                    <c:forEach var="aut" items="${listaAutores}">
                                        <option value="${aut.idAutor}">${aut.nombreCompleto} (${aut.nacionalidad})</option>
                                    </c:forEach>
                                </select>
                                <small class="mt-1 d-block"><a href="${pageContext.request.contextPath}/autores?action=nuevo" class="text-primary text-decoration-none fw-bold">+ Agregar nuevo autor</a></small>
                            </div>

                            <div class="d-grid gap-2 mt-4">
                                <button type="submit" class="btn btn-primary fw-bold">Guardar Libro</button>
                            </div>
                        </form>
                    </div>

                    <div class="card-footer text-center">
                        <a href="${pageContext.request.contextPath}/libros?action=listar" class="btn btn-outline-secondary btn-sm fw-bold">
                            Volver al Catálogo
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Incluimos el Footer -->
    <jsp:include page="footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>