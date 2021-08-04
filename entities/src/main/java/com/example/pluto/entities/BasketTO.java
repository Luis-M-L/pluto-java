package com.example.pluto.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "PLUTO_BASKETS")
public class BasketTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BASKET_ID")
    private Long id;

    @Column(unique = true)
    private String label;

    @OneToMany( mappedBy = "basket", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<WeightTO> weights;

    public BasketTO() {
    }

    public BasketTO(Long id, String ticker, List<WeightTO> weights) {
        this.id = id;
        this.label = ticker;
        weights.forEach(w -> w.setBasket(this));
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

    public List<WeightTO> getWeights() {
        return weights;
    }

    public void setWeights(List<WeightTO> weights) {
        weights.forEach(w -> w.setBasket(this));
        this.weights = weights;
    }

    @Override
    public String toString() {
        return "InstrumentTO{" +
                "id=" + id +
                ", label='" + label + '\'' +
                '}';
    }

    public boolean equals(Object comparing){
        BasketTO comp = (BasketTO) comparing;
        String compLabel = comp.getLabel() != null ? comp.getLabel() : "comparing label void";
        String refLabel = this.getLabel() != null ? this.getLabel() : "reference label void";
        return compLabel.equals(refLabel);
    }
}
