package ru.victormalkov.android.axolotl;

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
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.util.ArrayUtils;

import java.util.ArrayList;
import java.util.Random;


public class QuestionActivity extends AppCompatActivity {
    public static final int MAX_QUESTION_SELECT_ATTEMPTS = 5;
    public static final String TAG = "QuestionActivity";
    private AnswerListAdapter adapter;
    private QuizDatabaseHelper dbHelper;

    private AdView mAdView;

    private static final String QUESTION_COLUMN_NAME = "text";
    private static int DELAY;
    private static int TRANSITION_DURATION;

    private static int CORRECT_REWARD_STARS;
    private static int INCORRECT_PENALTY_STARS;
    private static int HINT_REMOVE_INCORRECT_STARS;

    private boolean clickable = true;
    private boolean hintUsed = false;

    private Question currentQuestion;
    private TravelController tc;

    private void initConstant() {
        DELAY = getResources().getInteger(R.integer.delay);
        TRANSITION_DURATION = getResources().getInteger(R.integer.transition_duration);
        CORRECT_REWARD_STARS = getResources().getInteger(R.integer.reward);
        INCORRECT_PENALTY_STARS = getResources().getInteger(R.integer.penalty);
        HINT_REMOVE_INCORRECT_STARS = getResources().getInteger(R.integer.hint_cost);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        initConstant();

        MobileAds.initialize(this, getString(R.string.addmobAppId));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice(getString(R.string.my_test_phone_id))
                .build();
        mAdView.loadAd(adRequest);

        dbHelper = QuizDatabaseHelper.getInstance(getApplicationContext());
        tc = TravelController.getInstance(getApplicationContext());

        ((ViewGroup) findViewById(R.id.constraintLayout)).getLayoutTransition().setDuration(TRANSITION_DURATION);
        findViewById(R.id.mapButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                synchronized (QuestionActivity.this) {
                    if (!clickable) {
                        return;
                    }
                    clickable = false;
                }
                Intent i = new Intent(QuestionActivity.this, RoadActivity.class);
                i.putExtra("current_question", currentQuestion.id);
                i.putIntegerArrayListExtra("answers", currentQuestion.answersId);
                i.putExtra("hinted", hintUsed);
                startActivity(i);
            }
        });

        if (savedInstanceState != null) {
            int id = savedInstanceState.getInt("current_question", -1);
            ArrayList<Integer> answersId = savedInstanceState.getIntegerArrayList("answers");
            hintUsed = savedInstanceState.getBoolean("hinted", false);
            currentQuestion = getQuestion(id, answersId);
        } else if (getIntent() != null && getIntent().hasExtra("current_question")) {
            int id = getIntent().getIntExtra("current_question", -1);
            ArrayList<Integer> answersId = getIntent().getIntegerArrayListExtra("answers");
            hintUsed = getIntent().getBooleanExtra("hinted", false);
            currentQuestion = getQuestion(id, answersId);
        } else {
            currentQuestion = getRandomQuestion();
            hintUsed = false;
        }

        updateView(currentQuestion);
    }

    private void updateView(final Question q) {
        // статистика
        final SharedPreferences pref = getPref();
        final int stars = pref.getInt("stars", 0);
        ((TextView)findViewById(R.id.scoreTextView)).setText(Integer.toString(stars));

        // вопрос
        TextView t = findViewById(R.id.mainWord);
        t.setText(q.text);

        // варианты ответа
        final RecyclerView recyclerView = findViewById(R.id.answerVariantsView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        RecyclerViewClickListener listener = new AnswerClickListener(q, recyclerView);
        adapter = new AnswerListAdapter(q.answers, listener);
        recyclerView.setAdapter(adapter);

        // фон
        ((ImageView)findViewById(R.id.imageView2)).setImageDrawable(tc.getRoadView().image);
        ((ImageView)findViewById(R.id.imageView2)).setScaleType(ImageView.ScaleType.CENTER_CROP);

        ((TextView) findViewById(R.id.mapButton)).setText(tc.getRoadSymbol());

        findViewById(R.id.hintButton).setOnClickListener(new HintButtonClickListener(q));
    }

    private Question getQuestion(int id, ArrayList<Integer> answersId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Question q = new Question();

        Cursor cur = db.rawQuery("select * from questions where id=?;", new String[] {Integer.toString(id)});
        if (cur.moveToFirst()) {
            q.id = id;
            q.text = cur.getString(cur.getColumnIndex(QUESTION_COLUMN_NAME));
            String qid = Integer.toString(cur.getInt(cur.getColumnIndex("id")));
            db.execSQL("update questions set askedTimes = ? where id = ?", new Object[]{cur.getInt(cur.getColumnIndex("askedTimes")) + 1, cur.getInt(cur.getColumnIndex("id"))});
            Log.v("Question", "preparing question #" + qid);
            if (answersId == null) {
                Cursor cur2 = db.rawQuery("select rowid, * from answers where question = ? order by random()", new String[]{qid});
                if (cur2.moveToFirst()) {
                    int ii = 0;
                    do {
                        q.answers.add(cur2.getString(cur2.getColumnIndex("text")));
                        q.answersId.add(cur2.getInt(cur2.getColumnIndex("rowid")));
                        boolean isRight = cur2.getInt(cur2.getColumnIndex("isRight")) != 0;
                        if (isRight) {
                            q.right = ii;
                        }
                        ii++;
                    } while (cur2.moveToNext());
                    if (q.right < 0) {
                        // ни один из ответов не помечен как правильный
                        Toast.makeText(getApplicationContext(), "Ошибка базы (тип 1), пропускаем вопрос #" + qid, Toast.LENGTH_SHORT).show();
                        q = null;
                    }
                } else {
                    // на вопрос нет ни одного ответа
                    Toast.makeText(getApplicationContext(), "Ошибка базы (тип 2), пропускаем вопрос #" + qid, Toast.LENGTH_SHORT).show();
                    q = null;
                }
                cur2.close();
            } else {
                Log.v(TAG, "load question with given answer list");
                String placeholders = makePlaceholders(answersId.size());
                String query = String.format("select rowid, * from answers where rowid in (%s) order by random()", placeholders);
                String[] arr = new String[answersId.size()];
                for (int i = 0; i < answersId.size(); i++) {
                    arr[i] = Integer.toString(answersId.get(i));
                }
                Cursor cur2 = db.rawQuery(query, arr);
                if (cur2.moveToFirst()) {
                    int ii = 0;
                    do {
                        q.answers.add(cur2.getString(cur2.getColumnIndex("text")));
                        q.answersId.add(cur2.getInt(cur2.getColumnIndex("rowid")));
                        boolean isRight = cur2.getInt(cur2.getColumnIndex("isRight")) != 0;
                        if (isRight) {
                            q.right = ii;
                        }
                        ii++;
                    } while (cur2.moveToNext());
                    if (q.right < 0) {
                        // ни один из ответов не помечен как правильный
                        Toast.makeText(getApplicationContext(), "Ошибка базы (тип 1), пропускаем вопрос #" + qid, Toast.LENGTH_SHORT).show();
                        q = null;
                    }
                } else {
                    // на вопрос нет ни одного ответа
                    Toast.makeText(getApplicationContext(), "Ошибка базы (тип 2), пропускаем вопрос #" + qid, Toast.LENGTH_SHORT).show();
                    q = null;
                }
                cur2.close();
            }
        } else {
            q = null;
        }
        cur.close();
        //db.close();
        if (q == null) {
            return getRandomQuestion();
        } else {
            return q;
        }
    }

    private String makePlaceholders(int len) {
        if (len < 1) {
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    private Question getRandomQuestion() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Question q = null;

        for (int attempt = 0; null == q && attempt < MAX_QUESTION_SELECT_ATTEMPTS; attempt++) {
            q = new Question();
            String[] topics = getResources().getStringArray(R.array.topics);
            String placeholders = makePlaceholders(topics.length);

            topics = ArrayUtils.concat(topics, topics);
            String query = String.format("select * from questions where id in (select id from questions where type in (%s) and askedTimes in (select min(askedTimes) from questions where type in (%s)) order by random() limit 1);",
                    placeholders, placeholders);
            Cursor cur = db.rawQuery(query, topics);
            if (cur.moveToFirst()) {
                q.text = cur.getString(cur.getColumnIndex(QUESTION_COLUMN_NAME));
                q.id = cur.getInt(cur.getColumnIndex("id"));
                String qid = Integer.toString(q.id);
                db.execSQL("update questions set askedTimes = ? where id = ?", new Object[]{cur.getInt(cur.getColumnIndex("askedTimes")) + 1, cur.getInt(cur.getColumnIndex("id"))});
                Log.v("Question", "preparing question #" + qid);
                Cursor cur2 = db.rawQuery("select rowid, * from answers where question = ? order by random()", new String[]{qid});
                if (cur2.moveToFirst()) {
                    int ii = 0;
                    do {
                        q.answers.add(cur2.getString(cur2.getColumnIndex("text")));
                        q.answersId.add(cur2.getInt(cur2.getColumnIndex("rowid")));
                        boolean isRight = cur2.getInt(cur2.getColumnIndex("isRight")) != 0;
                        if (isRight) {
                            q.right = ii;
                        }
                        ii++;
                    } while (cur2.moveToNext());
                    if (q.right < 0) {
                        // ни один из ответов не помечен как правильный
                        Toast.makeText(getApplicationContext(), "Ошибка базы (тип 1), пропускаем вопрос #" + qid, Toast.LENGTH_SHORT).show();
                        q = null;
                    }
                } else {
                    // на вопрос нет ни одного ответа
                    Toast.makeText(getApplicationContext(), "Ошибка базы (тип 2), пропускаем вопрос #" + qid, Toast.LENGTH_SHORT).show();
                    q = null;
                }
                cur2.close();
                cur.close();
            } else {
                cur.close();
                throw new RuntimeException("No questions found");
            }
        }
        if (q == null) {
            throw new RuntimeException("Database error, cannot find questions with answers");
        }
        //db.close();
        return q;
    }

    private class HintButtonClickListener implements View.OnClickListener {
        private Question q;

        HintButtonClickListener(Question q) {
            this.q = q;
        }

        @Override
        public void onClick(View v) {
            synchronized (QuestionActivity.this) {
                if (!clickable) {
                    return;
                }
                if (hintUsed) {
                    return;
                }
                clickable = false;
                hintUsed = true;
            }
            int stars = getPref().getInt("stars", 0);
            if (stars <= 0) {
                clickable = true;
                return;
            }
            int num = q.answers.size();
            int hint = new Random().nextInt(num - 1);
            if (hint >= q.right) {
                hint++;
            }
            q.answers.remove(hint);
            q.answersId.remove(hint);
            if (hint < q.right) {
                q.right--;
            }
            adapter.notifyItemRemoved(hint);

            stars = stars + HINT_REMOVE_INCORRECT_STARS;
            if (stars < 0) {
                stars = 0;
            }
            getPref().edit().putInt("stars", stars).apply();
            ((TextView)findViewById(R.id.scoreTextView)).setText(Integer.toString(stars));

            clickable = true;
        }
    }

    private class AnswerClickListener implements RecyclerViewClickListener {
        private Question q;
        private RecyclerView recyclerView;

        AnswerClickListener(Question q, RecyclerView recyclerView) {
            this.q = q;
            this.recyclerView = recyclerView;
        }

        @Override
        public void onClick(View view, int position) {
            synchronized (QuestionActivity.this) {
                if (!clickable) {
                    return;
                }
                clickable = false;
            }
            boolean correct = position == q.right;
            String text = correct ? getResources().getString(R.string.correct) : getResources().getString(R.string.incorrect);
            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);

            RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(q.right);
            if (vh != null) {
                ((CardView) ((LinearLayout) vh.itemView).getChildAt(0))
                        .setCardBackgroundColor(getResources().getColor(R.color.correct));
            }

            if (!correct) {
                ((CardView) ((LinearLayout) view).getChildAt(0))
                        .setCardBackgroundColor(getResources().getColor(R.color.incorrect));
            }
            toast.show();

            int stars = getPref().getInt("stars", 0);
            if (correct) {
                stars += CORRECT_REWARD_STARS;
            } else {
                stars += INCORRECT_PENALTY_STARS;
                if (stars < 0) {
                    stars = 0;
                }
            }
            getPref().edit().putInt("stars", stars).apply();
            if (stars >= tc.getDestScore()) {
                tc.arrive();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(QuestionActivity.this, CityActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        QuestionActivity.this.finish();
                        startActivity(i);
                    }
                }, DELAY);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.innerConstraintLayout).setVisibility(View.GONE);
                        currentQuestion = getRandomQuestion();
                        hintUsed = false;
                        updateView(currentQuestion);
                        findViewById(R.id.innerConstraintLayout).setVisibility(View.VISIBLE);
                        clickable = true;
                    }
                }, DELAY);
            }
        }
    }

    private SharedPreferences getPref() {
        return  getApplicationContext().getSharedPreferences(getApplicationContext().getPackageName() + ".score", Context.MODE_PRIVATE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_question", currentQuestion.id);
        outState.putIntegerArrayList("answers", currentQuestion.answersId);
        outState.putBoolean("hinted", hintUsed);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.v(TAG, "in onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        clickable = true;
//        int id = savedInstanceState.getInt("current_question", -1);
//        ArrayList<Integer> answersId = savedInstanceState.getIntegerArrayList("answers");
//        currentQuestion = getQuestion(id, answersId);
//        hintUsed = savedInstanceState.getBoolean("hinted", false);
//        updateView(currentQuestion);
    }

    @Override
    protected void onResume() {
        super.onResume();
        clickable = true;
    }
}
