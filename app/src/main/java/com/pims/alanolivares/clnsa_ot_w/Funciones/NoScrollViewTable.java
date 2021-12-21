package com.pims.alanolivares.clnsa_ot_w.Funciones;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;

import de.codecrafters.tableview.TableView;

public class NoScrollViewTable extends TableView {
    public NoScrollViewTable(Context context) {
        super(context);
    }

    public NoScrollViewTable(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    public NoScrollViewTable(Context context, AttributeSet attributes, int styleAttributes) {
        super(context, attributes, styleAttributes);
    }
    public void setAutoHeight(int numElem){
        getLayoutParams().height = 150+(100*numElem);
    }
    /*@Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasureSpec_custom = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom);
        getLayoutParams().height = getMeasuredHeight();
    }*/


}
