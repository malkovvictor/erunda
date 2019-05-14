package ru.heritagepw.android.erunda;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private AnswerListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        RecyclerViewClickListener listener = new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                Toast toast = Toast.makeText(getApplicationContext(), "Position: " + position, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        };


        ArrayList<String> list = new ArrayList<>();
        list.add("Представитель птиц из нескольких родов семейства утиных: пеганки, нырковые утки, савки, речные утки, утки-пароходы, мускусные утки и крохали; всего более 110 видов. Распространены утки широко, в России более 30 видов. Самцы уток называются се́лезнями, птенцы утки — утя́тами. ");
        list.add("Отряд вторично-водных позвоночных, которых обычно относят к сборной группе «пресмыкающихся». В рамках кладистики крокодилы рассматриваются как единственная выжившая субклада более широкой клады круротарзы или псевдозухии. ");
        list.add("Игрушка в виде утки, как правило, жёлтого цвета. Она может быть сделана из резины или пенопласта. Жёлтая резиновая уточка стала ассоциироваться с купанием.");

        RecyclerView recyclerView = findViewById(R.id.answerVariantsView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AnswerListAdapter(list, listener);
        recyclerView.setAdapter(adapter);

        TextView t = findViewById(R.id.mainWord);
        t.setText("Абракадабра");

    }
}
