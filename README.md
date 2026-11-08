# TP Grupal OO2 - Hito 2 - Grupo 03

## Sistema de Gestión de Festivales Gastronómicos

Aplicación web para la gestión de festivales gastronómicos, sus unidades de venta y el personal asociado. El sistema permite administrar la información de los festivales y de las unidades participantes, contemplando distintos tipos de unidades de venta, pedidos, caja, reportes y roles de usuario.

## Integrantes

| Integrante | Usuario de GitHub |
| --- | --- |
| Roberto Andrés Ruiz Pereira | [@rpruiz33](https://github.com/rpruiz33) |
| Federico Matías Salvatierra | [@Fsalvatierra](https://github.com/Fsalvatierra) |
| Leonel Alejandro Soloaga | [@Le0oneL](https://github.com/Le0oneL) |
| Ana Belén Vázquez | [@anavazquez141](https://github.com/anavazquez141) |

## Tecnologías utilizadas

- Java 17 como entorno de desarrollo y ejecución (configurado en Maven con `java.version` 17).
- Spring Boot.
- Spring MVC y Thymeleaf para la interfaz web.
- Spring Security para la autenticación y autorización por roles.
- Spring Data JPA e Hibernate para la persistencia.
- MySQL como base de datos.
- Maven para la gestión del proyecto y sus dependencias.
- Visual Studio Code como entorno de desarrollo.

## Diagrama de clases

El modelo de dominio y diagrama de clases de la aplicación se encuentra versionado en el repositorio en formato vectorial:

- 📄 [Diagrama de Clases UML (SVG)](./UML-final(15)(1)(3).svg)

![Diagrama de Clases UML](./UML-final(15)(1)(3).svg)

## Pull requests

Las funcionalidades y cambios del proyecto fueron integrados mediante 61 pull requests mergeadas en la rama `dev`:

| Pull request | Funcionalidad | Realizado por |
| --- | --- | --- |
| [#91](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/91) | fix: sincronizar fechas al actualizar festival,correccion ABM de fest | Leonel Soloaga |
| [#88](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/88) | fix liquidaciones mensuales y recibos individuales | Leonel Soloaga |
| [#87](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/87) | validaciones y seguridad de empleados y festivales | Leonel Soloaga |
| [#84](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/84) | Documentación de validaciones adicionales de carga | Ana Belén Vázquez |
| [#83](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/83) | Baja lógica de unidades de venta | Ana Belén Vázquez |
| [#82](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/82) | ABM de festivales y baja lógica | Roberto Andrés Ruiz Pereira |
| [#80](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/80) | Actualización funcional | Roberto Andrés Ruiz Pereira |
| [#79](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/79) | Reversión de cambios de festivales | Roberto Andrés Ruiz Pereira |
| [#78](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/78) | Reversión de cambios de festivales | Roberto Andrés Ruiz Pereira |
| [#77](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/77) | Reversión de cambios de festivales | Roberto Andrés Ruiz Pereira |
| [#76](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/76) | Cambios de ABM de festivales | Roberto Andrés Ruiz Pereira |
| [#75](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/75) | Historial de asignaciones | Federico Matías Salvatierra |
| [#74](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/74) | Separación de lógica de negocio de los controladores | Roberto Andrés Ruiz Pereira |
| [#73](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/73) | Validación de cánones | Federico Matías Salvatierra |
| [#72](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/72) | ABM de categorías de cocina | Roberto Andrés Ruiz Pereira |
| [#71](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/71) | Validación de pedidos | Leonel Alejandro Soloaga |
| [#69](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/69) | Modificación de unidad de venta | Ana Belén Vázquez |
| [#68](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/68) | Modificación de unidad de venta | Ana Belén Vázquez |
| [#67](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/67) | Historial de pedidos con filtros | Leonel Alejandro Soloaga |
| [#66](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/66) | Validaciones frontend y backend | Roberto Andrés Ruiz Pereira |
| [#65](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/65) | ABM de festivales | Roberto Andrés Ruiz Pereira |
| [#64](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/64) | Validaciones de liquidación | Roberto Andrés Ruiz Pereira |
| [#63](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/63) | Dashboard del empleado | Leonel Alejandro Soloaga |
| [#60](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/60) | Reporte de cánones | Ana Belén Vázquez |
| [#59](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/59) | Corrección de redirección del responsable | Ana Belén Vázquez |
| [#58](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/58) | Cálculo de canon en el perfil del responsable | Federico Matías Salvatierra |
| [#57](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/57) | Plato estrella | Leonel Alejandro Soloaga |
| [#56](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/56) | Filtro de empleados | Leonel Alejandro Soloaga |
| [#55](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/55) | Modificación e inhabilitación de platos | Leonel Alejandro Soloaga |
| [#54](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/54) | Baja lógica de empleados | Roberto Andrés Ruiz Pereira |
| [#52](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/52) | Gestión de platos | Leonel Alejandro Soloaga |
| [#47](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/47) | Gestión de festivales y unidades de venta | Federico Matías Salvatierra |
| [#46](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/46) | Gestión del responsable | Federico Matías Salvatierra |
| [#45](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/45) | Cierre de caja | Roberto Andrés Ruiz Pereira |
| [#44](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/44) | Gestión de platos | Leonel Alejandro Soloaga |
| [#43](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/43) | Reporte de recaudación | Roberto Andrés Ruiz Pereira |
| [#42](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/42) | Rentabilidad neta | Roberto Andrés Ruiz Pereira |
| [#41](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/41) | Funcionalidad de cajero | Leonel Alejandro Soloaga |
| [#39](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/39) | Ajustes de liquidación | Roberto Andrés Ruiz Pereira |
| [#38](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/38) | Cambios generales | Roberto Andrés Ruiz Pereira |
| [#37](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/37) | Ajustes de información | Roberto Andrés Ruiz Pereira |
| [#35](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/35) | Alta de pedidos | Leonel Alejandro Soloaga |
| [#34](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/34) | Reporte de recaudación y unificación de estilos | Federico Matías Salvatierra |
| [#33](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/33) | Registro de unidad de venta y responsable | Ana Belén Vázquez |
| [#32](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/32) | Liquidaciones | Roberto Andrés Ruiz Pereira |
| [#31](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/31) | Contraseña y registro | Roberto Andrés Ruiz Pereira |
| [#30](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/30) | Servicios, vistas y entidades de liquidación | Roberto Andrés Ruiz Pereira |
| [#29](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/29) | Liquidaciones | Roberto Andrés Ruiz Pereira |
| [#27](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/27) | Alta de pedidos | Leonel Alejandro Soloaga |
| [#26](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/26) | Creación de alta de pedidos | Leonel Alejandro Soloaga |
| [#25](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/25) | Alta de usuarios desde administración | Federico Matías Salvatierra |
| [#23](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/23) | Primera vista del índice de administración | Federico Matías Salvatierra |
| [#22](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/22) | Correcciones de entidades | Roberto Andrés Ruiz Pereira |
| [#21](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/21) | Entidades, relaciones y UML | Roberto Andrés Ruiz Pereira |
| [#17](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/17) | Corrección del README | Roberto Andrés Ruiz Pereira |
| [#16](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/16) | Actualización de desarrollo | Roberto Andrés Ruiz Pereira |
| [#14](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/14) | Actualización de desarrollo | Roberto Andrés Ruiz Pereira |
| [#10](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/10) | Prueba de funcionalidad | Roberto Andrés Ruiz Pereira |
| [#9](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/9) | Notificaciones de correo para eventos | Roberto Andrés Ruiz Pereira |
| [#5](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/5) | Diseño UML | Leonel Alejandro Soloaga |
| [#2](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/2) | Prueba inicial | Federico Matías Salvatierra |

Ver todas las pull requests en el [repositorio de GitHub](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pulls).

## Requisitos previos

Antes de ejecutar la aplicación, se debe contar con:

- JDK 17.
- MySQL instalado y en funcionamiento.
- Maven, o utilizar el Maven Wrapper incluido (`mvnw`).
- Acceso al repositorio del proyecto.

## Configuración

La aplicación obtiene la configuración sensible desde variables de entorno o desde un archivo `.env` ubicado en la carpeta `codificacion-grup03/`. No se deben subir credenciales reales al repositorio.

Crear el archivo `codificacion-grup03/.env` con una configuración similar a la siguiente:

```properties
DB_URL=jdbc:mysql://localhost:3306/festivales
DB_USERNAME=root
DB_PASSWORD=tu_clave
MAIL_USERNAME=tu_correo@gmail.com
MAIL_PASSWORD=tu_clave_de_aplicacion
```

La base de datos `festivales` debe existir previamente en MySQL. Al iniciar, Hibernate actualiza el esquema y la aplicación carga los datos de `src/main/resources/CargaDatos.sql`.

## Ejecución

Desde la carpeta del proyecto Java:

```bash
cd codificacion-grup03
./mvnw spring-boot:run
```

En Windows se puede utilizar:

```bat
mvnw.cmd spring-boot:run
```

También es posible construir el proyecto y ejecutar el archivo generado:

```bash
./mvnw clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

Una vez iniciada la aplicación, ingresar desde el navegador a:

```text
http://localhost:8080
```

## Casos de uso implementados

- Inicio de sesión, registro y gestión de contraseñas.
- Administración de usuarios y roles.
- Administración de festivales.
- Alta, modificación y consulta de unidades de venta.
- Gestión de categorías de cocina y platos.
- Asignación y consulta del personal de las unidades de venta.
- Gestión de pedidos y consulta del historial.
- Operaciones de cajero y cierre de caja.
- Liquidación de haberes.
- Consulta de reportes de ventas, recaudación, mayores cánones y rentabilidad neta.
- Envío de correos electrónicos para las funcionalidades que lo requieren.

## Roles principales

- **Administrador:** gestiona usuarios, festivales, unidades de venta y categorías.
- **Responsable:** administra la información de su unidad, empleados y platos.
- **Empleado:** consulta su información y participa en la operatoria asignada.
- **Cajero:** registra operaciones de caja y realiza cierres.
- **Usuario:** accede a las funcionalidades generales habilitadas para su cuenta.

## Casos de uso y responsables

La asignación se detalla a continuación especificando por cada integrante sus **casos de uso (CU)** asignados con enlace a sus correspondientes Pull Requests, y las **2 validaciones de carga de datos** requeridas:

| Caso de uso | Responsable | Pull Request(s) asociadas |
| --- | --- | --- |
| Autenticación, registro y contraseña | Roberto Andrés Ruiz Pereira | [#31](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/31) |
| Gestión de festivales (Alta y ABM) | Roberto Andrés Ruiz Pereira | [#65](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/65), [#82](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/82) |
| Baja lógica de unidades de venta desde el festival | Roberto Andrés Ruiz Pereira | [#82](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/82) |
| Gestión de categorías de cocina | Roberto Andrés Ruiz Pereira | [#72](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/72) |
| Liquidación de haberes y recibos | Roberto Andrés Ruiz Pereira | [#29](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/29), [#30](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/30), [#32](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/32) |
| Rentabilidad neta | Roberto Andrés Ruiz Pereira | [#42](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/42) |
| Cierre y reapertura de caja | Roberto Andrés Ruiz Pereira | [#45](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/45) |
| Reporte de recaudación | Roberto Andrés Ruiz Pereira | [#43](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/43) |
| Gestión y alta de usuarios/roles | Federico Matías Salvatierra | [#25](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/25) |
| Gestión de festivales y unidades | Federico Matías Salvatierra | [#47](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/47) |
| Gestión del responsable | Federico Matías Salvatierra | [#46](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/46) |
| Cálculo de canon en perfil responsable | Federico Matías Salvatierra | [#58](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/58) |
| Validación y cálculo de cánones | Federico Matías Salvatierra | [#73](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/73) |
| Historial de asignaciones de empleados | Federico Matías Salvatierra | [#75](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/75) |
| Registro de unidad de venta y responsable | Ana Belén Vázquez | [#33](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/33) |
| Modificación de unidad de venta | Ana Belén Vázquez | [#68](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/68), [#69](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/69) |
| Baja lógica de unidades de venta | Ana Belén Vázquez | [#83](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/83) |
| Reporte de mayores cánones | Ana Belén Vázquez | [#60](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/60) |
| Alta y registro de pedidos | Leonel Alejandro Soloaga | [#27](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/27), [#35](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/35) |
| Funcionalidad y perfil de cajero | Leonel Alejandro Soloaga | [#41](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/41) |
| Gestión y ABM de platos | Leonel Alejandro Soloaga | [#44](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/44), [#52](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/52), [#55](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/55) |
| Filtro y búsqueda de empleados | Leonel Alejandro Soloaga | [#56](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/56) |
| Plato estrella | Leonel Alejandro Soloaga | [#57](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/57) |
| Historial de pedidos con filtros | Leonel Alejandro Soloaga | [#67](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/67) |
| Validaciones de pedidos y cantidades | Leonel Alejandro Soloaga | [#71](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/71) |
| Validaciones y seguridad de empleados | Leonel Alejandro Soloaga | [#87](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/87) |
| Liquidaciones mensuales y recibos | Leonel Alejandro Soloaga | [#88](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/88) |

### Distribución de CU y Validaciones de Carga por Integrante

Conforme a la consigna de evaluación que exige **2 Casos de Uso y 2 Validaciones de Carga por persona**, se detalla la contribución individual:

#### 1. Roberto Andrés Ruiz Pereira ([@rpruiz33](https://github.com/rpruiz33))
- **Casos de Uso Principales:**
  1. **ABM y Baja Lógica de Festivales** ([PR #82](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/82)): Alta, edición con inmutabilidad de nombre, baja lógica sin romper histórico de pedidos.
  2. **Liquidación de Haberes y Recibos PDF** ([PR #30](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/30), [PR #32](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/32)): Cálculo con proporcionalidad por días, antigüedad y categoría.
  3. **Baja Lógica de Unidades de Venta desde el Festival** ([PR #82](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/82)): Inhabilitación de una unidad de venta asociada a un festival sin eliminarla físicamente, conservando el historial de pedidos y la posibilidad de reactivarla. El administrador puede ver el estado (Activa / Inactiva) de cada unidad en la vista del festival y dar de baja o reactivar individualmente.
- **Validaciones de Carga de Datos:**
  1. **Validación de fechas del festival:** fecha de inicio y fin obligatorias, inicio anterior a fin con al menos 1 día de diferencia en backend y frontend ([PR #66](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/66)).
  2. **Validación de formato de patente en Food Trucks:** formato argentino alfanumérico estricto (`AAA123` o `AA123BB`) tanto en cliente como servidor ([PR #66](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/66)).

#### 2. Ana Belén Vázquez ([@anavazquez141](https://github.com/anavazquez141))
- **Casos de Uso Principales:**
  1. **Registro y Modificación de Unidades de Venta** ([PR #33](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/33), [PR #69](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/69)): Alta pública de unidad con cálculo de canon y edición administrativa.
  2. **Baja Lógica de Unidades de Venta** ([PR #83](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/83)): Inhabilitación segura de unidad sin afectar festivales anteriores ni registros históricos.
- **Validaciones de Carga de Datos:**
  1. **Validación de formato y unicidad de DNI:** DNI numérico de 7 u 8 dígitos sin puntos ni letras y verificación de no duplicidad ([PR #84](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/84)).
  2. **Validación sintáctica y unicidad de Email:** formato RFC válido con regex y comprobación en base de datos de no existencia previa ([PR #84](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/84)).

#### 3. Leonel Alejandro Soloaga ([@Le0oneL](https://github.com/Le0oneL))
- **Casos de Uso Principales:**
  1. **Gestión de Platos y Plato Estrella** ([PR #52](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/52), [PR #57](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/57)): Alta, edición, inhabilitación y determinación del plato más vendido por unidad y festival.
  2. **Historial de Pedidos y Filtros de Búsqueda** ([PR #67](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/67), [PR #56](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/56)): Filtro multicriterio por estado, unidad, empleado y fechas.
- **Validaciones de Carga de Datos:**
  1. **Validación de carga de pedidos e ítems:** control de stock, platos pertenecientes a la unidad del cajero y cantidad de ítems mayor a cero ([PR #71](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/71)).
  2. **Validación de alta/edición de empleados:** validación de campos obligatorios, edad mínima legal, roles válidos y turnos de caja ([PR #87](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/87)).

#### 4. Federico Matías Salvatierra ([@Fsalvatierra](https://github.com/Fsalvatierra))
- **Casos de Uso Principales:**
  1. **Historial de Asignaciones de Personal** ([PR #75](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/75)): Trazabilidad de asignaciones laborales de cocineros, cajeros y empleados por festival.
  2. **Cálculo y Visualización de Canon en Perfil Responsable** ([PR #58](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/58)): Visualización y desglose del canon comercial aplicable a la unidad.
- **Validaciones de Carga de Datos:**
  1. **Validación de canon positivo:** verificación estricta de que el canon calculado para cualquier unidad sea estrictamente mayor a cero ([PR #73](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/73)).
  2. **Validación de superficie y parámetros de unidad:** superficie mínima en m² y parámetros positivos de montaje y puestos ([PR #47](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/47), [PR #73](https://github.com/rpruiz33/TPGrupalOO2Hito2Grupo3/pull/73)).

## Estructura principal

- `src/main/java`: controladores, entidades, servicios, repositorios, DTOs y configuración.
- `src/main/resources/templates`: vistas HTML renderizadas con Thymeleaf.
- `src/main/resources/static`: hojas de estilo, JavaScript, imágenes y librerías frontend.
- `src/main/resources/CargaDatos.sql`: datos iniciales de la aplicación.
- `src/main/resources/application.yml`: configuración de base de datos, correo, JPA y Thymeleaf.
