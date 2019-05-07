package root.example.com.monggopinarak.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.ViewGroup;

import java.util.ArrayList;

import root.example.com.monggopinarak.DataModel.getDataMenu;
import root.example.com.monggopinarak.R;

import static android.support.constraint.Constraints.TAG;

public class ListViewAdapter extends ArrayAdapter<getDataMenu> implements View.OnClickListener {

    private ArrayList<getDataMenu> dataSet;
    Context mContext;

    private static class ViewHolder {
        TextView MenuName,MenuPrice, tvAdd;
    }

    public ListViewAdapter(ArrayList<getDataMenu> data, Context context) {
        super(context, R.layout.list_view_style, data);
        this.dataSet = data;
        this.mContext = context;

    }

    @Override
    public void onClick(View v) {

        int position = (Integer) v.getTag();
        Object object = getItem(position);
        getDataMenu dataModel = (getDataMenu) object;
        Log.d(TAG, "onClick() returned: " + v.getId());

        switch (v.getId()) {

        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        getDataMenu dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_view_style, parent, false);
            viewHolder.MenuName = (TextView) convertView.findViewById(R.id.tvMenuName);
            viewHolder.MenuPrice = (TextView) convertView.findViewById(R.id.tvMenuPrice);
            viewHolder.tvAdd = (TextView) convertView.findViewById(R.id.tvAdd);
            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }
        viewHolder.MenuName.setText(dataModel.getName());
        viewHolder.MenuPrice.setText(dataModel.getPrice());
        viewHolder.tvAdd.setText("Add");

        // Return the completed view to render on screen
        return convertView;
    }
}
