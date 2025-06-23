-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- Host: mysql-beatpasstfg.alwaysdata.net
-- Generation Time: Jun 17, 2025 at 10:15 PM
-- Server version: 10.11.13-MariaDB
-- PHP Version: 7.4.33

drop database beatpasstfg_db;
create database if not exists beatpasstfg_db;
use beatpasstfg_db;

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
(71, 'Edu', 'edolaldecruz@gmail.com', '', '2025-06-16 17:06:01', '2025-06-16 17:06:01'),
(72, 'Fran', 'fran@eamil.com', '654321456', '2025-06-16 18:59:24', '2025-06-16 18:59:24');

-- --------------------------------------------------------

--
-- Table structure for table `compradores`
--

CREATE TABLE `compradores` (
  `id_comprador` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `fecha_creacion` datetime DEFAULT current_timestamp(),
  `fecha_modificacion` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `compradores`
--

INSERT INTO `compradores` (`id_comprador`, `nombre`, `email`, `telefono`, `fecha_creacion`, `fecha_modificacion`) VALUES
(1, 'Edu', 'gaudy.g@gmail.com', NULL, '2025-06-16 17:05:25', '2025-06-16 17:05:25');

-- --------------------------------------------------------

--
-- Table structure for table `compras`
--

CREATE TABLE `compras` (
  `id_compra` int(11) NOT NULL,
  `id_comprador` int(11) NOT NULL,
  `fecha_compra` datetime DEFAULT current_timestamp(),
  `total` decimal(10,2) NOT NULL CHECK (`total` >= 0),
  `stripe_payment_intent_id` varchar(255) DEFAULT NULL,
  `estado_pago` varchar(50) DEFAULT NULL,
  `fecha_pago_confirmado` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `compras`
--

INSERT INTO `compras` (`id_compra`, `id_comprador`, `fecha_compra`, `total`, `stripe_payment_intent_id`, `estado_pago`, `fecha_pago_confirmado`) VALUES
(51, 1, '2025-06-16 17:05:26', 50.00, 'pi_3Raeb94Et9Src69R0G3VkYlJ', 'PAGADO', '2025-06-16 17:05:23'),
(52, 1, '2025-06-16 18:58:48', 160.00, 'pi_3RagMs4Et9Src69R0l9CEmKI', 'PAGADO', '2025-06-16 18:58:46');

-- --------------------------------------------------------

--
-- Table structure for table `compra_entradas`
--

CREATE TABLE `compra_entradas` (
  `id_compra_entrada` int(11) NOT NULL,
  `id_compra` int(11) NOT NULL,
  `id_tipo_entrada` int(11) NOT NULL,
  `cantidad` int(11) NOT NULL CHECK (`cantidad` > 0),
  `precio_unitario` decimal(8,2) NOT NULL CHECK (`precio_unitario` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `compra_entradas`
--

INSERT INTO `compra_entradas` (`id_compra_entrada`, `id_compra`, `id_tipo_entrada`, `cantidad`, `precio_unitario`) VALUES
(55, 51, 57, 1, 50.00),
(56, 52, 58, 2, 80.00);

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
-- Dumping data for table `entradas`
--

INSERT INTO `entradas` (`id_entrada`, `id_compra_entrada`, `id_asistente`, `codigo_qr`, `estado`, `fecha_asignacion`, `fecha_uso`, `fecha_creacion`, `fecha_modificacion`) VALUES
(197, 55, 71, 'BEATPASS-TICKET-e38422c1-a929-497f-a3be-33e96243f605', 'ACTIVA', '2025-06-16 17:06:00', NULL, '2025-06-16 17:05:26', '2025-06-16 17:06:02'),
(198, 56, NULL, 'BEATPASS-TICKET-5b9f2e71-ddcc-4427-9448-0029c328a3e4', 'ACTIVA', NULL, NULL, '2025-06-16 18:58:48', '2025-06-16 18:58:48'),
(199, 56, 72, 'BEATPASS-TICKET-6dcebf77-6d2e-41f1-9adf-43e3b1a180dc', 'ACTIVA', '2025-06-16 18:59:24', NULL, '2025-06-16 18:58:48', '2025-06-16 18:59:24');

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
(22, 'Ritmos del Sur', 'Festival de música urbana y latina. Edición del 2024.', '2024-09-06', '2024-09-08', 'Estadio Olímpico, Sevilla', 11, 20000, 'https://placehold.co/600x400/EB5757/ffffff?text=Ritmos+del+Sur', 'FINALIZADO', '2025-05-12 12:53:20', '2025-06-11 17:59:33');

-- --------------------------------------------------------

--
-- Table structure for table `pulseras_nfc`
--

CREATE TABLE `pulseras_nfc` (
  `id_pulsera` int(11) NOT NULL,
  `codigo_uid` varchar(100) NOT NULL,
  `id_entrada` int(11) DEFAULT NULL,
  `saldo` decimal(10,2) DEFAULT 0.00 CHECK (`saldo` >= 0),
  `activa` tinyint(1) DEFAULT 1,
  `id_festival` int(11) NOT NULL,
  `fecha_asociacion` datetime DEFAULT NULL,
  `fecha_alta` datetime DEFAULT current_timestamp(),
  `ultima_modificacion` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
-- Table structure for table `tipos_entrada`
--

CREATE TABLE `tipos_entrada` (
  `id_tipo_entrada` int(11) NOT NULL,
  `id_festival` int(11) NOT NULL,
  `tipo` varchar(50) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `precio` decimal(8,2) NOT NULL CHECK (`precio` >= 0),
  `stock` int(11) NOT NULL CHECK (`stock` >= 0),
  `fecha_creacion` datetime DEFAULT current_timestamp(),
  `fecha_modificacion` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `requiere_nominacion` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `tipos_entrada`
--

INSERT INTO `tipos_entrada` (`id_tipo_entrada`, `id_festival`, `tipo`, `descripcion`, `precio`, `stock`, `fecha_creacion`, `fecha_modificacion`, `requiere_nominacion`) VALUES
(57, 19, 'General', 'Entrada general', 50.00, 9999, '2025-06-16 13:55:31', '2025-06-16 17:05:26', 1),
(58, 19, 'VIP', 'VIP', 80.00, 498, '2025-06-16 17:00:00', '2025-06-16 18:58:48', 1);

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
-- Indexes for table `compradores`
--
ALTER TABLE `compradores`
  ADD PRIMARY KEY (`id_comprador`),
  ADD UNIQUE KEY `uq_comprador_email` (`email`);

--
-- Indexes for table `compras`
--
ALTER TABLE `compras`
  ADD PRIMARY KEY (`id_compra`),
  ADD UNIQUE KEY `stripe_payment_intent_id` (`stripe_payment_intent_id`),
  ADD KEY `idx_compras_asistente` (`id_comprador`),
  ADD KEY `idx_compras_fecha` (`fecha_compra`),
  ADD KEY `idx_stripe_payment_intent` (`stripe_payment_intent_id`);

--
-- Indexes for table `compra_entradas`
--
ALTER TABLE `compra_entradas`
  ADD PRIMARY KEY (`id_compra_entrada`),
  ADD KEY `idx_compraentradas_compra` (`id_compra`),
  ADD KEY `idx_compraentradas_entrada` (`id_tipo_entrada`);

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
  ADD UNIQUE KEY `id_entrada_asignada` (`id_entrada`),
  ADD UNIQUE KEY `uq_pulseranfc_entradaasignada` (`id_entrada`),
  ADD KEY `idx_pulserasnfc_entradaasignada` (`id_entrada`),
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
-- Indexes for table `tipos_entrada`
--
ALTER TABLE `tipos_entrada`
  ADD PRIMARY KEY (`id_tipo_entrada`),
  ADD KEY `idx_entradas_festival` (`id_festival`);

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
  MODIFY `id_asistente` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=73;

--
-- AUTO_INCREMENT for table `compradores`
--
ALTER TABLE `compradores`
  MODIFY `id_comprador` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `compras`
--
ALTER TABLE `compras`
  MODIFY `id_compra` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=53;

--
-- AUTO_INCREMENT for table `compra_entradas`
--
ALTER TABLE `compra_entradas`
  MODIFY `id_compra_entrada` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=57;

--
-- AUTO_INCREMENT for table `consumos`
--
ALTER TABLE `consumos`
  MODIFY `id_consumo` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `entradas`
--
ALTER TABLE `entradas`
  MODIFY `id_entrada` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=200;

--
-- AUTO_INCREMENT for table `festivales`
--
ALTER TABLE `festivales`
  MODIFY `id_festival` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT for table `pulseras_nfc`
--
ALTER TABLE `pulseras_nfc`
  MODIFY `id_pulsera` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=106;

--
-- AUTO_INCREMENT for table `recargas`
--
ALTER TABLE `recargas`
  MODIFY `id_recarga` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `tipos_entrada`
--
ALTER TABLE `tipos_entrada`
  MODIFY `id_tipo_entrada` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=59;

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
  ADD CONSTRAINT `fk_compras_comprador` FOREIGN KEY (`id_comprador`) REFERENCES `compradores` (`id_comprador`) ON UPDATE CASCADE;

--
-- Constraints for table `compra_entradas`
--
ALTER TABLE `compra_entradas`
  ADD CONSTRAINT `compra_entradas_ibfk_1` FOREIGN KEY (`id_compra`) REFERENCES `compras` (`id_compra`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `compra_entradas_ibfk_2` FOREIGN KEY (`id_tipo_entrada`) REFERENCES `tipos_entrada` (`id_tipo_entrada`) ON UPDATE CASCADE;

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
  ADD CONSTRAINT `entradas_ibfk_1` FOREIGN KEY (`id_compra_entrada`) REFERENCES `compra_entradas` (`id_compra_entrada`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `entradas_ibfk_2` FOREIGN KEY (`id_asistente`) REFERENCES `asistentes` (`id_asistente`) ON DELETE SET NULL ON UPDATE CASCADE;

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
  ADD CONSTRAINT `pulseras_nfc_ibfk_1` FOREIGN KEY (`id_entrada`) REFERENCES `entradas` (`id_entrada`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `recargas`
--
ALTER TABLE `recargas`
  ADD CONSTRAINT `recargas_ibfk_1` FOREIGN KEY (`id_pulsera`) REFERENCES `pulseras_nfc` (`id_pulsera`) ON UPDATE CASCADE,
  ADD CONSTRAINT `recargas_ibfk_2` FOREIGN KEY (`id_usuario_cajero`) REFERENCES `usuarios` (`id_usuario`);

--
-- Constraints for table `tipos_entrada`
--
ALTER TABLE `tipos_entrada`
  ADD CONSTRAINT `tipos_entrada_ibfk_1` FOREIGN KEY (`id_festival`) REFERENCES `festivales` (`id_festival`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
