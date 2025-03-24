-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS beatpassTFG CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE beatpassTFG;

-- Tabla: usuarios (solo administradores y promotores)
CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol ENUM('ADMIN', 'PROMOTOR') NOT NULL,
    estado BOOLEAN DEFAULT TRUE
);

-- Tabla: asistentes (quienes compran o usan entradas)
CREATE TABLE asistentes (
    id_asistente INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    telefono VARCHAR(15) UNIQUE NOT NULL
);

-- Tabla: festivales
CREATE TABLE festivales (
    id_festival INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    ubicacion VARCHAR(255),
    id_promotor INT NOT NULL,
    FOREIGN KEY (id_promotor) REFERENCES usuarios(id_usuario)
);

-- Tabla: entradas (tipos de entrada del festival, ej: General, VIP)
CREATE TABLE entradas (
    id_entrada INT AUTO_INCREMENT PRIMARY KEY,
    id_festival INT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    precio DECIMAL(8,2) NOT NULL,
    stock INT NOT NULL,
    FOREIGN KEY (id_festival) REFERENCES festivales(id_festival)
);

-- Tabla: compras (cabecera de compra)
CREATE TABLE compras (
    id_compra INT AUTO_INCREMENT PRIMARY KEY,
    id_asistente INT NOT NULL, -- asistente que compra
    fecha_compra DATETIME DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_asistente) REFERENCES asistentes(id_asistente)
);

-- Tabla: compra_entradas (detalle de la compra: tipo y cantidad)
CREATE TABLE compra_entradas (
    id_compra_entrada INT AUTO_INCREMENT PRIMARY KEY,
    id_compra INT NOT NULL,
    id_entrada INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(8,2) NOT NULL,
    FOREIGN KEY (id_compra) REFERENCES compras(id_compra) ON DELETE CASCADE,
    FOREIGN KEY (id_entrada) REFERENCES entradas(id_entrada)
);

-- Tabla: entradas_asignadas (entradas individuales nominadas)
CREATE TABLE entradas_asignadas (
    id_entrada_asignada INT AUTO_INCREMENT PRIMARY KEY,
    id_compra_entrada INT NOT NULL,
    id_asistente INT NULL, -- a quién se asigna la entrada (puede ser null hasta ser nominada)
    codigo_qr VARCHAR(255) UNIQUE NOT NULL, -- QR único por entrada
    estado ENUM('ACTIVA', 'USADA', 'CANCELADA') DEFAULT 'ACTIVA',
    FOREIGN KEY (id_compra_entrada) REFERENCES compra_entradas(id_compra_entrada) ON DELETE CASCADE,
    FOREIGN KEY (id_asistente) REFERENCES asistentes(id_asistente)
);

-- Tabla: pulseras_nfc
CREATE TABLE pulseras_nfc (
    id_pulsera INT AUTO_INCREMENT PRIMARY KEY,
    codigo_uid VARCHAR(100) UNIQUE NOT NULL,
    id_entrada_asignada INT NOT NULL,
    saldo DECIMAL(10,2) DEFAULT 0.00,
    activa BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (id_entrada_asignada) REFERENCES entradas_asignadas(id_entrada_asignada)
);

-- Tabla: recargas
CREATE TABLE recargas (
    id_recarga INT AUTO_INCREMENT PRIMARY KEY,
    id_pulsera INT NOT NULL,
    monto DECIMAL(8,2) NOT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    metodo_pago VARCHAR(50),
    FOREIGN KEY (id_pulsera) REFERENCES pulseras_nfc(id_pulsera)
);

-- Tabla: consumos
CREATE TABLE consumos (
    id_consumo INT AUTO_INCREMENT PRIMARY KEY,
    id_pulsera INT NOT NULL,
    descripcion VARCHAR(255),
    monto DECIMAL(8,2) NOT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_festival INT NOT NULL,
    FOREIGN KEY (id_pulsera) REFERENCES pulseras_nfc(id_pulsera),
    FOREIGN KEY (id_festival) REFERENCES festivales(id_festival)
);

-- Tabla: estadísticas del festival (opcional para análisis de datos)
CREATE TABLE estadisticas_festival (
    id_festival INT PRIMARY KEY,
    entradas_vendidas INT DEFAULT 0,
    ingresos_totales DECIMAL(10,2) DEFAULT 0.00,
    recargas_totales DECIMAL(10,2) DEFAULT 0.00,
    consumos_totales DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (id_festival) REFERENCES festivales(id_festival)
);
