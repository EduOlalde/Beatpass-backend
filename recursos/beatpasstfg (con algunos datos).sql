-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 21, 2025 at 09:55 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `beatpasstfg`
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
(1, 'Pepito Perez', 'pep@pep.com', '666666666', '2025-04-21 16:22:32', '2025-04-21 16:22:32'),
(2, 'Pedrito Pedrez', 'ped@p.com', '666666667', '2025-04-21 16:42:13', '2025-04-21 16:42:13'),
(3, 'Maria Mari', 'mar@m.com', '666666669', '2025-04-21 16:42:27', '2025-04-21 18:24:49'),
(4, 'Larita Lara', 'lal@lal.com', '654533567', '2025-04-21 21:38:24', '2025-04-21 21:38:24');

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
(1, 1, '2025-04-21 16:26:15', 51.00),
(2, 1, '2025-04-21 16:43:21', 100.00),
(3, 2, '2025-04-21 21:37:29', 100.00);

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
(1, 1, 1, 2, 25.50),
(3, 3, 3, 2, 50.00);

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
(1, 1, 'General', 'Entrada general', 25.50, 9999, '2025-04-21 15:19:07', '2025-04-21 16:26:43'),
(3, 1, 'VIP', 'Entrada VIP', 50.00, 1998, '2025-04-21 21:37:07', '2025-04-21 21:37:29');

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
(1, 1, 1, 'BEATPASS-TICKET-28857f6a-6f64-4305-9e13-70d297af0af0', 'ACTIVA', '2025-04-21 14:38:42', NULL, '2025-04-21 16:26:15', '2025-04-21 16:38:42'),
(2, 1, NULL, 'BEATPASS-TICKET-05e07a27-5ed7-4852-9fd3-5a8005cc869a', 'CANCELADA', NULL, NULL, '2025-04-21 16:26:15', '2025-04-21 16:26:43'),
(5, 3, 4, 'BEATPASS-TICKET-c5a88837-f090-4ae2-86b6-e9d96fd9968b', 'ACTIVA', '2025-04-21 19:38:24', NULL, '2025-04-21 21:37:29', '2025-04-21 21:38:24'),
(6, 3, NULL, 'BEATPASS-TICKET-9cebece1-0794-477e-b330-1952ef1e17e5', 'ACTIVA', NULL, NULL, '2025-04-21 21:37:29', '2025-04-21 21:37:29');

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
(1, 'SuperFest', 'El festival más super de España', '2025-04-25', '2025-04-27', 'Zaragoza', 2, 20000, '', 'PUBLICADO', '2025-04-21 01:05:28', '2025-04-21 16:09:24');

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
  `fecha_asociacion` datetime DEFAULT NULL,
  `fecha_creacion` datetime DEFAULT current_timestamp(),
  `fecha_modificacion` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `fecha_alta` datetime DEFAULT current_timestamp(),
  `ultima_modificacion` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `pulseras_nfc`
--

INSERT INTO `pulseras_nfc` (`id_pulsera`, `codigo_uid`, `id_entrada_asignada`, `saldo`, `activa`, `fecha_asociacion`, `fecha_creacion`, `fecha_modificacion`, `fecha_alta`, `ultima_modificacion`) VALUES
(1, 'PULSERA-001', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(2, 'PULSERA-002', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(3, 'PULSERA-003', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(4, 'PULSERA-004', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(5, 'PULSERA-005', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(6, 'PULSERA-006', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(7, 'PULSERA-007', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(8, 'PULSERA-008', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(9, 'PULSERA-009', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(10, 'PULSERA-010', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(11, 'PULSERA-011', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(12, 'PULSERA-012', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(13, 'PULSERA-013', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(14, 'PULSERA-014', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(15, 'PULSERA-015', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(16, 'PULSERA-016', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(17, 'PULSERA-017', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(18, 'PULSERA-018', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(19, 'PULSERA-019', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(20, 'PULSERA-020', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(21, 'PULSERA-021', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(22, 'PULSERA-022', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(23, 'PULSERA-023', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(24, 'PULSERA-024', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(25, 'PULSERA-025', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(26, 'PULSERA-026', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(27, 'PULSERA-027', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(28, 'PULSERA-028', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(29, 'PULSERA-029', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(30, 'PULSERA-030', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(31, 'PULSERA-031', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(32, 'PULSERA-032', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(33, 'PULSERA-033', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(34, 'PULSERA-034', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(35, 'PULSERA-035', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(36, 'PULSERA-036', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(37, 'PULSERA-037', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(38, 'PULSERA-038', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(39, 'PULSERA-039', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(40, 'PULSERA-040', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(41, 'PULSERA-041', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(42, 'PULSERA-042', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(43, 'PULSERA-043', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(44, 'PULSERA-044', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(45, 'PULSERA-045', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(46, 'PULSERA-046', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(47, 'PULSERA-047', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(48, 'PULSERA-048', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(49, 'PULSERA-049', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(50, 'PULSERA-050', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(51, 'PULSERA-051', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(52, 'PULSERA-052', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(53, 'PULSERA-053', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(54, 'PULSERA-054', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(55, 'PULSERA-055', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(56, 'PULSERA-056', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(57, 'PULSERA-057', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(58, 'PULSERA-058', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(59, 'PULSERA-059', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(60, 'PULSERA-060', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(61, 'PULSERA-061', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(62, 'PULSERA-062', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(63, 'PULSERA-063', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(64, 'PULSERA-064', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(65, 'PULSERA-065', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(66, 'PULSERA-066', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(67, 'PULSERA-067', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(68, 'PULSERA-068', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(69, 'PULSERA-069', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(70, 'PULSERA-070', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(71, 'PULSERA-071', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(72, 'PULSERA-072', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(73, 'PULSERA-073', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(74, 'PULSERA-074', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(75, 'PULSERA-075', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(76, 'PULSERA-076', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(77, 'PULSERA-077', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(78, 'PULSERA-078', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(79, 'PULSERA-079', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(80, 'PULSERA-080', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(81, 'PULSERA-081', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(82, 'PULSERA-082', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(83, 'PULSERA-083', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(84, 'PULSERA-084', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(85, 'PULSERA-085', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(86, 'PULSERA-086', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(87, 'PULSERA-087', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(88, 'PULSERA-088', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(89, 'PULSERA-089', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(90, 'PULSERA-090', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(91, 'PULSERA-091', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(92, 'PULSERA-092', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(93, 'PULSERA-093', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(94, 'PULSERA-094', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(95, 'PULSERA-095', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(96, 'PULSERA-096', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(97, 'PULSERA-097', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(98, 'PULSERA-098', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(99, 'PULSERA-099', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09'),
(100, 'PULSERA-100', NULL, 0.00, 1, NULL, '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09', '2025-04-21 21:54:09');

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
  `rol` enum('ADMIN','PROMOTOR') NOT NULL,
  `estado` tinyint(1) DEFAULT 1,
  `cambio_password_requerido` tinyint(1) NOT NULL DEFAULT 1,
  `fecha_creacion` datetime DEFAULT current_timestamp(),
  `fecha_modificacion` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `usuarios`
--

INSERT INTO `usuarios` (`id_usuario`, `nombre`, `email`, `password`, `rol`, `estado`, `cambio_password_requerido`, `fecha_creacion`, `fecha_modificacion`) VALUES
(1, 'admin', 'admin@beatpass.com', '$2a$12$Ojk4J1J6IfqH58cHvIC3Fum2qEFhPGQY4Plw5hga0AiNRIbaRgkDa', 'ADMIN', 1, 0, '2025-04-18 17:11:43', '2025-04-21 13:57:12'),
(2, 'Promotor1', 'promotor1@beatpass.com', '$2a$12$/gvki8tXKm6Wxx8Pl2v0.ex1y79kYi2w0mIQf2XcImmOQM15Pdgmm', 'PROMOTOR', 1, 0, '2025-04-21 00:25:40', '2025-04-21 13:45:07'),
(3, 'Promotor2', 'promotor2@beatpass.com', '$2a$12$oa7doPOTayjcTQzoy7l27O/zn29zGOGUpIIS05YrZpNi3//2zOoO6', 'PROMOTOR', 1, 1, '2025-04-21 00:40:11', '2025-04-21 00:41:08');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `asistentes`
--
ALTER TABLE `asistentes`
  ADD PRIMARY KEY (`id_asistente`),
  ADD UNIQUE KEY `email` (`email`);

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
  ADD UNIQUE KEY `id_entrada_asignada` (`id_entrada_asignada`),
  ADD KEY `idx_pulserasnfc_entradaasignada` (`id_entrada_asignada`);

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
  ADD KEY `idx_usuarios_rol` (`rol`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `asistentes`
--
ALTER TABLE `asistentes`
  MODIFY `id_asistente` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `compras`
--
ALTER TABLE `compras`
  MODIFY `id_compra` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `compra_entradas`
--
ALTER TABLE `compra_entradas`
  MODIFY `id_compra_entrada` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `consumos`
--
ALTER TABLE `consumos`
  MODIFY `id_consumo` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `entradas`
--
ALTER TABLE `entradas`
  MODIFY `id_entrada` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `entradas_asignadas`
--
ALTER TABLE `entradas_asignadas`
  MODIFY `id_entrada_asignada` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `festivales`
--
ALTER TABLE `festivales`
  MODIFY `id_festival` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `pulseras_nfc`
--
ALTER TABLE `pulseras_nfc`
  MODIFY `id_pulsera` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=101;

--
-- AUTO_INCREMENT for table `recargas`
--
ALTER TABLE `recargas`
  MODIFY `id_recarga` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

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
