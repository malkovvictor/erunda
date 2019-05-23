package ru.victormalkov.android.axolotl;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class RoadActivity extends AppCompatActivity {
    private int currentQuestionId;
    private TravelController tc;
    private ArrayList<Integer> answers = null;
    private boolean hinted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentQuestionId = savedInstanceState.getInt("current_question", -1);
            if (currentQuestionId >= 0) {
                answers = savedInstanceState.getIntegerArrayList("answers");
                hinted = savedInstanceState.getBoolean("hinted");
            }
        } else if (getIntent()!= null){
            currentQuestionId = getIntent().getIntExtra("current_question", -1);
            if (currentQuestionId >= 0) {
                answers = getIntent().getIntegerArrayListExtra("answers");
                hinted = getIntent().getBooleanExtra("hinted", false);
            }
        }

        setContentView(R.layout.activity_road);
        tc = new TravelController(getApplicationContext());
        ImageView iv = findViewById(R.id.roadView);
        RoadView rv = tc.getRoadView();
        iv.setImageDrawable(rv.image);
        ((TextView)findViewById(R.id.roadPhotoCopyright)).setText(rv.credits);
        int star = getApplicationContext().getSharedPreferences(getApplicationContext().getPackageName() + ".score", Context.MODE_PRIVATE).getInt("stars", tc.getSourceScore());
        ((TextView)findViewById(R.id.scoreTextView2)).setText(Integer.toString(star));

        ((TextView)findViewById(R.id.cityFrom)).setText(String.format(
                "%s\n%d%s",
                tc.getCityName(tc.getSource()),
                tc.getSourceScore(),
                getResources().getString(R.string.star)
        ));

        ((TextView)findViewById(R.id.cityTo)).setText(String.format(
                "%s\n%d%s",
                tc.getCityName(tc.getDest()),
                tc.getDestScore(),
                getResources().getString(R.string.star)
        ));

        Button b = findViewById(R.id.roadButton);
        MyClickListener listener = new MyClickListener();
        b.setOnClickListener(listener);
        iv.setOnClickListener(listener);
    }

    private class MyClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(RoadActivity.this, QuestionActivity.class);
            i.putExtra("current_question", currentQuestionId);
            i.putExtra("hinted", hinted);
            i.putIntegerArrayListExtra("answers", answers);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            RoadActivity.this.finish();
            startActivity(i);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_question", currentQuestionId);
        outState.putIntegerArrayList("answers", answers);
        outState.putBoolean("hinted", hinted);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        currentQuestionId = savedInstanceState.getInt("current_question", -1);
    }
}