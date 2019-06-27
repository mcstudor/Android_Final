package com.example.mstudorFinal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {
    private static final int V_MIN = 10;
    private static final int E_MIN = 25;
    private SeekBar elevationBar, heightBar, velocityBar;
    private Stage stage;
    private MediaPlayer mp;
    private int mclipID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stage = findViewById(R.id.stage);

        setSeekBarListeners();
        setPreferences();
        setDefaultValues();
        mclipID = 0;
        mp = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onPause(){
        super.onPause();
        if(mp!= null) {
            mp.release();
            mp = null;
        }
        stage.pause();
    }



    @Override
    public void onResume(){
        super.onResume();
        setPreferences();
        if(stage.isRunning())
            setControlsEnabled(false);
    }

    private void setPreferences(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        stage.setPreferences(sharedPref);
    }

    public void startSim(View view){
        setPreferences();
        setControlsEnabled(false);
        playSound(R.raw.large_thump);
        stage.start();
    }




    public void onSimFinished(double maxX, double maxY) {
        playSound(R.raw.tank_firing);
        TextView maxYView = findViewById(R.id.maxY_result);
        maxYView.setText(String.format("%.1f",maxY));
        TextView maxXView = findViewById(R.id.maxX_result);
        maxXView.setText(String.format("%.1f",maxX));
        setControlsEnabled(true);

    }

    public void setTarget(View view){
        stage.setTarget();
    }

    public void onChangeSeekBars(){
        int height = heightBar.getProgress();
        int velocity = velocityBar.getProgress();
        int elevation = elevationBar.getProgress();
        if(height == 0 && elevation<E_MIN){
            elevationBar.setProgress(E_MIN);
            elevation = E_MIN;
        }

        stage.setValues(height, elevation, velocity + V_MIN);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch (id){
            case R.id.action_about:
                Toast.makeText(this, "Final Project, Spring 2019, Mark Studor",
                        Toast.LENGTH_SHORT).show();
                return true;
            case R.id.settingsButton:
                if(stage.isRunning())
                    return true;
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    private void setSeekBarListeners() {
        elevationBar = findViewById(R.id.elevation_bar);
        TextView elevationValue = findViewById(R.id.elevation_Value);
        elevationBar.setOnSeekBarChangeListener(
                new MySeekBarListener(this,
                elevationValue, 0));
        heightBar = findViewById(R.id.height_bar);
        TextView heightValue = findViewById(R.id.height_Value);
        heightBar.setOnSeekBarChangeListener(new MySeekBarListener(this, heightValue, 0));
        velocityBar = findViewById(R.id.velocity_bar);
        TextView velocityValue = findViewById(R.id.velocity_Value);
        velocityBar.setOnSeekBarChangeListener(new MySeekBarListener(this, velocityValue, V_MIN));

    }

    private void setDefaultValues(){
        elevationBar.setProgress(45);
        heightBar.setProgress(5);
        velocityBar.setProgress(15);//15 over min
    }

    private void setControlsEnabled(boolean b){

        findViewById(R.id.fireButton).setEnabled(b);
        findViewById(R.id.targetButton).setEnabled(b);
        heightBar.setEnabled(b);
        elevationBar.setEnabled(b);
        velocityBar.setEnabled(b);

    }

    public void playSound(int id){
        if(mp!=null && id == mclipID){
            mp.pause();
            mp.seekTo(0);
            mp.start();
        }
        else{
            if(mp !=null) mp.release();
            mclipID = id;
            mp = MediaPlayer.create(this, id);
            mp.setOnCompletionListener(this);
            mp.setVolume(0.6f, 0.6f);
            mp.start();
        }
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.release();
        mp = null;
    }
}
