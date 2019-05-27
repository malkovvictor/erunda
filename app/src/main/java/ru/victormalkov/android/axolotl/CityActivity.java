package ru.victormalkov.android.axolotl;

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
    private CityPhoto mCityPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tc = TravelController.getInstance(getApplicationContext());
        if (tc.getDest() >= 0) {
            Intent i = new Intent(this, RoadActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.finish();
            startActivity(i);
            return;
        }

        setContentView(R.layout.activity_city);
        if (savedInstanceState != null && savedInstanceState.containsKey("city_photo")) {
            int id = savedInstanceState.getInt("city_photo");
            int factId = savedInstanceState.getInt("fact_id");
            mCityPhoto = tc.getCityView(id, factId);

        }
        if (mCityPhoto == null) {
            mCityPhoto = tc.getCityViewByCity();
        }
        update();

        findViewById(R.id.startGameButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tc.chooseRoad();
                Intent i = new Intent(CityActivity.this, RoadActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                CityActivity.this.finish();
                startActivity(i);
            }
        });
    }

    private void update() {
        ((TextView)findViewById(R.id.cityName)).setText(tc.getCityName(tc.getSource()));
        if (mCityPhoto != null) {
            try {
                ((ImageView)findViewById(R.id.poiImageView)).setImageDrawable(
                        Drawable.createFromStream(
                                getAssets().open(mCityPhoto.getFilename()),
                                mCityPhoto.getFilename()
                        )
                );
                TextView copyright = findViewById(R.id.copyrightTextView);
                copyright.setText(Html.fromHtml(mCityPhoto.getCopyright()));
                copyright.setMovementMethod(LinkMovementMethod.getInstance());

                ((TextView)findViewById(R.id.poiName)).setText(mCityPhoto.name);
                ((TextView)findViewById(R.id.poiFact)).setText(mCityPhoto.fact);
                if (mCityPhoto.name == null || mCityPhoto.name.isEmpty()) {
                    findViewById(R.id.poiName).setVisibility(View.GONE);
                }
                if (mCityPhoto.fact == null || mCityPhoto.fact.isEmpty()) {
                    findViewById(R.id.poiFact).setVisibility(View.GONE);
                }
            } catch (IOException e) {
                // Don't worry
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCityPhoto != null) {
            outState.putInt("city_photo", mCityPhoto.id);
            outState.putInt("fact_id", mCityPhoto.factId);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (mCityPhoto == null && savedInstanceState.containsKey("city_photo")) {
            int id = savedInstanceState.getInt("city_photo");
            int factId = savedInstanceState.getInt("fact_id");
            mCityPhoto = tc.getCityView(id, factId);

        }
        update();
    }
}
