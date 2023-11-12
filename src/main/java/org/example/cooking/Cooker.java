package org.example.cooking;

import net.runelite.api.AnimationID;
import net.runelite.api.Skill;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.rsb.methods.Bank;
import net.runelite.rsb.script.Script;
import net.runelite.rsb.script.ScriptManifest;
import net.runelite.rsb.wrappers.RSArea;
import net.runelite.rsb.wrappers.RSObject;
import org.example.cooking.model.CookingItem;
import org.example.cooking.model.CookingSpot;
import org.example.cooking.repo.CookingItemHolder;
import org.example.cooking.repo.CookingSpotHolder;
import org.example.utils.ScriptUtils;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import static net.runelite.rsb.methods.MethodProvider.methods;

@ScriptManifest(authors = {"Markus"}, name = "Cooker", version = 1.1, description = "does Cooking")

public class Cooker extends Script {
    private final List<Integer> cookAnimations = Arrays.asList(AnimationID.COOKING_FIRE, AnimationID.COOKING_RANGE);

    private ScriptUtils scriptUtils;
    private CookingSpot currentSpot;
    private CookingItem cookingItem;

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
        if (inventory.isEmpty()) {
            return bank();
        }
        if (inventory.getItems(rsItem -> rsItem.getName().toLowerCase().contains("raw")).isEmpty()) {
            scriptUtils.antiban();
            return bank();
        } else {
            return cook();
        }
    }

    private CookingSpot getCurrentSpot() {
        if (currentSpot == null) {
            currentSpot = new CookingSpotHolder().findByRegion(getMyPlayer().getLocation().getWorldLocation().getRegionID()).orElseThrow();
        }
        return currentSpot;
    }

    private int cook() {
        int cookerId = getCurrentSpot().getCookerId();
        RSObject cooker = objects.getNearest(cookerId);

        if (interfaces.getComponent(270, 1).isValid()) {
            sleep(200, 600);
            keyboard.sendText(" ", false);
            scriptUtils.sleepUntil(() -> !interfaces.getComponent(270, 1).isValid(), 3000, 300);
            scriptUtils.moveOffScreen();
        } else if (cooker != null && cooker.isOnScreen()) {
            if (scriptUtils.sleepUntil(() -> cookAnimations.contains(getMyPlayer().getAnimation()), 3000)) {
                return random(1000, 2000);
            }
            cooker.doAction("Cook");
            scriptUtils.sleepUntil(() -> interfaces.getComponent(270, 1).isValid(), 7000, 300);
        } else {
            walking.walkTo(getCurrentSpot().getSpot().getRandomTile());
        }
        return random(300, 600);
    }

    private int bank() {
        RSArea bankArea = getCurrentSpot().getBank();
        if (!bankArea.contains(getMyPlayer().getLocation())) {
            walking.walkTo(bankArea.getRandomTile());
            sleep(7000, 13000, () -> bankArea.contains(getMyPlayer().getLocation()));
        } else {
            if (bank.open()) {
                if (!inventory.isEmpty()) {
                    bank.depositAll();
                }
                CookingItem itemToCook = getCookingItem();
                if (bank.setWithdrawModeTo(Bank.WithdrawMode.ALL) && bank.withdrawAll(itemToCook.getId())) {
                    bank.close();
                }
            }
        }
        return random(300, 600);
    }

    private CookingItem getCookingItem() {
        if (cookingItem == null || bank.getCount(cookingItem.getId()) < 28) {
            int currentCookingLevel = skills.getCurrentLevel(Skill.COOKING);
            cookingItem = new CookingItemHolder().getCookingItems().stream()
                    .filter(c -> c.getLevel() <= currentCookingLevel)
                    .filter(c -> bank.getCount(c.getId()) >= 28).findFirst().orElseThrow();
        }
        return cookingItem;
    }
}
