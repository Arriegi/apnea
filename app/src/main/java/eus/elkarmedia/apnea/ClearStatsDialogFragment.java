package eus.elkarmedia.apnea;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Jon Arriaga on 08/03/2017.
 */

public class ClearStatsDialogFragment extends DialogFragment {

    private SleepDbHelper dbHelper = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        dbHelper = new SleepDbHelper(getActivity());
        builder.setMessage(R.string.dialog_sure_clear_stats)
                .setPositiveButton(R.string.clear, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHelper.deleteSleeps(dbHelper.getWritableDatabase());
                        dialog.dismiss();
                        getActivity().recreate();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
