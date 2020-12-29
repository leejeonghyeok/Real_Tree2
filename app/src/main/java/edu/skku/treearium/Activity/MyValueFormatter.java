package edu.skku.treearium.Activity;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;

/******************* 통계의 소숫점을 없애기 위해 사용 ***********************/
public class MyValueFormatter extends ValueFormatter {
    private DecimalFormat mFormat;

    public MyValueFormatter() {
        mFormat = new DecimalFormat("#");
    }

    @Override
    public String getFormattedValue(float value) {
        return mFormat.format(value);
    }
}
