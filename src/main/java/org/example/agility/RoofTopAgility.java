package org.example.agility;

import net.runelite.api.Skill;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.rsb.event.listener.PaintListener;
import net.runelite.rsb.internal.MouseHandler;
import net.runelite.rsb.script.Script;
import net.runelite.rsb.script.ScriptManifest;
import net.runelite.rsb.wrappers.RSArea;
import net.runelite.rsb.wrappers.RSGroundItem;
import net.runelite.rsb.wrappers.RSObject;
import net.runelite.rsb.wrappers.RSTile;
import org.example.agility.model.AgilityCourse;
import org.example.agility.model.AgilityObstacle;
import org.example.agility.repo.ObstacleHolder;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Optional;

import static net.runelite.api.ItemID.MARK_OF_GRACE;
import static net.runelite.rsb.methods.MethodProvider.methods;


@ScriptManifest(authors = {"Markus"}, name = "Agility", version = 1.1, description = "does agility")
public class RoofTopAgility extends Script implements PaintListener {
    private AgilityCourse course;


    private static boolean clickOnItem(RSObject nearestObstacle) {
        int random = random(0, 100);
        if (random > 70) {
            nearestObstacle.doClick();
            sleep(random(15, 350));
        }
        if (random > 90) {
            nearestObstacle.doClick();
            sleep(random(15, 350));
        }
        return nearestObstacle.doClick();
    }

    private static boolean clickOnItem(RSGroundItem item, String action) {
        int random = random(0, 100);
        if (random > 70) {
            item.doAction(action);
            sleep(random(15, 350));
        }
        if (random > 90) {
            item.doClick();
            sleep(random(15, 350));
        }
        return item.doAction(action);
    }

    @Override
    public int loop() {
        if (!game.isLoggedIn()) {
            return 50;
        }
        if (interfaces.canContinue()) {
            interfaces.clickContinue(true);
            return 50;
        }
        if (prayer.isQuickPrayerOn()) {
            prayer.activateQuickPrayer(false);
        }
        if (!getMyPlayer().isIdle() || getMyPlayer().isMoving()) {
            return 50;
        }
        if (methods.client.getWidget(WidgetInfo.WORLD_MAP_VIEW) != null) {
            methods.inputManager.sendKey(KeyEvent.VK_ESCAPE);
            return 50;
        }
        if (course == null) {
            course = ObstacleHolder.findCourseByRegionId(getMyPlayer().getLocation().getWorldLocation().getRegionID());
            if (course == null) {
                return -1;
            }
        }
        if (bank.close()) {
            return 50;
        }
        if (shouldEat()) {
            return -1;
        }
        antiban();
        RSTile myLocation = getMyPlayer().getLocation();
        Optional<AgilityObstacle> currentObstacle = course.getObstacles().stream().filter(agilityObstacle -> agilityObstacle.getRsArea().contains(myLocation)).findFirst();
        if (currentObstacle.isPresent()) {
            AgilityObstacle obstacle = currentObstacle.get();
            return interactWithObstacle(obstacle);
        } else {
            RSTile tile = course.getFirstObstacle().getRsArea().getRandomTile();
            if (tile.getPlane() == getMyPlayer().getLocation().getPlane()) {
                if (calc.isNextToArea(course.getFirstObstacle().getRsArea(), 10)) {
                    return interactWithObstacle(course.getFirstObstacle());
                }
                walking.walkTileMM(tile, 2, 2);
                sleep(300, 500);
                sleep(2000, 4000, () -> !getMyPlayer().isMoving());
            }
        }

        return 50;
    }

    private Integer interactWithObstacle(AgilityObstacle obstacle) {
        RSGroundItem markOfGrace = getMarkOfGrace(obstacle.getRsArea());
        if (markOfGrace != null) {
            if (clickOnItem(markOfGrace, "Take")) {
                sleep(2000, () -> getMarkOfGrace(obstacle.getRsArea()) == null);
            }
            return 50;
        }
        RSObject nearestObstacle = objects.getNearest(obstacle.getObstacleId());
        if (nearestObstacle == null) {
            return 50;
        }
        AgilityObstacle nextObstacle = getNextObstacle(obstacle);
        if (clickOnItem(nearestObstacle)) {
            if (nextObstacle != null) {
                sleep(7000, () -> nextObstacle.getRsArea().contains(getMyPlayer().getLocation()));
            } else {
                sleep(5000, () -> !obstacle.getRsArea().contains(getMyPlayer().getLocation()));
            }
        }
        return 50;
    }

    private AgilityObstacle getNextObstacle(AgilityObstacle obstacle) {
        int i = course.getObstacles().indexOf(obstacle);
        return i == course.getObstacles().size() - 1 ? null : course.getObstacles().get(i + 1);
    }

    private boolean shouldEat() {
        return skills.getCurrentLevel(Skill.HITPOINTS) < 15;
    }

    private void antiban() {
        int possibility = random(0, 100);
        if (possibility == 98) {
            mouse.moveOffScreen();
            int random = random(30000, 60000);
            log("Sleeping for %d".formatted(random));
            sleep(random);
            return;
        }
        if (possibility == 96) {
            mouse.moveOffScreen();
            int random = random(10000, 30000);
            log("Sleeping for %d".formatted(random));
            sleep(random);
        }
    }

    private RSGroundItem getMarkOfGrace(RSArea area) {
        return groundItems.getNearest(item -> item.getItem().getID() == MARK_OF_GRACE && area.contains(item.getLocation()));
    }

    @Override
    public void onFinish() {
        getBot().getEventManager().removeListener(this);
        mouse.setSpeed(MouseHandler.DEFAULT_MOUSE_SPEED);
    }


    @Override
    public void onRepaint(final Graphics g) {
        g.setColor(Color.WHITE);

        g.drawString("ffeawfafewafwaweff aefwefafwafef", 561, 220);
    }

    @Override
    public boolean onStart() {
        mouse.setSpeed(8);
        return true;
    }

}