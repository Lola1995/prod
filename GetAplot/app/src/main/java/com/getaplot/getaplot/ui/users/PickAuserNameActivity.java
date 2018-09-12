package com.getaplot.getaplot.ui.users;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.User;
import com.getaplot.getaplot.ui.MainActivity;
import com.getaplot.getaplot.utils.Handy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class PickAuserNameActivity extends AppCompatActivity {
    private static final String TAG = "PickAuserNameActivity";
    ScrollView ss;
    ProgressDialog dialog;
    private DatabaseReference databaseReference, search;
    private FirebaseAuth mAuth;
    private TextInputEditText textInputEditText;
    private Context mcontext;
    private Toolbar mtoolbar;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Constant constant = null;
//        SharedPreferences.Editor editor;
//        SharedPreferences app_preferences;
//        int appTheme;
//        int themeColor;
//        int appColor;
//
//        app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        appColor = app_preferences.getInt("color", 0);
//        appTheme = app_preferences.getInt("theme", 0);
//        themeColor = appColor;
//        constant.color = appColor;
//
//        if (themeColor == 0){
//            setTheme(Constant.theme);
//        }else if (appTheme == 0){
//            setTheme(Constant.theme);
//        }else{
//            setTheme(appTheme);
//        }
        setContentView(R.layout.activity_pick_auser_name);
        setUpToolbar();
        hidesoftKeyBoard();
        dialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mcontext = this;
        ss = findViewById(R.id.ss);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        search = FirebaseDatabase.getInstance().getReference().child("userSearchNode").child(mAuth.getCurrentUser().getUid());
        button = findViewById(R.id.reqEmail);
        textInputEditText = findViewById(R.id.jj);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidesoftKeyBoard();
                String username = textInputEditText.getText().toString().toLowerCase().trim();
                if (!TextUtils.isEmpty(username)) {
                    if (username.length() < 3) {
                        Toast.makeText(mcontext, "Username should be atleast four characters", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!Handy.isAlphnuemeric(username)) {
                        Toast.makeText(mcontext, "A username can only contain letters and numbers no whitespaces", Toast.LENGTH_SHORT).show();
                        return;

                    }
                    if (username.contains("getaplot")) {
                        Toast.makeText(mcontext, "Picked username is not allowed", Toast.LENGTH_SHORT).show();
                        return;

                    }
                    if (username.contains("plot")) {
                        Toast.makeText(mcontext, "Picked username is not allowed", Toast.LENGTH_SHORT).show();
                        return;

                    }
                    if (username.contains("geta")) {
                        Toast.makeText(mcontext, "Picked username is not allowed", Toast.LENGTH_SHORT).show();
                        return;

                    }
                    if (username.contains("truly")) {
                        Toast.makeText(mcontext, "Picked username is not allowed", Toast.LENGTH_SHORT).show();
                        return;

                    }

                    if (username.contains(" ")) {
                        Toast.makeText(mcontext, "A username cannot contain spaces", Toast.LENGTH_SHORT).show();
                        final String newUsername = username.replaceAll(" ", "").toLowerCase();
                        hidesoftKeyBoard();
                        Snackbar.make(ss, "Suggestion found Try " + newUsername.toLowerCase(), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                button.setEnabled(true);
                                checkIfUsernameExists(newUsername);

                            }
                        }).show();

                    } else {
                        button.setEnabled(true);
                        checkIfUsernameExists(username.toLowerCase());
                    }


                } else {
                    Toast.makeText(mcontext, "Please type a username", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void hidesoftKeyBoard() {
        Log.d(TAG, "hidesoftKeyBoard: ");
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromInputMethod(this.getWindow().getCurrentFocus().getWindowToken(), 0);

        }


    }


    private void checkIfUsernameExists(final String newUsername) {
        hidesoftKeyBoard();
        dialog.setMessage("Checking username availability");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Log.d(TAG, "checkIfUsernameExists: checkinf for userExists" + newUsername);
        Query query = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .orderByChild("userName")
                .equalTo(newUsername);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        if (singleSnapshot.exists()) {
                            Snackbar.make(ss, "Username is unavailable,its already being used by another user", Snackbar.LENGTH_LONG).show();
                            dialog.dismiss();
                            button.setEnabled(true);

                            Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + singleSnapshot.getValue(User.class).getUserName());

                            dialog.dismiss();
                            return;
                        }
                    }

                } else {
                    Log.d(TAG, "onDataChange: the username is available");
                    dialog.dismiss();
                    Snackbar.make(ss, newUsername.concat(" is available"), Snackbar.LENGTH_INDEFINITE).setAction("Continue", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            button.setEnabled(true);
                            addUserName(newUsername);

                        }
                    }).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addUserName(final String newUsername) {
        button.setEnabled(false);
        dialog.setMessage("Setting your username");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Log.d(TAG, "addUserName: ");
        Map<String,Object> hasmap = new HashMap<>();
        hasmap.put("joined", ServerValue.TIMESTAMP);
        hasmap.put("userName", newUsername);
        databaseReference.updateChildren(hasmap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                search.child("userName").setValue(newUsername).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dialog.dismiss();
                        Intent i = new Intent(PickAuserNameActivity.this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        Toast.makeText(mcontext, "Your username has been set", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                button.setEnabled(true);
                Toast.makeText(mcontext, "Failed to set your username " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.cpt);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Choose a username");

    }
}
