package com.example.commerce.config;

import com.example.commerce.entities.CategoryEntity;
import com.example.commerce.entities.InventoryEntity;
import com.example.commerce.entities.ProductEntity;
import com.example.commerce.repositories.CategoryRepository;
import com.example.commerce.repositories.InventoryRepository;
import com.example.commerce.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public DatabaseSeeder(CategoryRepository categoryRepository,
                          ProductRepository productRepository,
                          InventoryRepository inventoryRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing Database Seeder...");

        if (categoryRepository.count() == 0) {
            logger.info("Seeding Categories...");
            CategoryEntity electronics = new CategoryEntity(null, "Electronics", "Electronic devices and gadgets", null, null);
            CategoryEntity clothing = new CategoryEntity(null, "Clothing", "Apparel and accessories", null, null);
            CategoryEntity home = new CategoryEntity(null, "Home & Kitchen", "Home appliances and kitchenware", null, null);
            CategoryEntity books = new CategoryEntity(null, "Books", "Physical and digital books", null, null);
            categoryRepository.save(electronics);
            categoryRepository.save(clothing);
            categoryRepository.save(home);
            categoryRepository.save(books);

            logger.info("Seeding Products and Inventory...");
            // Electronics
            createProductAndInventory("Laptop Pro", electronics, 1299.99, 50, "Warehouse A");
            createProductAndInventory("Smartphone X", electronics, 899.99, 120, "Warehouse B");
            createProductAndInventory("Wireless Earbuds", electronics, 149.99, 200, "Warehouse A");
            createProductAndInventory("Smartwatch Alpha", electronics, 249.99, 100, "Warehouse A");
            createProductAndInventory("4K Ultra Monitor", electronics, 399.99, 80, "Warehouse B");
            createProductAndInventory("Mechanical Keyboard", electronics, 129.99, 150, "Warehouse A");
            createProductAndInventory("Gaming Mouse", electronics, 79.99, 250, "Warehouse B");
            createProductAndInventory("Bluetooth Speaker", electronics, 59.99, 300, "Warehouse A");
            createProductAndInventory("High-Capacity Power Bank", electronics, 49.99, 400, "Warehouse B");

            // Clothing
            createProductAndInventory("Cotton T-Shirt", clothing, 19.99, 500, "Warehouse C");
            createProductAndInventory("Denim Jeans", clothing, 49.99, 300, "Warehouse C");
            createProductAndInventory("Running Shoes", clothing, 89.99, 150, "Warehouse D");
            createProductAndInventory("Winter Puffer Jacket", clothing, 129.99, 75, "Warehouse C");
            createProductAndInventory("Genuine Leather Belt", clothing, 34.99, 200, "Warehouse D");
            createProductAndInventory("Cozy Pullover Hoodie", clothing, 45.99, 250, "Warehouse C");
            createProductAndInventory("Classic Formal Shirt", clothing, 55.99, 180, "Warehouse D");
            createProductAndInventory("Casual White Sneakers", clothing, 65.99, 220, "Warehouse C");
            createProductAndInventory("Merino Wool Scarf", clothing, 29.99, 120, "Warehouse D");

            // Home & Kitchen
            createProductAndInventory("Espresso Coffee Maker", home, 299.99, 40, "Warehouse E");
            createProductAndInventory("High-Speed Blender", home, 89.99, 90, "Warehouse E");
            createProductAndInventory("Digital Air Fryer", home, 119.99, 110, "Warehouse F");
            createProductAndInventory("Robot Vacuum Cleaner", home, 249.99, 60, "Warehouse E");
            createProductAndInventory("LED Desk Lamp", home, 39.99, 180, "Warehouse F");
            createProductAndInventory("Ergonomic Office Chair", home, 199.99, 45, "Warehouse E");

            // Books
            createProductAndInventory("Clean Code Handbook", books, 45.99, 100, "Warehouse G");
            createProductAndInventory("System Design Interview", books, 39.99, 150, "Warehouse G");
            createProductAndInventory("Designing Data-Intensive Apps", books, 54.99, 80, "Warehouse H");
            createProductAndInventory("Head First Design Patterns", books, 49.99, 120, "Warehouse G");

            logger.info("Successfully seeded data!");
        } else {
            logger.info("Database already contains data, skipping seeder.");
        }
    }

    private void createProductAndInventory(String name, CategoryEntity category, Double price, Integer quantity, String location) {
        String sku = "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ProductEntity product = new ProductEntity(null, name, category, sku, price, null, null);
        product = productRepository.save(product);

        InventoryEntity inventory = new InventoryEntity(null, product, quantity, location);
        inventoryRepository.save(inventory);
    }
}
