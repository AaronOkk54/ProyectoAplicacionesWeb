# Proyecto_DW

## Descripción del Proyecto

Proyecto_DW es un sistema web desarrollado con **Spring Boot** para la gestión de **reservas de canchas de fútbol**.

El objetivo del sistema es permitir que los usuarios puedan:

* Registrarse en la plataforma
* Iniciar sesión
* Ver las canchas disponibles
* Consultar los detalles de cada cancha
* Realizar reservas en fechas y horarios específicos
* Confirmar reservas
* Ver sus reservas activas
* Consultar el historial de reservas
* Administrar su perfil

Este proyecto fue diseñado a partir de un **prototipo HTML previamente desarrollado**, el cual define la estructura de las vistas del sistema.

---

# Proyecto de referencia

Este proyecto debe **basarse en la arquitectura y estilo de desarrollo del proyecto anterior llamado**:

**Portafolio-DiegoQuiros**

El proyecto Portafolio-DiegoQuiros también está desarrollado con **Spring Boot** y utiliza una arquitectura MVC similar.

Por lo tanto:

* La estructura del código
* La organización de paquetes
* El estilo de controllers, services y repositories

deben seguir el mismo patrón utilizado en **Portafolio-DiegoQuiros**.

---

# Arquitectura del sistema

El sistema utiliza una arquitectura en capas típica de aplicaciones **Spring Boot**:

* **controller** → Maneja las rutas HTTP y conecta con las vistas HTML
* **service** → Contiene la lógica de negocio del sistema
* **repository** → Maneja el acceso a la base de datos mediante JPA
* **domain** → Contiene las entidades del sistema

Estructura del backend:

src/main/java/com/proyecto/reservas

controller
AuthController
CanchaController
ReservaController
UsuarioController
HomeController

domain
Usuario
Cancha
Reserva

repository
UsuarioRepository
CanchaRepository
ReservaRepository

service
UsuarioService
CanchaService
ReservaService

ProyectoApplication

---

# Entidades principales

## Usuario

Representa a los usuarios registrados en el sistema.

Atributos sugeridos:

* id
* nombre
* email
* password

---

## Cancha

Representa una cancha de fútbol que puede ser reservada.

Atributos sugeridos:

* id
* nombre
* tipo (fútbol 5, fútbol 7, fútbol 11)
* precioPorHora
* descripcion
* disponible

---

## Reserva

Representa una reserva realizada por un usuario para una cancha específica.

Atributos sugeridos:

* id
* fecha
* horaInicio
* horaFin
* usuario
* cancha

Relaciones:

* Un **usuario puede tener muchas reservas**
* Una **reserva pertenece a un usuario**
* Una **reserva pertenece a una cancha**
* Una **cancha puede tener muchas reservas**

---

# Flujo del sistema

1. El usuario entra al sistema desde **index.html**
2. Puede registrarse o iniciar sesión
3. Después de iniciar sesión accede al **menú principal**
4. Desde el menú puede ver la **lista de canchas**
5. Puede seleccionar una cancha y ver su **detalle**
6. Puede realizar una **reserva**
7. El sistema muestra una **confirmación de reserva**
8. El usuario puede consultar **sus reservas**
9. También puede ver su **historial de reservas**

---

# Estructura de templates

El sistema utiliza vistas HTML previamente diseñadas.

templates

index.html

auth
inicioSesion.html
registro.html

canchas
listaCanchas.html
detalleCancha.html

reservas
reserva.html
confirmacionReserva.html
misReservas.html
historialReservas.html

usuario
miPerfil.html

menu
menuPrincipal.html

---

# Objetivo del uso de Copilot

El objetivo es utilizar **GitHub Copilot en VS Code** para ayudar a generar el backend del sistema.

Copilot debe:

* Basarse en la arquitectura del proyecto **Portafolio-DiegoQuiros**
* Respetar la estructura de paquetes definida
* Generar entidades JPA
* Crear repositories con Spring Data JPA
* Implementar services con lógica de negocio
* Crear controllers que conecten con los templates HTML

---

# Tecnologías utilizadas

* Java
* Spring Boot
* Spring Data JPA
* Thymeleaf
* HTML
* CSS
* GitHub Copilot

## Base de Datos

El sistema utiliza una base de datos relacional en MySQL llamada **reservas_canchas**.

La base de datos fue diseñada para gestionar usuarios, canchas de fútbol y reservas de horarios.

### Tablas principales

**usuario**

Representa los usuarios registrados en el sistema.

Campos principales:

* id_usuario
* nombre
* apellidos
* correo
* password
* telefono
* fecha_creacion

**rol**

Define los roles que pueden tener los usuarios.

Campos:

* id_rol
* nombre_rol

Ejemplos de roles:

* ADMIN
* CLIENTE

**usuario_rol**

Tabla intermedia que relaciona usuarios con roles.

Campos:

* id_usuario
* id_rol

**cancha**

Representa las canchas disponibles para reservar.

Campos:

* id_cancha
* nombre
* ubicacion
* tipo
* precio_hora
* descripcion
* estado
* fecha_creacion

**reserva**

Representa las reservas realizadas por los usuarios.

Campos:

* id_reserva
* id_usuario
* id_cancha
* fecha
* hora_inicio
* hora_fin
* estado
* fecha_creacion

Estados posibles de una reserva:

* Reservada
* Cancelada
* Finalizada

**historial_reserva**

Registra acciones realizadas sobre una reserva.

Campos:

* id_historial
* id_reserva
* accion
* fecha

### Relaciones de la base de datos

Relaciones principales del sistema:

* Un **usuario** puede tener muchas **reservas**
* Una **cancha** puede tener muchas **reservas**
* Una **reserva** pertenece a un **usuario**
* Una **reserva** pertenece a una **cancha**
* Un **usuario** puede tener **uno o varios roles**
* Una **reserva** puede tener varios registros en **historial_reserva**

### Diagrama lógico simplificado

usuario 1 ----- N reserva
cancha 1 ----- N reserva
reserva 1 ----- N historial_reserva
usuario N ----- N rol
