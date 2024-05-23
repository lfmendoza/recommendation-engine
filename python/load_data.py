import json
from neo4j import GraphDatabase
import pandas as pd
import os

# Neo4j Aura credentials
uri = "neo4j+s://452a1661.databases.neo4j.io:7687"
user = "neo4j"
password = "pgurqecn14jK4u4ro_h-0T0u0oTy7stMZWezJPMqWx8"

# Load data from JSON files
def load_json(file_name):
    with open(file_name, 'r') as f:
        return json.load(f)

# Define the data directory
data_dir = 'python\\data'

cars_data = load_json(os.path.join(data_dir, 'cars.json'))
meta_pixel_data = load_json(os.path.join(data_dir, 'metaPixel.json'))
x_pixel_data = load_json(os.path.join(data_dir, 'xPixel.json'))
tiktok_pixel_data = load_json(os.path.join(data_dir, 'tiktokPixel.json'))

# Combine all pixel data into a single list
pixel_data = {
    "MetaPixel": meta_pixel_data,
    "XPixel": x_pixel_data,
    "TikTokPixel": tiktok_pixel_data
}

def load_data(uri, user, password, car_data, pixel_data):
    driver = GraphDatabase.driver(uri, auth=(user, password))

    with driver.session() as session:
        # Delete everything from the database
        session.execute_write(delete_all)

        # Create Car nodes
        for record in car_data:
            session.execute_write(create_car_node, record)

        # Create Pixel nodes
        session.execute_write(create_pixel_nodes)

        # Create AnnotatedToken nodes and link them to Pixel
        for pixel_type, data in pixel_data.items():
            session.execute_write(create_annotated_token_nodes, pixel_type, data)

        # Link Car nodes to Pixel nodes based on matching keywords
        for pixel_type, data in pixel_data.items():
            session.execute_write(create_pixel_car_relationships, pixel_type, data)

    driver.close()

def delete_all(tx):
    tx.run("MATCH (n) DETACH DELETE n")

def create_pixel_nodes(tx):
    query = """
    MERGE (m:Pixel {name: 'MetaPixel'})
    MERGE (x:Pixel {name: 'XPixel'})
    MERGE (t:Pixel {name: 'TikTokPixel'})
    """
    tx.run(query)

def create_car_node(tx, record):
    query = (
        "CREATE (c:Car {maker: $maker, year: $year, model: $model, mileage: $mileage, "
        "type: $type, price: $price, location: $location, carCode: $carCode})"
    )
    tx.run(query, record)

def create_annotated_token_nodes(tx, pixel_type, data):
    for record in data:
        query = (
            "MATCH (p:Pixel {name: $pixelType}) "
            "MERGE (t:AnnotatedToken {name: $keyword}) "
            "MERGE (p)-[r:HAS_TAG {device: $device, timestamp: $timestamp}]->(t)"
        )
        tx.run(query, keyword=record["keyword"], device=record["device"], timestamp=record["timestamp"], pixelType=pixel_type)

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

def create_pivot_table(data, file_name):
    df = pd.DataFrame(data)
    pivot_table = df.pivot(index='Pixel', columns='Car', values='Count').fillna(0).astype(int)
    pivot_table.to_excel(file_name, engine='openpyxl')
    return pivot_table

if __name__ == "__main__":
    load_data(uri, user, password, cars_data, pixel_data)
    data = query_counts(uri, user, password)
    pivot_table = create_pivot_table(data, "pivot_table.xlsx")
    print(pivot_table)