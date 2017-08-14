package com.learnfun.super8team.learnfun.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.learnfun.super8team.learnfun.Content.Constants;
import com.learnfun.super8team.learnfun.Async.NetworkAsync;
import com.learnfun.super8team.learnfun.R;
import com.learnfun.super8team.learnfun.Service.UserPreferences;

import net.gotev.uploadservice.MultipartUploadRequest;

import java.io.IOException;
import java.util.UUID;

public class WriteHistoryActivity extends AppCompatActivity {
    NetworkAsync requestNetwork;
    private Button closeWriteHistory , writeContentHistory,inputHistoryContent;
    private ImageView imageView;
    private ImageButton imageBtnGallery,imageBtnCamera;

    //Image request code
    private int PICK_IMAGE_REQUEST = 1;

    //storage permission code
    private static final int STORAGE_PERMISSION_CODE = 123;

    //Bitmap to get image from gallery
    private Bitmap bitmap;

    //Uri to store the image uri
    private Uri filePath;

    Intent intent;
    String placeNum="";
    EditText contentHistory;
    UserPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_history);

        //Requesting storage permission
        requestStoragePermission();

        closeWriteHistory = (Button)findViewById(R.id.closeWriteHistory);
        writeContentHistory = (Button)findViewById(R.id.writeContentHistory);
        imageView = (ImageView) findViewById(R.id.pictureImageView);
        inputHistoryContent = (Button) findViewById(R.id.inputHistoryContent);
        //imageBtnGallery = (ImageButton) findViewById(R.id.imageButtonGallery);
        inputHistoryContent.setOnClickListener(mainListener);
        closeWriteHistory.setOnClickListener(mainListener);
        writeContentHistory.setOnClickListener(mainListener);
        //imageBtnCamera.setOnClickListener(mainListener);
        imageView.setOnClickListener(mainListener);

        contentHistory = (EditText)findViewById(R.id.contentHistory);
        userPreferences = UserPreferences.getUserPreferences(this);
        Intent getIntent = getIntent();
        Bundle myBundle = getIntent.getExtras();
        placeNum = myBundle.getString("placeNum");

    }


    private View.OnClickListener mainListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.writeContentHistory:

//                    JSONObject sendData = new JSONObject();
//                    try {
//
//                        sendData.put("userId",userPreferences.getUserId());
//                        sendData.put("placeNum",placeNum);
//                        sendData.put("content",contentHistory.getText().toString()+" ");
//                        sendData.put("weather","sunny");
//                        Log.d("sendData = ", String.valueOf(sendData));
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    requestNetwork = new NetworkAsync(WriteHistoryActivity.this, "writeHistoryContent",  NetworkAsync.POST, sendData);
//                    requestNetwork.execute();
//                    uploadMultipart(); //이미지파일을 업로드함
                    if(uploadMultipart()) {
                        intent = new Intent();
                        setResult(0, intent);
                        finish();
                    }
                    break;

                case R.id.closeWriteHistory:

                    intent = new Intent();
                    setResult(0,intent);
                    finish();
                    break;

                case R.id.pictureImageView:
                    showFileChooser();
                    break;

                case R.id.inputHistoryContent:

                    contentHistory.setText("아이들이 수확 후 정리정돈까지 하고 있는 모습입니다 ㅎㅎ 사회적 소양을 함양중이네요~");
                    break;

            }

        }
    };


    /*
        * This is the method responsible for image upload
        * We need the full image path and the name for the image in this method
        * */
    public boolean uploadMultipart() {
        //getting name for the image
        //String name = editText.getText().toString().trim();
        //getting the actual path of the image
        String path = null;

        path=getPath(filePath);
        if(path==null) {
            Toast.makeText(this, "사진은 필수입니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
        //Log.d("path",path);
        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();
            Log.d("context",contentHistory.getText().toString());
            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, Constants.UPLOAD_URL)
                    .addFileToUpload(path, "image") //Adding file
                    //.addParameter("imageName", name) //Adding text parameter to the request
                    .addParameter("userId",userPreferences.getUserId())
                    .addParameter("placeNum",placeNum)
                    .addParameter("content",contentHistory.getText().toString()+" ")
                    .addParameter("weather","sunny")
                    .setUtf8Charset()
//                    .setNotificationConfig(new UploadNotificationConfig())
//                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload

        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    //method to show file chooser
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //method to get the file path from uri
    public String getPath(Uri uri) {
        if(uri==null) return null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    //Requesting permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }
    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }
}