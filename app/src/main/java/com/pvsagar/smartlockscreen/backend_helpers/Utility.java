package com.pvsagar.smartlockscreen.backend_helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.pvsagar.smartlockscreen.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by aravind on 10/9/14.
 * Utility class containing miscellaneous utility functions
 */
public class Utility {

    public static void checkForNullAndThrowException(Object o){
        if(o == null){
            throw new NullPointerException("Object cannot be null.");
        }
    }

    public static boolean checkForNullAndWarn(Object o, final String LOG_TAG){
        if(o == null){
            Log.w(LOG_TAG, "Object passed is null.");
        }
        return o == null;
    }


    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if (c <= '/' || c >= ':') {
                return false;
            }
        }
        return true;
    }

    public static boolean isEqual(double a, double b, double doubleErrorTolerance){
        return Math.abs(a - b) <= doubleErrorTolerance;
    }

    public static boolean isEqual(double a, double b){
        final double defaultDoubleErrorTolerance = 0.0000449;
        return isEqual(a, b, defaultDoubleErrorTolerance);
    }

    private static final int[] colors = {
            R.color.user_pic_background1,
            R.color.user_pic_background2,
            R.color.user_pic_background3,
            R.color.user_pic_background4,
            R.color.user_pic_background5,
            R.color.user_pic_background6,
            R.color.user_pic_background7,
            R.color.user_pic_background8,
    };

    public static int getRandomColor(Context context){
        Random r = new Random();
        r.setSeed(new Date().getTime());
        int index = r.nextInt(colors.length);
        return context.getResources().getColor(colors[index]);
    }

    public static int[] getPictureColors(){
        return colors;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private static final float SHADE_FACTOR = 0.9f;

    public static int getDarkerShade(int color) {
        return Color.rgb((int) (SHADE_FACTOR * Color.red(color)),
                (int) (SHADE_FACTOR * Color.green(color)),
                (int) (SHADE_FACTOR * Color.blue(color)));
    }

    public static int getLighterShade(int color) {
        return Color.rgb((int)Math.min(0xff, (Color.red(color) / SHADE_FACTOR)),
                (int)Math.min(0xff, (Color.green(color) / SHADE_FACTOR)),
                (int)Math.min(0xff, (Color.blue(color) / SHADE_FACTOR)));
    }

    private static int[] builtInDrawables = {
            R.drawable.env_building_1,
            R.drawable.env_building_2,
            R.drawable.env_ground,
            R.drawable.env_home,
            R.drawable.env_location,
            R.drawable.env_school,
            R.drawable.env_wifi_ap
    };

    public static List<Drawable> getBuiltInPictureDrawables(Context context){
        List<Drawable> drawables = new ArrayList<Drawable>(builtInDrawables.length);
        for(int r:builtInDrawables){
            Drawable drawable = context.getResources().getDrawable(r);
            drawables.add(new BitmapDrawable(context.getResources(),Picture.getCroppedBitmap(drawableToBitmap(drawable), Color.DKGRAY, Picture.CROP_MODE_CENTER)));
        }
        return drawables;
    }
}
