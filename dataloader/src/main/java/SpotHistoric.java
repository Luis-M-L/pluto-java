import com.pluto.PlutoConstants;
import com.pluto.bitfinex.parsers.BitfinexParser;
import com.pluto.entities.SpotEntity;
import com.pluto.exchanges.ExchangeParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static com.pluto.PlutoConstants.HEADER_NAME_CONTENT_TYPE;
import static com.pluto.PlutoConstants.HEADER_VALUE_APPLICATION_JSON;

public class SpotHistoric implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpotHistoric.class);

    private static Properties properties;
    private static String DATA_DIRECTORY;

    private ExchangeParser parser;

    @Override
    public void run() {
        initialize();
        File[] inputFilenames = getInputFiles();
        List<SpotEntity> spots = new LinkedList<>();
        for (File f : inputFilenames) {
            spots.addAll(loadFile(f));
        }
        save(spots);
    }

    private void initialize() {
        LOGGER.info("Initializing...");
        parser = new BitfinexParser();
        properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("dataloader.properties"));
        } catch (IOException e) {
            LOGGER.error("Error while loading properties file");
        }
        DATA_DIRECTORY = properties.getProperty("data.store.directory");
    }

    private boolean callService(String instrument, SpotEntity spot) {
        String url = new StringBuilder("http://").append(PlutoConstants.Socket.BITFINEX.value()).append(PlutoConstants.Path.BITFINEX_SPOT.value()).append(instrument).toString();
        ObjectMapper mapper = new ObjectMapper();
        boolean success = false;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header(HEADER_NAME_CONTENT_TYPE, HEADER_VALUE_APPLICATION_JSON)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(mapper.writeValueAsBytes(spot)))
                    .build();
            HttpResponse<String> response = null;
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            success = Boolean.valueOf(response.body());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return success;
    }

    private void save(List<SpotEntity> spots) {
        spots.forEach(s -> {
            int i = 0;
            while (i < 5) {
                LOGGER.debug("Saving spot (" + i + ") " + s.toString());
                i = callService(s.getInstrument(), s) ? 5 : i + 1;
            }
        });
    }

    private List<SpotEntity> loadFile(File f) {
        List<SpotEntity> spots = new LinkedList<>();
        if (f.canRead()) {
            try {
                FileInputStream input = new FileInputStream(f);
                byte[] bytes = input.readAllBytes();
                String data = new String(bytes);
                spots = parser.parseSpots(data);
            } catch (IOException e) {
                LOGGER.error("Error while reading file " + f.getName());
            }
        } else {
            LOGGER.error("Tried to read an unexistent file");
        }
        return spots;
    }

    private File[] getInputFiles() {
        File directory = new File(DATA_DIRECTORY);
        File[] files = new File[0];
        if (directory.exists() && directory.isDirectory()) {
            files = directory.listFiles();
        }
        return files;
    }
}
