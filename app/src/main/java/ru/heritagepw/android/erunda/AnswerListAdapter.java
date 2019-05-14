package ru.heritagepw.android.erunda;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class AnswerListAdapter extends RecyclerView.Adapter<AnswerListAdapter.MyViewHolder> {
    private List<String> answers;
    private RecyclerViewClickListener listener;

    public AnswerListAdapter(@NonNull List<String> answers, @Nullable RecyclerViewClickListener listener) {
        this.answers = answers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.answer_line, viewGroup, false);
        MyViewHolder vh = new MyViewHolder(v, listener);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.item.setText(answers.get(i));
    }

    @Override
    public int getItemCount() {
        return answers.size();
    }



    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView item;
        private RecyclerViewClickListener listener;

        public MyViewHolder(@NonNull View itemView, @Nullable RecyclerViewClickListener listener) {
            super(itemView);
            item = itemView.findViewById(R.id.text_answer_variant);
            this.listener = listener;
            itemView.setOnClickListener(this);
        }

        public void onClick(View v) {
            if (listener != null) {
                listener.onClick(v, getAdapterPosition());
            }
        }
    }
}
