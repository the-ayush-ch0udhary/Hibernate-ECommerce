package com.ecommerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
@NamedQueries({
    @NamedQuery(
        name = "Orders.findActive",
        query = "SELECT o FROM Orders o WHERE o.deleted = false ORDER BY o.orderDate DESC"
    ),
    @NamedQuery(
        name = "Orders.findByUserId",
        query = "SELECT o FROM Orders o WHERE o.user.id = :userId AND o.deleted = false ORDER BY o.orderDate DESC"
    ),
    @NamedQuery(
        name = "Orders.fetchWithDetailsAndUser",
        query = "SELECT DISTINCT o FROM Orders o " +
                "JOIN FETCH o.user u " +
                "LEFT JOIN FETCH o.orderDetails od " +
                "LEFT JOIN FETCH od.product p " +
                "WHERE o.id = :orderId AND o.deleted = false"
    )
})
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_user"))
    private Users user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderDetails> orderDetails = new ArrayList<>();

    public Orders() {
        this.orderDate = LocalDateTime.now();
        this.totalAmount = BigDecimal.ZERO;
        this.deleted = false;
    }

    public Orders(Users user) {
        this.user = user;
        this.orderDate = LocalDateTime.now();
        this.totalAmount = BigDecimal.ZERO;
        this.deleted = false;
    }

    public Orders(Users user, LocalDateTime orderDate) {
        this.user = user;
        this.orderDate = orderDate != null ? orderDate : LocalDateTime.now();
        this.totalAmount = BigDecimal.ZERO;
        this.deleted = false;
    }

    // Helper synchronization methods for OrderDetails
    public void addOrderDetail(OrderDetails detail) {
        orderDetails.add(detail);
        detail.setOrder(this);
        recalculateTotalAmount();
    }

    public void removeOrderDetail(OrderDetails detail) {
        orderDetails.remove(detail);
        detail.setOrder(null);
        recalculateTotalAmount();
    }

    public void recalculateTotalAmount() {
        this.totalAmount = orderDetails.stream()
                .filter(d -> !d.isDeleted())
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public List<OrderDetails> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetails> orderDetails) {
        this.orderDetails = orderDetails;
        recalculateTotalAmount();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Orders orders = (Orders) o;
        return Objects.equals(id, orders.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Orders{" +
                "id=" + id +
                ", orderDate=" + orderDate +
                ", totalAmount=" + totalAmount +
                ", deleted=" + deleted +
                '}';
    }
}
