import json
from neo4j import GraphDatabase
import pandas as pd
import os

# Credenciales para Neo4j
uri = "neo4j+s://452a1661.databases.neo4j.io:7687"
user = "neo4j"
password = "pgurqecn14jK4u4ro_h-0T0u0oTy7stMZWezJPMqWx8"

# Función para cargar los datos desde archivos JSON
def load_json(file_name):
    with open(file_name, 'r') as f:
        return json.load(f)

# Definir el directorio de los datos
data_dir = 'python\\data'

# Cargar datos desde archivos JSON
cars_data = load_json(os.path.join(data_dir, 'cars.json'))
meta_pixel_data = load_json(os.path.join(data_dir, 'metaPixel.json'))
x_pixel_data = load_json(os.path.join(data_dir, 'xPixel.json'))
tiktok_pixel_data = load_json(os.path.join(data_dir, 'tiktokPixel.json'))

# Combinar todos los datos de píxeles en una lista
pixel_data = {
    "MetaPixel": meta_pixel_data,
    "XPixel": x_pixel_data,
    "TikTokPixel": tiktok_pixel_data
}

# Función principal para cargar los datos en Neo4j
def load_data(uri, user, password, car_data, pixel_data):
    driver = GraphDatabase.driver(uri, auth=(user, password))

    with driver.session() as session:
        # Elimina todo de la base de datos
        session.execute_write(delete_all)

        # Crea nodos de autos
        for record in car_data:
            session.execute_write(create_car_node, record)

        # Crea nodos de píxeles
        session.execute_write(create_pixel_nodes)

        # Crea nodos de tokens anotados y enlazarlos a los píxeles
        for pixel_type, data in pixel_data.items():
            session.execute_write(create_annotated_token_nodes, pixel_type, data)

        # Enlaza nodos de autos a nodos de píxeles basándose en palabras clave coincidentes
        for pixel_type, data in pixel_data.items():
            session.execute_write(create_pixel_car_relationships, pixel_type, data)

    driver.close()

# Función para eliminar todos los nodos y relaciones en la base de datos
def delete_all(tx):
    tx.run("MATCH (n) DETACH DELETE n")

# Función para crear nodos de píxeles
def create_pixel_nodes(tx):
    query = """
    MERGE (m:Pixel {name: 'MetaPixel'})
    MERGE (x:Pixel {name: 'XPixel'})
    MERGE (t:Pixel {name: 'TikTokPixel'})
    """
    tx.run(query)

# Función para crear nodos de autos
def create_car_node(tx, record):
    query = (
        "CREATE (c:Car {maker: $maker, year: $year, model: $model, mileage: $mileage, "
        "type: $type, price: $price, location: $location, carCode: $carCode})"
    )
    tx.run(query, record)

# Función para crear nodos de tokens anotados y enlazarlos a los píxeles
def create_annotated_token_nodes(tx, pixel_type, data):
    for record in data:
        query = (
            "MATCH (p:Pixel {name: $pixelType}) "
            "MERGE (t:AnnotatedToken {name: $keyword}) "
            "MERGE (p)-[r:HAS_TAG {device: $device, timestamp: $timestamp}]->(t)"
        )
        tx.run(query, keyword=record["keyword"], device=record["device"], timestamp=record["timestamp"], pixelType=pixel_type)

# Función para crear relaciones entre nodos de autos y píxeles basándose en palabras clave
def create_pixel_car_relationships(tx, pixel_type, data):
    for record in data:
        query = (
            "MATCH (c:Car) "
            "WHERE c.maker = $keyword OR c.model = $keyword OR c.type = $keyword "
            "WITH c, collect(DISTINCT $keyword) AS keywords "
            "WITH c, keywords[0] AS keyword "
            "MATCH (p:Pixel {name: $pixelType}) "
            "MERGE (c)-[:TRACKED_BY]->(p)"
        )
        tx.run(query, keyword=record["keyword"], pixelType=pixel_type)

# Función para consultar los conteos de relaciones en la base de datos
def query_counts(uri, user, password):
    driver = GraphDatabase.driver(uri, auth=(user, password))

    query = """
    MATCH (c:Car)-[:TRACKED_BY]->(p:Pixel)-[r:HAS_TAG]->(t:AnnotatedToken)
    RETURN c.carCode AS Car, p.name AS Pixel, COUNT(t) AS Count
    """

    with driver.session() as session:
        result = session.run(query)
        data = [record.data() for record in result]

    driver.close()

    return data

# Función para crear una tabla dinámica a partir de los datos y guardarla en un archivo Excel
def create_pivot_table(data, file_name):
    df = pd.DataFrame(data)
    pivot_table = df.pivot(index='Pixel', columns='Car', values='Count').fillna(0).astype(int)
    pivot_table.to_excel(file_name, engine='openpyxl')
    return pivot_table

# Punto de entrada principal del script
if __name__ == "__main__":
    load_data(uri, user, password, cars_data, pixel_data)
    data = query_counts(uri, user, password)
    pivot_table = create_pivot_table(data, "pivot_table.xlsx")
    print(pivot_table)