package org.example.mining;

import com.google.common.collect.ImmutableSet;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.rsb.event.listener.PaintListener;
import net.runelite.rsb.script.Script;
import net.runelite.rsb.script.ScriptManifest;
import net.runelite.rsb.wrappers.RSArea;
import net.runelite.rsb.wrappers.RSObject;
import net.runelite.rsb.wrappers.RSTile;

import java.awt.*;
import java.util.Set;
import java.util.function.Predicate;

import static net.runelite.api.ItemID.PAYDIRT;
import static net.runelite.api.ObjectID.HOPPER_26674;
import static net.runelite.api.ObjectID.ORE_VEIN;
import static net.runelite.api.ObjectID.ORE_VEIN_26662;
import static net.runelite.api.ObjectID.ORE_VEIN_26663;
import static net.runelite.api.ObjectID.ORE_VEIN_26664;
import static net.runelite.rsb.methods.MethodProvider.methods;


@ScriptManifest(authors = {"Markus"}, name = "Motherlode miner", version = 1.1, description = "Mines in motherlode")
public class Motherloader extends Script implements PaintListener {
    public static final RSArea CLEANER_LOCATION = new RSArea(3749, 5668, 3751, 5674);
    public static final RSArea MINING_LOCATION = new RSArea(3736, 5645, 3758, 5655);
    public static final RSArea BANK_LOCATION = new RSArea(3756, 5662, 3758, 5666);
    public static final int INVENTORY_SIZE = 28;
    public static final RSArea MINING_LOCATION_DESTINATION = new RSArea(3743, 5647, 3749, 5658);
    private static final Set<Integer> MINE_SPOTS = ImmutableSet.of(ORE_VEIN, ORE_VEIN_26662, ORE_VEIN_26663, ORE_VEIN_26664);
    private static final int SACK_LARGE_SIZE = 162;
    private static final int SACK_SIZE = 81;
    final ScriptManifest properties = getClass().getAnnotation(
            ScriptManifest.class);
    private boolean emptySack = false;
    private RSObject wall = null;

    private static int getSackSize() {
        return methods.client.getVarbitValue(Varbits.SACK_NUMBER);
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
        if (!getMyPlayer().isIdle() || getMyPlayer().isLocalPlayerMoving()) {
            return 50;
        }


        if (isFullAndContainsPayDirt()) {
            cleanTheOres();
            return 50;
        }
        if (inventory.contains(rsItem -> rsItem.getName().toLowerCase().contains("ore") || rsItem.getName().toLowerCase().contains("coal"))) {
            depositAllToBank();
            return 50;
        }
        if (sackIsFull(getSackSize())) {
            emptySack = true;
        }
        if (emptySack) {
            emptySack();
            emptySack = getSackSize() != 0;
            return 50;
        }
        if (calc.isNextToArea(MINING_LOCATION, 5)) {
            mineRocks();
        } else {
            walking.walkTo(MINING_LOCATION_DESTINATION.getRandomTile());
        }

        return 50;
    }

    private boolean isFullAndContainsPayDirt() {
        return inventory.isFull() && inventory.contains(PAYDIRT);
    }

    private void mineRocks() {
        RSObject nearestWall = findNearestWall();
        if (nearestWall != null) {
            if (wall != null && calc.distanceBetween(nearestWall.getLocation(), wall.getLocation()) == 0) {
                long timeMillis = System.currentTimeMillis();
                while (System.currentTimeMillis() - timeMillis < 5000) {
                    sleep(500);
                    if (!getMyPlayer().isIdle()) {
                        return;
                    }
                }
            }
            wall = nearestWall;
            if (!calc.tileOnScreen(wall.getLocation())) {
                walkTile(wall.getLocation());
                sleep(10000, () -> !getMyPlayer().isLocalPlayerMoving());
            }
            wall.turnTo();
            if (wall.doAction("Mine")) {
                sleep(random(500, 3000));
                if (random(0, 50) > 20) {
                    mouse.moveOffScreen();
                }
            }
        } else {
            log("Could not find a wall to mine");
        }
    }

    private void emptySack() {
        RSObject sack = objects.getNearest(26688);
        if (sack.isOnScreen()) {
            sack.doAction("Search");
            sleep(random(200, 500));
            sleep(5000, () -> getMyPlayer().isIdle());
            int sackSize = getSackSize();
            sleep(5000, () -> sackSize != getSackSize());
        }
        if (calc.distanceBetween(getMyPlayer().getLocation(), sack.getLocation()) > 7) {
            log("Walking to sack");
            walkTile(sack.getLocation());
            log("arrived at sack");
            sleep(5000, () -> getMyPlayer().isIdle());
        }
    }

    private void depositAllToBank() {
        RSObject bankBooth = objects.getNearest(25937);
        if (!calc.isNextToArea(BANK_LOCATION, 4) && !bankBooth.isOnScreen()) {
            walkTile(BANK_LOCATION.getRandomTile());
            sleep(5000, () -> calc.isNextToArea(BANK_LOCATION, 4));
        }
        if (!calc.tileOnScreen(bankBooth.getLocation())) {
            camera.turnTo(bankBooth);
            log("Turning to bank");
        }
        if (bankBooth.doClick()) {
            sleep(5000, () -> bank.isDepositOpen());
            if (bank.depositAll()) {
                log("Depositing");
                sleep(5000, () -> inventory.isEmpty());
                log("Closing bank");
                bank.close();
            }
        }

    }

    private boolean sackIsFull(int curSackSize) {
        boolean sackUpgraded = methods.client.getVarbitValue(Varbits.SACK_UPGRADED) == 1;
        int maxSackSize = sackUpgraded ? SACK_LARGE_SIZE : SACK_SIZE;
        int spaceLeft = maxSackSize - curSackSize;
        return spaceLeft <= 0;
    }

    private void cleanTheOres() {
        RSObject hopper = objects.getNearest(HOPPER_26674);
        if (hopper == null) {
            return;
        }
        if (calc.pathLengthTo(hopper.getLocation(), true) > 7) {
            if (walkTile(CLEANER_LOCATION.getRandomTile())) {
                sleep(2000, () -> getMyPlayer().isLocalPlayerMoving());
                sleep(5000, () -> getMyPlayer().isIdle());
            }
        }
        int paydirtDeposited = inventory.getItems(PAYDIRT).length;
        if (hopper.doAction("Deposit")) {
            sleep(5000, () -> !inventory.contains(PAYDIRT));
            if (sackIsFull(getSackSize() + paydirtDeposited)) {
                emptySack = true;
            }
        }
    }

    private boolean walkTile(final RSTile tile) {
        if (!(calc.distanceTo(walking.getDestination()) <= random(7, 10))) {
            if (getMyPlayer().isLocalPlayerMoving()) {
                return false;
            }
        }
        final Point screen = calc.tileToScreen(tile);
        if (calc.pointOnScreen(screen)) {
            if (getMyPlayer().isLocalPlayerMoving()) {
                return false;
            }
            mouse.move(screen, random(-3, 4), random(-3, 4));
            return tile.doClick();
        } else {
            sleep(random(500, 750));
            return walking.walkTo(tile);
        }
    }

    private RSObject findNearestWall() {
        Predicate<RSObject> wallPredicate = rsObject -> MINE_SPOTS.contains(rsObject.getID());
        boolean upstairs = isUpstairs(getMyPlayer().getLocation().getLocalLocation());
        return objects.getNearest(wallPredicate
                .and(rsObject -> upstairs == isUpstairs(rsObject.getLocation().getLocalLocation()))
                .and(rsObject -> calc.canReach(rsObject.getLocation(), true))
                .and(rsObject -> MINING_LOCATION.contains(rsObject.getLocation()))
        );
    }

    boolean isUpstairs(LocalPoint localPoint) {
        return Perspective.getTileHeight(methods.client.wrappedClient, localPoint, 0) < -490;
    }

    @Override
    public void onFinish() {
        getBot().getEventManager().removeListener(this);
    }


    @Override
    public void onRepaint(final Graphics g) {
        g.setColor(Color.WHITE);

        g.drawString("ffeawfafewafwaweff aefwefafwafef", 561, 220);
        if (wall != null) {
            g.drawString("x: %d y: %d".formatted(wall.getLocation().getX(), wall.getLocation().getY()), 561, 250);

        }
    }

    @Override
    public boolean onStart() {
        return true;
    }

    // *******************************************************//
    // ON START
    // *******************************************************//

}