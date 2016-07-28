package com.pokescanner.helper;

/**
 * Created by Brian on 7/23/2016.
 */
import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.pokescanner.events.ForceRefreshEvent;

import org.greenrobot.eventbus.EventBus;

public class TouchableWrapper extends FrameLayout {

    public TouchableWrapper(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (EventBus.getDefault().hasSubscriberForEvent(ForceRefreshEvent.class)) {
                    EventBus.getDefault().post(new ForceRefreshEvent());
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}
