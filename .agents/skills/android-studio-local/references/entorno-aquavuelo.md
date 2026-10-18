# Entorno observado de AquaVuelo

Comprobado el 17 de octubre de 2026. Volver a verificar las rutas y versiones antes de usarlas en otra sesión.

- Proyecto: localizar la carpeta que contiene este archivo y `settings.gradle`.
- Módulo: `app`. Paquete: `org.atriadev.aquavuelo`. Runner de pruebas: `org.atriadev.aquavuelo.SmokeInstrumentation`.
- Código Java y vistas de Android; no migrar a Kotlin o Compose para ejecutar esta app.
- Gradle 8.13 y AGP 8.13.2. La sincronización del IDE terminó correctamente con `jbr-21`; el JBR 25 de Android Studio Rabbit resultó incompatible con Gradle 8.13.
- SDK: consultar `local.properties`; `compileSdk` y `targetSdk` 36, mínimo 26. Herramientas en `platform-tools` y `build-tools`; no asumir que una única versión del SDK está instalada.
- JDK elegido por el IDE: consultar `.gradle/config.properties`; se comprobó JBR 21.0.11.
- Android Studio en Windows: localizar `studio64.exe` y consultar el registro `idea.log` desde la carpeta de registros de la versión instalada.
- El equipo tiene 16 GB de RAM y un procesador Intel i3-7100U. Priorizar un solo trabajo de compilación y el teléfono conectado; comprobar el espacio libre antes de crear emuladores.
- El Redmi conectado ejecutó la app en API 34. No guardar su número de serie en esta skill; detectarlo de nuevo.
- La instalación inicial falló con `INSTALL_FAILED_USER_RESTRICTED Installation via USB is disabled`. Después de que el usuario autorizara la instalación, el paquete apareció instalado y en ejecución.
- Las capturas de la ventana del IDE fallaron repetidamente. Consultar el estado actual antes de reintentar; no prometer que cambiar de skill resuelve esa limitación.
- Las compilaciones por terminal pueden usar una carpeta Gradle propia en `work/`. No confundir su clave de depuración con la utilizada por Android Studio al firmar pruebas para una app ya instalada.

Esta referencia no contiene claves de firma, contraseñas ni identificadores privados del dispositivo.
