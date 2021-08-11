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

    @ManyToOne
    @JoinColumn(name = "ticker")
    private InstrumentTO instrument;

    private Double quantity;

    public PositionTO() {
    }

    public BasketTO getBasket() {
        return basket;
    }

    public void setBasket(BasketTO basket) {
        this.basket = basket;
    }

    public InstrumentTO getInstrument() {
        return instrument;
    }

    public void setInstrument(InstrumentTO instrument) {
        this.instrument = instrument;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }
}
