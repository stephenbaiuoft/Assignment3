package com.example.stephenbai.assignment3;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Environment.getExternalStoragePublicDirectory;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    static ImageView mImageView  = null;
    //Button take_pic = null;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int My_Perm_Switch = 2;
    private static final int LoaderPermission = 3;
    private static final int LocationPermission = 4 ;
    private static final int GetLocationPermission = 5 ;
    public static String mCurrentPhotoPath;
    public static String EXTRA_PATH = "";

    static final int REQUEST_TAKE_PHOTO = 1;
    private LocationManager locationManager;
    private LocationListener locationListener;

    // specifies to look at EXTERNAL_CONTENT, where DCIM is
    //static final Uri mDataUrl = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
    static final Uri mDataUrl = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    //static final Uri mThumbUrl = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
    //MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI

    static final String[] ThumbPROJECTION = new String[] {
            MediaStore.Images.Thumbnails._ID,
            MediaStore.Images.Thumbnails.DATA,
            //MediaStore.Images.Thumbnails.IMAGE_ID,
    };


    static final String[] PROJECTION = new String[] {
            //MediaStore.Images.ImageColumns._ID,
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.TITLE,
            MediaStore.MediaColumns.DATA
    };

    private GridView grid = null;
//ImageAdapter only shows non-dynamic pics
//    private ImageAdapter adapter = null;

    private SimpleCursorAdapter mAdapter;
    private static final int URL_LOADER = 0;

    public static List<String> local_file_list = new ArrayList<String>();

// ********************Menu Implmentation******** SEGMENT*****************Begin

// onCreateOptionsMenu gets the menu.menu into the default toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.camera:
                camera();
                return true;
            case R.id.restore:
                restore();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

// ********************Menu Implmentation**********SEGMENT***************END

    private String localfilename = "DefaultTempFile.txt";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");

        //getLoaderManager().initLoader(URL_LOADER, null, this);

/*
// Adapter that queries only once without loader
        String[] list = {MediaStore.Images.Media._ID};
        adapter = new ImageAdapter(this);
         cursor = getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                list, null, null, MediaStore.Images.Thumbnails._ID);
        columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
*/
        String[] mFromColumns = {MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns._ID};
/*
        String[] ThumbmFromColumns = {MediaStore.Images.Thumbnails.DATA,
                MediaStore.Images.Thumbnails._ID};
*/
        int[] mToFields = {R.id.item_image_view, R.id.item_text_view};

        mAdapter = new SimpleCursorAdapter(
                this,                   // current Context
                R.layout.row,     // Layout for a single row
                null,                   // no Cursor yet
                mFromColumns,           // Cursor columns to use
               // ThumbmFromColumns,
                mToFields,              // Layout fields to use
                0                       // no flags
        );

        // find grid id, and set content
        grid = (GridView) findViewById(R.id.gridView);
        grid.setAdapter(mAdapter);

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                // find locate this view
                ImageView imageView = (ImageView) arg1.findViewById(R.id.item_image_view);
                // Adapter is the current Cursor
                String imageFilePath = mAdapter.getCursor().getString(
                        Arrays.asList(PROJECTION).indexOf(MediaStore.MediaColumns.DATA));

                Toast.makeText(MainActivity.this, imageFilePath, Toast.LENGTH_LONG).show();

                imageView.setTag(imageFilePath);
                Intent i_fullimage = new Intent(MainActivity.this, FullView.class);

                i_fullimage.putExtra(EXTRA_PATH, imageFilePath);
                startActivity(i_fullimage);

            }
        });

        check_load_perm();
        //getSupportLoaderManager().initLoader(URL_LOADER, null, this);

        // Location Service Implementation
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);
// Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                Toast.makeText(getApplicationContext(),
                        location.toString(),Toast.LENGTH_LONG);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        check_location_perm();

// sets authentication
        FirebaseAuthSetUp();

// sets storage
        FirebaseStorageSetUp();

// Request Permission to Write to DCIM storage
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE);

        }

// Read from the local file and update the local array once
        File dir = new File(MainActivity.this.getFilesDir(), localfilename );

        if (dir !=null) {
            try {
                BufferedReader buffreader = new BufferedReader(new FileReader(dir));

                String line;
                line = buffreader.readLine();
                // read every line of the file into the line-variable, on line at the time
                while (line != null) {
                    // add to entries array list
                    local_file_list.add(line);
                    line = buffreader.readLine();
                    // do something with the line
                }
            } catch (Exception ex) {
                // print stack trace.
            }
        }
    }

    public static final int REQUEST_EXTERNAL_STORAGE = 6;

    @Override
    protected void onDestroy(){
        super.onDestroy();
        // create local file to store local array information
        File file = new File(MainActivity.this.getFilesDir(), localfilename);
        PrintWriter outputStream;
        try {
            // creates a new file every time
            outputStream = new PrintWriter(new BufferedWriter( new FileWriter(file, false)));

            //v.getContext().openFileOutput(filename, v.getContext().MODE_APPEND);
            for (String line : local_file_list) {
                outputStream.println(line);

                //outputStream.write("***".getBytes());
            }

            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //private ArrayList<String> firebase_file_list = new ArrayList<String>();

    private static FirebaseDatabase database;
    private static DatabaseReference myRef;

    public void AddToFirebaseDatabase(String filepath){

        //Uri file = Uri.fromFile(new File(filepath));
        //String filename = file.getLastPathSegment();

        // Write a message to the database


        // add value to that particular path
            myRef.push().setValue(mCurrentPhotoPath);
            //myRef.setValue( filepath );
            // Read from the database
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This now is a list
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    //String value = dataSnapshot.getValue(String.class);
                     //Log.w(TAG, "Value is: " + value);

//                    GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
//                      for (  Object item  :dataSnapshot.getValue( t )){
//                          firebase_file_list.add( (String) item);
//                    }

                    // signal latest file_name
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });



    }


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "MainActivity";

    public void FirebaseAuthSetUp(){
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference imagesRef ;

    public void FirebaseStorageSetUp(){
        storage = FirebaseStorage.getInstance();

        // Create a storage reference, gs://<my-bucket-name>storage of my firebase console
        storageRef = storage.getReferenceFromUrl("gs://mobileapp03-ea20a.appspot.com");
        //imagesRef = storageRef.child("images");

    }

// upload all pictures to Firebase
    public void UploadFiles( String filepath ){
// File or Blob
        Uri file = Uri.fromFile(new File(filepath));

// Create the file metadata
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

// Upload file and metadata to the path 'images/mountains.jpg'
       // String name = file.getLastPathSegment();

        UploadTask uploadTask = storageRef.child("images/"+file.getLastPathSegment()).putFile(file, metadata);

// Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                System.out.println("Upload is " + progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("Upload is paused");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Handle successful uploads on complete
                Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
            }
        });
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    /* What happens after the cursor loader finishes its job!!
     * Moves the query results into the adapter, causing the
     * Gridview fronting this adapter to re-display
     */
        mAdapter.swapCursor(cursor);
/*
        Bitmap bitmap;
        String imageFilePath;
        while (cursor.moveToNext()){

            imageFilePath = mAdapter.getCursor().getString(
                    Arrays.asList(PROJECTION).indexOf(MediaStore.MediaColumns.DATA));

            UploadFiles(imageFilePath);
        }

*/
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    /*
     * Clears out the adapter's reference to the Cursor.
     * This prevents memory leaks.
     */
        mAdapter.swapCursor(null);
    }


/*
* Callback that's invoked when the system has initialized the Loader and
* is ready to start the query. This usually happens when initLoader() is
* called. The loaderID argument contains the ID value passed to the
* initLoader() call.
*/

//  onCreateLoader is the call-back function when initLoader() is invoked
    @Override  public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle)
    {
    /*
     * Takes action based on the ID of the Loader that's being created
     * for loadermanager to report back to the client
     */
        switch (loaderID) {
            case URL_LOADER:
                // default queries the content provider
                // CursorLoader -> returns a cursorload
                //because cursorloader queries the ContentResolver
                return new CursorLoader(
                        this,   // Parent activity context
                        mDataUrl,        // Table to query
                        PROJECTION,     // Projection to return
                        null,            // Select All rows
                        null,            // No selection arguments
                        // latest upfront
                        MediaStore.MediaColumns.DATE_ADDED + " DESC"
                        //             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

//**********************************************************
    private static String locationProvider = LocationManager.GPS_PROVIDER;
    public void camera(){
        dispatchTakePictureIntent();



        //Toast.makeText(this, lastKnwonLocation.toString(), Toast.LENGTH_LONG);

       // restore();
    }

// restores all pictures from the cloud!
    public void restore(){

        final File storageDir =  getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        final ArrayList<String> remote = new ArrayList<>();
//
        myRef.orderByKey();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    remote.add( postSnapshot.getValue().toString() );
                }
                String filename;
                for (String filepath: remote) {
                    // only download missing files
                    if ( !local_file_list.contains(filepath) ) {
                        check_write_perm(filepath);

                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        File f = new File(filepath);
                        Uri contentUri = Uri.fromFile(f);
                        mediaScanIntent.setData(contentUri);
                        MainActivity.this.sendBroadcast(mediaScanIntent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });

        Toast.makeText(MainActivity.this, "restored", Toast.LENGTH_SHORT).show();
        // handle true or false case
    }

    public static void  mdeletelocally(String mCurrentPhotoPath){
        // takes out the one from the local_file_list
        local_file_list.remove(mCurrentPhotoPath);
    }

    public static void mdeleteall(final String mCurrentPhotoPath){
        local_file_list.remove(mCurrentPhotoPath);

        String tmp = "message"+mCurrentPhotoPath;
        try{
            final Map<String,String> map = new HashMap<>();
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        map.put(postSnapshot.getValue().toString(),
                                postSnapshot.getKey().toString());
                    }
                    String key = map.get(mCurrentPhotoPath);
                    DatabaseReference delete = database.getReference("message").child(key);
                    delete.removeValue();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    // ...
                }
            });

        }
        catch (DatabaseException ex){
            ex.printStackTrace();
        }




    }

    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

                Toast.makeText(getApplicationContext(),
                        photoFile + "Has not been saved!", Toast.LENGTH_LONG)
                        .show();

                //ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                ;
//                Uri photoURI = FileProvider.getUriForFile(this,
//                        "com.example.android.fileprovider",
//                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            mImageView.setImageBitmap(imageBitmap);


// Geotag location information to pictures
            //Location location = locationManager.getLastKnownLocation()
            get_location_perm();

// Did not have time to finish picture location information
            String fname = mCurrentPhotoPath.substring(
                    mCurrentPhotoPath.lastIndexOf("/")+1
            );
            Toast.makeText(this, fname+"   "+
                    lastKnwonLocation.toString(),Toast.LENGTH_LONG).show();

            galleryAddPic();

        }
    }

    private void check_perm(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    My_Perm_Switch);
        }
    }

    private void check_load_perm(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    LoaderPermission);
        }
        else{
            getSupportLoaderManager().initLoader(URL_LOADER, null, this);
        }
    }

    private void check_write_perm(String filepath){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE);

        }

        else{
            createFile(filepath);
        }
    }

    public void createFile(String filepath){
        Uri f = Uri.fromFile(new File(filepath));
        String filename = f.getLastPathSegment();
        StorageReference image = storageRef.child("images/" + filename);

        File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                + "/"+ filename);

        try{
            localFile.createNewFile();
            image.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                    Log.d("**onsSuccess Download", taskSnapshot.toString() );
                    // Local temp file has been created
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d(TAG, "failed to download ");
                    exception.printStackTrace();
                    // Handle any errors
                }
            });
        }
        catch(IOException ie){
            Log.d("createFile Function:","localFile was not created" );

        }

    }

    private void check_location_perm(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LocationPermission);
        }
        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, locationListener);
        }
    }

    private Location lastKnwonLocation;
    private void get_location_perm(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    GetLocationPermission);
        }
        else{
            //locationManager.requestLocationUpdates(
              //      LocationManager.NETWORK_PROVIDER,
               //     0,
                //    0, locationListener);
            lastKnwonLocation = locationManager.getLastKnownLocation(locationProvider);

        }
    }

    private File image = null;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case My_Perm_Switch: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String imageFileName = "JPEG_" + timeStamp + "_";
                    //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File storageDir =  getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

//                    getSupportLoaderManager()

                    try {
                         image = File.createTempFile(
                            imageFileName,  /* prefix */
                            ".jpg",         /* suffix */
                            storageDir      /* directory */
                    );
                        // Save a file: path for use with ACTION_VIEW intents

                        mCurrentPhotoPath =  image.getAbsolutePath();

                    }
                    catch(IOException ex){
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case LoaderPermission:{
                // run the cursor-load
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // This calls back onCreateLoader because of
                    // LoaderManager.LoaderCallbacks interface
                    getSupportLoaderManager().initLoader(URL_LOADER, null, this);
                }
            }
            case LocationPermission:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                   // locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                     //       0, 0, locationListener);
                }
            }
            case REQUEST_EXTERNAL_STORAGE:{
                Log.d(TAG, "External Writes Request Granted");
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        check_perm();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir =  getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);

        // Upload to the cloud
        UploadFiles(mCurrentPhotoPath);
        // Update local copy array
        local_file_list.add(mCurrentPhotoPath);

        // Update the database
        AddToFirebaseDatabase(mCurrentPhotoPath);


        // Upload twice just overwrite because of the same StorageFile Pointer
        //UploadFiles(mCurrentPhotoPath);
    }




}

