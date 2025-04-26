-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: mysql-beatpasstfg.alwaysdata.net
-- Generation Time: Apr 26, 2025 at 07:47 PM
-- Server version: 10.11.11-MariaDB
-- PHP Version: 7.4.33

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `beatpasstfg_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `asistentes`
--

CREATE TABLE `asistentes` (
  `id_asistente` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `fecha_creacion` datetime DEFAULT current_timestamp(),
  `fecha_modificacion` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `asistentes`
--

INSERT INTO `asistentes` (`id_asistente`, `nombre`, `email`, `telefono`, `fecha_creacion`, `fecha_modificacion`) VALUES
(29, 'Ana García López', 'ana.garcia@email.com', '611223344', '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(30, 'Luis Martínez Ruiz', 'luis.martinez@email.es', '622334455', '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(31, 'Elena Sánchez Gómez', 'elena.sanchez@email.net', NULL, '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(32, 'David Fernández Pérez', 'david.fernandez@email.org', '633445566', '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(33, 'Laura Jiménez González', 'laura.jimenez@email.com', '644556677', '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(34, 'Sergio Rodríguez Díaz', 'sergio.rodriguez@email.es', NULL, '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(35, 'Carmen Moreno Álvarez', 'carmen.moreno@email.com', '655667788', '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(36, 'Pablo Romero Navarro', 'pablo.romero@email.net', '666778899', '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(37, 'Isabel Gutiérrez Iglesias', 'isabel.gutierrez@email.org', '677889900', '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(38, 'Javier Vázquez Blanco', 'javier.vazquez@email.com', NULL, '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(39, 'Raquel Molina Castrillo', 'raquel.molina@email.es', '688990011', '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(40, 'Francisco Domínguez Soto', 'francisco.dominguez@email.net', '699001122', '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(41, 'Beatriz Ramos Gil', 'beatriz.ramos@email.com', '600112233', '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(42, 'Álvaro Ortega Crespo', 'alvaro.ortega@email.es', NULL, '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(43, 'Marta Reyes Gallego', 'marta.reyes@email.org', '612345678', '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(44, 'Adrián Santos Rubio', 'adrian.santos@email.com', '623456789', '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(45, 'Natalia Alonso Marín', 'natalia.alonso@email.es', '634567890', '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(46, 'Daniel Mora Vidal', 'daniel.mora@email.net', NULL, '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(47, 'Cristina Pascual Serrano', 'cristina.pascual@email.com', '645678901', '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(48, 'Rubén Soler Camacho', 'ruben.soler@email.org', '656789012', '2025-04-26 14:14:08', '2025-04-26 14:14:08'),
(49, 'Edu Ardo', 'edu@edu.com', '718273645', '2025-04-26 17:46:10', '2025-04-26 17:46:10');

-- --------------------------------------------------------

--
-- Table structure for table `compras`
--

CREATE TABLE `compras` (
  `id_compra` int(11) NOT NULL,
  `id_asistente` int(11) NOT NULL,
  `fecha_compra` datetime DEFAULT current_timestamp(),
  `total` decimal(10,2) NOT NULL CHECK (`total` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `compras`
--

INSERT INTO `compras` (`id_compra`, `id_asistente`, `fecha_compra`, `total`) VALUES
(29, 29, '2025-04-26 14:57:46', 150.00),
(30, 32, '2025-04-26 14:57:46', 120.00),
(31, 35, '2025-04-26 14:57:46', 270.00),
(32, 41, '2025-04-26 14:57:46', 210.00),
(33, 48, '2025-04-26 14:57:46', 120.00),
(34, 29, '2025-04-26 14:56:46', 900.00),
(35, 49, '2025-04-26 17:46:11', 60.00);

-- --------------------------------------------------------

--
-- Table structure for table `compra_entradas`
--

CREATE TABLE `compra_entradas` (
  `id_compra_entrada` int(11) NOT NULL,
  `id_compra` int(11) NOT NULL,
  `id_entrada` int(11) NOT NULL,
  `cantidad` int(11) NOT NULL CHECK (`cantidad` > 0),
  `precio_unitario` decimal(8,2) NOT NULL CHECK (`precio_unitario` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `compra_entradas`
--

INSERT INTO `compra_entradas` (`id_compra_entrada`, `id_compra`, `id_entrada`, `cantidad`, `precio_unitario`) VALUES
(32, 29, 44, 2, 75.00),
(33, 30, 47, 1, 120.00),
(34, 31, 50, 3, 90.00),
(35, 32, 45, 1, 150.00),
(36, 32, 49, 1, 60.00),
(37, 33, 52, 2, 60.00),
(38, 34, 50, 10, 90.00),
(39, 35, 49, 1, 60.00);

-- --------------------------------------------------------

--
-- Table structure for table `consumos`
--

CREATE TABLE `consumos` (
  `id_consumo` int(11) NOT NULL,
  `id_pulsera` int(11) NOT NULL,
  `id_festival` int(11) NOT NULL,
  `descripcion` tinytext DEFAULT NULL,
  `monto` decimal(8,2) NOT NULL CHECK (`monto` > 0),
  `fecha` datetime DEFAULT current_timestamp(),
  `id_punto_venta` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `entradas`
--

CREATE TABLE `entradas` (
  `id_entrada` int(11) NOT NULL,
  `id_festival` int(11) NOT NULL,
  `tipo` varchar(50) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `precio` decimal(8,2) NOT NULL CHECK (`precio` >= 0),
  `stock` int(11) NOT NULL CHECK (`stock` >= 0),
  `fecha_creacion` datetime DEFAULT current_timestamp(),
  `fecha_modificacion` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `entradas`
--

INSERT INTO `entradas` (`id_entrada`, `id_festival`, `tipo`, `descripcion`, `precio`, `stock`, `fecha_creacion`, `fecha_modificacion`) VALUES
(44, 18, 'Abono General', 'Acceso los 3 días al recinto general.', 75.00, 10000, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(45, 18, 'Abono VIP', 'Acceso los 3 días a zona VIP y general.', 150.00, 2000, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(46, 18, 'Entrada Jueves', 'Acceso el jueves 10 de Julio.', 35.00, 3000, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(47, 19, 'Abono Completo', 'Acceso todos los días + camping.', 120.00, 15000, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(48, 19, 'Entrada Viernes', 'Acceso el viernes 22 de Agosto.', 50.00, 5000, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(49, 19, 'Entrada Sábado', 'Acceso el sábado 23 de Agosto.', 60.00, 4999, '2025-04-26 14:57:46', '2025-04-26 17:46:12'),
(50, 20, 'Abono General', 'Acceso los 3 días.', 90.00, 20000, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(51, 20, 'Abono Premium', 'Acceso 3 días + Front Stage.', 180.00, 3000, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(52, 21, 'Abono Eco-Friendly', 'Acceso 3 días + Taller reciclaje.', 60.00, 5000, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(53, 21, 'Entrada Sábado', 'Acceso sábado 21 de Junio.', 30.00, 2000, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(54, 19, 'Abono VIP', 'Acceso todos los días + acceso VIP', 150.00, 1000, '2025-04-26 19:21:12', '2025-04-26 19:21:49');

-- --------------------------------------------------------

--
-- Table structure for table `entradas_asignadas`
--

CREATE TABLE `entradas_asignadas` (
  `id_entrada_asignada` int(11) NOT NULL,
  `id_compra_entrada` int(11) NOT NULL,
  `id_asistente` int(11) DEFAULT NULL,
  `codigo_qr` varchar(255) NOT NULL,
  `estado` enum('ACTIVA','USADA','CANCELADA') DEFAULT 'ACTIVA',
  `fecha_asignacion` datetime DEFAULT NULL,
  `fecha_uso` datetime DEFAULT NULL,
  `fecha_creacion` datetime DEFAULT current_timestamp(),
  `fecha_modificacion` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `entradas_asignadas`
--

INSERT INTO `entradas_asignadas` (`id_entrada_asignada`, `id_compra_entrada`, `id_asistente`, `codigo_qr`, `estado`, `fecha_asignacion`, `fecha_uso`, `fecha_creacion`, `fecha_modificacion`) VALUES
(54, 32, 29, 'QR-F18-A29-0cbeac4b-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(55, 32, 30, 'QR-F18-NULL-0cbeaecb-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(56, 33, 32, 'QR-F19-A32-0cbf2963-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(57, 34, 35, 'QR-F20-A35-0cbf83f4-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(58, 34, 31, 'QR-F20-NULL-0cbf862b-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(59, 34, 33, 'QR-F20-NULL-0cbf8720-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(60, 35, 41, 'QR-F18VIP-A41-0cc01a3d-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(61, 36, 34, 'QR-F19SAB-NULL-0cc06328-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(62, 37, 48, 'QR-F21-A48-0cc0a645-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(63, 37, 36, 'QR-F21-NULL-0cc0a79f-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(64, 38, 37, 'QR-F20-NULL-0cc312eb-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(65, 38, 38, 'QR-F20-NULL-0cc31564-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(66, 38, 39, 'QR-F20-NULL-0cc3163d-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(67, 38, 40, 'QR-F20-NULL-0cc316f1-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(68, 38, 42, 'QR-F20-NULL-0cc3179e-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(69, 38, 43, 'QR-F20-NULL-0cc31844-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(70, 38, 44, 'QR-F20-NULL-0cc318d7-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(71, 38, 45, 'QR-F20-NULL-0cc31977-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(72, 38, 46, 'QR-F20-NULL-0cc319f7-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(73, 38, 47, 'QR-F20-NULL-0cc31a85-229e-11f0-86e0-c1bc6dea4ec9', 'ACTIVA', '2025-04-26 14:57:46', NULL, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(74, 39, 49, 'BEATPASS-TICKET-abfc45b6-2cc6-4ed5-8373-631ccf9e3616', 'ACTIVA', '2025-04-26 15:46:40', NULL, '2025-04-26 17:46:11', '2025-04-26 17:46:41');

-- --------------------------------------------------------

--
-- Table structure for table `estadisticas_festival`
--

CREATE TABLE `estadisticas_festival` (
  `id_festival` int(11) NOT NULL,
  `entradas_vendidas` int(11) DEFAULT 0 CHECK (`entradas_vendidas` >= 0),
  `ingresos_entradas` decimal(12,2) DEFAULT 0.00 CHECK (`ingresos_entradas` >= 0),
  `recargas_totales` decimal(12,2) DEFAULT 0.00 CHECK (`recargas_totales` >= 0),
  `consumos_totales` decimal(12,2) DEFAULT 0.00 CHECK (`consumos_totales` >= 0),
  `saldo_no_reclamado` decimal(12,2) DEFAULT 0.00 CHECK (`saldo_no_reclamado` >= 0),
  `ultima_actualizacion` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `festivales`
--

CREATE TABLE `festivales` (
  `id_festival` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_fin` date NOT NULL,
  `ubicacion` varchar(255) DEFAULT NULL,
  `id_promotor` int(11) NOT NULL,
  `aforo` int(11) DEFAULT NULL,
  `imagen_url` varchar(255) DEFAULT NULL,
  `estado` enum('BORRADOR','PUBLICADO','CANCELADO','FINALIZADO') DEFAULT 'BORRADOR',
  `fecha_creacion` datetime DEFAULT current_timestamp(),
  `fecha_modificacion` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `festivales`
--

INSERT INTO `festivales` (`id_festival`, `nombre`, `descripcion`, `fecha_inicio`, `fecha_fin`, `ubicacion`, `id_promotor`, `aforo`, `imagen_url`, `estado`, `fecha_creacion`, `fecha_modificacion`) VALUES
(18, 'Festival del Sol Naciente', 'Música electrónica y alternativa bajo el sol.', '2025-07-10', '2025-07-12', 'Playa de Levante, Benidorm', 9, 15000, 'https://placehold.co/600x400/F2994A/ffffff?text=Festival+Sol+Naciente', 'PUBLICADO', '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(19, 'Luna Negra Fest', 'Rock y metal en un entorno único.', '2025-08-22', '2025-08-24', 'Recinto Ferial, Villarrobledo', 10, 25000, 'https://placehold.co/600x400/333333/ffffff?text=Luna+Negra+Fest', 'PUBLICADO', '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(20, 'Ritmos del Sur', 'Festival de música urbana y latina.', '2025-09-05', '2025-09-07', 'Estadio Olímpico, Sevilla', 11, 30000, 'https://placehold.co/600x400/EB5757/ffffff?text=Ritmos+del+Sur', 'PUBLICADO', '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(21, 'EcoSound Festival', 'Música indie y pop con conciencia ecológica.', '2025-06-20', '2025-06-22', 'Parque Natural, Sierra de Gredos', 12, 8000, 'https://placehold.co/600x400/27AE60/ffffff?text=EcoSound+Festival', 'BORRADOR', '2025-04-26 14:57:46', '2025-04-26 14:57:46');

-- --------------------------------------------------------

--
-- Table structure for table `pulseras_nfc`
--

CREATE TABLE `pulseras_nfc` (
  `id_pulsera` int(11) NOT NULL,
  `codigo_uid` varchar(100) NOT NULL,
  `id_entrada_asignada` int(11) DEFAULT NULL,
  `saldo` decimal(10,2) DEFAULT 0.00 CHECK (`saldo` >= 0),
  `activa` tinyint(1) DEFAULT 1,
  `id_festival` int(11) NOT NULL,
  `fecha_asociacion` datetime DEFAULT NULL,
  `fecha_creacion` datetime DEFAULT current_timestamp(),
  `fecha_modificacion` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `fecha_alta` datetime DEFAULT current_timestamp(),
  `ultima_modificacion` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `pulseras_nfc`
--

INSERT INTO `pulseras_nfc` (`id_pulsera`, `codigo_uid`, `id_entrada_asignada`, `saldo`, `activa`, `id_festival`, `fecha_asociacion`, `fecha_creacion`, `fecha_modificacion`, `fecha_alta`, `ultima_modificacion`) VALUES
(1, 'PULSERA-001', 1, 95.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:39:28', '2025-04-21 21:54:09', '2025-04-26 18:39:28'),
(2, 'PULSERA-002', 54, 0.00, 1, 18, '2025-04-26 14:57:46', '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(3, 'PULSERA-003', 56, 0.00, 1, 19, '2025-04-26 14:57:46', '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(4, 'PULSERA-004', 74, 0.00, 1, 19, '2025-04-26 14:57:46', '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(5, 'PULSERA-005', 8, 50.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:39:28', '2025-04-21 21:54:09', '2025-04-26 18:39:28'),
(6, 'PULSERA-006', 13, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:39:28', '2025-04-21 21:54:09', '2025-04-26 18:39:28'),
(7, 'PULSERA-007', 60, 0.00, 1, 18, '2025-04-26 14:57:46', '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(8, 'PULSERA-008', 66, 0.00, 1, 20, '2025-04-26 14:57:46', '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(9, 'PULSERA-009', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(10, 'PULSERA-010', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(11, 'PULSERA-011', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(12, 'PULSERA-012', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(13, 'PULSERA-013', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(14, 'PULSERA-014', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(15, 'PULSERA-015', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(16, 'PULSERA-016', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(17, 'PULSERA-017', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(18, 'PULSERA-018', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(19, 'PULSERA-019', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(20, 'PULSERA-020', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(21, 'PULSERA-021', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(22, 'PULSERA-022', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(23, 'PULSERA-023', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(24, 'PULSERA-024', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(25, 'PULSERA-025', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(26, 'PULSERA-026', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(27, 'PULSERA-027', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(28, 'PULSERA-028', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(29, 'PULSERA-029', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(30, 'PULSERA-030', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(31, 'PULSERA-031', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(32, 'PULSERA-032', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(33, 'PULSERA-033', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(34, 'PULSERA-034', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(35, 'PULSERA-035', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(36, 'PULSERA-036', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(37, 'PULSERA-037', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(38, 'PULSERA-038', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(39, 'PULSERA-039', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(40, 'PULSERA-040', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(41, 'PULSERA-041', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(42, 'PULSERA-042', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(43, 'PULSERA-043', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(44, 'PULSERA-044', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(45, 'PULSERA-045', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(46, 'PULSERA-046', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(47, 'PULSERA-047', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(48, 'PULSERA-048', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(49, 'PULSERA-049', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(50, 'PULSERA-050', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(51, 'PULSERA-051', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(52, 'PULSERA-052', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(53, 'PULSERA-053', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(54, 'PULSERA-054', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(55, 'PULSERA-055', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(56, 'PULSERA-056', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(57, 'PULSERA-057', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(58, 'PULSERA-058', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(59, 'PULSERA-059', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(60, 'PULSERA-060', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(61, 'PULSERA-061', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(62, 'PULSERA-062', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(63, 'PULSERA-063', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(64, 'PULSERA-064', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(65, 'PULSERA-065', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(66, 'PULSERA-066', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(67, 'PULSERA-067', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(68, 'PULSERA-068', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(69, 'PULSERA-069', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(70, 'PULSERA-070', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(71, 'PULSERA-071', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(72, 'PULSERA-072', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(73, 'PULSERA-073', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(74, 'PULSERA-074', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(75, 'PULSERA-075', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(76, 'PULSERA-076', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(77, 'PULSERA-077', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(78, 'PULSERA-078', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(79, 'PULSERA-079', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(80, 'PULSERA-080', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(81, 'PULSERA-081', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(82, 'PULSERA-082', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(83, 'PULSERA-083', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(84, 'PULSERA-084', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(85, 'PULSERA-085', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(86, 'PULSERA-086', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(87, 'PULSERA-087', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(88, 'PULSERA-088', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(89, 'PULSERA-089', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(90, 'PULSERA-090', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(91, 'PULSERA-091', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(92, 'PULSERA-092', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(93, 'PULSERA-093', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(94, 'PULSERA-094', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(95, 'PULSERA-095', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(96, 'PULSERA-096', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(97, 'PULSERA-097', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(98, 'PULSERA-098', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(99, 'PULSERA-099', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38'),
(100, 'PULSERA-100', NULL, 0.00, 1, 18, NULL, '2025-04-21 21:54:09', '2025-04-26 18:27:38', '2025-04-21 21:54:09', '2025-04-26 18:27:38');

-- --------------------------------------------------------

--
-- Table structure for table `recargas`
--

CREATE TABLE `recargas` (
  `id_recarga` int(11) NOT NULL,
  `id_pulsera` int(11) NOT NULL,
  `monto` decimal(8,2) NOT NULL CHECK (`monto` > 0),
  `fecha` datetime DEFAULT current_timestamp(),
  `metodo_pago` varchar(50) DEFAULT NULL,
  `id_usuario_cajero` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `usuarios`
--

CREATE TABLE `usuarios` (
  `id_usuario` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `rol` enum('ADMIN','PROMOTOR','CAJERO') NOT NULL,
  `estado` tinyint(1) DEFAULT 1,
  `cambio_password_requerido` tinyint(1) NOT NULL DEFAULT 1,
  `fecha_creacion` datetime DEFAULT current_timestamp(),
  `fecha_modificacion` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `usuarios`
--

INSERT INTO `usuarios` (`id_usuario`, `nombre`, `email`, `password`, `rol`, `estado`, `cambio_password_requerido`, `fecha_creacion`, `fecha_modificacion`) VALUES
(1, 'admin', 'admin@beatpass.com', '$2a$12$UzQ4wCJ/9WC9qTvXU3Rwg.BQdF1Ct8sGwA7eg1OCycSiiqExwwZPe', 'ADMIN', 1, 0, '2025-04-18 17:11:43', '2025-04-22 23:23:01'),
(4, 'Punto Venta 1', 'cajero@beatpass.com', '$2a$12$UzQ4wCJ/9WC9qTvXU3Rwg.BQdF1Ct8sGwA7eg1OCycSiiqExwwZPe', 'CAJERO', 1, 0, '2025-04-22 23:23:49', '2025-04-22 23:23:49'),
(9, 'Promociones FiestaTotal', 'contacto@fiestatotal.com', '$2a$12$p.1C/PPo/9SSgatchToISeRRG3xsnpzEzYmfFEfVpRxDhVnatOtMy', 'PROMOTOR', 1, 1, '2025-04-26 14:14:08', '2025-04-26 14:16:26'),
(10, 'Eventos Luna Llena', 'info@eventoslunallena.es', '$2a$12$Yr8obyuaQZEpXBuhDITvuugOxtAdCqxWJZyGuB5arl5LczkX9gbRe', 'PROMOTOR', 1, 0, '2025-04-26 14:14:08', '2025-04-26 14:37:12'),
(11, 'Ritmo Producciones', 'gestion@ritmoproducciones.com', '$2a$12$p.1C/PPo/9SSgatchToISeRRG3xsnpzEzYmfFEfVpRxDhVnatOtMy', 'PROMOTOR', 1, 0, '2025-04-26 14:14:08', '2025-04-26 14:16:26'),
(12, 'Noches de Verano SL', 'admin@nochesverano.es', '$2a$12$p.1C/PPo/9SSgatchToISeRRG3xsnpzEzYmfFEfVpRxDhVnatOtMy', 'PROMOTOR', 1, 1, '2025-04-26 14:14:08', '2025-04-26 14:16:26');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `asistentes`
--
ALTER TABLE `asistentes`
  ADD PRIMARY KEY (`id_asistente`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `uq_asistente_email` (`email`);

--
-- Indexes for table `compras`
--
ALTER TABLE `compras`
  ADD PRIMARY KEY (`id_compra`),
  ADD KEY `idx_compras_asistente` (`id_asistente`),
  ADD KEY `idx_compras_fecha` (`fecha_compra`);

--
-- Indexes for table `compra_entradas`
--
ALTER TABLE `compra_entradas`
  ADD PRIMARY KEY (`id_compra_entrada`),
  ADD KEY `idx_compraentradas_compra` (`id_compra`),
  ADD KEY `idx_compraentradas_entrada` (`id_entrada`);

--
-- Indexes for table `consumos`
--
ALTER TABLE `consumos`
  ADD PRIMARY KEY (`id_consumo`),
  ADD KEY `idx_consumos_pulsera` (`id_pulsera`),
  ADD KEY `idx_consumos_festival` (`id_festival`),
  ADD KEY `idx_consumos_fecha` (`fecha`);

--
-- Indexes for table `entradas`
--
ALTER TABLE `entradas`
  ADD PRIMARY KEY (`id_entrada`),
  ADD KEY `idx_entradas_festival` (`id_festival`);

--
-- Indexes for table `entradas_asignadas`
--
ALTER TABLE `entradas_asignadas`
  ADD PRIMARY KEY (`id_entrada_asignada`),
  ADD UNIQUE KEY `codigo_qr` (`codigo_qr`),
  ADD UNIQUE KEY `uq_entradaasignada_codigoqr` (`codigo_qr`),
  ADD KEY `idx_entradasasignadas_compraentrada` (`id_compra_entrada`),
  ADD KEY `idx_entradasasignadas_asistente` (`id_asistente`),
  ADD KEY `idx_entradasasignadas_estado` (`estado`);

--
-- Indexes for table `estadisticas_festival`
--
ALTER TABLE `estadisticas_festival`
  ADD PRIMARY KEY (`id_festival`);

--
-- Indexes for table `festivales`
--
ALTER TABLE `festivales`
  ADD PRIMARY KEY (`id_festival`),
  ADD KEY `idx_festivales_promotor` (`id_promotor`),
  ADD KEY `idx_festivales_fechas` (`fecha_inicio`,`fecha_fin`),
  ADD KEY `idx_festivales_estado` (`estado`);

--
-- Indexes for table `pulseras_nfc`
--
ALTER TABLE `pulseras_nfc`
  ADD PRIMARY KEY (`id_pulsera`),
  ADD UNIQUE KEY `codigo_uid` (`codigo_uid`),
  ADD UNIQUE KEY `uq_pulseranfc_codigouid` (`codigo_uid`),
  ADD UNIQUE KEY `id_entrada_asignada` (`id_entrada_asignada`),
  ADD UNIQUE KEY `uq_pulseranfc_entradaasignada` (`id_entrada_asignada`),
  ADD KEY `idx_pulserasnfc_entradaasignada` (`id_entrada_asignada`),
  ADD KEY `idx_pulserasnfc_festival` (`id_festival`);

--
-- Indexes for table `recargas`
--
ALTER TABLE `recargas`
  ADD PRIMARY KEY (`id_recarga`),
  ADD KEY `id_usuario_cajero` (`id_usuario_cajero`),
  ADD KEY `idx_recargas_pulsera` (`id_pulsera`),
  ADD KEY `idx_recargas_fecha` (`fecha`);

--
-- Indexes for table `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `uq_usuario_email` (`email`),
  ADD KEY `idx_usuarios_rol` (`rol`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `asistentes`
--
ALTER TABLE `asistentes`
  MODIFY `id_asistente` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=50;

--
-- AUTO_INCREMENT for table `compras`
--
ALTER TABLE `compras`
  MODIFY `id_compra` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=36;

--
-- AUTO_INCREMENT for table `compra_entradas`
--
ALTER TABLE `compra_entradas`
  MODIFY `id_compra_entrada` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=40;

--
-- AUTO_INCREMENT for table `consumos`
--
ALTER TABLE `consumos`
  MODIFY `id_consumo` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `entradas`
--
ALTER TABLE `entradas`
  MODIFY `id_entrada` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=55;

--
-- AUTO_INCREMENT for table `entradas_asignadas`
--
ALTER TABLE `entradas_asignadas`
  MODIFY `id_entrada_asignada` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=75;

--
-- AUTO_INCREMENT for table `festivales`
--
ALTER TABLE `festivales`
  MODIFY `id_festival` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT for table `pulseras_nfc`
--
ALTER TABLE `pulseras_nfc`
  MODIFY `id_pulsera` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=101;

--
-- AUTO_INCREMENT for table `recargas`
--
ALTER TABLE `recargas`
  MODIFY `id_recarga` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `compras`
--
ALTER TABLE `compras`
  ADD CONSTRAINT `compras_ibfk_1` FOREIGN KEY (`id_asistente`) REFERENCES `asistentes` (`id_asistente`) ON UPDATE CASCADE;

--
-- Constraints for table `compra_entradas`
--
ALTER TABLE `compra_entradas`
  ADD CONSTRAINT `compra_entradas_ibfk_1` FOREIGN KEY (`id_compra`) REFERENCES `compras` (`id_compra`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `compra_entradas_ibfk_2` FOREIGN KEY (`id_entrada`) REFERENCES `entradas` (`id_entrada`) ON UPDATE CASCADE;

--
-- Constraints for table `consumos`
--
ALTER TABLE `consumos`
  ADD CONSTRAINT `consumos_ibfk_1` FOREIGN KEY (`id_pulsera`) REFERENCES `pulseras_nfc` (`id_pulsera`) ON UPDATE CASCADE,
  ADD CONSTRAINT `consumos_ibfk_2` FOREIGN KEY (`id_festival`) REFERENCES `festivales` (`id_festival`) ON UPDATE CASCADE;

--
-- Constraints for table `entradas`
--
ALTER TABLE `entradas`
  ADD CONSTRAINT `entradas_ibfk_1` FOREIGN KEY (`id_festival`) REFERENCES `festivales` (`id_festival`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `entradas_asignadas`
--
ALTER TABLE `entradas_asignadas`
  ADD CONSTRAINT `entradas_asignadas_ibfk_1` FOREIGN KEY (`id_compra_entrada`) REFERENCES `compra_entradas` (`id_compra_entrada`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `entradas_asignadas_ibfk_2` FOREIGN KEY (`id_asistente`) REFERENCES `asistentes` (`id_asistente`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `estadisticas_festival`
--
ALTER TABLE `estadisticas_festival`
  ADD CONSTRAINT `estadisticas_festival_ibfk_1` FOREIGN KEY (`id_festival`) REFERENCES `festivales` (`id_festival`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `festivales`
--
ALTER TABLE `festivales`
  ADD CONSTRAINT `festivales_ibfk_1` FOREIGN KEY (`id_promotor`) REFERENCES `usuarios` (`id_usuario`) ON UPDATE CASCADE;

--
-- Constraints for table `pulseras_nfc`
--
ALTER TABLE `pulseras_nfc`
  ADD CONSTRAINT `FKb6mtkr037q3e0ppmrd1r0wrgj` FOREIGN KEY (`id_festival`) REFERENCES `festivales` (`id_festival`),
  ADD CONSTRAINT `pulseras_nfc_ibfk_1` FOREIGN KEY (`id_entrada_asignada`) REFERENCES `entradas_asignadas` (`id_entrada_asignada`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `recargas`
--
ALTER TABLE `recargas`
  ADD CONSTRAINT `recargas_ibfk_1` FOREIGN KEY (`id_pulsera`) REFERENCES `pulseras_nfc` (`id_pulsera`) ON UPDATE CASCADE,
  ADD CONSTRAINT `recargas_ibfk_2` FOREIGN KEY (`id_usuario_cajero`) REFERENCES `usuarios` (`id_usuario`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
