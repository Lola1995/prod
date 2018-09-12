package com.getaplot.getaplot.ui;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.preferences.Constant;
import com.getaplot.getaplot.utils.Handy;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class EnlargeImageView extends AppCompatActivity implements View.OnTouchListener {
PhotoView image;
    private Context mContext = EnlargeImageView.this;
    private Toolbar toolbar;
String url;
    private static final String TAG = "EnlargeImageView";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constant constant = null;
//
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_enlarge_image_view);
//        //toolbar =findViewById(R.id.tools);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("Photo");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        image = findViewById(R.id.fetch_enlarge_image);
        //image.setOnTouchListener(this);
//--------------------------------------SAVE PICTURE
 url = getIntent().getStringExtra("image_url");
        Log.d(TAG, "onCreate: img" + url);
        try {
            if (!url.equals("") && !url.equals("default")) {
                Glide.with(getApplicationContext()).load(url).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                       // progressBar.setVisibility(View.GONE);

                        e.printStackTrace();
                        Log.d(TAG, "onException: " + e.getMessage());
                        Toast.makeText(mContext, "Could load Photo", Toast.LENGTH_SHORT).show();

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                     // progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })

              .into(image);


            }

        } catch (NullPointerException e) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == STORAGE_REQUEST_CODE) {
//            if (grantResults.length > 0) {
//                Log.d(TAG, "onRequestPermissionsResult: Can now save");
//
//
//            } else {
//                Log.d(TAG, "onRequestPermissionsResult: Permission denied");
//                return;
//            }
//        }


    }

    private void savepic() {


    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        //view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        view.setScaleType(ImageView.ScaleType.MATRIX);

        float scale;

        /* Show an event in the LogCat view, for debugging */
        //dumpEvent(event);



        return true; // indicate event was handled
    }

    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /* Show an event in the LogCat view, for debugging */
//    private void dumpEvent(MotionEvent event)
//    {
//        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE","POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
//        StringBuilder sb = new StringBuilder();
//        int action = event.getAction();
//        int actionCode = action & MotionEvent.ACTION_MASK;
//        sb.append("event ACTION_").append(names[actionCode]);
//
//        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
//        {
//            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_INDEX_SHIFT); //ACTION_POINTER_ID_SHIFT
//
//            sb.append(")");
//        }
//
//        sb.append("[");
//        for (int i = 0; i < event.getPointerCount(); i++)
//        {
//            sb.append("#").append(i);
//            sb.append("(pid ").append(event.getPointerId(i));
//            sb.append(")=").append((int) event.getX(i));
//            sb.append(",").append((int) event.getY(i));
//            if (i + 1 < event.getPointerCount())
//                sb.append(";");
//        }
//
//        sb.append("]");
//        Log.d("Touch Events ---------", sb.toString());
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photomenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
   switch (item.getItemId()){
       case android.R.id.home:
           finish();
           break;

       case R.id.save:
           savepic();
break;
   }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        try {
            if (!getIntent().getStringExtra("event_id").equals("")) {
                Intent intent = new Intent(mContext, EventCommentsActivity.class);
                intent.putExtra("event_id", getIntent().getStringExtra("event_id"));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            } else {
                finish();
            }
        } catch (NullPointerException e) {
            finish();
        }

        super.onBackPressed();
    }

    public void goback(View view) {
        finish();
    }

    public void savePic(View view) {
        Log.d(TAG, "savepic: Saving the Picture");
        PhotoView imageView = findViewById(R.id.fetch_enlarge_image);
        if ((ContextCompat.checkSelfPermission(EnlargeImageView.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            //to get the image from the ImageView (say iv)
            BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
            if (draw == null) {
                Log.d(TAG, "savepic: NO PHOTO");
                Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();
                return;
            }
            Bitmap bitmap = draw.getBitmap();

            FileOutputStream outStream = null;
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/GetPlot/GetPlotPhotos");
            dir.mkdirs();
            String fileName = String.format("%d.jpg", System.currentTimeMillis());
            File outFile = new File(dir, fileName);
            try {
                outStream = new FileOutputStream(outFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            try {
                outStream.flush();
                Handy.scanFile(EnlargeImageView.this, Uri.fromFile(outFile));

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            Toast.makeText(EnlargeImageView.this, "Picture saved", Toast.LENGTH_SHORT).show();
        } else {
            String[] permissions = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            // ActivityCompat.requestPermissions(this, permissions, STORAGE_REQUEST_CODE);
        }

    }
}