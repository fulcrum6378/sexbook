package ir.mahdiparastesh.sexbook.mdtp.time;

interface TimePickerController {

    boolean is24HourMode();

    int getAccentColor();

    TimePickerDialog.Version getVersion();

    void tryVibrate();

    boolean isOutOfRange(Timepoint time, int index);

    boolean isAmDisabled();

    boolean isPmDisabled();

    Timepoint roundToNearest(Timepoint time, Timepoint.TYPE type);
}
