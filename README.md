# Sistema de Recomendaciones con Neo4j

## Descripción

Este proyecto implementa un sistema de recomendaciones basado en el algoritmo Bayesiano Ingenuo utilizando Neo4j como base de datos. El sistema permite realizar varias operaciones, como buscar recomendaciones basadas en un pixel específico, agregar nuevos nodos (vehículos y tokens anotados), eliminar nodos y aplicar filtros avanzados.

## Características

- **Búsqueda de Recomendaciones:** Obtén recomendaciones basadas en un pixel específico.
- **Agregar Nodos:** Añade nuevos vehículos y tokens anotados a la base de datos.
- **Eliminar Nodos:** Elimina un vehículo y todas sus relaciones.
- **Filtros Avanzados:** Aplica filtros para refinar los resultados.
- **Recomendaciones en Redes Sociales:** Obtén recomendaciones específicas para plataformas de redes sociales.

## Requisitos del Sistema

- **Java Development Kit (JDK) 11 o superior**
- **Apache Maven 3.6.3 o superior**
- **Neo4j Aura (Community Edition)**
- **Conexión a Internet**

## Instalación

### Prerrequisitos

1. **Instalar JDK:**
   - [Descargar JDK](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) e instalarlo según las instrucciones de la plataforma.

2. **Instalar Maven:**
   - [Descargar Maven](https://maven.apache.org/download.cgi) e instalarlo según las instrucciones de la plataforma.

3. **Configurar Neo4j Aura:**
   - Crear una cuenta en [Neo4j Aura](https://console.neo4j.io/) y configurar una base de datos.

### Configuración del Proyecto

1. **Clonar el Repositorio:**
```sh
   git clone https://github.com/tu-usuario/repo-de-recomendaciones.git
   cd repo-de-recomendaciones
```

3. ***Configurar el Archivo pom.xml:***

Asegúrate de que el archivo pom.xml contiene las siguientes dependencias:
```xml
<dependencies>
    <dependency>
        <groupId>org.neo4j.driver</groupId>
        <artifactId>neo4j-java-driver</artifactId>
        <version>4.3.6</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.12.3</version>
    </dependency>
</dependencies>
```

Para crear la base de datos correr el script de python y reemplazar las credenciales de Neo4j con las credenciales correspondientes.
Dentro de la carpeta `python\Data` se encuentras los archivos *.json que servirán como fuente para crear los nodos y relaciones del grafo
```python
python ./python/load_data.py
```

# Uso
## Menú Principal
### Después de iniciar la aplicación, verás el menú principal con las siguientes opciones:

**Buscar:** Permite elegir un pixel para obtener recomendaciones.
**Agregar Nodos:** Permite agregar un nuevo vehículo y tokens anotados.
**Eliminar Nodos:** Permite eliminar un vehículo basado en su carCode.
**Filter data:** Aplica filtros avanzados (solo visible si hay datos disponibles).
**Clear filter:** Restablece los parámetros de filtro (solo visible si hay datos disponibles).
**Social Media recommendation:** Obtén recomendaciones específicas para redes sociales (solo visible si hay datos disponibles).
**Salir:** Cierra la aplicación.

### Ejemplo de Uso
Buscar:

Elige la opción 1 y luego selecciona el pixel (MetaPixel, XPixel, TikTokPixel).
La aplicación mostrará las recomendaciones basadas en el pixel seleccionado.
Agregar Nodos:

Elige la opción 2 e ingresa los detalles del vehículo y tokens anotados según las indicaciones.
Eliminar Nodos:

Elige la opción 3 e ingresa el carCode del vehículo que deseas eliminar.
Filter Data:

Elige la opción 4 e ingresa el texto de filtro para refinar los resultados.
Clear Filter:

Elige la opción 5 para restablecer los parámetros de filtro y mostrar las recomendaciones iniciales.
Social Media Recommendation:

Elige la opción 6 y selecciona la plataforma de redes sociales para obtener recomendaciones específicas.
