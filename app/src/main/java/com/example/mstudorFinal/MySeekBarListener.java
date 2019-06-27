package com.example.mstudorFinal;

import android.content.Context;
import android.widget.SeekBar;
import android.widget.TextView;

public class MySeekBarListener implements SeekBar.OnSeekBarChangeListener {
    private TextView textView;
    private int progress;
    private Integer min;
    private Context context;

    public MySeekBarListener(Context context, TextView textView, int min){
        this.textView = textView;
        this.progress = 0;
        this.min = min;
        this.context = context;
    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        this.progress = progress;
        this.textView.setText(Integer.toString(progress+min));
        ((MainActivity)this.context).onChangeSeekBars();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        this.textView.setText(Integer.toString(progress+min));
        ((MainActivity)this.context).onChangeSeekBars();
    }

    public void setMin(int min){
        this.min = min;
    }
}
