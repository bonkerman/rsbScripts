package org.example.woodcutting;

import net.runelite.api.AnimationID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.rsb.script.Script;
import net.runelite.rsb.script.ScriptManifest;
import net.runelite.rsb.wrappers.RSObject;
import net.runelite.rsb.wrappers.RSTile;
import org.example.utils.ScriptUtils;
import org.example.woodcutting.model.WoodcuttingSpot;
import org.example.woodcutting.repo.WoodcuttingSpotHolder;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import static net.runelite.rsb.methods.MethodProvider.methods;

@ScriptManifest(authors = {"Markus"}, name = "WillowChopper", version = 0.3, description = "Does willows")
public class WillowChopper extends Script {

    private final List<Integer> woodcuttingAnimations = Arrays.asList(
            AnimationID.WOODCUTTING_ADAMANT,
            AnimationID.WOODCUTTING_RUNE,
            AnimationID.WOODCUTTING_STEEL,
            AnimationID.WOODCUTTING_BRONZE,
            AnimationID.WOODCUTTING_BLACK);
    private ScriptUtils scriptUtils;
    private WoodcuttingSpot currentSpot;

    @Override
    public boolean onStart() {
        scriptUtils = new ScriptUtils(this);
        return super.onStart();
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
        if (methods.client.getWidget(WidgetInfo.WORLD_MAP_VIEW) != null) {
            methods.inputManager.sendKey(KeyEvent.VK_ESCAPE);
            return 50;
        }
        if (!inventory.isOpen()) {
            inventory.open();
        }
        if (isWoodcutting()) {
            return random(800, 1200);
        }
        if (inventory.isFull()) {
            bank();
        } else {
            scriptUtils.antiban();
            chop();
        }
        return random(400, 600);
    }

    private void chop() {
        WoodcuttingSpot woodcuttingSpot = getCurrentSpot();
        if (!woodcuttingSpot.getSpot().contains(getMyPlayer().getLocation())) {
            walking.walkTo(woodcuttingSpot.getSpot().getRandomTile());
            sleep(800, 1200);
            sleep(3000, () -> !getMyPlayer().isMoving());
        } else {
            RSObject willowTree = objects.getNearest(woodcuttingSpot.getTreeName());
            if (willowTree != null && willowTree.doAction("Chop down")) {
                sleep(2000, () -> getMyPlayer().getAnimation() != -1);
                mouse.moveOffScreen();
            }
        }
    }

    private void bank() {
        if (!getCurrentSpot().getBank().contains(getMyPlayer().getLocation())) {
            RSTile randomTile = getCurrentSpot().getBank().getRandomTile();
            if (walking.walkTo(randomTile)) {
                sleep(800, 1200);
                sleep(3000, () -> !getMyPlayer().isMoving());
            }
        } else {
            if (bank.open() || bank.isDepositOpen()) {
                if (bank.depositAll()) {
                    bank.close();
                    scriptUtils.antiban();
                }
            }
        }

    }

    private WoodcuttingSpot getCurrentSpot() {
        if (currentSpot == null) {
            currentSpot = new WoodcuttingSpotHolder().findByRegion(getMyPlayer().getLocation().getWorldLocation().getRegionID()).orElseThrow();
        }
        return currentSpot;
    }

    private boolean isWoodcutting() {
        return woodcuttingAnimations.contains(getMyPlayer().getAnimation());
    }
}
