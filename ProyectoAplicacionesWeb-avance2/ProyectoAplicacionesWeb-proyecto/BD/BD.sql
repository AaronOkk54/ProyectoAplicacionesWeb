CREATE DATABASE reservas_canchas;
CREATE USER 'usuario_canchas'@'localhost' IDENTIFIED BY 'Clave_Canchas1.';
GRANT ALL PRIVILEGES ON reservas_canchas.* TO 'usuario_canchas'@'localhost';
FLUSH PRIVILEGES;
 
USE reservas_canchas;
 
CREATE TABLE rol (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT
);
 
CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    fecha_creacion DATETIME NOT NULL,
    fecha_actualizacion DATETIME,
    activo BOOLEAN DEFAULT TRUE
);
 
CREATE TABLE usuario_rol (
    id_usuario INT NOT NULL,
    id_rol INT NOT NULL,
    PRIMARY KEY (id_usuario, id_rol),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol) ON DELETE CASCADE
);
 
CREATE TABLE cancha (
    id_cancha INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    ubicacion VARCHAR(200) NOT NULL,
    precio_hora DECIMAL(12, 2) NOT NULL,
    estado VARCHAR(50) DEFAULT 'DISPONIBLE',
    capacidad INT,
    caracteristicas TEXT,
    horario_apertura VARCHAR(5),
    horario_cierre VARCHAR(5),
    fecha_creacion DATETIME NOT NULL,
    fecha_actualizacion DATETIME
);
 
CREATE TABLE reserva (
    id_reserva INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_cancha INT NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado VARCHAR(50) DEFAULT 'PENDIENTE',
    monto_total DECIMAL(12, 2) NOT NULL,
    metodo_pago VARCHAR(50),
    observaciones TEXT,
    fecha_creacion DATETIME NOT NULL,
    fecha_actualizacion DATETIME,
    fecha_confirmacion DATETIME,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_cancha) REFERENCES cancha(id_cancha) ON DELETE CASCADE
);
 
CREATE TABLE historial_reserva (
    id_historial INT AUTO_INCREMENT PRIMARY KEY,
    id_reserva INT NOT NULL,
    accion VARCHAR(100) NOT NULL,
    estado_anterior VARCHAR(50) NOT NULL,
    estado_nuevo VARCHAR(50) NOT NULL,
    descripcion TEXT,
    usuario VARCHAR(100) NOT NULL,
    fecha_cambio DATETIME NOT NULL,
    cambios_detalles TEXT,
    FOREIGN KEY (id_reserva) REFERENCES reserva(id_reserva) ON DELETE CASCADE
);
 
INSERT INTO rol (nombre, descripcion) VALUES ('ADMIN', 'Administrador del sistema');
INSERT INTO rol (nombre, descripcion) VALUES ('USUARIO', 'Usuario regular del sistema');

INSERT INTO cancha (
    nombre,
    descripcion,
    ubicacion,
    precio_hora,
    estado,
    capacidad,
    caracteristicas,
    horario_apertura,
    horario_cierre,
    fecha_creacion,
    fecha_actualizacion
) VALUES (
    'Cancha 1 - Fútbol',
    'Cancha de fútbol 11 con césped sintético de alta calidad',
    'Av. Central 123, San José',
    15000.00,
    'DISPONIBLE',
    22,
    'Césped sintético, iluminación LED, vestuarios, estacionamiento',
    '07:00',
    '22:00',
    NOW(),
    NULL
);
 
INSERT INTO cancha (
    nombre,
    descripcion,
    ubicacion,
    precio_hora,
    estado,
    capacidad,
    caracteristicas,
    horario_apertura,
    horario_cierre,
    fecha_creacion,
    fecha_actualizacion
) VALUES (
    'Cancha 2 - Fútbol',
    'Cancha de fútbol 11 con césped sintético de alta calidad',
    'Av. Central 123, San José',
    20000.00,
    'DISPONIBLE',
    14,
    'Césped artificial, Luces, vestuarios, estacionamiento',
    '07:00',
    '22:00',
    NOW(),
    NULL
    
    
);