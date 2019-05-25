package ru.victormalkov.android.axolotl;

import android.graphics.drawable.Drawable;

public class RoadImage {
    Drawable image;
    String credits;
    String symbol;

    public RoadImage(Drawable image, String credits) {
        this.image = image;
        if (credits == null || credits.isEmpty()) {
            this.credits = "";
        } else {
            if (credits.startsWith("Image") || credits.startsWith("Изображение")) {
                this.credits = credits;
            } else {
                this.credits = String.format("Image by %s", credits);
            }
        }
        this.symbol = symbol;
    }
}
