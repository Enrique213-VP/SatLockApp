# 🧩 UserSyncApp  
**Desarrollado en Kotlin**  
Implementando **MVVM**, **Retrofit**, **OkHttp**, **Room**, y el **Ciclo de Vida de Android**.

---

## 🎯 Objetivo

Desarrollar una aplicación Android que muestre una lista de usuarios obtenidos desde una API pública y que funcione correctamente tanto **online** como **offline**, permitiendo **persistir datos localmente** y **sincronizarlos automáticamente** cuando haya conexión disponible.

---

## 📱 Descripción general

**UserSyncApp** consume la API pública:  
👉 [https://jsonplaceholder.typicode.com/users](https://jsonplaceholder.typicode.com/users)

y permite:

- 👤 Ver la lista de usuarios (nombre, correo y ciudad).  
- ⭐ Marcar usuarios como favoritos.  
- 💾 Guardar los favoritos y datos en una base de datos local (Room).  
- 📴 Seguir funcionando sin conexión a internet (modo offline).  
- 🔄 Sincronizar automáticamente los cambios locales cuando vuelve la conexión.

---

## ⚙️ Tecnologías y librerías utilizadas

| Componente | Descripción |
|-------------|--------------|
| **Kotlin** | Lenguaje principal, moderno y expresivo. |
| **MVVM (Model-View-ViewModel)** | Patrón arquitectónico para separar lógica, vista y datos. |
| **Retrofit** | Cliente HTTP para consumir la API REST. |
| **OkHttp + LoggingInterceptor** | Manejo de peticiones HTTP y registro del tráfico de red. |
| **Room (Jetpack)** | Base de datos local para persistencia offline. |
| **LiveData y ViewModel (Jetpack Lifecycle)** | Gestión del ciclo de vida y observación reactiva de datos. |
| **Coroutines (suspend / withContext)** | Manejo eficiente de operaciones asíncronas. |

---

## 🧠 Arquitectura del proyecto

El proyecto sigue una estructura **limpia y modular**, basada en el patrón **MVVM**:


---

## 🔄 Flujo de funcionamiento

### 🟢 Inicio de la aplicación
- Se verifica si hay conexión.
- Si la hay, se consume la API y se guardan los usuarios en la base de datos local.
- Si no la hay, se cargan los datos guardados en Room.

### ⭐ Favoritos
- El usuario puede marcar o desmarcar favoritos incluso sin conexión.
- Los cambios se almacenan localmente con una bandera `pendingSync = true`.

### 🔁 Sincronización
- Cuando la conexión regresa, el repositorio ejecuta una sincronización simulada (`delay(2000)`).
- Se limpian las banderas `pendingSync`.
- Se actualiza la interfaz del usuario con los cambios sincronizados.

---

## 🌐 API utilizada

**Endpoint base:**  
`https://jsonplaceholder.typicode.com/`

**Recurso:**  
`GET /users`

---

## 💾 Persistencia local

La aplicación usa **Room** con la entidad `User`, la cual incluye:

- Campos básicos (`id`, `name`, `email`, `phone`, `website`, etc.)  
- Datos anidados (`Address`, `Company`, `Geo`)  
- Campos de control:  
  - `isFavorite: Boolean`  
  - `pendingSync: Boolean`  

### 📸 Validación de la base de datos en ejecución

![Validación de la BD](https://raw.githubusercontent.com/Enrique213-VP/SatLockApp/refs/heads/main/app/src/main/res/drawable/bd.png)

---

## 🧩 Características técnicas destacadas

✅ **Offline-First:** la app sigue funcionando sin conexión.  
✅ **Persistencia local:** almacenamiento completo con Room.  
✅ **Sincronización simulada:** reintento automático cuando vuelve la conexión.  
✅ **Ciclo de vida Jetpack:** uso correcto de ViewModel y LiveData.  
✅ **Buenas prácticas:** arquitectura limpia, modular y mantenible.  
✅ **Retrofit + OkHttp:** llamadas HTTP eficientes y monitoreadas.  
✅ **Coroutines:** operaciones asíncronas sin bloquear la UI.  

---

## 🚀 Ejecución del proyecto

1. **Clonar el repositorio** o importar el ZIP en **Android Studio**.  
2. Verificar que el archivo `Constants.kt` tenga la URL base correcta:

   ```kotlin
   const val BASE_URL_API = "https://jsonplaceholder.typicode.com/"

