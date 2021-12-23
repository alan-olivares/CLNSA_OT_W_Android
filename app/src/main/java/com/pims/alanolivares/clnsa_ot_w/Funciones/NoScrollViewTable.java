package com.pims.alanolivares.clnsa_ot_w.Funciones;

import android.content.Context;
import android.util.AttributeSet;
import de.codecrafters.tableview.TableView;

/**
 * <p>Clase que extiende de TableView para la modificación de la
 * librería de TableView, donde cambiamos la altura, por una altura
 * estatica de acuerdo al número de elementos que contiene
 * </p>
 *
 * @author Alan Israel Olivares Mora
 * @version v1.0
 *
 */

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
    /**
     * Método que modifica la altura de la tabla, y la calcula dependiento
     * al número de elementos que esta contiene
     *
     * @param numElem - Elementos contenidos por la tabla
     */
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
