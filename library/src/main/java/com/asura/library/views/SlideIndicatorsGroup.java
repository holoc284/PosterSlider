package com.asura.library.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.asura.library.R;
import com.asura.library.events.OnSlideChangeListener;
import com.asura.library.views.indicators.CircleIndicator;
import com.asura.library.views.indicators.DashIndicator;
import com.asura.library.views.indicators.IndicatorShape;
import com.asura.library.views.indicators.RoundSquareIndicator;
import com.asura.library.views.indicators.SquareIndicator;

import java.util.ArrayList;
import java.util.List;

public class SlideIndicatorsGroup extends LinearLayout implements OnSlideChangeListener {
    private final Context context;
    private int slidesCount;
    private Drawable selectedSlideIndicator;
    private Drawable unselectedSlideIndicator;
    private int defaultIndicator;
    private int indicatorSize;
    private boolean mustAnimateIndicators = true;
    private List<IndicatorShape> indicatorShapes = new ArrayList<>();

    public SlideIndicatorsGroup(Context context, Drawable selectedSlideIndicator, Drawable unselectedSlideIndicator, int defaultIndicator, int indicatorSize, boolean mustAnimateIndicators) {
        super(context);
        this.context = context;
        this.selectedSlideIndicator = selectedSlideIndicator;
        this.unselectedSlideIndicator = unselectedSlideIndicator;
        this.defaultIndicator = defaultIndicator;
        this.indicatorSize = indicatorSize;
        this.mustAnimateIndicators = mustAnimateIndicators;
        setup();
    }

    public void setSlides(int slidesCount) {
        removeAllViews();
        indicatorShapes.clear();
        this.slidesCount = 0;
        for (int i = 0; i < slidesCount; i++) {
            onSlideAdd();
        }
        this.slidesCount = slidesCount;
    }

    public void onSlideAdd() {
        this.slidesCount += 1;
        addIndicator();
    }

    private void addIndicator() {
        IndicatorShape indicatorShape;
        if (selectedSlideIndicator != null && unselectedSlideIndicator != null) {
            indicatorShape = new IndicatorShape(context, indicatorSize, mustAnimateIndicators) {
                @Override
                public void onCheckedChange(boolean isChecked) {
                    super.onCheckedChange(isChecked);
                    if (isChecked) {
                        setBackground(selectedSlideIndicator);
                    } else {
                        setBackground(unselectedSlideIndicator);
                    }
                }
            };
            indicatorShape.setBackground(unselectedSlideIndicator);
            indicatorShapes.add(indicatorShape);
            addView(indicatorShape);

        } else {
            switch (defaultIndicator) {
                case IndicatorShape.SQUARE:
                    indicatorShape = new SquareIndicator(context, indicatorSize, mustAnimateIndicators);
                    indicatorShapes.add(indicatorShape);
                    addView(indicatorShape);
                    break;
                case IndicatorShape.ROUND_SQUARE:
                    indicatorShape = new RoundSquareIndicator(context, indicatorSize, mustAnimateIndicators);
                    indicatorShapes.add(indicatorShape);
                    addView(indicatorShape);
                    break;
                case IndicatorShape.DASH:
                    indicatorShape = new DashIndicator(context, indicatorSize, mustAnimateIndicators);
                    indicatorShapes.add(indicatorShape);
                    addView(indicatorShape);
                    break;

                case IndicatorShape.CIRCLE:
                    indicatorShape = new CircleIndicator(context, indicatorSize, mustAnimateIndicators);
                    indicatorShapes.add(indicatorShape);
                    addView(indicatorShape);
                    break;
                default:
                    break;
            }
        }
    }

    public void setup() {
        setOrientation(LinearLayout.HORIZONTAL);
        setLayoutDirection(LAYOUT_DIRECTION_LTR);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, indicatorSize * 2);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        int margin = getResources().getDimensionPixelSize(R.dimen.default_indicator_margins) * 2;
        layoutParams.setMargins(0, 0, 0, margin);
        setLayoutParams(layoutParams);
    }

    @Override
    public void onSlideChange(int selectedSlidePosition) {
        for (int i = 0; i < indicatorShapes.size(); i++) {
            if (i == selectedSlidePosition) {
                indicatorShapes.get(i).onCheckedChange(true);
            } else {
                indicatorShapes.get(i).onCheckedChange(false);
            }
        }
    }

    public void changeIndicator(int defaultIndicator) {
        this.defaultIndicator = defaultIndicator;
        selectedSlideIndicator = null;
        unselectedSlideIndicator = null;
        setSlides(slidesCount);
    }

    public void changeIndicator(Drawable selectedSlideIndicator, Drawable unselectedSlideIndicator) {
        this.selectedSlideIndicator = selectedSlideIndicator;
        this.unselectedSlideIndicator = unselectedSlideIndicator;
        setSlides(slidesCount);
    }

    public void setMustAnimateIndicators(boolean mustAnimateIndicators){
        this.mustAnimateIndicators=mustAnimateIndicators;
        for (IndicatorShape indicatorShape :
                indicatorShapes) {
            indicatorShape.setMustAnimateChange(mustAnimateIndicators);
        }
    }
}
