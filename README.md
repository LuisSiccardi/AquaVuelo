# AquaVuelo

Simulador educativo Android de cohetes de agua. Interfaz en español, sin conexión, anuncios, analítica ni permisos. Guarda los últimos ajustes en el dispositivo.

## Funciones

- Ajuste de capacidad, porcentaje de agua, presión manométrica, masa vacía y diámetro de boquilla.
- Estimación de altura máxima, velocidad máxima y tiempo de vuelo.
- Gráfica de altura respecto al tiempo y reproducción animada del recorrido.
- Restablecimiento de valores y explicación del modelo y privacidad dentro de la app.

## Abrir el proyecto

En Android Studio, seleccionar **Open** y elegir esta carpeta. Usar JDK 21 para ejecutar Gradle, Android SDK 36, Gradle 8.13 y Android Gradle Plugin 8.13.2. El JDK 25 incluido en algunas versiones nuevas de Android Studio no es compatible con este Gradle; elegir JDK 21 en los ajustes de Gradle.

Android Studio crea `local.properties` con la ruta al SDK de cada equipo. Ese archivo no se incluye en el repositorio.

Paquete: `org.atriadev.aquavuelo`. Android mínimo: 8.0 (API 26). Android de destino: 16 (API 36).

## Modelo y límites

Modelo vertical unidimensional. Integra el movimiento con un paso de 0,001 s. Usa expansión adiabática del aire (exponente 1,4), caudal de agua por Bernoulli (coeficiente de descarga 0,85), masa variable, gravedad y resistencia cuadrática. La presión del control es relativa a la atmosférica; el cálculo termodinámico utiliza presión absoluta.

Constantes: gravedad 9,80665 m/s², atmósfera 101.325 Pa, agua 1.000 kg/m³, aire 1,225 kg/m³, diámetro exterior 9 cm, coeficiente de resistencia 0,5, temperatura inicial 20 °C. Se incluye la masa inicial del aire comprimido como masa constante durante el vuelo.

Omite el empuje del aire residual, dinámica del tubo de lanzamiento, viento, inclinación, estabilidad y paracaídas. El descenso es balístico. Los rangos de los controles no son límites de seguridad para botellas reales. No es una herramienta de certificación de lanzamientos.

Referencia conceptual: [NASA Glenn, Flight of a Water Rocket](https://www1.grc.nasa.gov/beginners-guide-to-aeronautics/flight-of-a-water-rocket/). Implementación propia; no contiene código ni recursos de OpenRocket y no está afiliada con NASA ni OpenRocket.

## Verificación

`ModelChecks.java` ejecuta pruebas numéricas independientes: casos sin presión o sin agua, límites de los controles, orden de las fases, contacto con el suelo, sensibilidad a la presión y convergencia al reducir el paso temporal. Configuración inicial: aproximadamente 28,66 m de altura y 5,25 s de vuelo.

`SmokeInstrumentation.java` comprueba apertura, cinco controles, caso sin presión, actualización del volumen de agua, recuperación de los resultados iniciales y ausencia de permisos. `tools/check_on_device.py` instala el APK de instrumentación, abre la app mediante ADB, ejecuta las comprobaciones y guarda el informe y una captura de la app. Después restaura los ajustes guardados y retira el componente temporal de pruebas. Requiere Python 3, un teléfono conectado y desbloqueado y una instalación de depuración de AquaVuelo que ya haya guardado sus ajustes.

Compilar `assembleDebugAndroidTest` y ejecutar desde la raíz del proyecto:

```text
python tools/check_on_device.py --adb RUTA_AL_ADB --test-apk RUTA_AL_APK_DE_INSTRUMENTACION --output RUTA_DE_RESULTADOS
```

El APK de instrumentación debe estar firmado con el mismo certificado de depuración que la app instalada. No es necesario ni adecuado usar la clave de publicación para estas pruebas. El arranque mediante ADB evita el inicio desde segundo plano que el teléfono Xiaomi cancelaba durante la instrumentación.

Validación local del 17 de octubre de 2026: APK de prueba, APK de instrumentación y AAB sin firma compilados. Pruebas numéricas aprobadas. Android Lint sin errores, con una advertencia sobre las reglas de transferencia de datos de Android 12; la app declara `allowBackup=false`. Instalación y apertura confirmadas en un teléfono Xiaomi con Android 14 (API 34); la captura compartida por el usuario muestra la pantalla principal y la gráfica.

Prueba automática aprobada en el mismo teléfono: apertura, cinco controles, presión cero, actualización cruzada del volumen, resultados iniciales y ausencia de permisos. Se revisó la captura generada de la pantalla principal. Los ajustes del usuario se restauraron y el componente temporal de pruebas se retiró. Este resultado no cubre todos los dispositivos Android ni sustituye las comprobaciones de Google Play.

## Skill del proyecto

La skill [android-studio-local](.agents/skills/android-studio-local/SKILL.md) describe el flujo para compilar, diagnosticar y probar el proyecto con Android Studio, Gradle y ADB. Incluye referencias a la skill oficial de Google y a la documentación de Android. No requiere instalar otra herramienta de control del IDE.

## Google Play

El APK de depuración permite probar la app en un teléfono. No es la versión firmada para un lanzamiento en el canal de pruebas internas de Google Play.

Para ese canal, generar un **Android App Bundle firmado** desde Android Studio, conservar la clave de carga y sus credenciales y subir el AAB a Play Console. El AAB de compilación sin firma, si se incluye, requiere firmarse antes de usarlo en ese canal.

La app no recopila ni comparte datos. Si se publica, las declaraciones de privacidad y los formularios deben describir esta versión concreta. Crear o subir archivos no equivale a confirmar que el aviso de inactividad ha desaparecido: comprobarlo en Play Console.
