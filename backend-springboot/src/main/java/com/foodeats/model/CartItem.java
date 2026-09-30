package com.foodeats.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(name = "item_name", length = 150)
    private String itemName;

    @Column(name = "price")
    private Double price;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "selected_options", length = 500)
    private String selectedOptions;

    public CartItem() {}

    public CartItem(Cart cart, MenuItem menuItem, Integer quantity) {
        this.cart = cart;
        this.menuItem = menuItem;
        this.quantity = quantity != null ? quantity : 1;
        if (menuItem != null) {
            this.itemName = menuItem.getName();
            this.price = menuItem.getPrice();
        }
    }

    public CartItem(Cart cart, MenuItem menuItem, Integer quantity, String selectedOptions) {
        this(cart, menuItem, quantity);
        this.selectedOptions = selectedOptions;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public MenuItem getMenuItem() { return menuItem; }
    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
        if (menuItem != null) {
            if (this.itemName == null) this.itemName = menuItem.getName();
            if (this.price == null) this.price = menuItem.getPrice();
        }
    }

    public String getItemName() {
        if (itemName != null) return itemName;
        return menuItem != null ? menuItem.getName() : "";
    }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Double getPrice() {
        if (price != null) return price;
        return menuItem != null ? menuItem.getPrice() : 0.0;
    }
    public void setPrice(Double price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getSelectedOptions() { return selectedOptions; }
    public void setSelectedOptions(String selectedOptions) { this.selectedOptions = selectedOptions; }
}
