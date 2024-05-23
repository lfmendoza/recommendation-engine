package org.example;


import java.util.Scanner;

public class App {

    private static RecommendationService recommendationService = new RecommendationService();
    private static String lastPixel = "";

    public static void main(String[] args) {
        // Connect to Neo4j
        recommendationService.connectToNeo4j("neo4j+s://452a1661.databases.neo4j.io:7687", "neo4j", "pgurqecn14jK4u4ro_h-0T0u0oTy7stMZWezJPMqWx8");

        // Display options menu
        displayMenu();

        // Close connection
        recommendationService.closeConnection();
    }

    private static void displayMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nOptions:");
            System.out.println("1. Buscar");
            System.out.println("2. Agregar nodos");
            System.out.println("3. Eliminar nodos");
            System.out.println("0. Salir");

            if (recommendationService.hasData()) {
                System.out.println("4. Filter data");
                System.out.println("5. Clear filter");
                System.out.println("6. Social Media recommendation");
            }

            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    buscar();
                    break;
                case 2:
                    agregarNodos();
                    break;
                case 3:
                    eliminarNodos();
                    break;
                case 4:
                    if (recommendationService.hasData()) {
                        filterData();
                    }
                    break;
                case 5:
                    if (recommendationService.hasData()) {
                        clearFilter();
                    }
                    break;
                case 6:
                    if (recommendationService.hasData()) {
                        socialMediaRecommendation();
                    }
                    break;
                case 0:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void buscar() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose the pixel:");
        System.out.println("1. MetaPixel");
        System.out.println("2. XPixel");
        System.out.println("3. TikTokPixel");

        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                lastPixel = "MetaPixel";
                break;
            case 2:
                lastPixel = "XPixel";
                break;
            case 3:
                lastPixel = "TikTokPixel";
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                return;
        }

        recommendationService.getRecommendationsByPixel(lastPixel);
    }

    private static void agregarNodos() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter carCode: ");
        String carCode = scanner.nextLine();

        System.out.print("Enter maker: ");
        String maker = scanner.nextLine();

        System.out.print("Enter year: ");
        int year = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.print("Enter model: ");
        String model = scanner.nextLine();

        System.out.print("Enter mileage: ");
        int mileage = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.print("Enter type: ");
        String type = scanner.nextLine();

        System.out.print("Enter price: ");
        double price = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        System.out.print("Enter location: ");
        String location = scanner.nextLine();

        System.out.print("Enter the number of annotated tokens: ");
        int tokenCount = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        for (int i = 0; i < tokenCount; i++) {
            System.out.println("Annotated Token " + (i + 1) + ":");
            System.out.print("Enter token name: ");
            String tokenName = scanner.nextLine();

            System.out.print("Enter pixel name (MetaPixel, XPixel, TikTokPixel): ");
            String pixelName = scanner.nextLine();

            System.out.print("Enter device: ");
            String device = scanner.nextLine();

            System.out.print("Enter timestamp: ");
            String timestamp = scanner.nextLine();

            recommendationService.addNode(carCode, maker, year, model, mileage, type, price, location, tokenName, pixelName, device, timestamp);
        }

        System.out.println("Node added successfully.");
    }

    private static void eliminarNodos() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the CarCode to delete: ");
        String carCode = scanner.nextLine();

        recommendationService.deleteNode(carCode);

        System.out.println("Node deleted successfully.");
    }

    private static void filterData() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter filter text: ");
        String filterText = scanner.nextLine();

        recommendationService.filterData(filterText);
    }

    private static void clearFilter() {
        System.out.println("Clearing filter...");
        recommendationService.getRecommendationsByPixel(lastPixel);
    }

    private static void socialMediaRecommendation() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose the platform:");
        System.out.println("1. Facebook");
        System.out.println("2. Instagram");
        System.out.println("3. X/Twitter");
        System.out.println("4. TikTok");

        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                recommendationService.getSocialMediaRecommendation("MetaPixel");
                break;
            case 2:
                recommendationService.getSocialMediaRecommendation("MetaPixel");
                break;
            case 3:
                recommendationService.getSocialMediaRecommendation("XPixel");
                break;
            case 4:
                recommendationService.getSocialMediaRecommendation("TikTokPixel");
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }
}
