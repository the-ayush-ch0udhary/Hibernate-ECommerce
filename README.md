# 🛒 Hibernate E-Commerce Management System

![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk)
![Hibernate](https://img.shields.io/badge/Hibernate-ORM_6-brown?logo=hibernate)
![MySQL](https://img.shields.io/badge/MySQL-Database-blue?logo=mysql)
![Maven](https://img.shields.io/badge/Maven-Build-red?logo=apachemaven)
![JUnit](https://img.shields.io/badge/JUnit-Testing-green?logo=junit5)

A robust Java-based **E-Commerce Management System** developed using **Hibernate ORM 6** and **MySQL**.
The project demonstrates entity relationships, database persistence, cascading operations, order management, password hashing, and advanced queries.

---

## ✨ Features

- 🗂️ **Category Management** — Creation and management with unique constraints
- 📦 **Product Catalog** — Products with pricing, stock tracking, and category associations
- 👤 **User Management** — Users with roles (`ADMIN`, `CUSTOMER`) and BCrypt password hashing
- 🛒 **Order Processing** — Order placement with automatic total calculation and stock deduction
- 📋 **OrderDetails** — Multiple line items per order linking products, quantities, and unit prices
- 🔗 **Relational Mappings** — Declarative JPA annotations (`@OneToMany`, `@ManyToOne`, `@JoinColumn`)
- ⚡ **Optimized Queries** — Eager retrieval using `JOIN FETCH` to eliminate N+1 select issues
- 🔍 **Named Queries** — Precompiled `@NamedQuery` for category product retrieval
- 📊 **CriteriaBuilder Queries** — Dynamic multi-predicate query filtering by price range
- 🗑️ **Soft Delete Support** — Non-destructive record deactivation via `deleted` flag
- 📄 **Pagination** — Product catalog pagination using `setFirstResult` and `setMaxResults`
- 🧪 **Automated Testing** — Comprehensive JUnit 5 test suite verifying all operations

---

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| ☕ **Java 17+** | Core programming language |
| 🔄 **Hibernate ORM 6.4.4** | Object-Relational Mapping (JPA) & persistence |
| 🗄️ **MySQL 8.x** | Relational database management system |
| 🔒 **jBCrypt** | Secure password hashing |
| 📦 **Maven** | Dependency management and build automation |
| 🧪 **JUnit 5 & AssertJ** | Automated unit and integration testing |

---

## 📂 Project Structure

```text
Hibernate-ECommerce/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── ecommerce/
│   │   │           │
│   │   │           ├── App.java                 # Interactive demo application
│   │   │           │
│   │   │           ├── entity/                  # JPA mapped entities
│   │   │           │   ├── Category.java
│   │   │           │   ├── Product.java
│   │   │           │   ├── Users.java
│   │   │           │   ├── Orders.java
│   │   │           │   ├── OrderDetails.java
│   │   │           │   └── Role.java
│   │   │           │
│   │   │           └── util/                    # Utility classes
│   │   │               ├── HibernateUtil.java   # SessionFactory management
│   │   │               └── PasswordUtil.java    # BCrypt password utility
│   │   │
│   │   └── resources/
│   │       ├── hibernate.cfg.xml                # Hibernate MySQL configuration
│   │       └── logback.xml                      # Formatted logging configuration
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── ecommerce/
│                   └── EcommerceHibernateTest.java # 8 JUnit 5 test cases
│
├── schema.sql                                   # Database DDL and sample seed data
├── pom.xml                                      # Maven project configuration
└── README.md
```

---

## ⚙️ How It Works

1. 🧱 **Entity Classes** define the domain model with JPA annotations (`@Entity`, `@Table`, `@Id`).
2. 🔄 **Hibernate ORM** maps Java entities directly to MySQL relational tables.
3. ➕ **Create Operations** persist Categories, Products, Users (with hashed passwords), and Orders.
4. 🔍 **Read Operations** fetch single or batch records using HQL and `JOIN FETCH`.
5. ✏️ **Stock Updates** automatically decrement inventory when orders are placed.
6. 🗑️ **Soft Delete** sets `deleted = true` so records remain physically in the database while being excluded from active queries.
7. 🛒 **OrderDetails** stores quantity and unit price for each item in an order.
8. 💰 **Order Total** is automatically computed from `∑ (quantity × unitPrice)`.
9. 📦 Orders support **multiple OrderDetails**, enabling multi-product checkouts in a single transaction.
10. 🔗 Hibernate cascades saves from `Orders` to `OrderDetails` via `CascadeType.ALL`.

---

## 🔗 Entity Relationships

```text
Category
   │
   │ One-to-Many
   ▼
Product
   │
   │ Many-to-One
   ▼
Category


Users
   │
   │ One-to-Many
   ▼
Orders
   │
   │ One-to-Many
   ▼
OrderDetails
   │
   │ Many-to-One
   ▼
Product
```

### Relationships Used

- 🗂️ **Category → Product** — `@OneToMany` (mappedBy = "category")
- 📦 **Product → Category** — `@ManyToOne` (JoinColumn: `category_id`)
- 👤 **Users → Orders** — `@OneToMany` (mappedBy = "user")
- 🛒 **Orders → Users** — `@ManyToOne` (JoinColumn: `user_id`)
- 🛒 **Orders → OrderDetails** — `@OneToMany` (cascade = ALL, orphanRemoval = true)
- 📋 **OrderDetails → Orders** — `@ManyToOne` (JoinColumn: `order_id`)
- 📋 **OrderDetails → Product** — `@ManyToOne` (JoinColumn: `product_id`)

---

## 🛒 Multiple Products in One Order

The application supports purchasing multiple products in a single order transaction:

```text
Order #1 (Total: $2642.99)
 ├── OrderDetails → ThinkPad X1 Carbon (Qty: 1 × $1499.99)
 ├── OrderDetails → UltraWide 34" Monitor (Qty: 2 × $499.50)
 └── OrderDetails → Effective Java (Qty: 3 × $48.00)
```

The total amount is calculated automatically:
$$\text{Total Amount} = \sum (\text{Quantity} \times \text{Unit Price})$$

---

## 🚀 How to Run

### 1. Open the Project
Import the project into **Eclipse, IntelliJ IDEA, or VS Code** as an *Existing Maven Project*.

### 2. Configure MySQL
Ensure MySQL is running and set your database credentials in:
```text
src/main/resources/hibernate.cfg.xml
```

```xml
<property name="hibernate.connection.url">jdbc:mysql://localhost:3306/ecommerce_db?createDatabaseIfNotExist=true</property>
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password">YOUR_DB_PASSWORD</property>
```

### 3. Run the Demonstration Application
- **In Eclipse / IDE**: Right-click `src/main/java/com/ecommerce/App.java` > **Run As** > **Java Application**.
- **Via Maven Terminal**:
  ```bash
  mvn compile exec:java
  ```

---

## 🧪 Testing

Execute the complete JUnit 5 test suite verifying CRUD, relationships, cascade behavior, named queries, criteria queries, soft delete, and pagination:

```bash
mvn clean test
```

---

## 📌 Project Status

**Completed — Academic / Learning Project**

This project demonstrates practical skills in **Java, Hibernate ORM 6, Relational Mappings, MySQL Integration, Order Processing, JPA Criteria API, and Maven**.

---

## 👨‍💻 Author

**Ayush Kumar Choudhary**
