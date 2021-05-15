package com.example.TouchApp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class CodeDialog extends AppCompatDialogFragment {

    private EditText enterCode;
    String codeToSend;
    Context activity;
    AlertDialogCallback<String> callback;

    public CodeDialog(Context activity, final AlertDialogCallback<String> callback) {
        this.activity=activity;
        this.callback=callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog, null);
        builder.setView(view)
                .setTitle("Enter 4-digit passcode")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String fieldText = enterCode.getText().toString();
                        int missingInput = 4 - fieldText.length();
                        for (int i = 0; i < missingInput; i++) {
                            fieldText = 0 + fieldText;
                        }
                        codeToSend=fieldText;
                        callback.alertDialogCallback(codeToSend);
                    }
                });

        enterCode = view.findViewById(R.id.enter_code);


        return builder.create();
    }

}
