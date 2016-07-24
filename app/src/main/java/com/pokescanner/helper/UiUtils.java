package com.pokescanner.helper;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


public class UiUtils {
    public static void hideKeyboard(EditText editText) {
        ((InputMethodManager) editText.getContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}
