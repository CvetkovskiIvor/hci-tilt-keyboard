package com.example.circularkeyboard;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;



public class IME_RollingMarble extends InputMethodService
        implements  KeyboardView.OnKeyboardActionListener {

    // Basic stuff:
    Context context;
    LayoutInflater inflater;
    KeyboardLayout layout;

    // Keyboard height: hardcoded here, but we can make it a SP-based user-defined value:
    float KEYBOARD_HEIGHT = 0.25f;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        inflater = getLayoutInflater();

        // no dim support
        getWindow().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // show your own keyboard layout:
        layout = new KeyboardLayout(inflater,
                context,
                this.getCurrentInputConnection(),
                KEYBOARD_HEIGHT);
        setInputView(layout);
        updateInputViewShown();

        // all application logic will be performed in respective keyboard layout!!!
    }




    @Override
    public View onCreateInputView() {
        return null;
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeUp() {
    }

    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onWindowShown() {
        // if window is redrawn, re-load keyboard layout:
        layout = new KeyboardLayout(inflater,
                context,
                this.getCurrentInputConnection(),
                KEYBOARD_HEIGHT);
        setInputView(layout);
        updateInputViewShown();
    }

    @Override
    public void onWindowHidden() {
        // Save the battery.
        // If window is hidden, no need to check sensor:
        if (layout!=null)
            layout.removeListener();
    }

}