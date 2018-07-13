package ru.myandroid.drebedengi_my;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class OnSwipeTouchListener implements OnTouchListener {
    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener (Context ctx) {
        gestureDetector = new GestureDetector(ctx, new GestureDetector.OnGestureListener() {
            private static final int SWIPE_TRESHOLD = 100;
            private static final int SWIPE_VELOCITY_TRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent motionEvent) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float Vx, float Vy) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_TRESHOLD && Math.abs(Vx) > SWIPE_VELOCITY_TRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                        result = true;
                    } else {
                        if (Math.abs(diffY) > SWIPE_TRESHOLD && Math.abs(Vx) > SWIPE_VELOCITY_TRESHOLD) {
                            if (diffY > 0) {
                                onSwipeDown();
                            } else {
                                onSwipeUp();
                            }
                        }
                        result = true;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return result;
            }
        });
    }

    public boolean onTouch(View v, MotionEvent event) {
        // Обработка клика
        return gestureDetector.onTouchEvent(event);
    }

    public void onSwipeRight() {}

    public void onSwipeLeft() {}

    public void onSwipeUp() {}

    public void onSwipeDown() {}
}
