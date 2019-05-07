package root.example.com.monggopinarak.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import root.example.com.monggopinarak.DataModel.getDataMenu;
import root.example.com.monggopinarak.R;

public class OrderAdapter extends ArrayAdapter<getDataMenu> {

    private ArrayList<getDataMenu> dataSet;
    Context mContext;

    private static class ViewHolder {
        TextView MenuName, MenuPrice, tvAdd;
    }

    public OrderAdapter(ArrayList<getDataMenu> data, Context context) {
        super(context, R.layout.listview_order, data);
        this.dataSet = data;
        this.mContext = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        getDataMenu dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        OrderAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listview_order, parent, false);
            viewHolder.MenuName = (TextView) convertView.findViewById(R.id.tvMenuName);
            viewHolder.MenuPrice = (TextView) convertView.findViewById(R.id.tvMenuPrice);
            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (OrderAdapter.ViewHolder) convertView.getTag();
            result = convertView;
        }
        viewHolder.MenuName.setText(dataModel.getName());
        viewHolder.MenuPrice.setText(dataModel.getPrice());

        // Return the completed view to render on screen
        return convertView;
    }
}
