package com.example.pluto.exchanges;

import com.example.pluto.entities.SnapshotTO;
import com.example.pluto.entities.SpotTO;

public interface ExchangeService {

    SnapshotTO getSnapshot(String instrument);

    SnapshotTO getSnapshot(String instrument, String time);

    SpotTO getSpot(String instrument);

    SpotTO getSpot(String instrument, String time);
}
