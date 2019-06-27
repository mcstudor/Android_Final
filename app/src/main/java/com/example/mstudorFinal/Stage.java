package com.example.mstudorFinal;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.Serializable;

import android.os.Handler;
import android.widget.Toast;

public class Stage extends View implements Serializable {
    //everything in cm
    private static final float g = - 9.8000f;

    private static final long TIMER_MSEC = 20;
    private static final float boxWidth = 110;
    private static final float boxHeight = 70;
    private static final RectF CANNON = new RectF(0, -1, 6.5f, 1);
    private static final RectF CANNONBALL = new RectF(-1, -1, 1, 1);
    private static final RectF GROUND = new RectF(0, -5, boxWidth, 5);
    private static final RectF DOME = new RectF(-4,-4,4,4);
    private final Bitmap pumpkin = BitmapFactory.decodeResource(getResources(), R.drawable.pumpkin);
    private final Bitmap golfBall = BitmapFactory.decodeResource(getResources(), R.drawable.golf_ball);

    private static final int SKY_BLUE = Color.rgb(135,206,235);
    private static final int GRASS_GREEN = Color.rgb(96, 128, 56);
    private static final int TRUE_GREY = Color.rgb(175,175,175);
    private static final int BROWN_RED = Color.rgb(135,60,60);

    private float x, y, vX, vY, aY, aX, maxY, res;
    private int elevation, mVelocity, height, target, tolerance, shape;
    private boolean running, paused, airRes;
    private Paint paint;
    private Handler drawHandler;
    private Runnable timer;



    public Stage(Context context) {
        super(context);
        initialize();
    }

    public Stage(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public Stage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize(){
        this.aY = g;
        this.aX = 0;
        this.vX = 0;
        this.vY = 0;
        this.y = 0;
        this.x = 0;
        this.target = 50;
        this.shape = 0;
        this.running = false;
        this.paused = false;
        this.paint = new Paint();
        paint.setColor(Color.BLACK);
        this.drawHandler = new Handler();
        this.timer = new Runnable() {
            @Override
            public void run() {
                update(TIMER_MSEC);

                if(y <= 0){
                    onFinish();
                } else
                    drawHandler.postDelayed(this, TIMER_MSEC);
            }
        };

    }

    public void pause(){
        if(running){
            drawHandler.removeCallbacks(timer);
            paused = true;
        }
    }

    public void resume(){
        if(running){
            drawHandler.postDelayed(timer, TIMER_MSEC);
            paused = false;
        }
    }

    private void stop(){
        if(running)
            drawHandler.removeCallbacks(timer);
        running = false;
        paused = false;
    }



    public void pauseResume(){
        if(running){
            if(paused){
                resume();
            } else{
                pause();
            }
        }
    }
    public void start(){
        if(!running) {
            this.y = height;
            aY = g;
            aX = 0;
            maxY = 0;
            drawHandler.postDelayed(timer, TIMER_MSEC);
            running = true;
        }
    }



    public void onFinish(){
        stop();
        String message;
        if(this.x >= target-tolerance && this.x <= target+tolerance){
            message = "YOU HIT!";
        } else{
            message = "You missed...";
        }

        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        ((MainActivity)getContext()).onSimFinished(x, maxY);
        this.x = 0;
        this.running = false;
        setValues(height, elevation, mVelocity);
    }




    private void update(float sec){
        //sec is in MS. Convert to S
        sec = sec/1000;
        if(airRes == true){
            float vM = (float)Math.sqrt((vY * vY) + (vX * vX));
            aY = -9.8f - (res * vY * vM);
            aX = -(res * vX * vM);
        }
        vY = vY + aY * sec;
        vX = vX + aX * sec;
        float newY = y + vY * sec;
        y = Math.max(newY, 0);
        if(newY>maxY)
            maxY=newY;
        x = x + vX * sec;

        this.invalidate();
    }



    public void setValues(@Nullable Integer height, @Nullable Integer elevation, @Nullable Integer velocity){
        if(height!=null)
            this.height = height;
        if(elevation != null)
            this.elevation = elevation;
        if(velocity != null) {
            this.mVelocity = velocity;
            vX = mVelocity * (float) Math.cos(Math.toRadians(elevation));
            vY = mVelocity * (float) Math.sin(Math.toRadians(elevation));
        }
        this.invalidate();
    }


    public void setTarget(){
        this.target = 10+(int)(Math.random()*85);
        this.invalidate();
    }

    public void setPreferences(SharedPreferences preferences){
        this.shape = Integer.parseInt(preferences.getString("ITEM", "0"));
        this.tolerance = Integer.parseInt(preferences.getString("PROXIMITY", "5"));
        this.airRes = preferences.getBoolean("AIR", false);

        switch(shape){
            case 0:
                //Cannonball
                res = .000015f;
                break;
            case 1:
                //Pumpkin
                res = .0079f;
                break;
            case 2:
                //Golf Ball
                res = .0038f;
                break;
            default:
                res  = 0;
        }
    }




    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if(width<height){
            setMeasuredDimension((int)(height/.636363), height);
        } else{
            setMeasuredDimension(width, (int)(width*.636363));
        }
    }

    @Override
    public Parcelable onSaveInstanceState(){
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putFloat("yPosition", y);
        bundle.putFloat("xPosition", x);
        bundle.putFloat("xVelocity", vX);
        bundle.putFloat("yVelocity", vY);
        bundle.putFloat("xAcceleration", aX);
        bundle.putFloat("yAcceleration", aY);
        bundle.putFloat("maxY", maxY);
        bundle.putInt("elevation",elevation);
        bundle.putInt("mVelocity",mVelocity);
        bundle.putInt("height",height);
        bundle.putInt("target",target);
        bundle.putBoolean("running", running);
        if(running){
            drawHandler.removeCallbacks(timer);
        }
        return bundle;
    }


    @Override
    public void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle)state;
        vX = bundle.getFloat("xVelocity");
        vY = bundle.getFloat("yVelocity");
        aX = bundle.getFloat("xAcceleration");
        aY = bundle.getFloat("yAcceleration");
        y = bundle.getFloat("yPosition");
        x = bundle.getFloat("xPosition");
        maxY = bundle.getFloat("maxY");
        elevation = bundle.getInt("elevation");
        mVelocity = bundle.getInt("mVelocity");
        height = bundle.getInt("height");
        target = bundle.getInt("target");
        running = bundle.getBoolean("running");
        state = bundle.getParcelable("instanceState");
        super.onRestoreInstanceState(state);
        paused = running;
    }

    public boolean isRunning(){
        return this.running;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            this.pauseResume();
            return true;
        }
        return super.onTouchEvent(event);


    }
    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.drawColor(SKY_BLUE);

        float cWidth = canvas.getWidth();
        float cHeight = canvas.getHeight();
        float yScale = cHeight/boxHeight;
        float xScale = cWidth/boxWidth;
        canvas.scale(1, -1, cWidth / 2, cHeight / 2);
        canvas.scale(yScale, xScale);
        canvas.translate(5, 5);



        paint.setColor(Color.BLACK);




        //draw projectile
        canvas.save();
        canvas.translate(x, y);
        switch(shape){
            case 1:
                canvas.drawBitmap(pumpkin, null, CANNONBALL, paint);
                break;
            case 2:
                canvas.drawBitmap(golfBall, null, CANNONBALL, paint);
                break;
            default:
            case 0:
                canvas.drawOval(CANNONBALL, paint);
                break;

        }

        canvas.restore();


        //draw CANNON
        canvas.save();
        canvas.translate(0, height);
        canvas.rotate(elevation);
        canvas.drawRect(CANNON, paint);
        canvas.restore();



        //draw dome
        canvas.save();
        canvas.translate(0, height);
        paint.setColor(BROWN_RED);
        canvas.drawOval(DOME, paint);
        canvas.restore();
        //draw tower
        paint.setColor(TRUE_GREY);
        canvas.drawRect(new RectF(-4,0,4, height), paint);
        //draw base
        canvas.save();
        canvas.translate(-5, -5);
        paint.setColor(GRASS_GREEN);
        canvas.drawRect(GROUND, paint);
        canvas.restore();
        //draw target
        paint.setColor(Color.RED);
        canvas.translate(target, -1);
        canvas.drawOval(new RectF(-tolerance, -tolerance*.2f, tolerance, tolerance*.2f), paint);
        paint.setColor(GRASS_GREEN);
        canvas.drawOval(new RectF(-tolerance*.70f, -tolerance*.14f, tolerance*.70f, tolerance*.14f), paint);
        paint.setColor(Color.RED);
        canvas.drawOval(new RectF(-tolerance*.40f, -tolerance*.08f, tolerance*.40f, tolerance*.08f), paint);



    }


}
