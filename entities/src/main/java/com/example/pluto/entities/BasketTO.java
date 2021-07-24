package com.example.pluto.entities;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "PLUTO_BASKETS")
public class BasketTO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "BASKET_ID")
    private Long id;

    private String label;

    @OneToMany( mappedBy = "basket", cascade = {CascadeType.ALL})
    private List<WeightsTO> weights;

    public BasketTO() {
    }

    public BasketTO(Long id, String ticker, List<WeightsTO> weights) {
        this.id = id;
        this.label = ticker;
        this.weights = weights;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<WeightsTO> getWeights() {
        return weights;
    }

    public void setWeights(List<WeightsTO> weights) {
        this.weights = weights;
    }

    @Override
    public String toString() {
        return "InstrumentTO{" +
                "id=" + id +
                ", label='" + label + '\'' +
                '}';
    }
}
