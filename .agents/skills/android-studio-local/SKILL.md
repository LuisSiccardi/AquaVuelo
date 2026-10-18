---
name: android-studio-local
description: Trabajar con proyectos Android locales en Android Studio, diagnosticar Gradle y la instalación en un teléfono, ejecutar pruebas y preparar APK o AAB. Usar para desarrollo Android nativo; no para manejar funciones personales del teléfono.
---

# Android Studio local

Continuar el proyecto existente y conservar su lenguaje, interfaz y dependencias salvo que el trabajo requiera cambiarlos. Una skill aporta un flujo de trabajo; no garantiza que la automatización visual del IDE esté disponible.

## Comprobar el entorno

- Localizar la raíz que contiene `settings.gradle` o `settings.gradle.kts` y los módulos Android. Leer `applicationId`, variantes y configuración de pruebas del proyecto; no deducir el paquete del nombre visible de la app.
- Resolver el SDK mediante `local.properties` o la configuración existente del proyecto. Localizar Gradle, ADB y un JDK compatible con esa versión de Gradle y AGP. El JDK incluido en una actualización de Android Studio puede ser demasiado reciente para un proyecto anterior.
- Distinguir la sincronización del IDE, la compilación, la instalación y el arranque. Un `BUILD SUCCESSFUL` no demuestra que el teléfono haya instalado la app.
- Leer [references/entorno-aquavuelo.md](references/entorno-aquavuelo.md) al trabajar con AquaVuelo en este equipo. Sus rutas y versiones son observaciones locales, no requisitos para otros proyectos.

## Android Studio y diagnóstico

Para operar menús usar la habilidad de control de aplicaciones disponible y observaciones actuales de la ventana. Si la captura o los controles fallan, recuperar la ventana una vez. No seguir con clics o escritura sin conocer el estado.

Usar las interfaces de desarrollo de Gradle y ADB para compilar, ejecutar pruebas y consultar la app. Los registros del IDE y Gradle permiten diagnosticar el estado, pero no sustituyen la observación de menús para decidir dónde hacer clic.

Separar recomendaciones opcionales de errores reales. No iniciar migraciones de AGP o de Gradle Daemon Toolchain para corregir un fallo de instalación del teléfono. En Windows, escapar los separadores de unidad de los archivos `.properties` cuando corresponda.

## Teléfono y pruebas

- Consultar los dispositivos conectados y seleccionar uno de forma inequívoca. Si hay varios destinos posibles, aclarar cuál usar y pasar su identificador a los comandos siguientes.
- Comprobar el paquete instalado y el proceso de la app antes de atribuir un fallo al ordenador. Preferir el teléfono ya conectado a crear un emulador innecesario.
- Ante `INSTALL_FAILED_USER_RESTRICTED`, informar el mensaje exacto. La persona debe aceptar la instalación o habilitar su opción de instalación por USB. No desactivar Play Protect, el verificador de paquetes ni otras protecciones para forzar una instalación.
- Para actualizar una versión de prueba, comprobar la compatibilidad de firma. No desinstalar ni borrar datos para ocultar una incompatibilidad. Una prueba de instrumentación necesita una firma compatible con la app objetivo.
- Ejecutar pruebas que comprueben comportamiento observable: apertura, controles, resultados, navegación y persistencia relevante. Separar pruebas aprobadas de las que solo están escritas o compiladas.
- Capturar únicamente la app objeto de la prueba. Revisar la imagen resultante antes de afirmar que la interfaz se ve correctamente. Conservar los ajustes de la persona cuando las pruebas los modifiquen.

## Artefactos y firma

Generar el tipo de archivo adecuado: APK de prueba para el teléfono y AAB firmado para un canal de Google Play que lo requiera. Verificar el artefacto final; una tarea llamada `signReleaseBundle` puede producir un AAB sin firma si no hay configuración de firma.

Conservar la propiedad de la clave en manos del usuario. No incluir claves, contraseñas ni configuración local del SDK en el repositorio o en el ZIP de fuentes. No crear credenciales duraderas ni guardar contraseñas en texto plano sin autorización específica para esa custodia. Si la firma se hace con una contraseña introducida por el usuario en Android Studio, dejarle esa entrada.

Subir un archivo, crear una ficha y resolver una advertencia de cuenta son resultados distintos. Comprobar por separado el estado de la publicación y el aviso de Play Console antes de afirmar que la cuenta está protegida.

## Fuentes

- [Skill oficial android-cli de Google](https://github.com/android/skills/tree/main/devtools/android-cli): usarla si está instalada junto con su herramienta y seguir sus referencias antes de interactuar con dispositivos.
- [Compatibilidad entre Android Studio y AGP](https://developer.android.com/studio/releases).
- [Preparar un dispositivo Android](https://developer.android.com/studio/run/device).

Informar qué cambió, qué pruebas se ejecutaron y qué queda pendiente. No presentar una skill instalada como una solución comprobada a una limitación de la herramienta de control.
