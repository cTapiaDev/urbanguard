# Proyecto Evaluado "BioDex"
La ONG "Naturaleza Viva" necesita una aplicación robusta para registrar la biodiversidad. No queremos un prtotipo rápido; queremos una base arquitectónica escalable que dure años.

---

## Requerimientos Primera Parte (17/02)
1. <b>Configuración del Entorno</b>
  - <b>Version Catalog:</b> El proyecto de usar `libs.versions.toml` para gestionar todas las dependencias.
  - <b>Git Flow:</b> Debe existir una rama `main` y una rama `develop`.
  - <b>Clean Architecture:</b> Paquetes estrictamente separados:
    - `com.biodex.app.data`
    - `com.biodex.app.domain`
    - `com.biodex.app.ui`
    - `com.biodex.app.core`

2. <b>Inyección de Dependencias</b>
  - <b>Hilt Setup:</b> La clase `Application` y la `MainActivity` deben tener las anotaciones de Hilt correspondientes.
  - <b>Módulos DI:</b> Deben existir al menos 3 módules en el paquete `di`:
    - `AppModule` (Contexto).
    - `NetworkModule` (Proveer Retrofit + Moshi Singleton, con url falsa por ahora, solo la configuración).
    - `DispatchersModule` (Proveer `IoDispatcher` inyectable).

3. <b>UI & Navegación</b>
  - <b>Single Activity:</b> Solo de haber una `MainActivity`.
  - <b>Navigation Component:</b> Uso de `NavGraph` y <b>SafeArgs</b> para la navegación.
  - <b>BaseFragment:</b> Debes implementar una clase abstracta `BaseFragment<VB: ViewBinding>` que maneje el inflado y la limpieza del binding (`onDestroyView`) automáticamente. Todos los grafments deben heredar de ella.
  - <b>Bottom Navigation:</b> Menú inferior funcional que navega entre `Home`, `Map` y `Profile`
  - <b>Diseño XML:</b> La pantalla `CreateSightingFragment` debe usar `TextInputLayout`, `NestedScrollView` y `MaterialCardView` siguiendo las guías de Material Design 3.

4. <b>Patrón MVVM & StateFlow</b>
  - <b>ViewModel Moderno:</b> No usar `LiveData`. El estado de la UI debe manejarse con `StateFlow` (`_uiState` privado, `uiState` público).
  - <b>UI State:</b> Crear una data class `CreateSightingUiState` que contenga todos los campos de la pantalla (loading, errores, datos, etc.).
  - <b>Fragment Extension:</b> Utilizar la función de extensión `collectFlow` (o `launchAndRepeatWithViewLifecycle`) para recolectar el estado de forma segura en el ciclo de vida.

5. <b>Capa de Dominio</b>
  - <b>Modelos Puros:</b> Crear la data class `Sighting` en la capa `domain`. No debe tener anotaciones de Android.
  - <b>Repository Interface:</b> Definir `SightingRepository` como una interfaz en el dominio.
  - <b>Use Cases:</b> La lógica de validación (ej: "El nombre de la especie no puede estar vacío") debe estar extraída en una clase `ValidateSightingUseCase`. El ViewModel debe inyectar y llamar a este Use Case, no hacer la validación él mismo. 

---

## Requerimientos Segunda Parte (18/02)
- Crear `SightingEntity`, DAO asociado y un `SightingMapper` para poder mover la información entre Entity >> Domain y viceversa.
- Implementar `SightingAdapter` para RecyclerView.
- Crear un ViewModel especializado para el RecyclerView y crear su diseño en homeFragment.

---

## Requerimientos Tercera Parte (20/02)
- <b>Selector Híbrido:</b> El usuario debe poder elegir entre tomar una foto nueva (CameraX) o seleccionar una de la galería (PhotoPicker).
- <b>Persistencia Directa:</b> Las fotos capturadas no deben guardarse en caché temporal; deben almacenarse en una carpeta llamada `BioDex_Photos` dentro del almacenamiento de la aplicación.
- <b>Feedback Visual:</b> Implementar un botón de `Eliminar Foto` que aparezca sobre la `ImageVie` una vez que la foto haya sido cargada, permitiendo al usuario volver a activar la cámara si se equivocó.
- <b>Estado Global:</b> El `btnSubmitSighting` solo debe estar habilitado si existe un URI válido en el `uiState` del ViewModel.

---

## Requerimientos Cuarta Parte (23/02)
- Crear `SightingFileManager` para gestionar archivos en Scoped Storage.
- Implementar el `ImageProcessor` limitando las fotos a <b>1000px</b> para máxima compatibilidad con dispositivos de gama baja.
- El procesamiento DEBE realizar en `Dispatchers.IO`.
- Vincular el estado `isLoading` del ViewModel a un `CircularProgressIndicator` en el Fragment.

---

## Requerimientos Quinta Parte (24/02)
- Integrar la dependencia de `play-services-location`
- Implementar el flujo de permisos `FINE` y `COARSE` en `CreateSightingFragment`.
- Obtener la ubicación al abrir el fragmento de creación y realizar el <b>Reverse Geocoding</b>
- Añadir `latitude`, `longitude` y `address` a su `CreateSightingUiState`.
- Implementar las funciones necesarios en `CreateSightingViewModel`
- El botón de envío en BioDex debe estar deshabilitado hasta que las coordenadas sean distintas de nulo.
- Guardar las coordenadas en la base de datos `SightingEntity` (Room).
