package com.example.finalyearprojectuser;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalyearprojectuser.integer.IntegerCheck;
import com.example.finalyearprojectuser.maps.MapsFragment;
import com.example.finalyearprojectuser.network.CheckNetwork;
import com.example.finalyearprojectuser.viewinfo.ViewCandidate;
import com.example.finalyearprojectuser.voter.RegisterCheckStatus;
import com.example.finalyearprojectuser.voter.VoterInfo;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements CheckNetwork.ReceiverListener{

    //Vars
    private TextInputEditText icNum;
    private Button login, register;
    private Intent intent;
    private Boolean loginButtonClicked = false;
    private DatabaseReference reference, userReference;
    private SharedPreferences sharedPreferences;
    private static final int REQUEST_CODE = 10;
    private Executor executor;
    private String voterName, voterState, voterIc, voterVoteStatus, voterStatus, voterPostcode;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private String blockCharacterSet = "(),+-/N;.~#^|$%&*!";
    @RequiresApi(api = Build.VERSION_CODES.R)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkConnection();

        final MediaPlayer mp = MediaPlayer.create(this, R.raw.button);

        executor = ContextCompat.getMainExecutor(this);

        //Database Linking
        //FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").setPersistenceEnabled(true);
        reference = FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Voter");

        //Linking
        login = findViewById(R.id.loginButton);
        register = findViewById(R.id.registerButton);
        icNum = findViewById(R.id.loginIcNum);

        //Set Only Integers
        icNum.setFilters(new InputFilter[]{new IntegerCheck(0f, 999999999999.0f)});

        intent = new Intent(MainActivity.this, MapsFragment.class);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MainActivity.this, RegisterCheckStatus.class);
                startActivity(intent);
            }
        });


        login.setOnClickListener(view -> {
            loginButtonClicked = true;
            checkConnection();
            logVoterIn();
        });

    }

    //Voter Login Function
    private void logVoterIn() {

        String ic_txt = Objects.requireNonNull(icNum.getText()).toString().trim().replace("-", "");

        if (loginButtonClicked) {
            checkConnection();
            if (ic_txt.isEmpty()) {
                icNum.setError("Voter Ic Is Required");
                icNum.requestFocus();
            } else {

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(ic_txt).exists()) {

                            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                            // Creating an Editor object to edit(write to the file)
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            // Storing the key and its value as the data fetched from edittext
                            editor.putString("voterIcNumber",ic_txt);
                            editor.commit();

                            userReference = FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Voter").child(ic_txt);

                            getData(new Callback() {
                                @Override
                                public void onCallBack(String result) {
                                    voterStatus = result;
                                    if (voterStatus.equals("VERIFIED")) {

                                        Toast.makeText(MainActivity.this, "Proceed To Fingerprint Authentication", Toast.LENGTH_SHORT).show();

                                        //Check Fingerprint Function
                                        checkBiometricSupport();

                                        //Fingerprint Check
                                        biometricPrompt = new BiometricPrompt(MainActivity.this,
                                                executor, new BiometricPrompt.AuthenticationCallback() {
                                            @Override
                                            public void onAuthenticationError(int errorCode,
                                                                              @NonNull CharSequence errString) {
                                                super.onAuthenticationError(errorCode, errString);
                                                Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onAuthenticationSucceeded(
                                                    @NonNull BiometricPrompt.AuthenticationResult result) {
                                                super.onAuthenticationSucceeded(result);
                                                startActivity(intent);
                                                Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onAuthenticationFailed() {
                                                super.onAuthenticationFailed();
                                                Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        //Create Alert Box
                                        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                                                .setTitle("Scan Your Fingerprint To Login")
                                                .setSubtitle("Use Only Enrolled Left Or Right Thumb")
                                                .setDescription("Fingerprint")
                                                .setNegativeButtonText("Cancel")
                                                .build();

                                        biometricPrompt.authenticate(promptInfo);
                                    }else
                                    {
                                        Toast.makeText(MainActivity.this, "Account Pending For Review, Please Be Patient As It Usually Take 3 Working Days To Process", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        } else {
                            showAlertDialog("Voter Is Not Registered. Please Proceed To Register Paget");
                        }

                    }

                    //If Error
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Fail to add data " + error, Toast.LENGTH_SHORT).show();
                    }
                });

            }

        }
    }

    //Pop Up Message To Show No Candidate In That State
    private void showAlertDialog(String state){

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

    //getCandidateData
    private void getData(){

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                VoterInfo voterInfo = snapshot.getValue(VoterInfo.class);
                assert voterInfo != null;

                voterName = voterInfo.getVoterName();
                voterIc = voterInfo.getVoterIcNum();
                voterState = voterInfo.getVoterState();
                voterVoteStatus = voterInfo.getVoterVoteStatus();
                voterPostcode = voterInfo.getVoterPostcode();

                // Storing data into SharedPreferences
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                // Creating an Editor object to edit(write to the file)
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Storing the key and its value as the data fetched from edittext
                editor.putString("voterIcNumber",voterIc);
                editor.putString("voterName", voterName);
                editor.putString("voterPostcode",voterPostcode);
                editor.putString("voterVoteStatus",voterVoteStatus);
                editor.commit();
            }

            //If Error
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Fail To Obtain Data " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Check Fingerprint Permission
    @RequiresApi(Build.VERSION_CODES.M)
    private Boolean checkBiometricSupport()
    {
        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        if (!keyguardManager.isDeviceSecure()) {
            notifyUser("Fingerprint authentication has not been enabled in settings");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC)!= PackageManager.PERMISSION_GRANTED) {
            notifyUser("Fingerprint Authentication Permission is not enabled");
            return false;
        }
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            return true;
        }
        else
            return true;
    }

    private void notifyUser(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    //Function To Obtain Data From The Database
    private void getData(@NonNull Callback callback){

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {

            String getVoteStatus = "kosong";

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                    VoterInfo voterInfo = snapshot.getValue(VoterInfo.class);
                    assert voterInfo != null;
                    getVoteStatus = voterInfo.getVoterVerifiedStatus();


                callback.onCallBack(getVoteStatus);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Callback Interface
    public interface Callback{
        void onCallBack(String result);
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
            showAlertDialog(message);
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