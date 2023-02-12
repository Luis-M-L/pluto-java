package com.example.pluto.tasks.assetmanager;

import com.example.pluto.entities.SpotEntity;
import com.example.pluto.entities.TradeTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Simulator extends Calculator implements Runnable {

    private Map<String, BigDecimal> positions;
    private StringBuilder outputBuilder;

    @Override
    public void run() {
        Map<String, BigDecimal> weights = getWeights();
        Set<String> currencies = new HashSet<>(weights.size());
        currencies.addAll(weights.keySet());

        initPositions(weights.keySet());
        initOutputBuilder(currencies);

        List<Map<String, SpotEntity>> spotsHist = getSpotsHist(weights.keySet());
        for (Map<String, SpotEntity> h : spotsHist) {
            BigDecimal threshold = BigDecimal.valueOf(0.01);    // Desviaciones del 1%

            Map<String, SpotEntity> spots = h;
            logger.debug("Spots for iteration: " + spots.toString());
            BigDecimal btc = BigDecimal.ONE;
            Map<String, BigDecimal> qBid = getUpperBounds(btc, spots, weights);
            Map<String, BigDecimal> qAsk = getLowerBounds(btc, spots, weights);
            if (qBid.isEmpty() || qAsk.isEmpty()) {
                logger.debug("Bounds could not be calculated");
            } else {
                List<TradeTO> trading = getSellingTrades(qBid, positions, threshold);
                trading.addAll(getBuyingTrades(qAsk, positions, threshold));
                updatePositions(trading, spots);

                saveState(currencies, spots);
            }
        }

        printOutput();
    }

    private void initPositions(Set<String> currencies) {
        initPositions(currencies, null);
    }

    private void initOutputBuilder(Set<String> currencies) {
        outputBuilder = new StringBuilder("TIMESTAMP;");
        currencies.forEach(c -> outputBuilder.append(c).append(";")
                .append("BID (").append(c).append("BTC)").append(";")
                .append("ASK (").append(c).append("BTC)").append(";"));
        outputBuilder.append(System.getProperty("line.separator"));
    }

    private void initPositions(Set<String> currencies, final BigDecimal btc) {
        positions = new HashMap<>(currencies.size());
        BigDecimal btcEq = btc == null ? BigDecimal.ONE : btc;
        currencies.forEach(c -> positions.put(c, "BTC".equals(c) ? btcEq : BigDecimal.ZERO));
    }

    private List<Map<String, SpotEntity>> getSpotsHist(Set<String> currencies) {
        StringBuilder sb = new StringBuilder();
        currencies.forEach(c -> sb.append(",").append(c).append("BTC"));
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse("2021-05-27 13:30:00", f);
        LocalDateTime end = LocalDateTime.parse("2022-05-27 13:00:10", f);
        List<SpotEntity> unordered = bitfinex.getSpots(sb.toString().substring(1), start.toEpochSecond(ZoneOffset.UTC), end.toEpochSecond(ZoneOffset.UTC));
        return spotsMapInList(unordered);
    }


    private static List<Map<String, SpotEntity>> spotsMapInList(List<SpotEntity> spots) {
        spots = spots.stream().sorted(Comparator.comparing(SpotEntity::getTimestamp)).collect(Collectors.toList());

        Timestamp ref = spots.get(0).getTimestamp();
        List<Map<String, SpotEntity>> snapshots = new LinkedList<>();
        Map<String, SpotEntity> saving = new HashMap<>();
        for (SpotEntity s : spots) {
            if (!s.getTimestamp().equals(ref)) {
                ref = s.getTimestamp();
                snapshots.add(saving);
                saving = new HashMap<>();
            }
            saving.put(s.getInstrument().replace("BTC", ""), s);
        }
        snapshots.add(saving);
        return snapshots;
    }

    public void updatePositions(List<TradeTO> trading, Map<String, SpotEntity> spots) {
        for (TradeTO t : trading) {
            int side = BigDecimal.ZERO.compareTo(t.getAmount()); // Buy = -1;
            String altCoin = t.getPair().replace("BTC", "");
            BigDecimal applyingSpot = side == -1 ? spots.get(altCoin).getOffer() : spots.get(altCoin).getBid();
            BigDecimal btcAmount = t.getAmount().multiply(applyingSpot);
            if (side == -1) {
                t.setAmount(applyFees(t.getAmount()));
                btcAmount = btcAmount.negate();
            } else {
                btcAmount = applyFees(btcAmount);
                t.setAmount(t.getAmount().negate());
            }
            positions.put(altCoin, positions.get(altCoin).add(t.getAmount()));
            positions.put("BTC", positions.get("BTC").add(btcAmount));
            logger.debug("Positions status: " + positions.toString());
        }
    }

    private BigDecimal applyFees(BigDecimal amount) {
        return amount.multiply(BigDecimal.ONE.subtract(new BigDecimal(0.002)));
    }

    public void saveState(Set<String> currencies, Map<String, SpotEntity> spots) {
        Timestamp timestamp = spots.get(spots.keySet().iterator().next()).getTimestamp();
        outputBuilder.append(timestamp);
        BigDecimal eq = getBTCEquivalent(positions, spots);
        outputBuilder.append(eq);
        outputBuilder.append(System.getProperty("line.separator"));
    }

    public void printOutput() {
        Path path = Paths.get("D:\\PublicHalf\\Documents\\Proyectos\\Git\\pluto-java\\assetmanager\\target\\simulation.csv");
        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            Files.writeString(path, outputBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, SpotEntity> getSpots(Set<String> currencies) {
        return null;
    }

    @Override
    public Map<String, BigDecimal> getPositions() {
        return positions;
    }
}
