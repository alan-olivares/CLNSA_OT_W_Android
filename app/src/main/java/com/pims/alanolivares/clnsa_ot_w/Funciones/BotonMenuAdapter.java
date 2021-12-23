package com.pims.alanolivares.clnsa_ot_w.Funciones;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.pims.alanolivares.clnsa_ot_w.R;

/**
 * <p>Clase que extiende de BaseAdapter para la creación de los menus
 * en MenuRelleno y MenuLlenado
 * </p>
 *
 * @author Alan Israel Olivares Mora
 * @version v1.0
 *
 */
public class BotonMenuAdapter extends BaseAdapter {
    private Context context;
    private String[] menuText;
    private int[] images;

    public BotonMenuAdapter(Context context, String[] menuText, int[] images) {
        this.context = context;
        this.menuText = menuText;
        this.images = images;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater vi = LayoutInflater.from(context);
            convertView =vi.inflate(R.layout.vista_cuadrada,null);
            TextView menu_text = convertView.findViewById(R.id.descripcion);
            menu_text.setText(menuText[position]);
            ImageView packageImage = convertView.findViewById(R.id.imagen);
            packageImage.setImageResource(images[position]);
            final View finalConvertView = convertView;
            packageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((GridView) parent).performItemClick(finalConvertView, position, 0);
                }
            });
        }
        return convertView;
    }

}