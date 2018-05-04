package lazinth.nex.cameraintent;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.media.ExifInterface;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

// import android.view.MenuItem;
// import java.net.URI;

public class CameraIntentActivity extends AppCompatActivity {

    private static final int ACTIVITY_START_CAMERA_APP = 0;
    private ImageView mPhotoCapturedImageView;
    private String mImageFileLocation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_intent);

        // create new ImageView-variable for the image to show at the top, and then assign the ImageView to our Variable
        // Code Cleanup to mPhotoCapturedImageView = (ImageView) findViewById(R.id.capturePhotoImageView);
        mPhotoCapturedImageView = findViewById(R.id.capturePhotoImageView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera_intent, menu);
        return true;
    }

/*    @Override
    public boolean onOptionsItemSelect (MenuItem item) {
        // Handle action bar item clicks here, the action bar will automatically handle clicks on the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id ==R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
    public void takePhoto(View view) {
       // Toast.makeText(this, "camera button pressed ;)", Toast.LENGTH_SHORT).show();
        Intent callCameraApplicationIntent = new Intent();
        // find deafult Intents on https://developer.android.com/reference/android/provider/MediaStore.html whole explaination can also be find there!!
        callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        // call for the createImageFile to save that picture in a secure try environment to catch nullpointers

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e){
            e.printStackTrace();
        }
        callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));

        //start another activity + with expresscode (the private final integer defined as ACTIVITY_START_...) to
        startActivityForResult(callCameraApplicationIntent, ACTIVITY_START_CAMERA_APP);

    }
    // get result with another function:
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {

        if (requestCode ==ACTIVITY_START_CAMERA_APP && resultCode == RESULT_OK) {
            //the if statement is to make sure there is an activity to handle the takePhotoIntent AND the outcoming result is "positive" (which in this case ironically means that the result code is -1 -_-')
/*   to get the thumbnail into the image view:
            //doing sth with the returned "Intent data"/getting the bundle of data
            Bundle extras = data.getExtras();
            //extract Bitmap out of tht data and show a thumbnail ..
            Bitmap photoCapturedBitmap = (Bitmap) extras.get("data");
            mPhotoCapturedImageView.setImageBitmap(photoCapturedBitmap);
            Toast.makeText(this, "there ist your photo", Toast.LENGTH_SHORT).show();


          // this time showing the picture (not as above) as a full size image and save it too
            Bitmap photoCapturedBitmap = BitmapFactory.decodeFile(mImageFileLocation);
            mPhotoCapturedImageView.setImageBitmap(photoCapturedBitmap);
*/
            rotateImage(setReducedImageSize());
            //following Line is unnecessary ;)
            Toast.makeText(this, "here is your photo and it's even saved at" + mImageFileLocation, Toast.LENGTH_LONG).show();
        }
    }
    //saving the picture to a specified file:
    // Therefore, the following function is no void but a File Method
    File createImageFile() throws IOException{
        // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Image_" + timeStamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        //create that file now and you need to catch migthly happening exceptions...

        File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);
        // got an private String for the image path created on top to save the filepath in.
        mImageFileLocation = image.getAbsolutePath();

        return image;
    }
    /*scale Down Image to reduce the Ram amount needed for this application
    the photo will still be in mPhotoCapturedImageView
    BitmapFactory gives us the option to resize
     */
    private Bitmap setReducedImageSize(){

        int targetImageViewWidth = mPhotoCapturedImageView.getWidth();
        int targetImageViewHeight = mPhotoCapturedImageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        //"dummy"-Load of actual photo just to get the details...
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mImageFileLocation, bmOptions);
        int cameraImageWidth = bmOptions.outWidth;
        int cameraImageHeight = bmOptions.outHeight;

        // calculating the scaleFactor with try and catch because sometimes the targetImageView numbers can't be pulled. This then results in an division by zero...
        int scaleFactor = 0;
        try {
            scaleFactor = Math.min(cameraImageWidth/targetImageViewWidth, cameraImageHeight/targetImageViewHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bmOptions.inSampleSize = scaleFactor;
        //since inJustDecodeBounds is true above we can't load the picture yet - therefore...
        bmOptions.inJustDecodeBounds = false;

        //displaying the image now in its smaller size....
        //Bitmap photoReducedSizeBitmap = BitmapFactory.decodeFile(mImageFileLocation, bmOptions);
        //mPhotoCapturedImageView.setImageBitmap(photoReducedSizeBitmap);
        return BitmapFactory.decodeFile(mImageFileLocation, bmOptions);


    }
    // new method to fix the orientation problem
    private void rotateImage(Bitmap bitmap) {
        // use the information which brings the image with itself to get its orientation
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(mImageFileLocation);
            } catch (IOException e){
            e.printStackTrace();
        }
        //after we created a new exifInterface object with the Data from mImageFileLocation, we equate an Integer with the orientation tag. we also set an default value (=0).
        assert exifInterface != null;
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        switch (orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
            default:
        }
        //uses the decision from above on a new bitmap and "pastes" it into the mPhotoCapturedImageView view
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        mPhotoCapturedImageView.setImageBitmap(rotatedBitmap);
    }

    //second Task: take only the red Pixels and count those with 0.5 or more "strength"
    //first the analogon to takePhoto/onActivity result, were we try to display the amount of red pixels this time
    public void getRedPixelAmount(View view) {
        stainImage();
    // count(stainImage());



    }

    //analogous to rotateImage which uses the bitmap from the setReducedImageSize method and counts the red pixels

    public void count (Bitmap bitmap){
        int redPixel = 0;
        int targetImageViewWidth2 = mPhotoCapturedImageView.getWidth();
        int targetImageViewHeight2 = mPhotoCapturedImageView.getHeight();

       BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        /*"dummy"-Load of actual photo just to get the details...
        bmOptions.inJustDecodeBounds = true;
        */
        BitmapFactory.decodeFile(mImageFileLocation, bmOptions);
        int cameraImageWidth2 = bmOptions.outWidth;
        int cameraImageHeight2 = bmOptions.outHeight;
        //        bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();int pixel = bitmap.getPixel(x,y);
    }

    // analogous to private Bitmap setReducedImageSize() to produce a red stained image
    private Bitmap stainImage () {

        // BitmapFactory.decodeFile(mImageFileLocation);
        // int width2 = mPhotoCapturedImageView.getWidth();
        // int height = mPhotoCapturedImageView.getHeight();
        // use the color filter to multiply the (alpha and) red with 100% and blue and green with Zero.
        int mul = 0xFFFF0000;
        int add = 0x00000000;
        ColorFilter redStainer = new LightingColorFilter(mul, add);

        mPhotoCapturedImageView.setColorFilter(redStainer);
        //just to make sure this works:
        /*       Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        mPhotoCapturedImageView.setImageBitmap(rotatedBitmap);  */
      /*  Drawable stainedImage = this.mImageFileLocation.;
        return stainedImage.setColorFilter(redStainer); */
        return BitmapFactory.decodeFile(mImageFileLocation);


    }
}

