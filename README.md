# IoT EduCloud – Monitoreo Ambiental con ESP8266 y Android 

Sistema IoT para monitoreo climatológico que integra un dispositivo ESP8266, Firebase Realtime Database y una aplicación Android desarrollada en Java con control por roles.

---

## Descripción General

El proyecto implementa una arquitectura IoT real donde un ESP8266 obtiene datos meteorológicos desde la API de **OpenWeatherMap**, los procesa y los envía a **Firebase Realtime Database**.  
La aplicación Android consume estos datos en tiempo real y permite control remoto del dispositivo.

### Flujo del sistema

OpenWeather API → ESP8266 → Firebase → Aplicación Android

---

##  Componentes del Sistema

### ESP8266 (Nodo IoT)

El sketch implementa:

- Conexión WiFi y sincronización NTP con zona horaria de El Salvador (UTC-6)  
- Consumo HTTP de la API OpenWeather  
- Procesamiento de JSON con ArduinoJson  
- Envío de datos a Firebase Realtime  
- Control remoto ON/OFF desde Firebase  
- Optimización para evitar envíos redundantes mediante umbrales:
  - Temperatura: 0.1 °C  
  - Humedad: 0.5 %  
  - Presión: 0.1 hPa  
- Registro de última lectura como referencia  
- Timestamp en formato ISO local

### Aplicación Android (Java)

Implementa tres roles:

#### Viewer
- Visualización de temperatura, humedad y presión  
- Historial completo de lecturas  
- Filtrado por fecha  
- Estado del dispositivo (ON/OFF)

#### Controller
- Visualización de última lectura  
- Control del estado del dispositivo

#### Admin
- Acceso total a todas las funciones

---

## Tecnologías Utilizadas

- Android Studio – Java  
- Firebase Realtime Database  
- ESP8266 – Arduino  
- ArduinoJson  
- API OpenWeatherMap  
- NTP para sincronización horaria

---

## Estructura en Firebase

```
/devices
   /esp32_01
      /control/state
      /last_reading
      /readings/{timestamp}
```

---

## Cómo Ejecutar

1. Configurar credenciales en el sketch:

```cpp
#define WIFI_SSID ""
#define WIFI_PASSWORD ""
#define FIREBASE_HOST ""
#define FIREBASE_AUTH ""
#define OPENWEATHER_API_KEY ""
```

2. Cargar el sketch al ESP8266  
3. Abrir el proyecto Android en Android Studio  
4. Configurar conexión con Firebase  
5. Ejecutar la app

---

##  Notas

- El proyecto fue diseñado para trabajar con un sensor físico;  
  al no contar con el hardware, se utilizó un simulador externo y datos reales desde OpenWeather.
- La comunicación y lógica IoT son completamente funcionales.

---

## Autoría

Proyecto realizado de forma colaborativa.  
Este repositorio contiene mi aporte enfocado en la aplicación Android e integración con Firebase.


---

Flujo de comunicación entre dispositivos

<img width="714" height="264" alt="image" src="https://github.com/user-attachments/assets/d433fd46-14cb-47be-b268-09438b8af9b0" />



## Capturas por Rol

###  Viewer

<p align="center">
  <img src="https://github.com/user-attachments/assets/bf9bd9fa-d4b8-4cce-abc0-d813d7fde707" width="350">
  <img src="https://github.com/user-attachments/assets/66f1bb09-269e-4d04-8c46-35e0577d047a" width="350">
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/03154ab7-be05-425a-ab26-80464002dacc" width="350">
  <img src="https://github.com/user-attachments/assets/cc28faf2-0d66-4355-9e99-dbc4419dd8cb" width="350">
</p>

---

### Operator

<p align="center">
  <img src="https://github.com/user-attachments/assets/3a853228-c9ef-4165-8c85-defbcb7f57e9" width="350">
  <img src="https://github.com/user-attachments/assets/24129a96-75c0-42a1-bde3-ce9e642c2df4" width="350">
</p>





