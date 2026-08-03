package servlets;

import dao.AutorDAO;
import dao.CategoriaDAO;
import dao.LibroDAO;
import entities.Autor;
import entities.Categoria;
import entities.Libro;
import enums.RolUsuario;
import exceptions.LibroException;
import entities.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "LibroServlet", urlPatterns = {"/libros"})
public class LibroServlet extends HttpServlet {

    private LibroDAO libroDAO;
    private CategoriaDAO categoriaDAO;
    private AutorDAO autorDAO; // 👈 1. Declaramos el DAO de Autores

    @Override
    public void init() throws ServletException {
        this.libroDAO = new LibroDAO();
        this.categoriaDAO = new CategoriaDAO();
        this.autorDAO = new AutorDAO(); // 👈 2. Lo inicializamos
    }

    // --- MANEJO DE PETICIONES GET (Listar, Buscar o Formulario de Alta) ---
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) {
            action = "listar";
        }

        try {
            switch (action) {
                case "listar":
                    listarLibros(request, response);
                    break;
                case "buscar":
                    buscarLibros(request, response);
                    break;
                case "nuevo":
                    mostrarFormularioNuevo(request, response);
                    break;
                default:
                    listarLibros(request, response);
                    break;
            }
        } catch (LibroException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/error_pantalla.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Ocurrió un error inesperado al cargar los libros.");
            request.getRequestDispatcher("/error_pantalla.jsp").forward(request, response);
        }
    }

    // --- MANEJO DE PETICIONES POST (Guardar nuevo libro) ---
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try {
            if ("insertar".equals(action)) {
                verificarPermisosAdmin(request);
                insertarLibro(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/libros?action=listar");
            }
        } catch (LibroException e) {
            request.setAttribute("error", e.getMessage());
            // 👈 3. Recargamos tanto categorías como autores si falla el alta para redisplayar el form
            request.setAttribute("categorias", categoriaDAO.getAll());
            request.setAttribute("listaAutores", autorDAO.getAll());
            request.getRequestDispatcher("/formNuevoLibro.jsp").forward(request, response);
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error interno al procesar el libro.");
            request.getRequestDispatcher("/error_pantalla.jsp").forward(request, response);
        }
    }

    // --- LÓGICA DE NEGOCIO Y ACCIONES ---

    private void listarLibros(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Libro> listaLibros = libroDAO.getAll();

        // 1. Enviamos las categorías para que el selector del catálogo siempre las muestre
        request.setAttribute("listaCategorias", categoriaDAO.getAll());
        request.setAttribute("listaLibros", listaLibros);
        request.getRequestDispatcher("/catalogo.jsp").forward(request, response);
    }

    private void buscarLibros(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String query = request.getParameter("q");
        String idCategoriaStr = request.getParameter("idCategoria");
        List<Libro> listaLibros;

        // 2. Lógica para filtrar por título, por categoría, o por ambos a la vez
        boolean tieneQuery = (query != null && !query.trim().isEmpty());
        boolean tieneCategoria = (idCategoriaStr != null && !idCategoriaStr.trim().isEmpty());

        if (tieneQuery && tieneCategoria) {
            int idCat = Integer.parseInt(idCategoriaStr);
            // Opcional: si tu DAO tiene un método combinado, úsalo. Si no, puedes filtrar o implementar buscarPorTituloYCategoria
            listaLibros = libroDAO.buscarPorTituloYCategoria(query, idCat);
        } else if (tieneQuery) {
            listaLibros = libroDAO.buscarPorTitulo(query);
        } else if (tieneCategoria) {
            int idCat = Integer.parseInt(idCategoriaStr);
            listaLibros = libroDAO.buscarPorCategoria(idCat); // Asegúrate de tener este método en tu LibroDAO, o implementarlo
        } else {
            listaLibros = libroDAO.getAll();
        }

        // 3. Enviamos también las categorías para que no se deslacen al filtrar
        request.setAttribute("listaCategorias", categoriaDAO.getAll());
        request.setAttribute("listaLibros", listaLibros);
        request.setAttribute("busquedaActual", query);
        request.getRequestDispatcher("/catalogo.jsp").forward(request, response);
    }

    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 👈 4. Mandamos las categorías y los autores para cargarlos en los select del formulario
        request.setAttribute("categorias", categoriaDAO.getAll());
        request.setAttribute("listaAutores", autorDAO.getAll());
        request.getRequestDispatcher("/formNuevoLibro.jsp").forward(request, response);
    }

    private void insertarLibro(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String isbn = request.getParameter("isbn");
        String titulo = request.getParameter("titulo");
        String descripcion = request.getParameter("descripcion");
        String idCategoriaStr = request.getParameter("idCategoria");
        String idAutorStr = request.getParameter("idAutor"); // 👈 5. Recogemos el autor enviado
        String imagen = request.getParameter("imagen");

        if (titulo == null || titulo.trim().isEmpty()) {
            throw new LibroException("El título del libro es obligatorio.");
        }

        if (idAutorStr == null || idAutorStr.trim().isEmpty()) {
            throw new LibroException("Debe seleccionar un autor para el libro.");
        }

        Libro nuevoLibro = new Libro();
        nuevoLibro.setIsbn(isbn);
        nuevoLibro.setTitulo(titulo);
        nuevoLibro.setDescripcion(descripcion);
        nuevoLibro.setImagen(imagen);

        // Asociar la categoría si fue seleccionada
        if (idCategoriaStr != null && !idCategoriaStr.isEmpty()) {
            int idCat = Integer.parseInt(idCategoriaStr);
            Categoria cat = categoriaDAO.getById(idCat);
            if (cat != null) {
                nuevoLibro.setCategoria(cat);
            }
        }

        // 👈 6. Asociar el autor seleccionado a la lista que espera el LibroDAO
        int idAutor = Integer.parseInt(idAutorStr);
        Autor autor = autorDAO.getById(idAutor);
        if (autor != null) {
            List<Autor> autoresList = new ArrayList<>();
            autoresList.add(autor);
            nuevoLibro.setAutores(autoresList);
        }

        libroDAO.insert(nuevoLibro);

        // Redirigir al listado de libros con éxito
        response.sendRedirect(request.getContextPath() + "/libros?action=listar");
    }

    // --- MÉTODO AUXILIAR DE SEGURIDAD ---
    private void verificarPermisosAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogueado") : null;

        if (usuario == null || (usuario.getRol() != RolUsuario.ADMIN && usuario.getRol() != RolUsuario.BIBLIOTECARIO)) {
            throw new SecurityException("Acceso denegado. No tienes permisos para realizar esta acción.");
        }
    }
}