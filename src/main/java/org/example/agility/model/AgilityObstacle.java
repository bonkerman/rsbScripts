package org.example.agility.model;

import lombok.Data;
import net.runelite.rsb.wrappers.RSArea;

@Data
public class AgilityObstacle {
    final RSArea rsArea;
    final int obstacleId;

    public AgilityObstacle(RSArea rsArea, int obstacleId) {
        this.rsArea = rsArea;
        this.obstacleId = obstacleId;
    }
}
