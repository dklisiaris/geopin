package gr.upatras.ceid.geopin.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import gr.upatras.ceid.geopin.R;
import gr.upatras.ceid.geopin.adapters.CategoryAdapter;
import gr.upatras.ceid.geopin.db.DBHandler;
import gr.upatras.ceid.geopin.db.models.Category;
import gr.upatras.ceid.geopin.db.models.Place;

/**
 * A Custom alert dialog for user to add or edit pins(places)
 */
public class EditPinDialog {
    private Context mContext;
    private Dialog mDialog;
    private LayoutInflater mInflater;

    private SubmitListener mSubmitListener;

    private Place mPlace;

    public EditPinDialog(Context context, Place place, SubmitListener submitListener) {
        this.mContext = context;
        this.mPlace = place;
        this.mSubmitListener = submitListener;


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


        final Button submitButton = (Button) infoView.findViewById(R.id.btn_submit);
        final Button cancelButton = (Button) infoView.findViewById(R.id.btn_cancel);

        final EditText titleEdit          = (EditText)infoView.findViewById(R.id.title_edit);
        final EditText descriptionEdit    = (EditText)infoView.findViewById(R.id.description_edit);

        final Spinner spinner = (Spinner) infoView.findViewById(R.id.categories_spinner);
        loadSpinnerData(spinner);

        //If place has an id, then its edit action.
        if(mPlace.getId()>0){
            titleEdit.setText(mPlace.getTitle());

            if(mPlace.getDescription()!=null)
                descriptionEdit.setText(mPlace.getDescription());
        }

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

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                mPlace.setTitle(titleEdit.getText().toString());
                mPlace.setDescription(descriptionEdit.getText().toString());

                if (mPlace.isValid()){
                    mDialog.dismiss();
                    mSubmitListener.onSubmit(mPlace);
                }
                else
                {
                    if(titleEdit.getText().length()==0)
                        titleEdit.setError(mContext.getString(R.string.title_required));
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                mDialog.dismiss();
            }
        });

        mDialog.show();
    }

    /**
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerData(Spinner s) {
        // database handler
        DBHandler db = DBHandler.getInstance(mContext);

        // Spinner Drop down elements
        List<Category> categories = db.getAllCategories();

        CategoryAdapter adapter = new CategoryAdapter(categories, mContext);

        // apply the Adapter:
        s.setAdapter(adapter);

        // Set default selection.
        // If place has an id, then its edit action.
        if(mPlace.getId()>0){
            s.setSelection(adapter.getItemPositionById(mPlace.getCategory_id()));
        }
        else{
            // Default selected category is the first one.
            mPlace.setCategory_id(categories.get(0).getId());
        }

        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * Called when a new item was selected (in the Spinner)
             */
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {
                Category c = (Category) parent.getItemAtPosition(pos);
                mPlace.setCategory_id(c.getId());

            }

            public void onNothingSelected(AdapterView parent) {
                // Do nothing.
            }
        });
    }

    public interface SubmitListener {
        public void onSubmit(Place place);
    }

}
