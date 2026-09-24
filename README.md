# Hibernate ORM E-Commerce Application

A Hibernate 6 application managing an e-commerce database with JPA entities, relational mappings, CRUD operations, and advanced queries.

---

## 📌 Entities & Relationships

- **Category**: One-to-Many with `Product` (Unique `name`, `description`, `deleted` flag).
- **Product**: Many-to-One with `Category` (`name`, `price`, `stockQuantity`, `deleted` flag).
- **Users**: One-to-Many with `Orders` (`username`, `password` hashed with BCrypt, `email`, `role: ADMIN | CUSTOMER`).
- **Orders**: Many-to-One with `Users`, One-to-Many with `OrderDetails` (`orderDate`, `totalAmount`).
- **OrderDetails**: Many-to-One with `Orders` and `Product` (`quantity`, `unitPrice`).

---

## ⚙️ Database Configuration

Configured in `src/main/resources/hibernate.cfg.xml`:
- **Database**: MySQL 8.x (`ecommerce_db`)
- **User / Password**: `root` / `YOUR_DB_PASSWORD`
- **URL**: `jdbc:mysql://localhost:3306/ecommerce_db?createDatabaseIfNotExist=true`
- **DDL Mode**: `update` (or `create` for clean demo run)

A complete SQL schema is also available in `schema.sql`.

---

## 🚀 How to Run

### In Eclipse IDE:
1. **File** > **Import...** > **Maven** > **Existing Maven Projects** > Select project folder > **Finish**.
2. **Run Application**: Right-click `src/main/java/com/ecommerce/App.java` > **Run As** > **Java Application**.
3. **Run Tests**: Right-click `src/test/java/com/ecommerce/EcommerceHibernateTest.java` > **Run As** > **JUnit Test**.

### Using Maven Command Line:
```bash
# Run all CRUD unit tests
mvn clean test

# Run main demonstration
mvn compile exec:java
```

---

## 🌟 Implemented Features

- **Entity Mappings & Cascading**: Cascade persist and orphan removal across relationships.
- **Eager Retrieval**: `JOIN FETCH` queries to prevent the N+1 select problem.
- **Named Queries**: `@NamedQuery("Product.findByCategory")` for fast category filtering.
- **CriteriaBuilder**: Dynamic multi-criteria query for price range filtering.
- **Soft Delete**: `deleted` boolean flag to deactivate records without breaking foreign keys.
- **Pagination**: Offset and limit pagination using `setFirstResult` and `setMaxResults`.
- **Security**: BCrypt password hashing for users.
