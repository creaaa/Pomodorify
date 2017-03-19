
package com.example.masa.bizzarestrangeplayer.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ResetDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // ダイアログを生成
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        return builder.setTitle("Really reset this set?")
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

//                                countDown.cancel();
//
//                                state = TimerState.Standby;
//                                currentSet = 1;
//
//                                playerToggleButton.setVisibility(View.VISIBLE);
//                                playerToggleButton.setChecked(false);  // まさかここで、onCheckedChangeが呼ばれてる？→合ってた
//
//                                invalidateOptionsMenu();
//
//                                renewViews(workoutTime);
                            }
                        })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        })
                .create();
    }
}