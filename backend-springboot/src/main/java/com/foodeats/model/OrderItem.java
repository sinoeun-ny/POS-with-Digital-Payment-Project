package com.foodeats.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    @Column(name = "menu_item_name", nullable = false, length = 150)
    private String menuItemName;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "options_summary", length = 255)
    private String optionsSummary;

    public OrderItem() {}

    public OrderItem(Order order, MenuItem menuItem, Integer quantity, Double price) {
        this.order = order;
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.price = price;
        if (menuItem != null) {
            this.menuItemName = menuItem.getName();
        }
    }

    public OrderItem(Order order, MenuItem menuItem, Integer quantity, Double price, String optionsSummary) {
        this(order, menuItem, quantity, price);
        this.optionsSummary = optionsSummary;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public MenuItem getMenuItem() { return menuItem; }
    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
        if (menuItem != null && this.menuItemName == null) {
            this.menuItemName = menuItem.getName();
        }
    }

    @JsonProperty("name")
    public String getMenuItemName() {
        if (menuItemName != null) return menuItemName;
        return menuItem != null ? menuItem.getName() : "";
    }
    public void setMenuItemName(String menuItemName) { this.menuItemName = menuItemName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getOptionsSummary() { return optionsSummary; }
    public void setOptionsSummary(String optionsSummary) { this.optionsSummary = optionsSummary; }
}
