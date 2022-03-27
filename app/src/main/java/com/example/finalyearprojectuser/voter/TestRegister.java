package com.example.finalyearprojectuser.voter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalyearprojectuser.MainActivity;
import com.example.finalyearprojectuser.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.reginald.editspinner.EditSpinner;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class TestRegister extends AppCompatActivity {

    //Vars
    private DatabaseReference reference;
    private TextInputEditText name, phone, address, postcode, email;
    private EditSpinner stateSpinner;
    private Integer option = 0;
    private ArrayAdapter<String> stateList;
    private Button backButton, submitButton, uploadFrontIc, uploadBackIc;
    private Boolean registerButtonClicked = false;
    private TextView displayIc;
    private ProgressBar progressBar;
    private Intent intent;
    private ImageView frontIc, backIc, frontIcStatus, backIcStatus;
    private String icNum, frontIcLink, backIcLink, frontUrlLinkCheck, backUrlLinkCheck;
    private StorageReference imgRef;
    private String state[] = {"Johor", "Kedah", "Kelantan", "Melacca", "Negeri Sembilan", "Pahang", "Penang", "Perak", "Perlis", "Sabah", "Sarawak", "Selangor", "Terengganu", "Kuala Lumpur", "Labuan", "Putrajaya"};
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    VoterInfo voterInfo;

    //Camera
    private StorageReference mStorage;
    private static final int CAMERA_REQUEST_CODE=1;
    private Uri mImageUri = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_register);

        //Linking
        name = findViewById(R.id.newVoterName);
        phone = findViewById(R.id.newVoterHpNum);
        address = findViewById(R.id.newVoterAddress);
        postcode = findViewById(R.id.newVoterPostcode);
        email = findViewById(R.id.newVoterEmail);
        displayIc = findViewById(R.id.icNumDisplay);
        stateSpinner = findViewById(R.id.newVoterState);
        backButton = findViewById(R.id.voterRegisterBackButton);
        submitButton = findViewById(R.id.voterRegisterSubmitButton);
        uploadFrontIc = findViewById(R.id.newVoterIcFrontTakePictureButton);
        uploadBackIc = findViewById(R.id.newVoterIcBackTakePictureButton);
        frontIc = findViewById(R.id.newVoterIcFront);
        backIc = findViewById(R.id.newVoterIcBack);
        frontIcStatus = findViewById(R.id.newVoterIcFrontDisplay);
        backIcStatus = findViewById(R.id.newVoterIcBackDisplay);


        //Bundle extras = getIntent().getExtras();
        //icNum = extras.getString("ic");
       // displayIc.setText(icNum);

        voterInfo = new VoterInfo();

        stateList = new ArrayAdapter<String>(TestRegister.this, android.R.layout.simple_spinner_item, state);
        stateList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(stateList);

        mStorage = FirebaseStorage.getInstance().getReference();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(TestRegister.this, RegisterCheckStatus.class);
                startActivity(intent);
            }
        });

        submitButton.setOnClickListener(view -> {
            registerButtonClicked = true;
            register();
        });


        uploadFrontIc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, CAMERA_REQUEST_CODE);
                }


            }

        });

        uploadBackIc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open gallery
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
                option = 2;
                imgRef = FirebaseStorage.getInstance().getReference();

            }
        });
    }


    private void startPosting(){


        if(mImageUri != null){
            StorageReference filepath = mStorage.child("Blog_Images").child(mImageUri.getLastPathSegment());

            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                    Log.d("downlaodRUL", String.valueOf(downloadUrl));

                    //DatabaseReference newPost = mDatabase.push();
                    //newPost.child("title").setValue(title_val);
                    //newPost.child("desc").setValue(desc_val);
                    //newPost.child("image").setValue(downloadUrl.toString());

                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){

            mImageUri = data.getData();
            //uploadBackIc.setImageURI(mImageUri);

            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

            startPosting();


        /* Bitmap mImageUri1 = (Bitmap) data.getExtras().get("data");
         mSelectImage.setImageBitmap(mImageUri1);

          Toast.makeText(this, "Image saved to:\n" +
                  data.getExtras().get("data"), Toast.LENGTH_LONG).show();


*/



        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                //mSelectImage.setImageURI(resultUri);
                mImageUri = resultUri;
                Log.d("URL", String.valueOf(mImageUri));

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


    }



    //Register Voter Function
    private void register() {

        //Vars
        String name_txt = Objects.requireNonNull(name.getText()).toString().trim();
        String phone_txt = Objects.requireNonNull(phone.getText()).toString().trim().replace("","");
        String state_txt = stateSpinner.getText().toString();
        String address_txt = Objects.requireNonNull(address.getText()).toString().trim();
        String postcode_txt = Objects.requireNonNull(postcode.getText()).toString().trim();
        String email_txt = Objects.requireNonNull(email.getText()).toString().trim();

        if (registerButtonClicked) {
            if (name_txt.isEmpty()) {
                name.setError("Voter Name Is Required");
                name.requestFocus();
            }  else if (state_txt.isEmpty()) {
                stateSpinner.setError("Voter State Is Required");
                stateSpinner.requestFocus();
            } else if(phone_txt.isEmpty()){
                phone.setError("Voter Phone Number Is Required");
                phone.requestFocus();
            }else if(phone_txt.length() < 10){
                phone.setError("Voter Phone Number Does Not Have Enough Characters");
                phone.requestFocus();
            } else if(email_txt.isEmpty()){
                email.setError("Voter Email Is Required");
                email.requestFocus();
            }else if(!email_txt.matches(emailPattern)){
                email.setError("Voter Email Is Not Valid");
                email.requestFocus();
            }else if(address_txt.isEmpty()){
                address.setError("Voter Address Is Required");
                address.requestFocus();
            }else if(postcode_txt.isEmpty()){
                postcode.setError("Voter Postcode Is Required");
                postcode.requestFocus();
            } else if(postcode_txt.length() < 5){
                postcode.setError("Voter Postcode Does Not Have Enough Characters");
                postcode.requestFocus();
            }else if(frontUrlLinkCheck == null){
                Toast.makeText(TestRegister.this, "Front IC Is Required", Toast.LENGTH_SHORT).show();
            } else if(backUrlLinkCheck == null){
                Toast.makeText(TestRegister.this, "Back IC Is Required", Toast.LENGTH_SHORT).show();
            } else {

                //Database Linkage
                reference = FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Voter").child(icNum);

                voterInfo.setVoterName(name_txt);
                voterInfo.setVoterIcNum(icNum);
                voterInfo.setVoterEmail(email_txt);
                voterInfo.setVoterState(state_txt);
                voterInfo.setVoterHpNumber(phone_txt);
                voterInfo.setVoterAddress(address_txt);
                voterInfo.setVoterPostcode(postcode_txt);
                voterInfo.setVoterFrontIc(frontIcLink);
                voterInfo.setVoterBackIc(backIcLink);
                voterInfo.setVoterVoteStatus("1");
                voterInfo.setVoterVoteCandidate("Null");
                voterInfo.setVoterVerifiedStatus("Un-Verified");

                //Add value into database
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(TestRegister.this, "Voter Already Registered", Toast.LENGTH_SHORT).show();
                        } else {
                            reference.setValue(voterInfo);
                            Toast.makeText(TestRegister.this, "New Voter Register Successful, Registration Pending For Review", Toast.LENGTH_SHORT).show();
                            intent = new Intent(TestRegister.this, MainActivity.class);
                            startActivity(intent);
                        }

                    }

                    //If Error
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TestRegister.this, "Fail to add data " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }


        }

    }
}
