package com.ecommerce;

import com.ecommerce.entity.*;
import com.ecommerce.util.HibernateUtil;
import com.ecommerce.util.PasswordUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application demonstrating Hibernate ORM operations directly
 * using Hibernate Session and Transaction (beginner-friendly, no DAO/Service layers).
 */
public class App {

    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("           HIBERNATE ORM E-COMMERCE APPLICATION (DIRECT SESSION DEMO)          ");
        System.out.println("===============================================================================");

        Long customerId = null;
        Long orderId = null;
        Long jacketProductId = null;

        // =====================================================================
        // TASK 4.1: INSERT NEW CATEGORIES, PRODUCTS, AND USERS
        // =====================================================================
        System.out.println("\n[1] --- INSERTING CATEGORIES, PRODUCTS, AND USERS ---");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // 1. Create and save Categories
            Category electronics = new Category("Electronics", "Phones, laptops, and gadgets");
            Category books = new Category("Books", "Technical, educational, and story books");
            Category apparel = new Category("Apparel", "Clothing and winter wear");

            session.persist(electronics);
            session.persist(books);
            session.persist(apparel);

            // 2. Create and save Products linked to Categories
            Product laptop = new Product("ThinkPad X1 Carbon", new BigDecimal("1499.99"), 20, electronics);
            Product phone = new Product("Pixel 8 Pro", new BigDecimal("899.00"), 35, electronics);
            Product monitor = new Product("UltraWide 34\" Monitor", new BigDecimal("499.50"), 15, electronics);
            Product book1 = new Product("Effective Java", new BigDecimal("48.00"), 50, books);
            Product book2 = new Product("Clean Code", new BigDecimal("45.00"), 40, books);
            Product jacket = new Product("Winter Jacket", new BigDecimal("119.00"), 25, apparel);

            session.persist(laptop);
            session.persist(phone);
            session.persist(monitor);
            session.persist(book1);
            session.persist(book2);
            session.persist(jacket);

            // 3. Create and save Users with Hashed Passwords
            String hashedAdminPass = PasswordUtil.hashPassword("AdminPass@2026");
            Users admin = new Users("admin_user", hashedAdminPass, "admin@store.com", Role.ADMIN);

            String hashedCustomerPass = PasswordUtil.hashPassword("Secret123!");
            Users customer = new Users("john_doe", hashedCustomerPass, "john@example.com", Role.CUSTOMER);

            session.persist(admin);
            session.persist(customer);

            // Commit the transaction to save all records
            tx.commit();

            customerId = customer.getId();
            jacketProductId = jacket.getId();

            System.out.println("Saved Categories: " + electronics.getName() + ", " + books.getName() + ", " + apparel.getName());
            System.out.println("Saved Products: " + laptop.getName() + ", " + phone.getName() + ", " + book1.getName());
            System.out.println("Registered User: " + customer.getUsername() + " (Password hashed with BCrypt: " + customer.getPassword().substring(0, 15) + "...)");
            System.out.println("Password verification ('Secret123!'): " + PasswordUtil.checkPassword("Secret123!", customer.getPassword()));
        }

        // =====================================================================
        // TASK 4.2: CREATE ORDER WITH MULTIPLE ORDER DETAILS (CASCADING)
        // =====================================================================
        System.out.println("\n[2] --- CREATING ORDER WITH MULTIPLE ORDER DETAILS ---");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // Load the customer
            Users customer = session.get(Users.class, customerId);

            // Fetch products to order
            Product laptop = session.createQuery("FROM Product WHERE name = 'ThinkPad X1 Carbon'", Product.class).uniqueResult();
            Product monitor = session.createQuery("FROM Product WHERE name = 'UltraWide 34\" Monitor'", Product.class).uniqueResult();
            Product book = session.createQuery("FROM Product WHERE name = 'Effective Java'", Product.class).uniqueResult();

            // Create new Order for the user
            Orders order = new Orders(customer, LocalDateTime.now());

            // Create OrderDetails (line items)
            OrderDetails item1 = new OrderDetails(order, laptop, 1, laptop.getPrice());
            OrderDetails item2 = new OrderDetails(order, monitor, 2, monitor.getPrice());
            OrderDetails item3 = new OrderDetails(order, book, 3, book.getPrice());

            // Add details to order (updates order amount and bidirectional link)
            order.addOrderDetail(item1);
            order.addOrderDetail(item2);
            order.addOrderDetail(item3);

            // Update stock quantities
            laptop.setStockQuantity(laptop.getStockQuantity() - 1);
            monitor.setStockQuantity(monitor.getStockQuantity() - 2);
            book.setStockQuantity(book.getStockQuantity() - 3);

            // Persisting the order automatically saves OrderDetails due to cascade = CascadeType.ALL
            session.persist(order);

            tx.commit();

            orderId = order.getId();
            System.out.println("Created Order #" + order.getId() + " for Customer: " + customer.getUsername());
            System.out.println("Calculated Order Total: $" + order.getTotalAmount());
            System.out.println("Number of items in order: " + order.getOrderDetails().size());
        }

        // =====================================================================
        // TASK 4.3: FETCH ORDER ALONG WITH ASSOCIATED USER AND PRODUCTS (JOIN FETCH)
        // =====================================================================
        System.out.println("\n[3] --- FETCHING ORDER ALONG WITH ASSOCIATED USER AND PRODUCTS ---");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Using JOIN FETCH to load Order, User, OrderDetails, and Product in ONE single query
            String hql = "SELECT DISTINCT o FROM Orders o " +
                         "JOIN FETCH o.user u " +
                         "LEFT JOIN FETCH o.orderDetails od " +
                         "LEFT JOIN FETCH od.product p " +
                         "WHERE o.id = :orderId";

            Orders fetchedOrder = session.createQuery(hql, Orders.class)
                    .setParameter("orderId", orderId)
                    .uniqueResult();

            System.out.println("Order ID: " + fetchedOrder.getId());
            System.out.println("Customer Name: " + fetchedOrder.getUser().getUsername() + " (" + fetchedOrder.getUser().getEmail() + ")");
            System.out.println("Order Date: " + fetchedOrder.getOrderDate());
            System.out.println("Order Total: $" + fetchedOrder.getTotalAmount());
            System.out.println("Line Items:");
            for (OrderDetails item : fetchedOrder.getOrderDetails()) {
                System.out.println("  -> Product: " + item.getProduct().getName() +
                        " | Qty: " + item.getQuantity() +
                        " | Unit Price: $" + item.getUnitPrice() +
                        " | Subtotal: $" + item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }

        // =====================================================================
        // BONUS 1: NAMED QUERY (Fetch products by Category)
        // =====================================================================
        System.out.println("\n[4] --- BONUS: NAMED QUERY (Product.findByCategory: 'Electronics') ---");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Product> electronicsProducts = session.createNamedQuery("Product.findByCategory", Product.class)
                    .setParameter("categoryName", "Electronics")
                    .getResultList();

            for (Product p : electronicsProducts) {
                System.out.println("  * " + p.getName() + " - $" + p.getPrice() + " [Stock: " + p.getStockQuantity() + "]");
            }
        }

        // =====================================================================
        // BONUS 2: CRITERIA QUERY USING CRITERIABUILDER
        // =====================================================================
        System.out.println("\n[5] --- BONUS: CRITERIABUILDER QUERY (Price between $20 and $100) ---");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Product> cq = cb.createQuery(Product.class);
            Root<Product> product = cq.from(Product.class);

            List<Predicate> predicates = new ArrayList<>();
            // Only active (not soft deleted) products
            predicates.add(cb.isFalse(product.get("deleted")));
            // Price >= 20.00
            predicates.add(cb.greaterThanOrEqualTo(product.get("price"), new BigDecimal("20.00")));
            // Price <= 100.00
            predicates.add(cb.lessThanOrEqualTo(product.get("price"), new BigDecimal("100.00")));

            cq.where(predicates.toArray(new Predicate[0]));
            cq.orderBy(cb.asc(product.get("price")));

            List<Product> results = session.createQuery(cq).getResultList();
            for (Product p : results) {
                System.out.println("  * Found: " + p.getName() + " - $" + p.getPrice());
            }
        }

        // =====================================================================
        // BONUS 3: SOFT DELETE FUNCTIONALITY
        // =====================================================================
        System.out.println("\n[6] --- BONUS: SOFT DELETE DEMONSTRATION ---");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            System.out.println("Soft deleting Product ID: " + jacketProductId);
            // Soft delete by updating deleted flag instead of physical deletion
            session.createMutationQuery("UPDATE Product p SET p.deleted = true WHERE p.id = :id")
                    .setParameter("id", jacketProductId)
                    .executeUpdate();

            tx.commit();
        }

        // Verify it is excluded from active query
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Product> activeApparel = session.createNamedQuery("Product.findByCategory", Product.class)
                    .setParameter("categoryName", "Apparel")
                    .getResultList();

            System.out.println("Active products in 'Apparel' category after soft delete: " + activeApparel.size());

            // But record still physically exists in database:
            Product rawProduct = session.get(Product.class, jacketProductId);
            System.out.println("Physical product still in DB? " + (rawProduct != null) + " (isDeleted = " + rawProduct.isDeleted() + ")");
        }

        // =====================================================================
        // BONUS 4: PAGINATION FOR PRODUCT LISTINGS
        // =====================================================================
        System.out.println("\n[7] --- BONUS: PAGINATION (Page size = 2) ---");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            int pageSize = 2;

            // Page 1: first 2 items (offset = 0)
            List<Product> page1 = session.createQuery("FROM Product p WHERE p.deleted = false ORDER BY p.id ASC", Product.class)
                    .setFirstResult(0)
                    .setMaxResults(pageSize)
                    .getResultList();

            System.out.println("Page 1 (first 2 items):");
            for (Product p : page1) {
                System.out.println("  - [" + p.getId() + "] " + p.getName() + " ($" + p.getPrice() + ")");
            }

            // Page 2: next 2 items (offset = 2)
            List<Product> page2 = session.createQuery("FROM Product p WHERE p.deleted = false ORDER BY p.id ASC", Product.class)
                    .setFirstResult(2)
                    .setMaxResults(pageSize)
                    .getResultList();

            System.out.println("Page 2 (next 2 items):");
            for (Product p : page2) {
                System.out.println("  - [" + p.getId() + "] " + p.getName() + " ($" + p.getPrice() + ")");
            }
        }

        System.out.println("\n===============================================================================");
        System.out.println("           DEMONSTRATION FINISHED SUCCESSFULLY!                                ");
        System.out.println("===============================================================================");

        HibernateUtil.shutdown();
    }
}
