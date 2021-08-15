package com.example.pluto.entities;

import javax.persistence.*;

@Entity
@Table(name = "PLUTO_POSITIONS")
public class PositionTO {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "BASKET_ID")
    private BasketTO basket;

    private String currency;

    private Double quantity;

    public PositionTO() {
    }

    public BasketTO getBasket() {
        return basket;
    }

    public void setBasket(BasketTO basket) {
        this.basket = basket;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }
}
