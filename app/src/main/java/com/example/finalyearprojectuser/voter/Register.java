package com.example.finalyearprojectuser.voter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalyearprojectuser.MainActivity;
import com.example.finalyearprojectuser.R;
import com.example.finalyearprojectuser.integer.IntegerCheck;
import com.example.finalyearprojectuser.network.CheckNetwork;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.reginald.editspinner.EditSpinner;
import com.squareup.picasso.Picasso;
import java.util.Objects;

public class Register extends AppCompatActivity implements CheckNetwork.ReceiverListener {

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
    private String state[] = {"JOHOR", "KEDAH", "KELANTAN", "MELACCA", "NEGERI SEMBILAN", "PAHANG", "PENANG", "PERAK", "PERLIS", "SABAH", "SARAWAK", "SELANGOR", "TERENGGANU", "KUALA LUMPUR", "LABUAN", "PUTRAJAYA"};
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    VoterInfo voterInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_voter);

        checkConnection();

        checkConnection();
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
        uploadFrontIc = findViewById(R.id.newVoterIcFrontUploadButton);
        uploadBackIc = findViewById(R.id.newVoterIcBackUploadButton);
        frontIc = findViewById(R.id.newVoterIcFront);
        backIc = findViewById(R.id.newVoterIcBack);
        frontIcStatus = findViewById(R.id.newVoterIcFrontDisplay);
        backIcStatus = findViewById(R.id.newVoterIcBackDisplay);

        //Set To Uppercase
        name.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        address.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

        //Set Only Integers
        phone.setFilters(new InputFilter[]{new IntegerCheck(0f, 99999999999.0f)});
        postcode.setFilters(new InputFilter[]{new IntegerCheck(0f, 99999.0f)});

        Bundle extras = getIntent().getExtras();
        icNum = extras.getString("ic");
        displayIc.setText(icNum);

        voterInfo = new VoterInfo();

        stateList = new ArrayAdapter<String>(Register.this, android.R.layout.simple_spinner_item, state);
        stateList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(stateList);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(Register.this, RegisterCheckStatus.class);
                startActivity(intent);
            }
        });

        submitButton.setOnClickListener(view -> {
            registerButtonClicked = true;
            register();
            checkConnection();
        });

        uploadFrontIc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open gallery
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
                option = 1;
                imgRef = FirebaseStorage.getInstance().getReference();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(option == 1) {
            ProgressDialog progress = new ProgressDialog(this);
            progress.setTitle("Loading Front IC");
            progress.setMessage("Wait While Loading... Please Be Patient");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();

            if (requestCode == 1000) {
                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = data.getData();
                    frontIcStatus.setImageResource(R.drawable.ic_baseline_assignment_turned_in_24);
                    //profilePic.setImageURI(imageUri);
                    frontUrlLinkCheck = String.valueOf(imageUri);
                    StorageReference fileRef = imgRef.child("Voter/" + icNum + "/frontIC.jpg");
                    fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    frontIcLink = String.valueOf(uri);
                                    //Load saved image from firebase firestore database.
                                    imgRef = FirebaseStorage.getInstance().getReference();
                                    StorageReference profileRef = imgRef.child("Voter/" + icNum + "/frontIC.jpg");
                                    profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(frontIc);
                                        }
                                    });
                                    progress.dismiss();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Register.this, "Failed To Upload.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }else if(option == 2){
            if (requestCode == 1000) {
                if (resultCode == Activity.RESULT_OK) {
                    ProgressDialog progress = new ProgressDialog(this);
                    progress.setTitle("Loading Front IC");
                    progress.setMessage("Wait While Loading... Please Be Patient");
                    progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                    progress.show();

                    Uri imageUri = data.getData();
                    backIcStatus.setImageResource(R.drawable.ic_baseline_assignment_turned_in_24);
                    //profilePic.setImageURI(imageUri);
                    backUrlLinkCheck = String.valueOf(imageUri);
                    StorageReference fileRef = imgRef.child("Voter/" + icNum + "/backIC.jpg");
                    fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    backIcLink = String.valueOf(uri);

                                    //Load saved image from firebase firestore database.
                                    imgRef = FirebaseStorage.getInstance().getReference();
                                    StorageReference profileRef = imgRef.child("Voter/" + icNum + "/backIC.jpg");
                                    profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(backIc);
                                        }
                                    });
                                    progress.dismiss();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Register.this, "Failed To Upload.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
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
                Toast.makeText(Register.this, "Front IC Is Required", Toast.LENGTH_SHORT).show();
            } else if(backUrlLinkCheck == null){
                Toast.makeText(Register.this, "Back IC Is Required", Toast.LENGTH_SHORT).show();
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
                voterInfo.setVoterVoteCandidate("NULL");
                voterInfo.setVoterVerifiedStatus("UN-VERIFIED");
                voterInfo.setVoterVoteSenator("NULL");

                //Add value into database
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(Register.this, "Voter Already Registered", Toast.LENGTH_SHORT).show();
                        } else {
                            reference.setValue(voterInfo);
                            Toast.makeText(Register.this, "New Voter Register Successful, Registration Pending For Review", Toast.LENGTH_SHORT).show();
                            intent = new Intent(Register.this, MainActivity.class);
                            startActivity(intent);
                        }

                    }

                    //If Error
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Register.this, "Fail to add data " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }

    }

    //Pop Up Message To Show No Candidate In That State
    private void showAlertDialogForInvalid(String state){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final AlertDialog dialog = alert.create();
        alert.setTitle("Error");
        alert.setMessage(state);
        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.create().show();
    }

    //Check Connection
    private void checkConnection() {

        // initialize intent filter
        IntentFilter intentFilter = new IntentFilter();

        // add action
        intentFilter.addAction("android.new.conn.CONNECTIVITY_CHANGE");

        // register receiver
        registerReceiver(new CheckNetwork(), intentFilter);

        // Initialize listener
        CheckNetwork.Listener = (CheckNetwork.ReceiverListener) this;

        // Initialize connectivity manager
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Initialize network info
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        // get connection status
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

        // display snack bar
        showSnackBar(isConnected);
    }

    private void showSnackBar(boolean isConnected) {

        // initialize color and message
        String message;
        int color;

        // check condition
        if (isConnected) {

        } else {

            // when internet
            // is disconnected
            // set message
            message = "Not Connected to Internet";
            showAlertDialogForInvalid(message);
        }
    }


    @Override
    public void onNetworkChange(boolean isConnected) {
        // display snack bar
        showSnackBar(isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // call method
        checkConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // call method
        checkConnection();
    }
}
