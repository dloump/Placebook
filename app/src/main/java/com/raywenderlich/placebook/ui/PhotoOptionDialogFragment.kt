package com.raywenderlich.placebook.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class PhotoOptionDialogFragment : DialogFragment() {

    //defining interface
    interface PhotoOptionDialogListener {
        fun onCaptureClick()
        fun onPickClick()
    }

    //defining property to hold instance
    private lateinit var listener: PhotoOptionDialogListener

    //
    override fun onCreateDialog(savedInstanceState: Bundle?):
            Dialog {
        //listener property set to parent activity
        listener = activity as PhotoOptionDialogListener
        //initializing option indices, because position may change per device capabilities
        var captureSelectIdx = -1
        var pickSelectIdx = -1
        //defining options arraylist to hold alertdialog options
        val options = ArrayList<String>()
        //setting temporary unmutable variable
        val context = activity as Context
        //if device has camera, camera option added to options array
        if (canCapture(context)) {
            options.add("Camera")
            captureSelectIdx = 0
        }
        //if device can pick an image from gallery, gallery option added to options array
        if (canPick(context)) {
            options.add("Gallery")
            pickSelectIdx = if (captureSelectIdx == 0) 1 else 0
        }
        //building alertdialog using options list & onClickListener is
        //provided to respond to User selection
        return AlertDialog.Builder(context)
                .setTitle("Photo Option")
                .setItems(options.toTypedArray<CharSequence>()) { _,
                                                                  which ->
                    if (which == captureSelectIdx) {
                        //if camera option was selected, oncaptureclick is called on listener
                        listener.onCaptureClick()
                    } else if (which == pickSelectIdx) {
                        //else if gallery option was selected, onpickclick is called on listener
                        listener.onPickClick()
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
    }
    companion object {
        //determining if device is capable of picking image from gallery & checking to see if
        //Intent can be resolved
        fun canPick(context: Context) : Boolean {
            val pickIntent = Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            return (pickIntent.resolveActivity(
                    context.packageManager) != null)
        }
        //determining if device has camera to capture new image
        fun canCapture(context: Context) : Boolean {
            val captureIntent = Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE)
            return (captureIntent.resolveActivity(
                    context.packageManager) != null)
        }
        //helper method to help parent activity when creating new photooptiondialog fragment
        fun newInstance(context: Context) =
                //if device can pick from a gallery or take a new image,
                //PhotoOptionDialogFragment is created & returned, otherwise null is
                //returned
                if (canPick(context) || canCapture(context)) {
                    PhotoOptionDialogFragment()
                } else {
                    null
                }
    }
}