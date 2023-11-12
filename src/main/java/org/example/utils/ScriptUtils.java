package org.example.utils;

import net.runelite.rsb.methods.Methods;
import net.runelite.rsb.script.Script;

import java.util.function.BooleanSupplier;

public class ScriptUtils extends Methods {

    public ScriptUtils(Script script) {
        init(script.ctx);
    }

    public void antiban() {
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

    public boolean sleepUntil(BooleanSupplier condition, int millisToTest) {
        return sleepUntil(condition, millisToTest, 50);
    }

    public boolean sleepUntil(BooleanSupplier condition, int millisToTest, int interval) {
        boolean conditionResult = condition.getAsBoolean();
        if (conditionResult) {
            return true;
        }
        long currentTimeMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - currentTimeMillis < millisToTest) {
            if (condition.getAsBoolean()) {
                return true;
            }
            sleep(interval);
        }
        return false;
    }

    public void moveOffScreen() {
        int random1 = random(0, 100);
        if(random1<90){
            mouse.moveOffScreen();
        }
    }
}
