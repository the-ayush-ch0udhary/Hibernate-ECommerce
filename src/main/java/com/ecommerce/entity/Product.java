package com.ecommerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "products")
@NamedQueries({
    @NamedQuery(
        name = "Product.findByCategory",
        query = "SELECT p FROM Product p WHERE p.category.name = :categoryName AND p.deleted = false"
    ),
    @NamedQuery(
        name = "Product.findByCategoryId",
        query = "SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.deleted = false"
    ),
    @NamedQuery(
        name = "Product.findActive",
        query = "SELECT p FROM Product p WHERE p.deleted = false"
    ),
    @NamedQuery(
        name = "Product.findByPriceRange",
        query = "SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.deleted = false ORDER BY p.price ASC"
    )
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_category"))
    private Category category;

    public Product() {
    }

    public Product(String name, BigDecimal price, Integer stockQuantity, Category category) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.deleted = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id) || (Objects.equals(name, product.name) && Objects.equals(category, product.category));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", deleted=" + deleted +
                '}';
    }
}
