package com.pvsagar.smartlockscreen.services.window_helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.applogic_objects.passphrases.Pattern;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;
import com.pvsagar.smartlockscreen.baseclasses.Overlay;
import com.pvsagar.smartlockscreen.frontend_helpers.WallpaperHelper;
import com.pvsagar.smartlockscreen.receivers.AdminActions;
import com.pvsagar.smartlockscreen.services.BaseService;
import com.sagar.lockpattern_gridview.PatternGridView;
import com.sagar.lockpattern_gridview.PatternInterface;

import java.util.List;
import java.util.Timer;

/**
 * Created by aravind on 23/9/14.
 * Helper class for showing a pattern lock screen.
 */
public class PatternLockOverlay extends Overlay {
    private static final String LOG_TAG = PatternLockOverlay.class.getSimpleName();

    private static final int COLOR_INVALID_PATTERN = Color.rgb(255, 80, 50);
    private static final int COLOR_VALID_PATTERN = Color.rgb(80, 200, 70);

    RelativeLayout enterPatternLayout;
    TextView statusView;
    PatternGridView patternGridView;
    private ImageView wallpaperView;

    public PatternLockOverlay(Context context, WindowManager windowManager) {
        super(context, windowManager);
    }

    @Override
    protected WindowManager.LayoutParams buildLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_DIM_BEHIND | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                PixelFormat.TRANSLUCENT);
        params.dimAmount = 0.5f;
        params.x = 0;
        params.y = 0;
        params.systemUiVisibility = getFullScreenSystemUiVisibility();
        return params;
    }

    @Override
    protected View buildLayout() {
        final String currentPassword = AdminActions.getCurrentPassphraseString();

        if(enterPatternLayout == null) {
            enterPatternLayout = (RelativeLayout) getInflater().inflate(R.layout.enter_pattern, null);
            statusView = (TextView) enterPatternLayout.findViewById(R.id.enter_pattern_status_textview);
            patternGridView = (PatternGridView) enterPatternLayout.findViewById(R.id.enter_pattern_grid);
            wallpaperView = (ImageView) enterPatternLayout.findViewById(R.id.wallpaper_image_view);
            setSystemUiVisibility(enterPatternLayout);

            enterPatternLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!(v instanceof PatternGridView)){
                        getContext().startService(BaseService.getServiceIntent(getContext(), null,
                                BaseService.ACTION_DISMISS_PATTERN_OVERLAY_ONLY));
                    }
                }
            });
        }

        if(!SharedPreferencesHelper.isLockscreenNotificationsShown(getContext())) {
            Drawable wallpaper = WallpaperHelper.getWallpaperDrawable(getContext());
            wallpaperView.setImageDrawable(wallpaper);
        }

        patternGridView.setPatternListener(new PatternInterface.PatternListener() {
            Timer mClearTimer;
            @Override
            public void onPatternStarted() {

            }

            @Override
            public void onPatternEntered(List<Integer> pattern) {
                Pattern enteredPattern = new Pattern(pattern);
                if(enteredPattern.compareString(currentPassword)){
                    getContext().startService(BaseService.getServiceIntent(getContext(), null, BaseService.ACTION_UNLOCK));
                    patternGridView.setRingColor(COLOR_VALID_PATTERN);
                    patternGridView.setInputEnabled(false);
                } else {
                    patternGridView.setRingColor(COLOR_INVALID_PATTERN);
                    statusView.setText("Wrong pattern");
                    //TODO Set timer
                }
            }

            @Override
            public void onPatternCleared() {
                if(mClearTimer != null){
                    mClearTimer.cancel();
                    mClearTimer = null;
                }
                statusView.setText("");
            }
        });

        patternGridView.clearPattern();
        patternGridView.setInputEnabled(true);

        return enterPatternLayout;
    }

    private void setSystemUiVisibility(final View layout) {
        layout.setSystemUiVisibility(getFullScreenSystemUiVisibility());
        layout.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                setSystemUiVisibility(layout);
            }
        });
    }


    @Override
    public void execute() {
        super.execute();
    }
}
