package com.summertaker.reader.common;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.summertaker.reader.R;

public abstract class BaseActivity extends AppCompatActivity {

    protected String mTag = "== " + getClass().getSimpleName();
    protected String mVolleyTag = mTag;

    protected Context mContext;
    protected Resources mResources;
    //protected SharedPreferences mSharedPreferences;
    //protected SharedPreferences.Editor mSharedEditor;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;

    protected abstract void onSwipeRight();

    protected abstract void onSwipeLeft();

    protected void initToolbar(String title) {
        mContext = BaseActivity.this;
        mResources = mContext.getResources();

        //mSharedPreferences = getSharedPreferences(Config.USER_PREFERENCE_KEY, 0);
        //mSharedEditor = mSharedPreferences.edit();

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        // Horizontal Scroll Bar 때문에 사용하지 않는다.
        //gestureDetector = new GestureDetector(this, new SwipeDetector());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);

            if (title != null) {
                actionBar.setTitle(title);
            }
        }

        if (toolbar != null) {
            toolbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onToolbarClick();
                }
            });
        }
    }

    protected void onToolbarClick() {

    }

    @Override
    public void onStop() {
        super.onStop();

        BaseApplication.getInstance().cancelPendingRequests(mVolleyTag);
    }

    public class SwipeDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            // Check movement along the Y-axis. If it exceeds SWIPE_MAX_OFF_PATH,
            // then dismiss the swipe.
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                return false;
            }

            //toast( "start = "+String.valueOf( e1.getX() )+" | end = "+String.valueOf( e2.getX() )  );
            //from left to right
            if (e2.getX() > e1.getX()) {
                if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    onSwipeRight();
                    return true;
                }
            }

            if (e1.getX() > e2.getX()) {
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    onSwipeLeft();
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TouchEvent dispatcher.
        if (gestureDetector != null) {
            if (gestureDetector.onTouchEvent(ev))
                // If the gestureDetector handles the event, a swipe has been
                // executed and no more needs to be done.
                return true;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
}

