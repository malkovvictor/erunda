package ru.heritagepw.android.erunda;

import java.util.ArrayList;
import java.util.List;

class Question {
    enum Type {
        RARE_WORD("редкое слово");

        public String getName() {
            return rusName;
        }

        private String rusName;
        private Type(String name) {
            rusName = name;
        }
    }

    Type type;
    String text;
    List<String> answers = new ArrayList<>();
    int right = -1;
}
