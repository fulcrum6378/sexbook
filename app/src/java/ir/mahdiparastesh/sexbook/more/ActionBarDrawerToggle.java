package ir.mahdiparastesh.sexbook.more;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import androidx.annotation.DoNotInline;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class ActionBarDrawerToggle implements DrawerLayout.DrawerListener {

    public interface DelegateProvider {
        @Nullable
        Delegate getDrawerToggleDelegate();
    }

    public interface Delegate {
        void setActionBarUpIndicator(Drawable upDrawable, @StringRes int contentDescRes);

        void setActionBarDescription(@StringRes int contentDescRes);

        Drawable getThemeUpIndicator();

        Context getActionBarThemedContext();

        boolean isNavigationVisible();
    }

    private final Delegate mActivityImpl;
    private final DrawerLayout mDrawerLayout;

    private DrawerArrowDrawable mSlider;
    private boolean mDrawerSlideAnimationEnabled = true;
    private Drawable mHomeAsUpIndicator;
    boolean mDrawerIndicatorEnabled = true;
    private boolean mHasCustomUpIndicator;
    private final int mOpenDrawerContentDescRes;
    private final int mCloseDrawerContentDescRes;
    View.OnClickListener mToolbarNavigationClickListener;
    private boolean mWarnedForDisplayHomeAsUp = false;

    public ActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout,
                                 @StringRes int openDrawerContentDescRes,
                                 @StringRes int closeDrawerContentDescRes) {
        this(activity, null, drawerLayout, null, openDrawerContentDescRes,
                closeDrawerContentDescRes);
    }

    public ActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout,
                                 Toolbar toolbar, @StringRes int openDrawerContentDescRes,
                                 @StringRes int closeDrawerContentDescRes) {
        this(activity, toolbar, drawerLayout, null, openDrawerContentDescRes,
                closeDrawerContentDescRes);
    }

    ActionBarDrawerToggle(Activity activity, Toolbar toolbar, DrawerLayout drawerLayout,
                          DrawerArrowDrawable slider, @StringRes int openDrawerContentDescRes,
                          @StringRes int closeDrawerContentDescRes) {
        if (toolbar != null) {
            mActivityImpl = new ToolbarCompatDelegate(toolbar);
            toolbar.setNavigationOnClickListener(v -> {
                if (mDrawerIndicatorEnabled) toggle();
                else if (mToolbarNavigationClickListener != null)
                    mToolbarNavigationClickListener.onClick(v);
            });
        } else if (activity instanceof DelegateProvider) // Allow the Activity to provide an impl
            mActivityImpl = ((DelegateProvider) activity).getDrawerToggleDelegate();
        else mActivityImpl = new FrameworkActionBarDelegate(activity);

        mDrawerLayout = drawerLayout;
        mOpenDrawerContentDescRes = openDrawerContentDescRes;
        mCloseDrawerContentDescRes = closeDrawerContentDescRes;
        if (slider == null)
            mSlider = new DrawerArrowDrawable(mActivityImpl.getActionBarThemedContext());
        else mSlider = slider;

        mHomeAsUpIndicator = getThemeUpIndicator();
    }

    public void syncState() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) setPosition(1);
        else setPosition(0);
        if (mDrawerIndicatorEnabled) {
            setActionBarUpIndicator(mSlider,
                    mDrawerLayout.isDrawerOpen(GravityCompat.START) ?
                            mCloseDrawerContentDescRes : mOpenDrawerContentDescRes);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (!mHasCustomUpIndicator) mHomeAsUpIndicator = getThemeUpIndicator();
        syncState();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null && item.getItemId() == android.R.id.home && mDrawerIndicatorEnabled) {
            toggle();
            return true;
        }
        return false;
    }

    void toggle() {
        int drawerLockMode = mDrawerLayout.getDrawerLockMode(GravityCompat.START);
        if (mDrawerLayout.isDrawerVisible(GravityCompat.START)
                && (drawerLockMode != DrawerLayout.LOCK_MODE_LOCKED_OPEN))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else if (drawerLockMode != DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            mDrawerLayout.openDrawer(GravityCompat.START);
    }

    public void setHomeAsUpIndicator(Drawable indicator) {
        if (indicator == null) {
            mHomeAsUpIndicator = getThemeUpIndicator();
            mHasCustomUpIndicator = false;
        } else {
            mHomeAsUpIndicator = indicator;
            mHasCustomUpIndicator = true;
        }

        if (!mDrawerIndicatorEnabled)
            setActionBarUpIndicator(mHomeAsUpIndicator, 0);
    }

    public void setHomeAsUpIndicator(int resId) {
        Drawable indicator = null;
        if (resId != 0) indicator = ContextCompat.getDrawable(mDrawerLayout.getContext(), resId);
        setHomeAsUpIndicator(indicator);
    }

    public boolean isDrawerIndicatorEnabled() {
        return mDrawerIndicatorEnabled;
    }

    public void setDrawerIndicatorEnabled(boolean enable) {
        if (enable != mDrawerIndicatorEnabled) {
            if (enable) {
                setActionBarUpIndicator(mSlider,
                        mDrawerLayout.isDrawerOpen(GravityCompat.START) ?
                                mCloseDrawerContentDescRes : mOpenDrawerContentDescRes);
            } else setActionBarUpIndicator(mHomeAsUpIndicator, 0);
            mDrawerIndicatorEnabled = enable;
        }
    }

    @NonNull
    public DrawerArrowDrawable getDrawerArrowDrawable() {
        return mSlider;
    }

    public void setDrawerArrowDrawable(@NonNull DrawerArrowDrawable drawable) {
        mSlider = drawable;
        syncState();
    }

    public void setDrawerSlideAnimationEnabled(boolean enabled) {
        mDrawerSlideAnimationEnabled = enabled;
        if (!enabled) setPosition(0);
    }

    public boolean isDrawerSlideAnimationEnabled() {
        return mDrawerSlideAnimationEnabled;
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        if (mDrawerSlideAnimationEnabled)
            setPosition(Math.min(1f, Math.max(0, slideOffset)));
        else setPosition(0); // disable animation.
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {
        setPosition(1);
        if (mDrawerIndicatorEnabled) setActionBarDescription(mCloseDrawerContentDescRes);
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        setPosition(0);
        if (mDrawerIndicatorEnabled) setActionBarDescription(mOpenDrawerContentDescRes);
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }

    public View.OnClickListener getToolbarNavigationClickListener() {
        return mToolbarNavigationClickListener;
    }

    public void setToolbarNavigationClickListener(
            View.OnClickListener onToolbarNavigationClickListener) {
        mToolbarNavigationClickListener = onToolbarNavigationClickListener;
    }

    void setActionBarUpIndicator(Drawable upDrawable, int contentDescRes) {
        if (!mWarnedForDisplayHomeAsUp && !mActivityImpl.isNavigationVisible()) {
            Log.w("ActionBarDrawerToggle", "DrawerToggle may not show up because NavigationIcon"
                    + " is not visible. You may need to call "
                    + "actionbar.setDisplayHomeAsUpEnabled(true);");
            mWarnedForDisplayHomeAsUp = true;
        }
        mActivityImpl.setActionBarUpIndicator(upDrawable, contentDescRes);
    }

    void setActionBarDescription(int contentDescRes) {
        mActivityImpl.setActionBarDescription(contentDescRes);
    }

    Drawable getThemeUpIndicator() {
        return mActivityImpl.getThemeUpIndicator();
    }

    private void setPosition(float position) {
        if (position == 1f)
            mSlider.setVerticalMirror(true);
        else if (position == 0f)
            mSlider.setVerticalMirror(false);
        mSlider.setProgress(position);
    }

    private static class FrameworkActionBarDelegate implements Delegate {
        private final Activity mActivity;

        FrameworkActionBarDelegate(Activity activity) {
            mActivity = activity;
        }

        @Override
        public Drawable getThemeUpIndicator() {
            final TypedArray a = getActionBarThemedContext().obtainStyledAttributes(null,
                    new int[]{android.R.attr.homeAsUpIndicator},
                    android.R.attr.actionBarStyle, 0);
            final Drawable result = a.getDrawable(0);
            a.recycle();
            return result;
        }

        @Override
        public Context getActionBarThemedContext() {
            final ActionBar actionBar = mActivity.getActionBar();
            if (actionBar != null) return actionBar.getThemedContext();
            return mActivity;
        }

        @Override
        public boolean isNavigationVisible() {
            final ActionBar actionBar = mActivity.getActionBar();
            return actionBar != null
                    && (actionBar.getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP) != 0;
        }

        @Override
        public void setActionBarUpIndicator(Drawable themeImage, int contentDescRes) {
            final ActionBar actionBar = mActivity.getActionBar();
            if (actionBar != null) {
                FrameworkActionBarDelegate.Api18Impl.setHomeAsUpIndicator(actionBar, themeImage);
                FrameworkActionBarDelegate.Api18Impl.setHomeActionContentDescription(actionBar, contentDescRes);
            }
        }

        @Override
        public void setActionBarDescription(int contentDescRes) {
            final ActionBar actionBar = mActivity.getActionBar();
            if (actionBar != null) {
                FrameworkActionBarDelegate.Api18Impl.setHomeActionContentDescription(actionBar, contentDescRes);
            }
        }

        static class Api18Impl {
            private Api18Impl() {
                // This class is not instantiable.
            }

            @DoNotInline
            static void setHomeActionContentDescription(ActionBar actionBar, int resId) {
                actionBar.setHomeActionContentDescription(resId);
            }

            @DoNotInline
            static void setHomeAsUpIndicator(ActionBar actionBar, Drawable indicator) {
                actionBar.setHomeAsUpIndicator(indicator);
            }

        }
    }

    static class ToolbarCompatDelegate implements Delegate {

        final Toolbar mToolbar;
        final Drawable mDefaultUpIndicator;
        final CharSequence mDefaultContentDescription;

        ToolbarCompatDelegate(Toolbar toolbar) {
            mToolbar = toolbar;
            mDefaultUpIndicator = toolbar.getNavigationIcon();
            mDefaultContentDescription = toolbar.getNavigationContentDescription();
        }

        @Override
        public void setActionBarUpIndicator(Drawable upDrawable, @StringRes int contentDescRes) {
            mToolbar.setNavigationIcon(upDrawable);
            setActionBarDescription(contentDescRes);
        }

        @Override
        public void setActionBarDescription(@StringRes int contentDescRes) {
            if (contentDescRes == 0) {
                mToolbar.setNavigationContentDescription(mDefaultContentDescription);
            } else {
                mToolbar.setNavigationContentDescription(contentDescRes);
            }
        }

        @Override
        public Drawable getThemeUpIndicator() {
            return mDefaultUpIndicator;
        }

        @Override
        public Context getActionBarThemedContext() {
            return mToolbar.getContext();
        }

        @Override
        public boolean isNavigationVisible() {
            return true;
        }
    }
}
