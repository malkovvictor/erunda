package ru.victormalkov.android.axolotl;

import java.util.ArrayList;
import java.util.List;

class Question {
    int id;
    String text;
    List<String> answers = new ArrayList<>();
    int right = -1;
}
