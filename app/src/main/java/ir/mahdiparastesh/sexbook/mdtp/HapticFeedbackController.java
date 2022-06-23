package ir.mahdiparastesh.sexbook.mdtp;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.Settings;

import ir.mahdiparastesh.sexbook.Fun;

public class HapticFeedbackController {
    private static final int VIBRATE_DELAY_MS = 125;
    private static final int VIBRATE_LENGTH_MS = 50;

    private static boolean checkGlobalSetting(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) == 1;
    }

    private final Context mContext;
    private final ContentObserver mContentObserver;

    private boolean mIsGloballyEnabled;
    private long mLastVibrate;

    public HapticFeedbackController(Context context) {
        mContext = context;
        mContentObserver = new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                mIsGloballyEnabled = checkGlobalSetting(mContext);
            }
        };
    }

    public void start() {
        mIsGloballyEnabled = checkGlobalSetting(mContext);
        Uri uri = Settings.System.getUriFor(Settings.System.HAPTIC_FEEDBACK_ENABLED);
        mContext.getContentResolver().registerContentObserver(uri, false, mContentObserver);
    }

    public void stop() {
        mContext.getContentResolver().unregisterContentObserver(mContentObserver);
    }

    public void tryVibrate() {
        if (!mIsGloballyEnabled) return;
        long now = SystemClock.uptimeMillis();
        if (now - mLastVibrate >= VIBRATE_DELAY_MS) {
            Fun.shake(mContext, VIBRATE_LENGTH_MS);
            mLastVibrate = now;
        }
    }
}
