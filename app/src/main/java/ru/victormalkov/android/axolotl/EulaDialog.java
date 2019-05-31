package ru.victormalkov.android.axolotl;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

public class EulaDialog extends DialogFragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("");
        View v = inflater.inflate(R.layout.legal_window, null);
        ((TextView)v.findViewById(R.id.termsView)).setMovementMethod(LinkMovementMethod.getInstance());
        v.findViewById(R.id.termsAcceptButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TravelController.getInstance(v.getContext().getApplicationContext()).doAcceptEula(true);
                dismiss();
            }
        });
        v.findViewById(R.id.termsRejectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TravelController.getInstance(v.getContext().getApplicationContext()).doAcceptEula(false);
                getActivity().finishAndRemoveTask();
                System.exit(1);
            }
        });
        return v;
    }
}
