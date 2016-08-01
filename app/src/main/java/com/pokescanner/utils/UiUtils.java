package com.pokescanner.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.pokescanner.helper.ExpirationFilter;
import com.pokescanner.helper.PokemonListLoader;
import com.pokescanner.objects.FilterItem;
import com.pokescanner.objects.Pokemons;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static com.pokescanner.helper.Generation.hexagonal_number;


public class UiUtils {
    public static final int BASE_DELAY = 5000;
    public static void hideKeyboard(EditText editText) {
        ((InputMethodManager) editText.getContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static String getSearchTime(int val,Context context) {
        int calculatedValue = hexagonal_number(val) * BASE_DELAY;
        long millis = calculatedValue;
        DateTime dt = new DateTime(millis);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("mm:ss");
        return fmt.print(dt);
    }

    public static boolean isPokemonExpiredFiltered(Pokemons pokemons,Context context) {
        long millis = ExpirationFilter.getFilter(context).getPokemonExpirationMinSec() * BASE_DELAY;
        //Create a date from the expire time (Long value)
        DateTime date = new DateTime(pokemons.getExpires());
        //If our date time is after now then it's expired and we'll return expired (So we don't get an exception
        if (date.isAfter(new Instant())) {
            Interval interval;
            interval = new Interval(new Instant(), date);

            return millis > interval.toDurationMillis();
        }
        return false;
    }

    public static boolean isPokemonFiltered(Pokemons pokemons) {
        //lol this is really long but it's simple and to the point
        return PokemonListLoader.getFilteredList().contains(new FilterItem(pokemons.getNumber()));
    }

    public static boolean isPokemonFiltered(int number) {
        //lol this is really long but it's simple and to the point
        return PokemonListLoader.getFilteredList().contains(new FilterItem(number));
    }
}
