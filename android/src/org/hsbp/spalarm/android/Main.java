package org.hsbp.spalarm.android;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import yuku.ambilwarna.AmbilWarnaDialog;

public class Main extends Activity implements AmbilWarnaDialog.OnAmbilWarnaListener
{
    private int currentColor = Color.WHITE;
    private final static String CURRENT_COLOR = "org.hsbp.spalarm.android.Main.CURRENT_COLOR";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putInt(CURRENT_COLOR, currentColor);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        currentColor = savedInstanceState.getInt(CURRENT_COLOR);
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void selectColor(final View v) {
        new AmbilWarnaDialog(this, currentColor, this).show();
    }

    @Override
    public void onOk(final AmbilWarnaDialog dialog, final int color) {
        currentColor = color;
    }

    @Override
    public void onCancel(final AmbilWarnaDialog dialog) { /* ignore */ }
}
