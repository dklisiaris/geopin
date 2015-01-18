package gr.upatras.ceid.geopin.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import gr.upatras.ceid.geopin.R;

/**
 * A Custom alert dialog for user to add or edit pins(places)
 */
public class EditPinDialog {
    private Activity mContext;
    private Dialog mDialog;
    private LayoutInflater mInflater;

    public EditPinDialog(Activity context) {
        this.mContext = context;

    }

    /**
     * Shows a dialog for the user to add or edit a place's attributes.
     */
    @SuppressLint("NewApi")
    public void show() {
        this.mInflater = LayoutInflater.from(mContext);
        final View infoView = mInflater.inflate(R.layout.edit_pin_dialog, null);

//        TextView dTitle = (TextView)infoView.findViewById(R.id.dialog_title);
//        TextView title = (TextView)infoView.findViewById(R.id.title);


        Button dialogButton = (Button) infoView.findViewById(R.id.btn_cancel);


//        dTitle.setText("Πληροφορίες");
//        title.setText("Αναλυτικές Πληροφορίες");

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        } else {
            builder = new AlertDialog.Builder(mContext);
        }

        builder.setView(infoView);

        mDialog = builder.create();
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);



        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                mDialog.dismiss();
            }
        });

        mDialog.show();
    }

}
