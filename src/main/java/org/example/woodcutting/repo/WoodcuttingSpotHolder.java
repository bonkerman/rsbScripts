package org.example.woodcutting.repo;

import net.runelite.rsb.wrappers.RSArea;
import org.example.woodcutting.model.WoodcuttingSpot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WoodcuttingSpotHolder {
    private final List<WoodcuttingSpot> spots;

    public WoodcuttingSpotHolder() {
        spots = new ArrayList<>();
        WoodcuttingSpot draynor = new WoodcuttingSpot(new RSArea(3092, 3240, 3095, 3246, 0), new RSArea(3080, 3226, 3090, 3238, 0), 12338, "Willow tree");
        spots.add(draynor);
    }

    public Optional<WoodcuttingSpot> findByRegion(int region){
        return spots.stream().filter(woodcuttingSpot -> woodcuttingSpot.getRegion()==region).findFirst();
    }
}
