package org.example.cooking.repo;

import lombok.Getter;
import org.example.cooking.model.CookingItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class CookingItemHolder {
    @Getter
    private final List<CookingItem> cookingItems;

    public CookingItemHolder() {
        this.cookingItems = new ArrayList<>();
        CookingItem trout = new CookingItem(335, 15);
        CookingItem salmon = new CookingItem(331, 25);
        CookingItem lobster = new CookingItem(377, 40);
        CookingItem swordfish = new CookingItem(371, 45);
        CookingItem tuna = new CookingItem(359, 30);
        cookingItems.add(trout);
        cookingItems.addAll(Arrays.asList(tuna, trout, salmon, swordfish, lobster));
        cookingItems.sort(Comparator.comparingInt(CookingItem::getLevel));
    }
}
