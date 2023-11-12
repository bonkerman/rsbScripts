package org.example.agility.repo;

import net.runelite.rsb.wrappers.RSArea;
import org.example.agility.model.AgilityCourse;
import org.example.agility.model.AgilityObstacle;

import java.util.ArrayList;
import java.util.List;

public class ObstacleHolder {

    private static final List<AgilityCourse> courses;

    static {
        courses = new ArrayList<>();
        List<AgilityObstacle> canifisCourse = new ArrayList<>();
        canifisCourse.add(new AgilityObstacle(new RSArea(3501, 3480, 3511, 3491, 0), 14843));
        canifisCourse.add(new AgilityObstacle(new RSArea(3504, 3491, 3510, 3498, 2), 14844));
        canifisCourse.add(new AgilityObstacle(new RSArea(3496, 3503, 3504, 3507, 2), 14845));
        canifisCourse.add(new AgilityObstacle(new RSArea(3485, 3498, 3494, 3506, 2), 14848));
        canifisCourse.add(new AgilityObstacle(new RSArea(3474, 3491, 3480, 3500, 3), 14846));
        canifisCourse.add(new AgilityObstacle(new RSArea(3476, 3480, 3485, 3488, 2), 14894));
        canifisCourse.add(new AgilityObstacle(new RSArea(3488, 3468, 3504, 3477, 3), 14847));
        canifisCourse.add(new AgilityObstacle(new RSArea(3508, 3473, 3517, 3484, 2), 14897));
        AgilityCourse agilityCourse = new AgilityCourse("Canifis rooftop course", 13878, canifisCourse, canifisCourse.get(0));
        List<AgilityObstacle> seersObstacles = new ArrayList<>();
        seersObstacles.add(new AgilityObstacle(new RSArea(2728, 3485, 2731, 3489, 0), 14927));
        seersObstacles.add(new AgilityObstacle(new RSArea(2721, 3490, 2730, 3497, 3), 14928));
        seersObstacles.add(new AgilityObstacle(new RSArea(2704, 3488, 2714, 3498, 2), 14932));
        seersObstacles.add(new AgilityObstacle(new RSArea(2709, 3476, 2716, 3482, 2), 14929));
        seersObstacles.add(new AgilityObstacle(new RSArea(2699, 3469, 2716, 3476, 3), 14930));
        seersObstacles.add(new AgilityObstacle(new RSArea(2696, 3460, 2703, 3466, 2), 14931));
        courses.add(agilityCourse);
        AgilityCourse seers = new AgilityCourse("seers rooftop course", 10806, seersObstacles, seersObstacles.get(0));
        courses.add(seers);
    }

    public static AgilityCourse findCourseByRegionId(int regionId) {
        return courses.stream().filter(agilityCourse -> agilityCourse.getRegionId() == regionId).findFirst().orElse(null);
    }
}
