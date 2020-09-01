package com.lampineapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * Wrapper class for alert dialogs.
 * Implement the onXYBtnClick() methods as click listeners.
 */
abstract public class DialogWithTwoButtons {

    private AlertDialog mDialog;
    static private int currDialogCount = 0;

    public DialogWithTwoButtons(Activity activity, String message, String posBtnText, String negBtnText, boolean show) {

        // Only one dialog can exist at one instance!
        if (currDialogCount == 0) {
            final AlertDialog.Builder mDialogBuilder;
            mDialogBuilder = new AlertDialog.Builder(activity).setMessage(message);
            if (posBtnText != "") {
                mDialogBuilder.setPositiveButton(posBtnText, posBtnListener);
            }
            if (negBtnText != "") {
                mDialogBuilder.setCancelable(false);
                mDialogBuilder.setNegativeButton(negBtnText, negBtnListener);
            }
            mDialog = mDialogBuilder.create();
            mDialog.setCanceledOnTouchOutside(false);
            if (show == true) {
                mDialog.show();
            }
            currDialogCount++;
        }
    }

    private DialogInterface.OnClickListener posBtnListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int btn) {
            onPositiveBtnClick();
        }
    };

    private DialogInterface.OnClickListener negBtnListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int btn) {
            onNegativeBtnClick();
        }
    };

    abstract void onPositiveBtnClick();
    abstract void onNegativeBtnClick();

    public void cancel() {
        currDialogCount--;
        mDialog.cancel();
    }

    public void hide() {
        mDialog.hide();
    }


}
