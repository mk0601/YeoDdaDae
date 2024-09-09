package com.bucheon.yeoddadae;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class SttDialog extends Dialog {
    Context context;
    SttDialogListener listener;

    ImageButton sttBackBtn;
    ImageButton sttListenBtn;
    TextView sttStatusTxt;


    public SttDialog(@NonNull Context context, SttDialogListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stt_dialog);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);

        sttBackBtn = findViewById(R.id.sttBackBtn);
        sttListenBtn = findViewById(R.id.sttListenBtn);
        sttStatusTxt = findViewById(R.id.sttStatusTxt);

        sttListenBtn.setImageResource(R.drawable.mic_activate);
        setSttStatusTxt("Listening...");

        sttBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        sttListenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onMessageSend("SttDialog버튼클릭");
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        listener.onMessageSend("SttDialog닫힘");
    }

    void changeToActiveIcon () {
        sttListenBtn.setImageResource(R.drawable.mic_activate);
    }

    void changeToInactivateIcon() {
        sttListenBtn.setImageResource(R.drawable.mic_inactivate);
    }

    void setSttStatusTxt(String s) {
        sttStatusTxt.setText(s);
    }
}
