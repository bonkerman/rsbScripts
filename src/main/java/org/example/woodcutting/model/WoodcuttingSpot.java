package org.example.woodcutting.model;

import lombok.Data;
import net.runelite.rsb.wrappers.RSArea;

@Data
public class WoodcuttingSpot {
    private final RSArea bank;
    private final RSArea spot;
    private final int region;
    private final String treeName;
}
