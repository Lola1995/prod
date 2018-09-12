package com.getaplot.getaplot.ui.settings;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.firebase.ui.auth.AuthUI;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.fragments.dialog.CustomHelpDialog;
import com.getaplot.getaplot.ui.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.LinkedList;
import java.util.List;

public class AccountConfigsettingsActivity extends AppCompatActivity implements CustomHelpDialog.OnInputListener {
    private static final String TAG = "AccountConfigsettingsAc";
    Toolbar toolbar;
    Context context;
    ListView listView;
    private ProgressBar progressBar;


    @Override
    public void sendInput(String input) {
        Log.d(TAG, "sendInput: got the input: " + input);


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_configsettings);
        toolbar = findViewById(R.id.confibar);
        context = AccountConfigsettingsActivity.this;
        progressBar = findViewById(R.id.signout);
        progressBar.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Options");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ListView listView = findViewById(R.id.two);
        List<String> set = new LinkedList<>();
        set.add("Invite  A Friend");
        set.add("Need Help");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        try {
            if (!user.getPhoneNumber().equals("")) {
                Log.d(TAG, "onCreate: Users Phone " + user.getPhoneNumber());


            } else {
                set.add("Sign Out");
            }
        } catch (NullPointerException ex) {
            set.add("Sign Out");
            Log.d(TAG, "onCreate: NullPointerException:" + ex.getMessage());
        }


        listView.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, set));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, "Hey! grab the GetAplot events social App for your android phone and \n" +
                            " get the fun started\n" +
                            "Download it for free at https://goo.gl/vgE2nk");
                    intent.setType("text/plain");
                    startActivity(intent);

                }
                if (i == 1) {
                    Log.d(TAG, "onClick: opening dialog.");


                    CustomHelpDialog dialog = new CustomHelpDialog();
                    dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                    dialog.setCancelable(false);


                    dialog.show(getFragmentManager(), "CustomHelpDialog");
                }
                if (i == 2) {
                    progressBar.setVisibility(View.VISIBLE);

                    AuthUI.getInstance()
                            .signOut(AccountConfigsettingsActivity.this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {
                                    // user is now signed out
                                    progressBar.setVisibility(View.GONE);
                                    Intent startMain = new Intent(AccountConfigsettingsActivity.this, MainActivity.class);
                                    startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(startMain);
                                    finish();
                                }
                            });


                }
                if (i == 3) {


                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setInputToGetAplot() {

        //todo update us


    }
}
