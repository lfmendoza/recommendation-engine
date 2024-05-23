package org.example;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;

import java.util.List;

public class RecommendationService {

    private Driver driver;
    private boolean hasData = false;

    public void connectToNeo4j(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public void getRecommendationsByPixel(String pixel) {
        try (Session session = driver.session()) {
            List<String> paramTokenList = List.of(pixel);

            String query = "WITH $paramTokenList AS param_token_list\n" +
                    "// step 1: total count\n" +
                    "MATCH (:Pixel)-[r:HAS_TAG]->(:AnnotatedToken)\n" +
                    "WITH count(r) AS count_total, param_token_list\n" +
                    "MATCH (:AnnotatedToken)<-[:HAS_TAG]-(pixel:Pixel)-[:TRACKED_BY]-(car:Car)\n" +
                    "WHERE pixel.name IN param_token_list\n" +
                    "WITH DISTINCT car, count_total, param_token_list\n" +
                    "// step 3: size of each car\n" +
                    "MATCH (car)-[:TRACKED_BY]->(:Pixel)-[r:HAS_TAG]->(:AnnotatedToken)\n" +
                    "WITH car, count(r) AS count_car, count_total, param_token_list\n" +
                    "UNWIND param_token_list AS param_token\n" +
                    "// step 4: number of assignments for (car, token) combinations\n" +
                    "MATCH (pixel:Pixel {name: param_token})-[:HAS_TAG]-(token:AnnotatedToken)\n" +
                    "OPTIONAL MATCH (car)-[:TRACKED_BY]->()-[r:HAS_TAG]->(token)\n" +
                    "WITH car, token, count(r) AS count_token_car, count_car, count_total\n" +
                    "WITH *, CASE\n" +
                    "    WHEN count_token_car = 0 THEN 0.1 / count_car\n" +
                    "    WHEN count_token_car > 0 THEN toFloat(count_token_car) / count_car\n" +
                    "END AS posterior\n" +
                    "WITH car, token, count_token_car, count_car, count_total, posterior, apoc.agg.product(posterior) AS product\n" +
                    "RETURN car.carCode, round(product * count_car / count_total, 5) AS score\n" +
                    "ORDER BY score DESC\n" +
                    "LIMIT 20";

            Result result = session.run(query, org.neo4j.driver.Values.parameters("paramTokenList", paramTokenList));

            hasData = result.hasNext();
            System.out.println("Recommendations for pixel: " + pixel);
            while (result.hasNext()) {
                Record record = result.next();
                String carCode = record.get("car.carCode").asString();
                double score = record.get("score").asDouble();

                System.out.println("Car: " + carCode + ", Score: " + score);
            }
        }
    }

    public void filterData(String filterText) {
        try (Session session = driver.session()) {
            String query = "MATCH (c:Car) " +
                    "WHERE c.maker CONTAINS $filterText OR c.model CONTAINS $filterText OR c.year CONTAINS $filterText " +
                    "OR c.mileage CONTAINS $filterText OR c.type CONTAINS $filterText OR c.location CONTAINS $filterText " +
                    "RETURN c.carCode, c.price, c.maker, c.model, c.year, c.mileage " +
                    "ORDER BY c.price ASC " +
                    "LIMIT 20";

            Result result = session.run(query, org.neo4j.driver.Values.parameters("filterText", filterText));

            hasData = result.hasNext();
            if (!hasData) {
                System.out.println("No information matches the search parameters.");
                return;
            }

            System.out.println("Filtered data:");
            while (result.hasNext()) {
                Record record = result.next();
                String carCode = record.get("c.carCode").asString();
                double price = record.get("c.price").asDouble();
                String maker = record.get("c.maker").asString();
                String model = record.get("c.model").asString();
                int year = record.get("c.year").asInt();
                int mileage = record.get("c.mileage").asInt();

                System.out.println("CarCode: " + carCode + ", Price: " + price + ", Maker: " + maker + ", Model: " + model + ", Year: " + year + ", Mileage: " + mileage);
            }
        }
    }

    public void addNode(String carCode, String maker, int year, String model, int mileage, String type, double price, String location, String tokenName, String pixelName, String device, String timestamp) {
        try (Session session = driver.session()) {
            String carQuery = "CREATE (c:Car {carCode: $carCode, maker: $maker, year: $year, model: $model, mileage: $mileage, type: $type, price: $price, location: $location})";
            session.run(carQuery, org.neo4j.driver.Values.parameters("carCode", carCode, "maker", maker, "year", year, "model", model, "mileage", mileage, "type", type, "price", price, "location", location));

            String tokenQuery = "MATCH (c:Car {carCode: $carCode}), (p:Pixel {name: $pixelName}) " +
                    "MERGE (t:AnnotatedToken {name: $tokenName}) " +
                    "MERGE (p)-[r:HAS_TAG {device: $device, timestamp: $timestamp}]->(t) " +
                    "MERGE (c)-[:TRACKED_BY]->(p)";
            session.run(tokenQuery, org.neo4j.driver.Values.parameters("carCode", carCode, "tokenName", tokenName, "pixelName", pixelName, "device", device, "timestamp", timestamp));
        }
    }

    public void deleteNode(String carCode) {
        try (Session session = driver.session()) {
            String query = "MATCH (c:Car {carCode: $carCode}) DETACH DELETE c";
            session.run(query, org.neo4j.driver.Values.parameters("carCode", carCode));
        }
    }

    public void getSocialMediaRecommendation(String platform) {
        System.out.println("Social Media recommendation for: " + platform);
        getRecommendationsByPixel(platform);
    }

    public boolean hasData() {
        return hasData;
    }

    public void closeConnection() {
        if (driver != null) {
            driver.close();
        }
    }
}