package gr.upatras.ceid.geopin.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;

public class MultiSpinner extends Spinner implements OnMultiChoiceClickListener, OnCancelListener{

    private List<String> items;
    private boolean[] selected;
    private String defaultText;
    private MultiSpinnerListener listener;
    private ArrayAdapter<String> adapter;

    public MultiSpinner(Context context) {
        super(context);
    }

    public MultiSpinner(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
    }

    public MultiSpinner(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if(which==0 && isChecked){
            selected[0] = true;
            for(int i=1;i<selected.length;i++){
                selected[i] = false;
                ((AlertDialog) dialog).getListView().setItemChecked(i, false);
            }
        }
        else if(which==0 && !isChecked){
            selected[0] = true;
            ((AlertDialog) dialog).getListView().setItemChecked(0, false);
        }
        else if (which!=0 && isChecked){
            selected[0] = false;
            ((AlertDialog) dialog).getListView().setItemChecked(0, false);
            selected[which] = true;
        }
        else{
            selected[which] = false;
            boolean othersSelected = false;
            for(int i=1;i<selected.length;i++){
                if(selected[i] == true){
                    othersSelected = true;
                    break;
                }
            }
            if(!othersSelected){
                selected[0] = true;
                ((AlertDialog) dialog).getListView().setItemChecked(0, true);
            }
        }

        //adapter.notifyDataSetChanged();
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        // refresh text on spinner
        StringBuffer spinnerBuffer = new StringBuffer();
        boolean someUnselected = false;
        for (int i = 0; i < items.size(); i++) {
            if (selected[i] == true) {
                spinnerBuffer.append(items.get(i));
                spinnerBuffer.append(", ");
            } else {
                someUnselected = true;
            }
        }
        String spinnerText;
        if (someUnselected) {
            spinnerText = spinnerBuffer.toString();
            if (spinnerText.length() > 2)
                spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
        } else {
            spinnerText = defaultText;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[] { spinnerText });
        setAdapter(adapter);
        listener.onItemsSelected(selected);
    }

    @Override
    public boolean performClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(
                items.toArray(new CharSequence[items.size()]), selected, this);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.setOnCancelListener(this);
        builder.show();
        return true;
    }

    public void setItems(List<String> items, String allText,
                         MultiSpinnerListener listener) {
        this.items = items;
        this.defaultText = allText;
        this.listener = listener;

        // only zero position (all) is selected by default
        selected = new boolean[items.size()];
        for (int i = 0; i < selected.length; i++){
            if(i==0)selected[i] = true;
            else selected[i] = false;
        }

        // all text on the spinner
        adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, new String[] { allText });
        setAdapter(adapter);
    }


    public boolean[] getSelected() {
        return selected;
    }

    public void setSelected(boolean[] selected) {
        this.selected = selected;
    }



    public interface MultiSpinnerListener {
        public void onItemsSelected(boolean[] selected);
    }

}