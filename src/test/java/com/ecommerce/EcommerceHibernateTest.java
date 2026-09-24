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
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EcommerceHibernateTest {

    @AfterAll
    public static void tearDown() {
        HibernateUtil.shutdown();
    }

    @Test
    @Order(1)
    @DisplayName("Task 2 & 3: Save and fetch Category and Product using direct Hibernate Session")
    public void testCreateCategoryAndProduct() {
        Long categoryId;
        Long productId;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Category category = new Category("Hardware", "PC components");
            session.persist(category);

            Product product = new Product("NVMe SSD 1TB", new BigDecimal("109.99"), 50, category);
            session.persist(product);

            tx.commit();

            categoryId = category.getId();
            productId = product.getId();
            assertThat(categoryId).isNotNull();
            assertThat(productId).isNotNull();
        }

        // Verify retrieval in a new session
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Product loaded = session.get(Product.class, productId);
            assertThat(loaded).isNotNull();
            assertThat(loaded.getName()).isEqualTo("NVMe SSD 1TB");
            assertThat(loaded.getCategory().getId()).isEqualTo(categoryId);
        }
    }

    @Test
    @Order(2)
    @DisplayName("Task 2 & 4: Register User with hashed password and verify credentials")
    public void testUserRegistrationAndPasswordHashing() {
        Long userId;
        String rawPassword = "TestPassword@123";
        String hashedPassword = PasswordUtil.hashPassword(rawPassword);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Users user = new Users("test_shopper", hashedPassword, "shopper@test.com", Role.CUSTOMER);
            session.persist(user);

            tx.commit();
            userId = user.getId();
        }

        // Retrieve and test password match
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Users loadedUser = session.get(Users.class, userId);
            assertThat(loadedUser).isNotNull();
            assertThat(loadedUser.getPassword()).isNotEqualTo(rawPassword);
            assertThat(PasswordUtil.checkPassword(rawPassword, loadedUser.getPassword())).isTrue();
            assertThat(loadedUser.getRole()).isEqualTo(Role.CUSTOMER);
        }
    }

    @Test
    @Order(3)
    @DisplayName("Task 4: Create Order with multiple OrderDetails and verify cascading")
    public void testCreateOrderWithMultipleOrderDetails() {
        Long orderId;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Category gaming = new Category("Gaming", "Gaming gear");
            session.persist(gaming);

            Product console = new Product("PlayConsole 5", new BigDecimal("499.00"), 10, gaming);
            Product game = new Product("Cyber Runner", new BigDecimal("59.99"), 20, gaming);
            session.persist(console);
            session.persist(game);

            Users buyer = new Users("gamer_boy", PasswordUtil.hashPassword("Pass@123"), "gamer@test.com", Role.CUSTOMER);
            session.persist(buyer);

            // Create Order
            Orders order = new Orders(buyer, LocalDateTime.now());

            // Create OrderDetails
            OrderDetails item1 = new OrderDetails(order, console, 1, console.getPrice());
            OrderDetails item2 = new OrderDetails(order, game, 2, game.getPrice());

            order.addOrderDetail(item1);
            order.addOrderDetail(item2);

            // Deduct stock
            console.setStockQuantity(console.getStockQuantity() - 1);
            game.setStockQuantity(game.getStockQuantity() - 2);

            // Persisting order cascades to OrderDetails automatically
            session.persist(order);

            tx.commit();
            orderId = order.getId();

            // Total: 1 * 499.00 + 2 * 59.99 = 618.98
            assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("618.98"));
        }

        // Verify order items were saved via cascade
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Orders loaded = session.get(Orders.class, orderId);
            assertThat(loaded).isNotNull();
            assertThat(loaded.getOrderDetails()).hasSize(2);
        }
    }

    @Test
    @Order(4)
    @DisplayName("Task 4: Fetch Order along with associated User and Products using JOIN FETCH")
    public void testFetchOrderWithDetailsAndProducts() {
        Long orderId;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Category gadgets = new Category("Gadgets", "Smart devices");
            session.persist(gadgets);

            Product smartBand = new Product("Smart Band 7", new BigDecimal("49.99"), 30, gadgets);
            session.persist(smartBand);

            Users user = new Users("fit_user", PasswordUtil.hashPassword("Fit123!"), "fit@test.com", Role.CUSTOMER);
            session.persist(user);

            Orders order = new Orders(user, LocalDateTime.now());
            order.addOrderDetail(new OrderDetails(order, smartBand, 1, smartBand.getPrice()));
            session.persist(order);

            tx.commit();
            orderId = order.getId();
        }

        // Fetch using single JOIN FETCH query
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT o FROM Orders o " +
                         "JOIN FETCH o.user u " +
                         "LEFT JOIN FETCH o.orderDetails od " +
                         "LEFT JOIN FETCH od.product p " +
                         "WHERE o.id = :id";

            Orders order = session.createQuery(hql, Orders.class)
                    .setParameter("id", orderId)
                    .uniqueResult();

            assertThat(order).isNotNull();
            assertThat(order.getUser().getUsername()).isEqualTo("fit_user");
            assertThat(order.getOrderDetails()).isNotEmpty();
            assertThat(order.getOrderDetails().get(0).getProduct().getName()).isEqualTo("Smart Band 7");
        }
    }

    @Test
    @Order(5)
    @DisplayName("Bonus 1: Named Query for fetching products by category")
    public void testNamedQueryFindByCategory() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Category stationery = new Category("Stationery", "Office supplies");
            session.persist(stationery);

            Product pen = new Product("Ballpoint Pen Pack", new BigDecimal("5.00"), 100, stationery);
            Product diary = new Product("2026 Daily Diary", new BigDecimal("15.00"), 50, stationery);
            session.persist(pen);
            session.persist(diary);

            tx.commit();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Product> products = session.createNamedQuery("Product.findByCategory", Product.class)
                    .setParameter("categoryName", "Stationery")
                    .getResultList();

            assertThat(products).hasSize(2);
            assertThat(products).extracting(Product::getName)
                    .containsExactlyInAnyOrder("Ballpoint Pen Pack", "2026 Daily Diary");
        }
    }

    @Test
    @Order(6)
    @DisplayName("Bonus 2: Dynamic search query using CriteriaBuilder")
    public void testCriteriaQuery() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Category kitchen = new Category("Kitchen", "Cookware");
            session.persist(kitchen);

            session.persist(new Product("Stainless Steel Knife", new BigDecimal("25.00"), 20, kitchen));
            session.persist(new Product("Non-Stick Frying Pan", new BigDecimal("45.00"), 15, kitchen));
            session.persist(new Product("Luxury Espresso Machine", new BigDecimal("299.00"), 5, kitchen));

            tx.commit();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Product> cq = cb.createQuery(Product.class);
            Root<Product> product = cq.from(Product.class);
            jakarta.persistence.criteria.Join<Product, Category> categoryJoin = product.join("category");

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(product.get("deleted")));
            predicates.add(cb.equal(categoryJoin.get("name"), "Kitchen"));
            predicates.add(cb.greaterThanOrEqualTo(product.get("price"), new BigDecimal("20.00")));
            predicates.add(cb.lessThanOrEqualTo(product.get("price"), new BigDecimal("50.00")));

            cq.where(predicates.toArray(new Predicate[0]));

            List<Product> filtered = session.createQuery(cq).getResultList();
            assertThat(filtered).hasSize(2);
            assertThat(filtered).extracting(Product::getName)
                    .containsExactlyInAnyOrder("Stainless Steel Knife", "Non-Stick Frying Pan");
        }
    }

    @Test
    @Order(7)
    @DisplayName("Bonus 3: Soft delete by setting deleted flag instead of physical deletion")
    public void testSoftDelete() {
        Long productId;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Category tempCategory = new Category("Temporary Category", "Temp items");
            session.persist(tempCategory);

            Product item = new Product("Temporary Item", new BigDecimal("10.00"), 5, tempCategory);
            session.persist(item);

            tx.commit();
            productId = item.getId();
        }

        // Perform soft delete
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            session.createMutationQuery("UPDATE Product p SET p.deleted = true WHERE p.id = :id")
                    .setParameter("id", productId)
                    .executeUpdate();

            tx.commit();
        }

        // Active query should exclude it
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Product> active = session.createNamedQuery("Product.findByCategory", Product.class)
                    .setParameter("categoryName", "Temporary Category")
                    .getResultList();

            assertThat(active).isEmpty();

            // Record still exists in DB with deleted = true
            Product softDeletedProduct = session.get(Product.class, productId);
            assertThat(softDeletedProduct).isNotNull();
            assertThat(softDeletedProduct.isDeleted()).isTrue();
        }
    }

    @Test
    @Order(8)
    @DisplayName("Bonus 4: Pagination using setFirstResult and setMaxResults")
    public void testPagination() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Category booksCat = new Category("Comics", "Comic books");
            session.persist(booksCat);

            for (int i = 1; i <= 4; i++) {
                session.persist(new Product("Comic Issue #" + i, new BigDecimal("4.99"), 50, booksCat));
            }

            tx.commit();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            int pageSize = 2;

            List<Product> page1 = session.createQuery("FROM Product p WHERE p.deleted = false ORDER BY p.id ASC", Product.class)
                    .setFirstResult(0)
                    .setMaxResults(pageSize)
                    .getResultList();

            List<Product> page2 = session.createQuery("FROM Product p WHERE p.deleted = false ORDER BY p.id ASC", Product.class)
                    .setFirstResult(2)
                    .setMaxResults(pageSize)
                    .getResultList();

            assertThat(page1).hasSize(2);
            assertThat(page2).hasSize(2);
            assertThat(page1.get(0).getId()).isNotEqualTo(page2.get(0).getId());
        }
    }
}
