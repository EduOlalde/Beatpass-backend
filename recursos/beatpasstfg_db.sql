-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: mysql-beatpasstfg.alwaysdata.net
-- Generation Time: May 12, 2025 at 01:08 PM
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
(62, 'Cristina Martínez', 'cristinam@gmail.com', '', '2025-05-12 12:13:36', '2025-05-12 12:13:36'),
(63, 'Patricia Martínez', 'patrim@gmail.com', '678123456', '2025-05-12 12:16:40', '2025-05-12 12:16:40'),
(64, 'Eduardo Martínez', 'edumar@gmail.com', '634123512', '2025-05-12 12:17:29', '2025-05-12 12:17:29'),
(65, 'Pedro Ramirez', 'pedroram@hotmail.com', '624312987', '2025-05-12 12:36:24', '2025-05-12 12:36:24'),
(66, 'Mario Lopez', 'mariolop@gmail.com', '', '2025-05-12 12:37:45', '2025-05-12 12:37:45'),
(67, 'Carlos Lopez', 'carlos90@gamil.com', '', '2025-05-12 12:38:11', '2025-05-12 12:38:11'),
(68, 'Raul Ramirez', 'raul91@hotmail.com', '', '2025-05-12 12:39:43', '2025-05-12 12:39:43');

-- --------------------------------------------------------

--
-- Table structure for table `compras`
--

CREATE TABLE `compras` (
  `id_compra` int(11) NOT NULL,
  `id_asistente` int(11) NOT NULL,
  `fecha_compra` datetime DEFAULT current_timestamp(),
  `total` decimal(10,2) NOT NULL CHECK (`total` >= 0),
  `stripe_payment_intent_id` varchar(255) DEFAULT NULL,
  `estado_pago` varchar(50) DEFAULT NULL,
  `fecha_pago_confirmado` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `compras`
--

INSERT INTO `compras` (`id_compra`, `id_asistente`, `fecha_compra`, `total`, `stripe_payment_intent_id`, `estado_pago`, `fecha_pago_confirmado`) VALUES
(46, 62, '2025-05-12 12:13:37', 360.00, 'pi_3RNtMV4Et9Src69R1tAn3EBg', 'PAGADO', '2025-05-12 10:13:31'),
(47, 65, '2025-05-12 12:36:25', 360.00, 'pi_3RNtid4Et9Src69R0cRWJytG', 'PAGADO', '2025-05-12 10:36:23'),
(48, 65, '2025-05-12 13:02:38', 170.00, 'pi_3RNu804Et9Src69R1YxTBRN2', 'PAGADO', '2025-05-12 11:02:36');

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
(50, 46, 47, 3, 120.00),
(51, 47, 50, 4, 90.00),
(52, 48, 55, 2, 85.00);

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

--
-- Dumping data for table `consumos`
--

INSERT INTO `consumos` (`id_consumo`, `id_pulsera`, `id_festival`, `descripcion`, `monto`, `fecha`, `id_punto_venta`) VALUES
(2, 104, 22, 'Bebida', 12.00, '2025-05-12 13:05:00', NULL);

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
(47, 19, 'Abono Completo', 'Acceso todos los días + camping.', 120.00, 12997, '2025-04-26 14:57:46', '2025-05-12 12:14:50'),
(48, 19, 'Entrada Viernes', 'Acceso el viernes 22 de Agosto.', 50.00, 3000, '2025-04-26 14:57:46', '2025-05-12 12:14:29'),
(49, 19, 'Entrada Sábado', 'Acceso el sábado 23 de Agosto.', 60.00, 3000, '2025-04-26 14:57:46', '2025-05-12 12:14:37'),
(50, 20, 'Abono General', 'Acceso los 3 días.', 90.00, 19996, '2025-04-26 14:57:46', '2025-05-12 12:36:25'),
(51, 20, 'Abono Premium', 'Acceso 3 días + Front Stage.', 135.00, 3000, '2025-04-26 14:57:46', '2025-05-12 13:01:36'),
(52, 21, 'Abono Eco-Friendly', 'Acceso 3 días + Taller reciclaje.', 60.00, 5000, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(53, 21, 'Entrada Sábado', 'Acceso sábado 21 de Junio.', 30.00, 2000, '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(54, 19, 'Abono VIP', 'Acceso todos los días + acceso VIP', 150.00, 1000, '2025-04-26 19:21:12', '2025-05-12 12:14:09'),
(55, 22, 'Abono General', 'Acceso los 3 días.', 85.00, 17998, '2025-05-12 13:00:48', '2025-05-12 13:02:38'),
(56, 22, 'Abono VIP', 'Acceso 3 días + Front Stage.', 130.00, 2000, '2025-05-12 13:01:24', '2025-05-12 13:01:24');

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
(186, 50, 62, 'BEATPASS-TICKET-d8643481-c856-452d-af04-008506a6e99f', 'ACTIVA', '2025-05-12 10:15:40', NULL, '2025-05-12 12:13:37', '2025-05-12 12:15:40'),
(187, 50, 63, 'BEATPASS-TICKET-8c884382-dd83-4d38-9463-b1bb37fa43b9', 'ACTIVA', '2025-05-12 10:16:40', NULL, '2025-05-12 12:13:37', '2025-05-12 12:16:40'),
(188, 50, 64, 'BEATPASS-TICKET-fafca816-cf86-4ec9-9343-e110dccea27a', 'ACTIVA', '2025-05-12 10:17:29', NULL, '2025-05-12 12:13:37', '2025-05-12 12:17:29'),
(189, 51, 65, 'BEATPASS-TICKET-4d04960d-6847-4b87-b797-7b5a528dbce3', 'ACTIVA', '2025-05-12 10:36:56', NULL, '2025-05-12 12:36:25', '2025-05-12 12:36:56'),
(190, 51, 66, 'BEATPASS-TICKET-4dffde9f-54c8-4ce6-a808-02961f5fc8f3', 'ACTIVA', '2025-05-12 10:37:45', NULL, '2025-05-12 12:36:25', '2025-05-12 12:37:45'),
(191, 51, 67, 'BEATPASS-TICKET-8208aaed-0947-48cc-b082-5b57bca419fd', 'ACTIVA', '2025-05-12 10:38:11', NULL, '2025-05-12 12:36:25', '2025-05-12 12:38:11'),
(192, 51, 68, 'BEATPASS-TICKET-2cf2c110-936f-4977-aff9-41fdf5dbca55', 'ACTIVA', '2025-05-12 10:39:43', NULL, '2025-05-12 12:36:25', '2025-05-12 12:39:43'),
(193, 52, 65, 'BEATPASS-TICKET-30c84976-d23f-4ece-97f9-36f47feb98f7', 'ACTIVA', '2025-05-12 11:02:49', NULL, '2025-05-12 13:02:38', '2025-05-12 13:02:49'),
(194, 52, 68, 'BEATPASS-TICKET-5ad1e96a-e206-4599-b86b-bfd2dc8ea859', 'ACTIVA', '2025-05-12 11:03:03', NULL, '2025-05-12 13:02:38', '2025-05-12 13:03:03');

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
(19, 'Luna Negra Fest', 'Rock y metal en un entorno único.', '2025-08-22', '2025-08-24', 'Recinto Ferial, Villarrobledo', 10, 25000, 'https://placehold.co/600x400/333333/ffffff?text=Luna+Negra+Fest', 'PUBLICADO', '2025-04-26 14:57:46', '2025-05-07 21:37:24'),
(20, 'Ritmos del Sur', 'Festival de música urbana y latina. Edición del 2025.', '2025-09-05', '2025-09-07', 'Estadio Olímpico, Sevilla', 11, 30000, 'https://placehold.co/600x400/EB5757/ffffff?text=Ritmos+del+Sur', 'PUBLICADO', '2025-04-26 14:57:46', '2025-05-12 12:52:41'),
(21, 'EcoSound Festival', 'Música indie y pop con conciencia ecológica.', '2025-06-20', '2025-06-22', 'Parque Natural, Sierra de Gredos', 12, 8000, 'https://placehold.co/600x400/27AE60/ffffff?text=EcoSound+Festival', 'BORRADOR', '2025-04-26 14:57:46', '2025-04-26 14:57:46'),
(22, 'Ritmos del Sur', 'Festival de música urbana y latina. Edición del 2024.', '2024-09-06', '2024-09-08', 'Estadio Olímpico, Sevilla', 11, 20000, 'https://placehold.co/600x400/EB5757/ffffff?text=Ritmos+del+Sur', 'PUBLICADO', '2025-05-12 12:53:20', '2025-05-12 12:56:26');

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
  `fecha_alta` datetime DEFAULT current_timestamp(),
  `ultima_modificacion` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `pulseras_nfc`
--

INSERT INTO `pulseras_nfc` (`id_pulsera`, `codigo_uid`, `id_entrada_asignada`, `saldo`, `activa`, `id_festival`, `fecha_asociacion`, `fecha_alta`, `ultima_modificacion`) VALUES
(101, 'pulsera-001', 186, 100.00, 1, 19, '2025-05-12 10:21:01', '2025-05-12 12:21:01', '2025-05-12 12:21:59'),
(102, 'pritmos-001', 189, 50.00, 1, 20, '2025-05-12 10:51:42', '2025-05-12 12:51:42', '2025-05-12 13:08:09'),
(103, 'prit2024-001', 193, 100.00, 1, 22, '2025-05-12 11:03:40', '2025-05-12 13:03:40', '2025-05-12 13:04:32'),
(104, 'prit2024-002', 194, 108.00, 1, 22, '2025-05-12 11:03:47', '2025-05-12 13:03:47', '2025-05-12 13:05:00');

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

--
-- Dumping data for table `recargas`
--

INSERT INTO `recargas` (`id_recarga`, `id_pulsera`, `monto`, `fecha`, `metodo_pago`, `id_usuario_cajero`) VALUES
(4, 101, 100.00, '2025-05-12 12:21:59', 'Tarjeta', 4),
(5, 103, 100.00, '2025-05-12 13:04:32', 'Efectivo', 4),
(6, 104, 120.00, '2025-05-12 13:04:48', 'Efectivo', 4),
(7, 102, 50.00, '2025-05-12 13:08:09', 'Efectivo', 4);

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
(10, 'Eventos Luna Llena', 'info@eventoslunallena.es', '$2a$12$Yr8obyuaQZEpXBuhDITvuugOxtAdCqxWJZyGuB5arl5LczkX9gbRe', 'PROMOTOR', 1, 0, '2025-04-26 14:14:08', '2025-05-07 21:20:39'),
(11, 'Ritmo Producciones', 'gestion@ritmoproducciones.com', '$2a$12$p.1C/PPo/9SSgatchToISeRRG3xsnpzEzYmfFEfVpRxDhVnatOtMy', 'PROMOTOR', 1, 0, '2025-04-26 14:14:08', '2025-04-26 14:16:26'),
(12, 'Noches de Verano SL', 'admin@nochesverano.es', '$2a$12$p.1C/PPo/9SSgatchToISeRRG3xsnpzEzYmfFEfVpRxDhVnatOtMy', 'PROMOTOR', 1, 1, '2025-04-26 14:14:08', '2025-04-27 23:33:52'),
(13, 'Punto Venta 2', 'cajero2@beatpass.com', '$2a$12$SSmhSya66QM1nBm2BzASreN9rr104wacFb5zgiswP2A5tPLGfNn7u', 'CAJERO', 1, 1, '2025-04-28 00:44:47', '2025-04-28 00:44:47');

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
  ADD UNIQUE KEY `stripe_payment_intent_id` (`stripe_payment_intent_id`),
  ADD KEY `idx_compras_asistente` (`id_asistente`),
  ADD KEY `idx_compras_fecha` (`fecha_compra`),
  ADD KEY `idx_stripe_payment_intent` (`stripe_payment_intent_id`);

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
  MODIFY `id_asistente` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=69;

--
-- AUTO_INCREMENT for table `compras`
--
ALTER TABLE `compras`
  MODIFY `id_compra` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=49;

--
-- AUTO_INCREMENT for table `compra_entradas`
--
ALTER TABLE `compra_entradas`
  MODIFY `id_compra_entrada` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=53;

--
-- AUTO_INCREMENT for table `consumos`
--
ALTER TABLE `consumos`
  MODIFY `id_consumo` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `entradas`
--
ALTER TABLE `entradas`
  MODIFY `id_entrada` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=57;

--
-- AUTO_INCREMENT for table `entradas_asignadas`
--
ALTER TABLE `entradas_asignadas`
  MODIFY `id_entrada_asignada` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=195;

--
-- AUTO_INCREMENT for table `festivales`
--
ALTER TABLE `festivales`
  MODIFY `id_festival` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT for table `pulseras_nfc`
--
ALTER TABLE `pulseras_nfc`
  MODIFY `id_pulsera` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=105;

--
-- AUTO_INCREMENT for table `recargas`
--
ALTER TABLE `recargas`
  MODIFY `id_recarga` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

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
