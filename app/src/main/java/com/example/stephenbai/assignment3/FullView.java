package com.example.stephenbai.assignment3;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class FullView extends AppCompatActivity {

    private ImageView mImageView;
    private String mCurrentPhotoPath;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fullview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.delete:
                pic_delete();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

// Delete current selected picture
    private void pic_delete(){
        //
        /*
        if (getFragmentManager().findFragmentById(content) == null) {
            //getSupportFragmentManager()
            getSupportFragmentManager().beginTransaction()
                    .add( android.R.id.content,
                            new DeleteFrag()).addToBackStack(null).commit();
        }*/

/* Pop-up menu */
        CharSequence choices[] = new CharSequence[] {"Yes(only locally)", "No"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Local Copy Only? ");

        builder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                File file = new File(mCurrentPhotoPath);
                // local copy
                if (which ==0){
                    if (!file.delete()){
                        Toast.makeText(getApplicationContext(),
                                file + "is not locally deleted successfully", Toast.LENGTH_LONG)
                                .show();
                    }else{

                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        File f = new File(mCurrentPhotoPath);
                        Uri contentUri = Uri.fromFile(f);
                        mediaScanIntent.setData(contentUri);
                        // Inside Builder, so call FullView
                        FullView.this.sendBroadcast(mediaScanIntent);
                        MainActivity.mdeletelocally(mCurrentPhotoPath);
                        Toast.makeText(getApplicationContext(),
                                file + "is deleted locally", Toast.LENGTH_LONG)
                                .show();
                    }
                }
                // remote to firebase
                else{
                    if (!file.delete()){
                        Toast.makeText(getApplicationContext(),
                                file + "is not at all deleted successfully ", Toast.LENGTH_LONG)
                                .show();
                    }else{
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        File f = new File(mCurrentPhotoPath);
                        Uri contentUri = Uri.fromFile(f);
                        mediaScanIntent.setData(contentUri);
                        // Inside Builder, so call FullView
                        FullView.this.sendBroadcast(mediaScanIntent);

                        MainActivity.mdeleteall(mCurrentPhotoPath);
                        Toast.makeText(getApplicationContext(),
                                file + "is deleted entirely", Toast.LENGTH_LONG)
                                .show();
                    }
                }

                finish();
                // the user clicked on colors[which]
            }
        });
        builder.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        mImageView = (ImageView) findViewById(R.id.fullimageView);
        Intent intent = getIntent();
        mCurrentPhotoPath = intent.getStringExtra(MainActivity.EXTRA_PATH);

        mImageView.post(new Runnable() {
            @Override
            public void run() {
                setPic();
            }
        });

    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

}
