package org.example.cooking.repo;

import net.runelite.rsb.wrappers.RSArea;
import org.example.cooking.model.CookingSpot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CookingSpotHolder {
    private final List<CookingSpot> cookingSpots;

    public CookingSpotHolder() {
        this.cookingSpots = new ArrayList<>();
        CookingSpot draynorSpot = new CookingSpot(12338,new RSArea(3092, 3240, 3095, 3246, 0), new RSArea(3094, 3235, 3099, 3238, 0), 43475);
        cookingSpots.add(draynorSpot);
    }


    public Optional<CookingSpot> findByRegion(int region) {
        return cookingSpots.stream().filter(cookingSpot -> cookingSpot.getRegion() == region).findFirst();
    }
}
