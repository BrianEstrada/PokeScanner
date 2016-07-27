package com.pokescanner.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.pokescanner.helper.PokemonListLoader;
import com.pokescanner.objects.FilterItem;
import com.pokescanner.objects.Pokemons;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static com.pokescanner.helper.Generation.hexagonal_number;


public class UiUtils {
    public static void hideKeyboard(EditText editText) {
        ((InputMethodManager) editText.getContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static String getSearchTime(int val,Context context) {
        int calculatedValue = hexagonal_number(val) * SettingsUtil.getSettings(context).getServerRefresh() * 1000;
        long millis = calculatedValue;
        DateTime dt = new DateTime(millis);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("mm:ss");
        return fmt.print(dt);
    }

    public static boolean isPokemonFiltered(Pokemons pokemons) {
        //lol this is really long but it's simple and to the point
        return PokemonListLoader.getFilteredList().contains(new FilterItem(pokemons.getNumber()));
    }
}
