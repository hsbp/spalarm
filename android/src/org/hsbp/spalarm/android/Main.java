package org.hsbp.spalarm.android;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;
import android.widget.TextView;
import yuku.ambilwarna.AmbilWarnaDialog;

public class Main extends Activity implements
    AmbilWarnaDialog.OnAmbilWarnaListener, TimePickerDialog.OnTimeSetListener
{
    private int currentColor = Color.WHITE;
    private int hourOfDay = 6;
    private int minuteOfHour = 20;
    private final static String CURRENT_COLOR = "org.hsbp.spalarm.android.Main.CURRENT_COLOR";
    private final static String HOUR_OF_DAY = "org.hsbp.spalarm.android.Main.HOUR_OF_DAY";
    private final static String MINUTE_OF_HOUR = "org.hsbp.spalarm.android.Main.MINUTE_OF_HOUR";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        updatePreview();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putInt(CURRENT_COLOR, currentColor);
        outState.putInt(HOUR_OF_DAY, hourOfDay);
        outState.putInt(MINUTE_OF_HOUR, minuteOfHour);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        currentColor = savedInstanceState.getInt(CURRENT_COLOR);
        hourOfDay = savedInstanceState.getInt(HOUR_OF_DAY);
        minuteOfHour = savedInstanceState.getInt(MINUTE_OF_HOUR);
        super.onRestoreInstanceState(savedInstanceState);
        updatePreview();
    }

    private void updatePreview() {
        final TextView preview = (TextView)findViewById(R.id.preview);
        preview.setText(String.format("%d:%02d", hourOfDay, minuteOfHour));
        preview.setTextColor(isDarkColor(currentColor) ? Color.WHITE : Color.BLACK);
        preview.setBackgroundColor(currentColor);
    }

    private static boolean isDarkColor(int color) {
        final float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return hsv[2] < 0.5;
    }

    public void selectColor(final View v) {
        new AmbilWarnaDialog(this, currentColor, this).show();
    }

    public void selectTime(final View v) {
        new TimePickerDialog(this, this, hourOfDay, minuteOfHour, true).show();
    }

    @Override
    public void onOk(final AmbilWarnaDialog dialog, final int color) {
        currentColor = color;
        updatePreview();
    }

    @Override
    public void onCancel(final AmbilWarnaDialog dialog) { /* ignore */ }

    @Override
    public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
        this.hourOfDay = hourOfDay;
        this.minuteOfHour = minute;
        updatePreview();
    }
}
