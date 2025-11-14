# Guía de Despliegue en Render con Docker

Este documento describe cómo desplegar el microservicio Diary Service en Render utilizando Docker.

## 📋 Prerequisitos

- Cuenta en [Render.com](https://render.com)
- Repositorio Git con el código del microservicio
- API Key de Google Gemini
- JWT Secret Key (la misma que usa el Auth Service)

## 🚀 Despliegue Automático con render.yaml

### Paso 1: Preparar el Repositorio

Asegúrate de que tu repositorio contenga los siguientes archivos:
- `Dockerfile`
- `render.yaml`
- `.dockerignore`
- `pom.xml`
- Código fuente en `/src`

### Paso 2: Conectar con Render

1. Inicia sesión en [Render Dashboard](https://dashboard.render.com)
2. Haz clic en **"New +"** y selecciona **"Blueprint"**
3. Conecta tu repositorio de GitHub/GitLab
4. Render detectará automáticamente el archivo `render.yaml`

### Paso 3: Configurar Variables de Entorno Secretas

Render creará automáticamente la base de datos y el servicio web, pero necesitas configurar manualmente las siguientes variables de entorno secretas:

#### En el Dashboard de Render:

1. Ve a tu servicio **diary-service**
2. Navega a **"Environment"** en el menú lateral
3. Agrega las siguientes variables:

```
JWT_SECRET_KEY=tu_clave_jwt_secreta_aqui
GEMINI_API_KEY=tu_api_key_de_gemini_aqui
```

**IMPORTANTE**:
- `JWT_SECRET_KEY` debe ser **exactamente la misma** que usas en el Auth Service
- `GEMINI_API_KEY` obtén tu clave en [Google AI Studio](https://makersuite.google.com/app/apikey)

### Paso 4: Configurar CORS (Opcional)

Si tu frontend está desplegado, actualiza la variable de entorno:

```
CORS_ALLOWED_ORIGINS=https://tu-frontend.onrender.com,http://localhost:5174
```

Separa múltiples orígenes con comas (sin espacios).

### Paso 5: Desplegar

1. Haz clic en **"Apply"** o **"Create"**
2. Render comenzará a:
   - Crear la base de datos PostgreSQL
   - Construir la imagen Docker
   - Desplegar el servicio

El proceso puede tardar entre 5-10 minutos.

---

## 🔧 Despliegue Manual (Sin render.yaml)

Si prefieres configurar manualmente:

### 1. Crear Base de Datos PostgreSQL

1. En Render Dashboard, clic en **"New +"** → **"PostgreSQL"**
2. Configura:
   - **Name**: `diary-db`
   - **Database**: `diary_db`
   - **User**: `diary_user`
   - **Plan**: Free

### 2. Crear Web Service

1. Clic en **"New +"** → **"Web Service"**
2. Conecta tu repositorio
3. Configura:
   - **Name**: `diary-service`
   - **Runtime**: `Docker`
   - **Plan**: `Free`
   - **Health Check Path**: `/actuator/health`

### 3. Configurar Variables de Entorno

Agrega todas las variables listadas en `.env.example`:

**Variables de Base de Datos** (desde la BD creada):
```
DB_HOST=<host de tu BD en Render>
DB_PORT=5432
DB_NAME=diary_db
DB_USERNAME=diary_user
DB_PASSWORD=<password de tu BD en Render>
```

**Variables de Aplicación**:
```
PORT=8082
JWT_SECRET_KEY=<tu_clave_jwt>
GEMINI_API_KEY=<tu_api_key_gemini>
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent
CORS_ALLOWED_ORIGINS=https://tu-frontend.onrender.com
HIBERNATE_DDL_AUTO=update
SHOW_SQL=false
LOG_LEVEL=INFO
```

### 4. Desplegar

Haz clic en **"Create Web Service"** y espera a que complete el build.

---

## ✅ Verificar el Despliegue

### 1. Health Check

Visita: `https://tu-servicio.onrender.com/actuator/health`

Deberías ver:
```json
{
  "status": "UP"
}
```

### 2. Swagger UI

Visita: `https://tu-servicio.onrender.com/swagger-ui.html`

Deberías ver la documentación interactiva de la API.

### 3. Logs

En Render Dashboard:
1. Ve a tu servicio
2. Haz clic en **"Logs"** en el menú lateral
3. Verifica que no haya errores

---

## 🔍 Solución de Problemas

### El servicio no inicia

**Síntoma**: El servicio se reinicia constantemente

**Solución**:
1. Verifica los logs en Render Dashboard
2. Asegúrate de que todas las variables de entorno estén configuradas
3. Verifica que `JWT_SECRET_KEY` y `GEMINI_API_KEY` estén presentes

### Error de conexión a la base de datos

**Síntoma**: `Cannot connect to database`

**Solución**:
1. Verifica que la base de datos esté en estado "Available"
2. Comprueba que las variables `DB_*` estén correctamente configuradas
3. Si usas `render.yaml`, verifica que el nombre de la BD coincida: `diary-db`

### Health check falla

**Síntoma**: Render marca el servicio como "Unhealthy"

**Solución**:
1. Verifica que el endpoint `/actuator/health` sea accesible
2. Espera 60 segundos después del despliegue (período de inicio)
3. Revisa los logs para errores de inicio

### CORS errors en el frontend

**Síntoma**: `CORS policy: No 'Access-Control-Allow-Origin' header`

**Solución**:
1. Actualiza `CORS_ALLOWED_ORIGINS` con la URL de tu frontend
2. No incluyas espacios entre las URLs separadas por comas
3. Incluye el protocolo completo: `https://` o `http://`

---

## 🔄 Actualizar el Servicio

Render despliega automáticamente cuando haces push a la rama principal:

1. Haz cambios en tu código
2. Commit y push a GitHub/GitLab
3. Render detectará los cambios y redesplegará automáticamente

Para forzar un redespliegue manual:
1. Ve a tu servicio en Render Dashboard
2. Haz clic en **"Manual Deploy"** → **"Deploy latest commit"**

---

## 💡 Mejores Prácticas

1. **Seguridad**:
   - Nunca commits archivos `.env` con credenciales reales
   - Usa siempre variables de entorno para datos sensibles
   - Rota las claves API periódicamente

2. **Monitoreo**:
   - Revisa los logs regularmente
   - Configura alertas en Render para caídas del servicio
   - Monitorea el uso de recursos

3. **Base de Datos**:
   - En producción, considera un plan de pago para mejor rendimiento
   - Configura backups automáticos
   - Usa `HIBERNATE_DDL_AUTO=validate` en producción (después de la primera migración)

4. **CORS**:
   - Especifica orígenes exactos en producción
   - No uses `*` en producción
   - Lista solo los dominios necesarios

---

## 📞 Soporte

- [Documentación de Render](https://render.com/docs)
- [Render Community](https://community.render.com)
- [Render Status](https://status.render.com)

---

## 📝 Notas Adicionales

### Plan Free de Render

- El servicio se "duerme" después de 15 minutos de inactividad
- La primera petición después de dormir puede tardar 30-60 segundos
- Limite de 750 horas de uso al mes
- Base de datos PostgreSQL con 1GB de almacenamiento

### Upgrade a Plan de Pago

Para producción real, considera:
- **Starter Plan ($7/mes)**: Servicio siempre activo
- **PostgreSQL Standard ($7/mes)**: 10GB, backups automáticos

---

Última actualización: 2025-11-14
