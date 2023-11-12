package org.example.thieving;

import net.runelite.api.Skill;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.rsb.internal.MouseHandler;
import net.runelite.rsb.script.Script;
import net.runelite.rsb.script.ScriptManifest;
import net.runelite.rsb.wrappers.RSArea;
import net.runelite.rsb.wrappers.RSItem;
import net.runelite.rsb.wrappers.RSNPC;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.runelite.rsb.methods.MethodProvider.methods;

@ScriptManifest(authors = {"Markus"}, name = "MasterFarmer", version = 0.3, description = "Does blackjacking")
public class MasterFarmer extends Script {

    private static final int STUNNED_GRAPHIC = 245;
    private static final int FARMER_ID = 5730;
    private static final int WITHDRAW_COUNT = 7;
    private static final RSArea BANK_AREA = new RSArea(3092, 3240, 3095, 3246, 0);
    private int foodToEat = 333;


    private boolean healthBelow(int health) {
        int realLevel = skills.getCurrentLevel(Skill.HITPOINTS);
        return realLevel < health;
    }


    private ScriptState getState() {

        if (getMyPlayer().getGraphic() == STUNNED_GRAPHIC) {
            return ScriptState.STUNNED;
        }
        if (getMyPlayer().isInCombat()) {
            return ScriptState.FIGHTING;
        }
        if (inventory.isFull() || getItemsToEat().isEmpty()) {
            return ScriptState.BANKING;
        }
        return ScriptState.PICKPOCKETING;
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
        ScriptState scriptState = getState();
        RSNPC farmer = npcs.getNearest(FARMER_ID);
        return switch (scriptState) {
            case STUNNED -> doActionsWhileStunned(farmer);
            case BANKING -> bank();
            case PICKPOCKETING -> pickpocket(farmer);
            case FIGHTING -> eatIfNeeded();
        };
    }

    private int eatIfNeeded() {
        if (healthBelow(10)) {
            eat();
        }

        return 50;
    }

    private int pickpocket(RSNPC farmer) {
        if (farmer != null) {
            if (!farmer.isOnScreen()) {
                if (calc.distanceBetween(farmer.getPosition(), getMyPlayer().getPosition()) > 10) {
                    walking.walkTileMM(farmer.getPosition());
                } else {
                    camera.turnTo(farmer);
                }
            } else if (farmer.doAction("Pickpocket")) {
                sleep(3000, () -> getMyPlayer().getAnimation() != -1);
                return random(300, 500);
            }
        }
        return 50;
    }

    private int doActionsWhileStunned(RSNPC farmer) {
        if (healthBelow(10)) {
            eat();
        }
        if (!farmer.isOnScreen()) {
            if (calc.distanceBetween(farmer.getPosition(), getMyPlayer().getPosition()) > 10) {
                walking.walkTileMM(farmer.getPosition());
            } else {
                camera.turnTo(farmer);
            }
            return 50;
        } else {
            farmer.doHover();
        }
        return 50;
    }

    private int bank() {
        if (!BANK_AREA.contains(getMyPlayer().getLocation())) {
            walking.walkTileMM(BANK_AREA.getRandomTile());
            sleep(500, 1000);
            sleep(2000, () -> getMyPlayer().isIdle());
        }
        if (bank.open()) {
            if (!inventory.isEmpty()) {
                bank.depositAll();
            }
            if (bank.getCount(foodToEat) < WITHDRAW_COUNT) {
                return -1;
            }
            if (bank.withdraw(foodToEat, WITHDRAW_COUNT)) {
                int inventoryCount = inventory.getCount(foodToEat);
                sleep(2000, () -> inventory.getCount(foodToEat) != inventoryCount);
                bank.close();
            }
        }
        return random(100, 200);
    }

    private void eat() {
        if (!inventory.isOpen()) {
            inventory.open();
        }
        List<RSItem> itemsToEat = getItemsToEat();
        if (itemsToEat.isEmpty()) {
            return;
        }
        Collections.shuffle(itemsToEat);
        RSItem rsItem = itemsToEat.get(0);
        if (rsItem != null && rsItem.doAction("Eat")) {
            int count = inventory.getCount(rsItem.getID());
            sleep(2000, () -> inventory.getCount(rsItem.getID()) != count);
        }
    }

    private List<RSItem> getItemsToEat() {
        return new ArrayList<>(inventory.getItems(item -> Arrays.stream(item.getInventoryActions()).toList().contains("Eat")));
    }

    @Override
    public void onFinish() {
        mouse.setSpeed(MouseHandler.DEFAULT_MOUSE_SPEED);
        super.onFinish();
    }

    @Override
    public boolean onStart() {

        if (game.isLoggedIn()) {
            List<RSItem> itemsToEat = getItemsToEat();
            if (!itemsToEat.isEmpty()) {
                foodToEat = itemsToEat.get(0).getID();
            }
        }
        mouse.setSpeed(4);
        return super.onStart();
    }

    public enum ScriptState {
        STUNNED,
        FIGHTING,
        BANKING,
        PICKPOCKETING
    }
}
