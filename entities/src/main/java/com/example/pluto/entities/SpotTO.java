package com.example.pluto.entities;

public class SpotTO {

    private Double bid;
    private Double mid;
    private Double offer;

    public SpotTO() {
    }

    public SpotTO(Double bid, Double offer) {
        this.bid = bid;
        this.offer = offer;
        this.mid = (bid != null && offer != null) ? bid + offer / 2 : null;
    }

    public Double getBid() {
        return bid;
    }

    public void setBid(Double bid) {
        this.bid = bid;
        setMid();
    }

    public Double getMid() {
        return mid;
    }

    public void setMid() {
        this.mid = (this.bid != null && this.offer != null) ? this.bid + this.offer / 2 : null;
    }

    public Double getOffer() {
        return offer;
    }

    public void setOffer(Double offer) {
        this.offer = offer;
        setMid();
    }

    @Override
    public String toString() {
        return "SpotTO{" +
                "bid=" + bid +
                ", mid=" + mid +
                ", offer=" + offer +
                '}';
    }
}