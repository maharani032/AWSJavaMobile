package com.mahar.awsjavamobile;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.mobile.config.AWSConfiguration;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ActivityResultLauncher<Intent> activityResultLauncherForUploadImage;

    private Button buttonUploadImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAmplify();
        setContentView(R.layout.activity_main);
//        To Register Activity
        RegisterActivityForUploadImage();

        buttonUploadImage=findViewById(R.id.buttonUploadImage);
        buttonUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this
                        , Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this
                            ,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }else{
                    setStorageInfo();
                    choosePhoto();
                }

            }
        });

    }
    private void setStorageInfo() {
        JSONObject s3Config = new AWSConfiguration(this)
                .optJsonObject("S3TransferUtility");
        try {
            String storageBucketName = s3Config.getString("Bucket");
            String region = s3Config.getString("Region");
        } catch (JSONException e) {
            Log.e(TAG, "Can't find S3 bucket", e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,
                            "Error: Can't find S3 bucket. \nHave you run 'amplify add storage'? ",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    public void RegisterActivityForUploadImage(){
        activityResultLauncherForUploadImage= registerForActivityResult(new ActivityResultContracts
                .StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                int resultCode=result.getResultCode();
                Intent data=result.getData();

                if (resultCode==RESULT_OK && data!=null){

                    Uri selectedImage = data.getData();
                    uploadFile(selectedImage);
                }

            }
        });
    }

    public void choosePhoto() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncherForUploadImage.launch(i);
    }

    public void initAmplify(){
        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.addPlugin(new AWSS3StoragePlugin());
            Amplify.configure(getApplicationContext());

            Log.i("MyAmplifyApp", "Initialized Amplify");
        } catch (AmplifyException error) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error);
        }
    }
    private void uploadFile(Uri imagePath) {
// To upload data to S3 from an InputStream:

//        try {
//            InputStream exampleInputStream = getContentResolver().openInputStream(imagePath);
//
//            Amplify.Storage.uploadInputStream(
//                    "ExampleKey",
//                    exampleInputStream,
//                    result -> Log.i("MyAmplifyApp", "Successfully uploaded: " + result.getKey()),
//                    storageFailure -> Log.e("MyAmplifyApp", "Upload failed", storageFailure)
//            );
//        }  catch (FileNotFoundException error) {
//            Log.e("MyAmplifyApp", "Could not find file to open for input stream.", error);
//        }

//To upload to S3 from a data object, specify the key and the file to be uploaded
        UUID uuid = UUID.randomUUID();
        File exampleFile = new File(getApplicationContext().getFilesDir(), uuid.toString());

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(exampleFile));
            writer.append("Example file contents");
            writer.close();
        } catch (Exception exception) {
            Log.e("MyAmplifyApp", "Upload failed", exception);
        }

        Amplify.Storage.uploadFile(
                uuid.toString(), //filename
                exampleFile, //file
                result -> Log.i("MyAmplifyApp", "Successfully uploaded: " + result.getKey()),
                storageFailure -> Log.e("MyAmplifyApp", "Upload failed", storageFailure)
        );

    }


}