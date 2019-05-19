package ru.heritagepw.android.axolotl;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RoadActivity extends AppCompatActivity {
    private int currentQuestionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentQuestionId = savedInstanceState.getInt("current_question", -1);
        } else if (getIntent()!= null){
            currentQuestionId = getIntent().getIntExtra("current_question", -1);
        }

        setContentView(R.layout.activity_road);
        TravelController tc = new TravelController(getApplicationContext());
        TextView v = findViewById(R.id.tempRoadText);

        v.setText(String.format("Едем из города %1$s в город %2$s. Приедем на отметке %3$d!", tc.getCityName(tc.getSource()), tc.getCityName(tc.getDest()), tc.getDestScore()));

        Button b = findViewById(R.id.roadbutton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RoadActivity.this, QuestionActivity.class);
                i.putExtra("current_question", currentQuestionId);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_question", currentQuestionId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentQuestionId = savedInstanceState.getInt("current_question", -1);
    }
}
