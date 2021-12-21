package com.pims.alanolivares.clnsa_ot_w.Funciones;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.pims.alanolivares.clnsa_ot_w.R;

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
        View grid;
        if (convertView == null){
            LayoutInflater vi = LayoutInflater.from(context);
            convertView =vi.inflate(R.layout.vista_cuadrada,null);
            TextView menu_text = (TextView) convertView.findViewById(R.id.descripcion);
            menu_text.setText(menuText[position]);
            ImageView packageImage = (ImageView) convertView.findViewById(R.id.imagen);
            packageImage.setImageResource(images[position]);
            final View finalConvertView = convertView;
            packageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((GridView) parent).performItemClick(finalConvertView, position, 0);
                }
            });
        }else {
            convertView = (View) convertView;
        }
        return convertView;
    }

}