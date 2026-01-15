#include <ESP8266WiFi.h>
#include <FirebaseESP8266.h>
#include <time.h>
#include <ArduinoJson.h>

// Config WiFi

#define WIFI_SSID "" // Nombre de la red Wifi
#define WIFI_PASSWORD "" // Clave de la red Wifi

// Firebase config
#define FIREBASE_HOST "" // URL de Host de proyecto en Firebase
#define FIREBASE_AUTH "" // Token otorgado por Firebase

// OpenWeatherMap config
#define OPENWEATHER_API_KEY "" // Token otorgada por OpenWeatherMap
#define CITY_ID "3583361"  // ID de lugar (San Salvador, El Salvador)
#define OPENWEATHER_HOST "api.openweathermap.org" // URL de API

// Configuración de zona horaria (El Salvador: UTC-6)
#define TIMEZONE_OFFSET -6 * 3600

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

WiFiClient client;

// Variables para almacenar los últimos datos enviados
float lastTemperature = 0;
float lastHumidity = 0;
float lastPressure = 0;
bool firstRead = true;

void setup() {
  Serial.begin(115200);
 
  // Conectar a WiFi
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Conectando a WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\n✅ WiFi conectado");

  // Configurar hora con zona horaria de El Salvador (UTC-6)
  configTime(TIMEZONE_OFFSET, 0, "pool.ntp.org", "time.nist.gov");
  Serial.print("⏳ Sincronizando hora de El Salvador");
  while (time(nullptr) < 100000) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\n🕒 Hora sincronizada (UTC-6)");

  // Configurar Firebase
  config.host = FIREBASE_HOST;
  config.signer.tokens.legacy_token = FIREBASE_AUTH;
  
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
  
  // Configurar tiempo de espera
  fbdo.setBSSLBufferSize(1024, 1024);
  fbdo.setResponseSize(1024);
}

// Función para verificar el estado de control en Firebase
bool isDeviceEnabled() {
  Serial.println("🔍 Verificando estado del dispositivo...");
  
  if (Firebase.getString(fbdo, "/devices/esp32_01/control/state")) {
    String state = fbdo.stringData();
    Serial.println("   Estado actual: " + state);
    return state == "ON";
  } else {
    Serial.println("❌ Error leyendo estado, asumiendo OFF");
    return false;
  }
}

// Función para obtener los últimos datos guardados en Firebase
bool getLastStoredData(float &temp, float &hum, float &pres) {
  Serial.println("🔍 Obteniendo últimos datos guardados...");
  
  if (Firebase.getJSON(fbdo, "/devices/esp32_01/last_reading")) {
    FirebaseJson &json = fbdo.jsonObject();
    FirebaseJsonData result;
    
    if (json.get(result, "temperature")) {
      temp = result.to<float>();
    }
    if (json.get(result, "humidity")) {
      hum = result.to<float>();
    }
    if (json.get(result, "pressure")) {
      pres = result.to<float>();
    }
    
    Serial.println("   Últimos datos - Temp: " + String(temp) + "°C, Hum: " + String(hum) + "%, Pres: " + String(pres) + " hPa");
    return true;
  } else {
    Serial.println("   No se encontraron datos previos");
    return false;
  }
}

// Función para guardar los últimos datos en una referencia especial
void saveLastReading(float temp, float hum, float pres, const char* timestamp) {
  FirebaseJson json;
  json.set("temperature", temp);
  json.set("humidity", hum);
  json.set("pressure", pres);
  json.set("timestamp", timestamp);
  
  if (Firebase.setJSON(fbdo, "/devices/esp32_01/last_reading", json)) {
    Serial.println("💾 Última lectura guardada como referencia");
  }
}

// Función para verificar si los datos han cambiado significativamente
bool haveDataChanged(float newTemp, float newHum, float newPres) {
  if (firstRead) {
    firstRead = false;
    return true; // Siempre guardar la primera lectura
  }
  
  // Umbrales de cambio (ajustables según necesidad)
  const float tempThreshold = 0.1;   // 0.1°C
  const float humThreshold = 0.5;    // 0.5%
  const float presThreshold = 0.1;   // 0.1 hPa
  
  bool tempChanged = abs(newTemp - lastTemperature) >= tempThreshold;
  bool humChanged = abs(newHum - lastHumidity) >= humThreshold;
  bool presChanged = abs(newPres - lastPressure) >= presThreshold;
  
  if (tempChanged || humChanged || presChanged) {
    Serial.println("📊 Cambios detectados:");
    if (tempChanged) Serial.println("   - Temperatura: " + String(lastTemperature) + " → " + String(newTemp) + "°C");
    if (humChanged) Serial.println("   - Humedad: " + String(lastHumidity) + " → " + String(newHum) + "%");
    if (presChanged) Serial.println("   - Presión: " + String(lastPressure) + " → " + String(newPres) + " hPa");
    return true;
  } else {
    Serial.println("📊 Datos sin cambios significativos, omitiendo envío");
    return false;
  }
}

// Función para obtener datos meteorológicos reales
bool obtenerDatosMeteorologicos(float &temperature, float &humidity, float &pressure) {
  Serial.println("🌤️  Obteniendo datos meteorológicos...");
  
  if (!client.connect(OPENWEATHER_HOST, 80)) {
    Serial.println("❌ Error conectando a OpenWeatherMap");
    return false;
  }

  // Hacer petición HTTP
  String url = "/data/2.5/weather?id=" + String(CITY_ID) + "&appid=" + String(OPENWEATHER_API_KEY) + "&units=metric";
  
  client.println("GET " + url + " HTTP/1.1");
  client.println("Host: " + String(OPENWEATHER_HOST));
  client.println("Connection: close");
  client.println();
  
  // Esperar respuesta
  unsigned long timeout = millis();
  while (client.available() == 0) {
    if (millis() - timeout > 5000) {
      Serial.println("❌ Timeout en la respuesta");
      client.stop();
      return false;
    }
  }

  // Leer respuesta
  String response = "";
  while (client.available()) {
    response += client.readStringUntil('\r');
  }
  client.stop();

  // Buscar el JSON en la respuesta (después de los headers)
  int jsonStart = response.indexOf('{');
  int jsonEnd = response.lastIndexOf('}');
  
  if (jsonStart == -1 || jsonEnd == -1) {
    Serial.println("❌ No se encontró JSON en la respuesta");
    return false;
  }
  
  String jsonString = response.substring(jsonStart, jsonEnd + 1);
  
  // Parsear JSON
  DynamicJsonDocument doc(1024);
  DeserializationError error = deserializeJson(doc, jsonString);
  
  if (error) {
    Serial.print("❌ Error parseando JSON: ");
    Serial.println(error.c_str());
    return false;
  }

  // Extraer datos
  temperature = doc["main"]["temp"];
  humidity = doc["main"]["humidity"];
  pressure = doc["main"]["pressure"];
  
  Serial.println("✅ Datos meteorológicos obtenidos:");
  Serial.println("   Temperatura: " + String(temperature) + "°C");
  Serial.println("   Humedad: " + String(humidity) + "%");
  Serial.println("   Presión: " + String(pressure) + " hPa");
  
  return true;
}

// Función para obtener timestamp en formato local de El Salvador
void getLocalTimestamp(char* buffer, size_t bufferSize) {
  time_t now = time(nullptr);
  struct tm* timeinfo = localtime(&now);
  strftime(buffer, bufferSize, "%Y-%m-%dT%H:%M:%S", timeinfo);
}

void loop() {
  // Verificar si el dispositivo está habilitado
  if (!isDeviceEnabled()) {
    Serial.println("⏸️  Dispositivo en estado OFF, esperando...");
    //delay(30000); // Esperar 1/2 minuto antes de verificar nuevamente
    delay(15000);
    return;
  }

  // Obtener timestamp local segun ID de (El Salvador)
  char timestamp[25];
  getLocalTimestamp(timestamp, sizeof(timestamp));
  Serial.println("🕒 Hora local: " + String(timestamp));

  // Obtener datos meteorológicos REALES
  float temperature, humidity, pressure;
  
  if (obtenerDatosMeteorologicos(temperature, humidity, pressure)) {
    // Verificar si los datos han cambiado significativamente
    if (haveDataChanged(temperature, humidity, pressure)) {
      // Ruta en Firebase con timestamp
      String path = "/devices/esp32_01/readings/";
      path += timestamp;

      Serial.println("📡 Enviando a Firebase: " + path);

      FirebaseJson json;
      json.set("temperature", temperature);
      json.set("humidity", humidity);
      json.set("pressure", pressure);
      json.set("timestamp", timestamp);
      json.set("location", "San Salvador, El Salvador");
      json.set("source", "OpenWeatherMap");      

      if (Firebase.setJSON(fbdo, path.c_str(), json)) {
        Serial.println("✅ Lecturas reales enviadas a Firebase");
        
        // Actualizar últimos datos guardados
        lastTemperature = temperature;
        lastHumidity = humidity;
        lastPressure = pressure;
        
        // Guardar referencia de última lectura
        saveLastReading(temperature, humidity, pressure, timestamp);
      } else {
        Serial.print("❌ Error enviando a Firebase: ");
        Serial.println(fbdo.errorReason());
      }
    }
  } else {
    Serial.println("⚠️  Error obteniendo datos reales, omitiendo ciclo");
  }

  Serial.println("⏳ Esperando 5 minutos...");
  delay(300000);  // Espera 5 minutos antes del próximo ciclo
}
