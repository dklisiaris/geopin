package gr.upatras.ceid.geopin.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import gr.upatras.ceid.geopin.R;
import gr.upatras.ceid.geopin.maps.Step;

public class InstructionsAdapter extends ArrayAdapter<Step> {
    private Context mContext                = null;
    private static LayoutInflater mInflater = null;

    public InstructionsAdapter(Context context){
        super(context, R.layout.activity_main_map);
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<Step> data) {
        clear();
        if (data != null) {
            for (Step entry : data) {
                add(entry);
            }
        }
    }

    /**
     * Populate new items in the list.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.instructions_list_item, parent, false);
        } else {
            view = convertView;
        }

        Step item = getItem(position);
        ((TextView)view.findViewById(R.id.instruction)).setText(Html.fromHtml(item.getHtmlInstructions()));
        ((TextView)view.findViewById(R.id.duration)).setText(item.getDurationText());
        ((TextView)view.findViewById(R.id.distance)).setText(item.getDistanceText());


        return view;
    }

}
