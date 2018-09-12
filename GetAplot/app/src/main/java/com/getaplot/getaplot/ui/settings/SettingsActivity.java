package com.getaplot.getaplot.ui.settings;
        import android.annotation.TargetApi;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.graphics.Paint;
        import android.graphics.drawable.ShapeDrawable;
        import android.graphics.drawable.shapes.OvalShape;
        import android.os.Build;
        import android.os.Bundle;
        import android.preference.PreferenceManager;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.Toolbar;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.ImageButton;
        import android.widget.RelativeLayout;

        import com.getaplot.getaplot.R;
        import com.getaplot.getaplot.preferences.Constant;
        import com.getaplot.getaplot.preferences.Methods;
        import com.getaplot.getaplot.ui.MainActivity;
        import com.turkialkhateeb.materialcolorpicker.ColorChooserDialog;
        import com.turkialkhateeb.materialcolorpicker.ColorListener;


public class SettingsActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences, app_preferences;
    SharedPreferences.Editor editor;
    ImageButton button;
    Methods methods;
RelativeLayout relativeLayout;
    int appTheme;
    int themeColor;
    int appColor;
    Constant constant;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_settings);
//        relativeLayout.setVisibility(View.GONE);
//relativeLayout=findViewById(R.id.rel);
//relativeLayout.setOnClickListener(new View.OnClickListener() {
//    @Override
//    public void onClick(View view) {
//            SharedPreferences.Editor editor1=app_preferences.edit();
//                editor1.putInt("color",R.color.colorPrimaryDark);
//                editor1.putInt("theme",R.style.AppTheme);
//                editor1.apply();
//                Intent i=new Intent(getBaseContext(),MainActivity.class);
//                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(i);
//                Toast.makeText(SettingsActivity.this, "Apperance settings reset", Toast.LENGTH_SHORT).show();
//
//    }
//});

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_setting);
        toolbar.setTitle("Appearance Settings");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        methods = new Methods();
        button = findViewById(R.id.button_color);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        colorize();

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ColorChooserDialog dialog = new ColorChooserDialog(SettingsActivity.this);
                dialog.setTitle("Choose custom coloue");
                dialog.setColorListener(new ColorListener() {
                    @Override
                    public void OnColorClick(View v, int color) {
                        colorize();
                        Constant.color = color;

                        methods.setColorTheme();
                        editor.putInt("color", color);
                        editor.putInt("theme",Constant.theme);
                        editor.commit();

                        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });

                dialog.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void colorize(){
        ShapeDrawable d = new ShapeDrawable(new OvalShape());
        d.setBounds(58, 58, 58, 58);

        d.getPaint().setStyle(Paint.Style.FILL);
        d.getPaint().setColor(Constant.color);

        button.setBackground(d);
    }
}
