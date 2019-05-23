package ru.victormalkov.android.axolotl;

import android.graphics.drawable.Drawable;

public class RoadView {
    Drawable image;
    String credits;
    String symbol;

    public RoadView(Drawable image, String credits) {
        this.image = image;
        if (credits == null || credits.isEmpty()) {
            this.credits = "";
        } else {
            this.credits = String.format("Image by %s", credits);
        }
        this.symbol = symbol;
    }
}
