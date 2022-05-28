import com.example.pluto.entities.SpotEntity;
import com.example.pluto.entities.TradeTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;

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
            BigDecimal btc = BigDecimal.ONE;
            Map<String, BigDecimal> qBid = getUpperBounds(btc, spots, weights);
            Map<String, BigDecimal> qAsk = getLowerBounds(btc, spots, weights);
            List<TradeTO> trading = getSellingTrades(qBid, positions, threshold);
            trading.addAll(getBuyingTrades(qAsk, positions, threshold));
            updatePositions(trading, spots);

            saveState(currencies, spots);
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
        Set<String> pairs = new HashSet<>(currencies.size());
        currencies.forEach(c -> pairs.add(c+"BTC"));
        return RESTClient.getSpotsHist(currencies);
    }

    public void updatePositions(List<TradeTO> trading, Map<String, SpotEntity> spots) {
        for (TradeTO t : trading) {
            int side = BigDecimal.ZERO.compareTo(t.getAmount()); // Buy = -1;
            BigDecimal applyingSpot = side == -1 ? spots.get(t.getPair()).getOffer() : spots.get(t.getPair()).getBid();
            BigDecimal btcAmount = t.getAmount().multiply(applyingSpot);
            if (side == -1) {
                t.setAmount(applyFees(t.getAmount()));
            } else {
                btcAmount = applyFees(btcAmount);
            }
            String altCoin = t.getPair().replace("BTC", "");
            positions.put(altCoin, positions.get(altCoin).add(t.getAmount()));
            positions.put("BTC", positions.get("BTC").add(btcAmount));
        }
    }

    private BigDecimal applyFees(BigDecimal amount) {
        return amount.multiply(BigDecimal.ONE.subtract(new BigDecimal(0.002)));
    }

    public void saveState(Set<String> currencies, Map<String, SpotEntity> spots) {
        Timestamp timestamp = spots.get(0).getTimestamp();
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
