package org.example.thieving;

import lombok.Setter;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;
import net.runelite.rsb.script.Script;
import net.runelite.rsb.script.ScriptManifest;
import net.runelite.rsb.wrappers.RSNPC;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ScriptManifest(authors = {"Markus"}, name = "Blackjacker", version = 0.3, description = "Does blackjacking")
public class BlackJack extends Script {

    public static final String KNOCK_OUT = "Knock-Out";
    public static final String PICKPOCKET = "Pickpocket";
    public static final int KNOCKED_OUT_ANIMATION = 838;
    public static final String ARGHH_MY_HEAD = "Arghh my head.";
    @Setter
    private ScriptState state = ScriptState.PICKPOCKET;

    private static Optional<MenuEntry> getEntryWithName(List<MenuEntry> menuEntries, String menuName) {
        return menuEntries.stream()
                .filter(menuEntry -> Text.removeTags(menuEntry.getOption()).equalsIgnoreCase(menuName))
                .findFirst();
    }

    private ScriptState getState(RSNPC bandit) {
        String message = bandit.getMessage();
        if (ARGHH_MY_HEAD.equals(message)) {
            if (healthBelow20()) {
                return ScriptState.EAT;
            }
            return ScriptState.KNOCKED_OUT;
        }
        if ((message != null && message.toLowerCase().contains("zzz")) || bandit.getAnimation() == KNOCKED_OUT_ANIMATION) {
            return ScriptState.PICKPOCKET;
        }
        return null;
    }

    private boolean healthBelow20() {

        int realLevel = skills.getRealLevel(Skill.HITPOINTS);
        log(realLevel);
        return realLevel < 20;
    }

    @Override
    public int loop() {
        log(state);
        return 500;
    }

    @Subscribe
    public void onStatChanged(StatChanged statChanged) {
        log(statChanged.toString());
        if (statChanged.getSkill() != Skill.THIEVING) {
            return;
        }
        if (statChanged.getXp() < 20) {
            setState(ScriptState.KNOCKED_OUT);
        } else {
            if (state == ScriptState.KNOCKED_OUT) {
                setState(ScriptState.PICKPOCKET);
            } else if (state == ScriptState.PICKPOCKET) {
                setState(ScriptState.KNOCK_OUT);
            }
        }
    }

    @Subscribe
    public void onPostMenuSort(PostMenuSort postMenuSort) {
        // The menu is not rebuilt when it is open, so don't swap or else it will
        // repeatedly swap entries
        if (getBot().client.isMenuOpen()) {
            return;
        }

        List<MenuEntry> menuEntries = Arrays.asList(getBot().client.getMenuEntries());

        boolean isKnockable = menuEntries.stream().anyMatch(menuEntry -> Text.removeTags(menuEntry.getOption()).equalsIgnoreCase("knock-out"));
        if (isKnockable) {
            Optional<NPC> npc = getEntryWithName(menuEntries, "knock-out").map(MenuEntry::getNpc);
            int animation = npc.map(NPC::getAnimation)
                    .orElse(-1);
            Boolean isSleepy = npc.map(NPC::getOverheadText).map(text -> text.contains("zzz")).orElse(false);
            Optional<MenuEntry> entryToSwap;
            if (animation == KNOCKED_OUT_ANIMATION || Boolean.TRUE.equals(isSleepy)) {
                entryToSwap = getEntryWithName(menuEntries, "pickpocket");
            } else {
                entryToSwap = getEntryWithName(menuEntries, "knock-out");
            }
            if (entryToSwap.isPresent()) {
                MenuEntry menuEntry = entryToSwap.get();
                MenuEntry firstMenuEntry = menuEntries.get(menuEntries.size() - 1);
                swapEntries(getBot().client.getMenuEntries(), menuEntry, firstMenuEntry);
            }
        }
    }

    private void swapEntries(MenuEntry[] menuEntries, MenuEntry menuEntry, MenuEntry firstMenuEntry) {
        if (menuEntry == firstMenuEntry) {
            return;
        }

        int index1 = indexOfEntry(menuEntries, firstMenuEntry);
        int index2 = indexOfEntry(menuEntries, menuEntry);
        if (index1 == -1 || index2 == -1) {
            return;
        }
        menuEntries[index1] = menuEntry;
        menuEntries[index2] = firstMenuEntry;

        // Item op4 and op5 are CC_OP_LOW_PRIORITY so they get added underneath Use,
        // but this also makes them right-click only. Change them to CC_OP to avoid this.
        if (menuEntry.getType() == MenuAction.CC_OP_LOW_PRIORITY) {
            menuEntry.setType(MenuAction.CC_OP);
        }
        if (firstMenuEntry.getType() == MenuAction.CC_OP_LOW_PRIORITY) {
            firstMenuEntry.setType(MenuAction.CC_OP);
        }

        getBot().client.setMenuEntries(menuEntries);
    }

    private int indexOfEntry(Object[] menuEntries, Object menuEntryToFind) {
        for (int i = 0; i < menuEntries.length; i++) {
            if (menuEntries[i] == menuEntryToFind) {
                return i;
            }
        }
        return -1;
    }

    public enum ScriptState {
        KNOCKED_OUT,
        PICKPOCKET,
        KNOCK_OUT, EAT
    }
}
