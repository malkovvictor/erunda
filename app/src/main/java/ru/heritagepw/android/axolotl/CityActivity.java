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
        setContentView(R.layout.activity_city);
        tc = new TravelController(getApplicationContext());

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
