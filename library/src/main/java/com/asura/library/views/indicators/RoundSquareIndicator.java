package com.asura.library.views.indicators;

import android.content.Context;
import android.os.Build;

import androidx.core.content.res.ResourcesCompat;

import com.asura.library.R;


public class RoundSquareIndicator extends IndicatorShape {

    public RoundSquareIndicator(Context context, int indicatorSize, boolean mustAnimateChanges) {
        super(context, indicatorSize, mustAnimateChanges);
        setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.indicator_round_square_unselected, null));
    }

    @Override
    public void onCheckedChange(boolean isChecked) {
        super.onCheckedChange(isChecked);
        if (isChecked) {
            setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.indicator_round_square_selected, null));
        } else {
            setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.indicator_round_square_unselected, null));
        }
    }
}
