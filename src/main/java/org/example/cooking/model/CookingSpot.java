package org.example.cooking.model;

import lombok.Data;
import net.runelite.rsb.wrappers.RSArea;

@Data
public class CookingSpot {
    private final int region;
    private final RSArea bank;
    private final RSArea spot;
    private final int cookerId;

}
