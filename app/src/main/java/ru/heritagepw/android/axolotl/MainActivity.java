package ru.heritagepw.android.axolotl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {
    private AnswerListAdapter adapter;
    private QuizDatabaseHelper dbHelper;

    private AdView mAdView;

    private static final String QUESTION_COLUMN_NAME = "text";
    private static final int DELAY = 500;

    private boolean clickable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, getString(R.string.addmobAppId));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice(getString(R.string.my_test_phone_id))
                .build();
        mAdView.loadAd(adRequest);

        dbHelper = new QuizDatabaseHelper(getApplicationContext());
        updateView(getNextQuestion());
    }

    private void updateView(final Question q) {
        // статистика
        final SharedPreferences pref = getPref();
        final int scorePlus = pref.getInt("correctAnswers", 0);
        final int scoreTotal = pref.getInt("total", 0);
        TextView score = findViewById(R.id.scoreTextView);
        score.setText(getResources().getString(R.string.solved) + ": " + scorePlus + " " + getResources().getString(R.string.from) + " " + scoreTotal);
        pref.edit().putInt("total", scoreTotal + 1).apply();

        // вопрос
        TextView t = findViewById(R.id.mainWord);
        t.setText(q.text);

        // варианты ответа
        final RecyclerView recyclerView = findViewById(R.id.answerVariantsView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        RecyclerViewClickListener listener = new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                synchronized (MainActivity.this) {
                    if (!clickable) {
                        return;
                    }
                    clickable = false;
                }
                boolean correct = position == q.right;
                String text = correct ? getResources().getString(R.string.correct) : getResources().getString(R.string.incorrect);
                Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);

                View v = recyclerView.getChildAt(q.right);
                ((CardView) ((LinearLayout) v).getChildAt(0))
                        .setCardBackgroundColor(getResources().getColor(R.color.correct));

                if (!correct) {
                    ((CardView) ((LinearLayout) view).getChildAt(0))
                            .setCardBackgroundColor(getResources().getColor(R.color.incorrect));
                }
                toast.show();

                if (correct) {
                    pref.edit().putInt("correctAnswers", scorePlus + 1).apply();
                }


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateView(getNextQuestion());
                        clickable = true;

/*                        Intent i = new Intent(MainActivity.this, MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);*/
                    }
                }, DELAY);
            }
        };

        adapter = new AnswerListAdapter(q.answers, listener);
        recyclerView.setAdapter(adapter);

    }

    private Question getNextQuestion() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Question q = new Question();

        Cursor cur = db.rawQuery("select * from questions where id in (select id from questions where askedTimes in (select min(askedTimes) from questions) order by random() limit 1);", null);
        if (cur.moveToFirst()) {
            q.text = cur.getString(cur.getColumnIndex(QUESTION_COLUMN_NAME));
            String qid = Integer.toString(cur.getInt(cur.getColumnIndex("id")));
            Log.v("Question", "preparing question #" + qid);
            Cursor cur2 = db.rawQuery("select * from answers where question = ? order by random()", new String[] {qid});
            if (cur2.moveToFirst()) {
                int ii = 0;
                do {
                    q.answers.add(cur2.getString(cur2.getColumnIndex("text")));
                    boolean isRight = cur2.getInt(cur2.getColumnIndex("isRight")) != 0;
                    if (isRight) {
                        q.right = ii;
                    }
                    ii++;
                } while (cur2.moveToNext());
                cur2.close();
            } else {
                throw new RuntimeException("No answers found");
            }
            db.execSQL("update questions set askedTimes = ? where id = ?", new Object[] {cur.getInt(cur.getColumnIndex("askedTimes")) + 1, cur.getInt(cur.getColumnIndex("id"))});
            cur.close();
        } else {
            throw new RuntimeException("No questions found");
        }

        db.close();
        return q;
    }
    private SharedPreferences getPref() {
        return  getApplicationContext().getSharedPreferences(getApplicationContext().getPackageName() + ".score", Context.MODE_PRIVATE);
    }
}