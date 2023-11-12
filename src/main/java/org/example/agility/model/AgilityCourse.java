package org.example.agility.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class AgilityCourse {
    private final String courseName;
    private final int regionId;
    private final List<AgilityObstacle> obstacles;
    private final AgilityObstacle firstObstacle;
}
