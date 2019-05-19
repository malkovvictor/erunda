package ru.heritagepw.android.axolotl;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class CityActivity extends AppCompatActivity {
    private TravelController tc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tc = new TravelController(getApplicationContext());
        if (tc.getDest() >= 0) {
            startActivity(new Intent(this, RoadActivity.class));
            return;
        }

        setContentView(R.layout.activity_city);
        ((TextView)findViewById(R.id.poiText)).setText(tc.getCityName(tc.getSource()));

        findViewById(R.id.startGameButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tc.chooseRoad();
                Intent i = new Intent(CityActivity.this, RoadActivity.class);
                startActivity(i);
            }
        });
    }
}
