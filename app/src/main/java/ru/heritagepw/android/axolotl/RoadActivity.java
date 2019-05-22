package ru.heritagepw.android.axolotl;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class RoadActivity extends AppCompatActivity {
    private int currentQuestionId;
    private  TravelController tc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentQuestionId = savedInstanceState.getInt("current_question", -1);
        } else if (getIntent()!= null){
            currentQuestionId = getIntent().getIntExtra("current_question", -1);
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
            startActivity(i);
        }
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
