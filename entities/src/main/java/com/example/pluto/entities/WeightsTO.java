package com.example.pluto.entities;

import javax.persistence.*;

@Entity
@Table(name = "PLUTO_WEIGHTS")
public class WeightsTO {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String instrument;
    private Double weight;

    @ManyToOne(fetch = FetchType.LAZY)
    private BasketTO basket;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public BasketTO getBasket() {
        return basket;
    }

    public void setBasket(BasketTO basket) {
        this.basket = basket;
    }

    @Override
    public String toString() {
        return "WeightsTO{" +
                "id=" + id +
                ", instrument=" + instrument +
                ", weight=" + weight +
                ", basket=" + basket +
                '}';
    }
}
