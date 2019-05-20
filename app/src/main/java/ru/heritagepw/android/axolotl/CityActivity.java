package ru.heritagepw.android.axolotl;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

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
        ((TextView)findViewById(R.id.cityName)).setText(tc.getCityName(tc.getSource()));

        CityPhoto cp = tc.getCityView();
        if (cp != null) {
            try {
                ((ImageView)findViewById(R.id.poiImageView)).setImageDrawable(
                        Drawable.createFromStream(
                                getAssets().open(cp.getFilename()),
                                cp.getFilename()
                        )
                );
                TextView copyright = findViewById(R.id.copyrightTextView);
                copyright.setText(Html.fromHtml(cp.getCopyright()));
                copyright.setMovementMethod(LinkMovementMethod.getInstance());

                ((TextView)findViewById(R.id.poiName)).setText(cp.name);
            } catch (IOException e) {
                // Don't worry
            }
        }

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
