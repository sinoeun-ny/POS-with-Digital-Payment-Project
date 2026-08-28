package com.foodeats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "item_options")
public class ItemOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "category", "options"})
    private MenuItem menuItem;

    @Column(nullable = false)
    private String optionGroup = "Options"; // e.g., 'Size', 'Extra Toppings', 'Spice Level'

    @Column(nullable = false)
    private String optionName; // e.g., 'Regular', 'Large', 'Extra Cheese'

    @Column(nullable = false)
    private Double priceAdjustment = 0.00;

    private Boolean isAvailable = true;

    public ItemOption() {}

    public ItemOption(MenuItem menuItem, String optionGroup, String optionName, Double priceAdjustment, Boolean isAvailable) {
        this.menuItem = menuItem;
        this.optionGroup = optionGroup;
        this.optionName = optionName;
        this.priceAdjustment = priceAdjustment != null ? priceAdjustment : 0.00;
        this.isAvailable = isAvailable != null ? isAvailable : true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MenuItem getMenuItem() { return menuItem; }
    public void setMenuItem(MenuItem menuItem) { this.menuItem = menuItem; }

    public String getOptionGroup() { return optionGroup; }
    public void setOptionGroup(String optionGroup) { this.optionGroup = optionGroup; }

    public String getOptionName() { return optionName; }
    public void setOptionName(String optionName) { this.optionName = optionName; }

    public Double getPriceAdjustment() { return priceAdjustment; }
    public void setPriceAdjustment(Double priceAdjustment) { this.priceAdjustment = priceAdjustment; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}
