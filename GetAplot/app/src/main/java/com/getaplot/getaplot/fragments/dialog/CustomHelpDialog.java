package com.getaplot.getaplot.fragments.dialog;

/**
 * Created by Elia on 12/21/2017.
 */

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.getaplot.getaplot.R;
import com.getaplot.getaplot.utils.Handy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;


public class CustomHelpDialog extends DialogFragment {


    private static final String TAG = "CustomHelpDialog";
    public OnInputListener mOnInputListener;
    DatabaseReference HelpIssues;


    //widgets
    ProgressBar progressBar;
    private TextInputEditText mInput;
    private Button mActionOk, mActionCancel;

    @Nullable

    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.custom_help_dialog, container, false);
        HelpIssues = FirebaseDatabase.getInstance().getReference().child("HelpIssues");
        mActionCancel = view.findViewById(R.id.action_cancel);

        mActionOk = view.findViewById(R.id.action_ok);
        progressBar = view.findViewById(R.id.submitting);
        mInput = view.findViewById(R.id.input);


        mActionCancel.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                Log.d(TAG, "onClick: closing dialog");

                getDialog().dismiss();

            }

        });


        mActionOk.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                Log.d(TAG, "onClick: capturing input");


                String input = mInput.getText().toString();

                if (!input.equals("")) {

                    //Easiest way: just set the value

                    if (input.trim().length() < 40) {
                        Toast.makeText(getActivity(), "You need to describe your  issue further", Toast.LENGTH_SHORT).show();

                        return;

                    } else {
                        Map<String, Object> addMap = new HashMap<>();
                        addMap.put("message_text", input);
                        mActionOk.setEnabled(false);
                        mActionCancel.setEnabled(false);
                        progressBar.setVisibility(View.VISIBLE);


                        addMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        addMap.put("sentAt", ServerValue.TIMESTAMP);
                        addMap.put("fitnessNum", Handy.fitnessNumber());


                        HelpIssues.push().updateChildren(addMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    try {
                                        Toast.makeText(getActivity().getBaseContext(), "Feedback successfully sent,thanks", Toast.LENGTH_SHORT).show();
                                    } catch (NullPointerException e) {

                                    }

                                    getDialog().dismiss();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), "Error sending your feedback,thanks try again", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onFailure: " + e.getMessage());
                            }
                        });


                    }


                } else {
                    Toast.makeText(getActivity(), "Please describe your issue", Toast.LENGTH_SHORT).show();
                }


                //"Best Practice" but it takes longer


            }

        });


        return view;

    }
    //vars

    @Override

    public void onAttach(Context context) {

        super.onAttach(context);

        try {

            mOnInputListener = (OnInputListener) getActivity();

        } catch (ClassCastException e) {

            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());

        }

    }


    public interface OnInputListener {

        void sendInput(String input);

    }

}

