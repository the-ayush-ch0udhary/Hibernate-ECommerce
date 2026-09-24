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

public class App {

    public static void main(String[] args) {
        Long customerId = null;
        Long orderId = null;
        Long jacketProductId = null;

        // 1. Insert Categories, Products, and Users
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Category electronics = new Category("Electronics", "Phones, laptops, and gadgets");
            Category books = new Category("Books", "Technical, educational, and story books");
            Category apparel = new Category("Apparel", "Clothing and winter wear");

            session.persist(electronics);
            session.persist(books);
            session.persist(apparel);

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

            Users admin = new Users("admin_user", PasswordUtil.hashPassword("AdminPass@2026"), "admin@store.com", Role.ADMIN);
            Users customer = new Users("john_doe", PasswordUtil.hashPassword("Secret123!"), "john@example.com", Role.CUSTOMER);

            session.persist(admin);
            session.persist(customer);

            tx.commit();

            customerId = customer.getId();
            jacketProductId = jacket.getId();

            System.out.println("Categories and products saved successfully.");
            System.out.println("User registered: " + customer.getUsername());
        }

        // 2. Create Order with multiple OrderDetails
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Users customer = session.get(Users.class, customerId);
            Product laptop = session.createQuery("FROM Product WHERE name = 'ThinkPad X1 Carbon'", Product.class).uniqueResult();
            Product monitor = session.createQuery("FROM Product WHERE name = 'UltraWide 34\" Monitor'", Product.class).uniqueResult();
            Product book = session.createQuery("FROM Product WHERE name = 'Effective Java'", Product.class).uniqueResult();

            Orders order = new Orders(customer, LocalDateTime.now());

            OrderDetails item1 = new OrderDetails(order, laptop, 1, laptop.getPrice());
            OrderDetails item2 = new OrderDetails(order, monitor, 2, monitor.getPrice());
            OrderDetails item3 = new OrderDetails(order, book, 3, book.getPrice());

            order.addOrderDetail(item1);
            order.addOrderDetail(item2);
            order.addOrderDetail(item3);

            laptop.setStockQuantity(laptop.getStockQuantity() - 1);
            monitor.setStockQuantity(monitor.getStockQuantity() - 2);
            book.setStockQuantity(book.getStockQuantity() - 3);

            session.persist(order);
            tx.commit();

            orderId = order.getId();
            System.out.println("Created Order #" + order.getId() + " - Total: $" + order.getTotalAmount());
        }

        // 3. Fetch Order with User and Products (JOIN FETCH)
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT o FROM Orders o " +
                         "JOIN FETCH o.user u " +
                         "LEFT JOIN FETCH o.orderDetails od " +
                         "LEFT JOIN FETCH od.product p " +
                         "WHERE o.id = :orderId";

            Orders fetchedOrder = session.createQuery(hql, Orders.class)
                    .setParameter("orderId", orderId)
                    .uniqueResult();

            System.out.println("Order #" + fetchedOrder.getId() + " by " + fetchedOrder.getUser().getUsername() + ":");
            for (OrderDetails item : fetchedOrder.getOrderDetails()) {
                System.out.println("  - " + item.getProduct().getName() + " | Qty: " + item.getQuantity() + " | Price: $" + item.getUnitPrice());
            }
        }

        // 4. Named Query
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Product> electronics = session.createNamedQuery("Product.findByCategory", Product.class)
                    .setParameter("categoryName", "Electronics")
                    .getResultList();

            System.out.println("Electronics products found: " + electronics.size());
        }

        // 5. CriteriaBuilder Query
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Product> cq = cb.createQuery(Product.class);
            Root<Product> product = cq.from(Product.class);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(product.get("deleted")));
            predicates.add(cb.greaterThanOrEqualTo(product.get("price"), new BigDecimal("20.00")));
            predicates.add(cb.lessThanOrEqualTo(product.get("price"), new BigDecimal("100.00")));

            cq.where(predicates.toArray(new Predicate[0]));
            cq.orderBy(cb.asc(product.get("price")));

            List<Product> results = session.createQuery(cq).getResultList();
            System.out.println("Products between $20 and $100: " + results.size());
        }

        // 6. Soft Delete
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createMutationQuery("UPDATE Product p SET p.deleted = true WHERE p.id = :id")
                    .setParameter("id", jacketProductId)
                    .executeUpdate();
            tx.commit();
            System.out.println("Soft deleted product ID: " + jacketProductId);
        }

        // 7. Pagination
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            int pageSize = 2;
            List<Product> page1 = session.createQuery("FROM Product p WHERE p.deleted = false ORDER BY p.id ASC", Product.class)
                    .setFirstResult(0)
                    .setMaxResults(pageSize)
                    .getResultList();

            System.out.println("Page 1 (first " + page1.size() + " items):");
            for (Product p : page1) {
                System.out.println("  - " + p.getName() + " ($" + p.getPrice() + ")");
            }
        }

        HibernateUtil.shutdown();
    }
}
