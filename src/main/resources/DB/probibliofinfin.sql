CREATE SCHEMA IF NOT EXISTS `probiblioteca`;
USE `probiblioteca`;

SET FOREIGN_KEY_CHECKS = 0;


DROP TABLE IF EXISTS `libros`;
CREATE TABLE `libros` (
  `idLibro` int NOT NULL AUTO_INCREMENT,
  `ISBN` varchar(20) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `autor` varchar(255) DEFAULT NULL,
  `descripcion` varchar(1000) DEFAULT NULL,
  `categoria` enum('EDUCATIVO', 'NOVELA', 'CIENCIA_FICCION', 'MATEMATICA', 'FANTASIA', 'HISTORIA', 'OTRO') DEFAULT 'OTRO',
  PRIMARY KEY (`idLibro`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `usuarios`;
CREATE TABLE `usuarios` (
  `idUsuario` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) DEFAULT NULL,
  `apellido` varchar(100) DEFAULT NULL,
  `email` varchar(100) NOT NULL UNIQUE,
  `telefono` varchar(45) DEFAULT NULL,
  `contrasenia` varchar(255) DEFAULT NULL,
  `rol` enum('BIBLIOTECARIO', 'USUARIO', 'ADMIN'),
  PRIMARY KEY (`idUsuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `prestamos`;
CREATE TABLE `prestamos` (
  `idPrestamo` int NOT NULL AUTO_INCREMENT,
  `fechaPrestamo` date NOT NULL,
  `fechaDevolucion` date DEFAULT NULL,
  `estado` varchar(20) DEFAULT 'ACTIVO',
  `idUsuario` int NOT NULL,
  `idLibro` int NOT NULL,
  PRIMARY KEY (`idPrestamo`),
  KEY `idx_prestamo_usuario` (`idUsuario`),
  KEY `idx_prestamo_libro` (`idLibro`),
  CONSTRAINT `fk_prestamo_usuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`) ON DELETE CASCADE,
  CONSTRAINT `fk_prestamo_libro` FOREIGN KEY (`idLibro`) REFERENCES `libros` (`idLibro`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


SET FOREIGN_KEY_CHECKS = 1;