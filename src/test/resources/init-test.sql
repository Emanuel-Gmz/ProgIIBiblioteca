CREATE SCHEMA IF NOT EXISTS `probiblioteca_test`;
USE `probiblioteca_test`;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Tabla de Autores (Nueva)
DROP TABLE IF EXISTS `autores`;
CREATE TABLE `autores` (
  `idAutor` int NOT NULL AUTO_INCREMENT,
  `nombreCompleto` varchar(255) NOT NULL,
  `nacionalidad` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`idAutor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Tabla de Categorías (Nueva, reemplaza el enum)
DROP TABLE IF EXISTS `categorias`;
CREATE TABLE `categorias` (
  `idCategoria` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL UNIQUE,
  `descripcion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idCategoria`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Tabla de Libros (Modificada)
DROP TABLE IF EXISTS `libros`;
CREATE TABLE `libros` (
  `idLibro` int NOT NULL AUTO_INCREMENT,
  `ISBN` varchar(20) DEFAULT NULL UNIQUE,
  `titulo` varchar(255) NOT NULL,
  `descripcion` varchar(1000) DEFAULT NULL,
  `idCategoria` int DEFAULT NULL,
  `imagen` varchar(255) NULL,
  PRIMARY KEY (`idLibro`),
  CONSTRAINT `fk_libro_categoria` FOREIGN KEY (`idCategoria`) REFERENCES `categorias` (`idCategoria`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Tabla Intermedia Libros-Autores (Para libros con más de un autor)
DROP TABLE IF EXISTS `libros_autores`;
CREATE TABLE `libros_autores` (
  `idLibro` int NOT NULL,
  `idAutor` int NOT NULL,
  PRIMARY KEY (`idLibro`, `idAutor`),
  CONSTRAINT `fk_la_libro` FOREIGN KEY (`idLibro`) REFERENCES `libros` (`idLibro`) ON DELETE CASCADE,
  CONSTRAINT `fk_la_autor` FOREIGN KEY (`idAutor`) REFERENCES `autores` (`idAutor`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Tabla de Ejemplares (Nueva: La copia física del libro)
DROP TABLE IF EXISTS `ejemplares`;
CREATE TABLE `ejemplares` (
  `idEjemplar` int NOT NULL AUTO_INCREMENT,
  `idLibro` int NOT NULL,
  `codigoInventario` varchar(50) UNIQUE NOT NULL, -- Ej: BIB-001, BIB-002
  `estado` enum('DISPONIBLE', 'PRESTADO', 'MANTENIMIENTO', 'EXTRAVIADO') DEFAULT 'DISPONIBLE',
  PRIMARY KEY (`idEjemplar`),
  CONSTRAINT `fk_ejemplar_libro` FOREIGN KEY (`idLibro`) REFERENCES `libros` (`idLibro`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Tabla de Usuarios (Tu original, optimizada)
DROP TABLE IF EXISTS `usuarios`;
CREATE TABLE `usuarios` (
  `idUsuario` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL UNIQUE,
  `telefono` varchar(45) DEFAULT NULL,
  `contrasenia` varchar(255) NOT NULL,
  `rol` enum('BIBLIOTECARIO', 'USUARIO', 'ADMIN') DEFAULT 'USUARIO',
  PRIMARY KEY (`idUsuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Tabla de Préstamos (Modificada: Se presta un Ejemplar, no el Libro genérico)
DROP TABLE IF EXISTS `prestamos`;
CREATE TABLE `prestamos` (
  `idPrestamo` int NOT NULL AUTO_INCREMENT,
  `idUsuario` int NOT NULL,
  `idEjemplar` int NOT NULL,
  `fechaPrestamo` date NOT NULL,
  `fechaLimite` date NOT NULL, -- Fecha esperada de devolución
  `fechaDevolucion` date DEFAULT NULL, -- Fecha real en que lo trajo
  `estado` enum('ACTIVO', 'DEVUELTO', 'VENCIDO') DEFAULT 'ACTIVO',
  PRIMARY KEY (`idPrestamo`),
  KEY `idx_prestamo_usuario` (`idUsuario`),
  KEY `idx_prestamo_ejemplar` (`idEjemplar`),
  CONSTRAINT `fk_prestamo_usuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`) ON DELETE CASCADE,
  CONSTRAINT `fk_prestamo_ejemplar` FOREIGN KEY (`idEjemplar`) REFERENCES `ejemplares` (`idEjemplar`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Tabla de Multas (Nueva)
DROP TABLE IF EXISTS `multas`;
CREATE TABLE `multas` (
  `idMulta` int NOT NULL AUTO_INCREMENT,
  `idPrestamo` int NOT NULL,
  `idUsuario` int NOT NULL,
  `monto` decimal(10,2) NOT NULL,
  `fechaGeneracion` date NOT NULL,
  `estado` enum('PENDIENTE', 'PAGADA', 'CONDONADA') DEFAULT 'PENDIENTE',
  PRIMARY KEY (`idMulta`),
  CONSTRAINT `fk_multa_prestamo` FOREIGN KEY (`idPrestamo`) REFERENCES `prestamos` (`idPrestamo`) ON DELETE CASCADE,
  CONSTRAINT `fk_multa_usuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;