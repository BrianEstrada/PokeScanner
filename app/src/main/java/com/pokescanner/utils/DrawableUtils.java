package com.pokescanner.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokescanner.R;
import com.pokescanner.helper.Settings;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by admin on 23-07-2016.
 */
public class DrawableUtils
{
    public static final int PokemonType = 6;
    public static final int PokeStopType = 4;
    public static final int LuredPokeStopType = 5;
    public static final int RedGymType = 2;
    public static final int BlueGymType = 1;
    public static final int YellowGymType = 3;
    public static final int NeutralGymType = 0;

    public static String getExpireTime(long expireTime) {
        //Create a date from the expire time (Long value)
        DateTime date = new DateTime(expireTime);
        //If our date time is after now then it's expired and we'll return expired (So we don't get an exception
        if (date.isAfter(new Instant())) {
            Interval interval;
            interval = new Interval(new Instant(), date);
            //turn our interval into MM:SS
            DateTime dt = new DateTime(interval.toDurationMillis());
            DateTimeFormatter fmt = DateTimeFormat.forPattern("mm:ss");
            return fmt.print(dt);
        }else
        {
            return "Expired";
        }
    }
    public static  Bitmap getBitmap(Context context, String URI) {
        int unitScale = Settings.get(context).getScale();
        int resourceID = context.getResources().getIdentifier(URI, "drawable", context.getPackageName());
        return DrawableUtils.getBitmapFromView(resourceID, "", context,100);
    }

    public static int getResourceID(int pokemonid,Context context) {
        String uri = "p" + pokemonid;
        int resourceID = context.getResources().getIdentifier(uri, "drawable", context.getPackageName());
        return resourceID;
    }

    public static Bitmap getBitmapFromView(int drawableId, String text, Context context,int type) {
        int scale = Settings.get(context).getScale();

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View pokeView = inflater.inflate(R.layout.custom_marker, null);

        TextView timer = (TextView) pokeView.findViewById(R.id.timer);
        ImageView icon = (ImageView) pokeView.findViewById(R.id.icon);

        if(Settings.get(context).isUseOldMapMarker()){
            timer.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            timer.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            timer.setTypeface(Typeface.create("Helvetica", Typeface.BOLD));
            timer.setTextSize(convertToPixels(context, 7));
        }

        timer.setBackgroundColor(getColorFromType(type,context));

        timer.setText(text);
        icon.setImageBitmap(bm);

        if (timer.length() == 0)
        {
            timer.setVisibility(View.GONE);
        }

        pokeView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        pokeView.layout(0, 0, pokeView.getMeasuredWidth(), pokeView.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(pokeView.getMeasuredWidth(), pokeView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        pokeView.layout(0, 0, pokeView.getMeasuredWidth(), pokeView.getMeasuredHeight());
        pokeView.draw(c);
        bitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth()/scale,bitmap.getHeight()/scale,false);
        return bitmap;
    }

    public static int getColorFromType(int unitType, Context context) {
        switch(unitType){
            case PokemonType:
                return ResourcesCompat.getColor(context.getResources(),R.color.IconPokemon,null);
            case PokeStopType:
                return ResourcesCompat.getColor(context.getResources(),R.color.IconPokeStop,null);
            case LuredPokeStopType:
                return ResourcesCompat.getColor(context.getResources(),R.color.IconPokeLure,null);
            case BlueGymType:
                return ResourcesCompat.getColor(context.getResources(),R.color.GymBlue,null);
            case RedGymType:
                return ResourcesCompat.getColor(context.getResources(),R.color.GymRed,null);
            case YellowGymType:
                return ResourcesCompat.getColor(context.getResources(),R.color.GymYellow,null);
            case NeutralGymType:
                return ResourcesCompat.getColor(context.getResources(),R.color.GymNeutral,null);
            default:
                return ResourcesCompat.getColor(context.getResources(),R.color.IconPokemon,null);
        }
    }
    public static int convertToPixels(Context context, int nDP) {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;

    }
}
