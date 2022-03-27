package com.example.finalyearprojectuser.voter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalyearprojectuser.MainActivity;
import com.example.finalyearprojectuser.R;
import com.example.finalyearprojectuser.integer.IntegerCheck;
import com.example.finalyearprojectuser.network.CheckNetwork;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class RegisterCheckStatus extends AppCompatActivity implements CheckNetwork.ReceiverListener{

    //Vars
    private Button registerButton;
    private TextInputEditText icNum;
    private Intent intent;
    private DatabaseReference reference;
    private Boolean registerButtonClicked = false;

    private String blockCharacterSet = "(),+-/N;.~#^|$%&*!";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_check_status);

        checkConnection();

        //Linking
        registerButton = findViewById(R.id.registerButton);
        icNum = findViewById(R.id.registerIcNum);

        //Set Only Integers
        icNum.setFilters(new InputFilter[]{new IntegerCheck(0f, 999999999999.0f)});

        registerButton.setOnClickListener(view -> {
            registerButtonClicked = true;
            register();
            checkConnection();
        });


    }

    //Register Voter Function
    private void register() {

        //Vars
        String ic_txt = Objects.requireNonNull(icNum.getText()).toString().trim().replace("-","");


        if (registerButtonClicked) {
            if (ic_txt.isEmpty()) {
                icNum.setError("IC Number Is A Must To Verify");
                icNum.requestFocus();
            }  else if (ic_txt.length() < 12) {
                icNum.setError("IC Number Must Have 12 Characters");
                icNum.requestFocus();
            }  else {
                reference = FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Voter").child(ic_txt);

                //Add value into database
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            showAlertDialog("Voter Already Registered. Please Proceed To Login Page");

                        } else {
                            Toast.makeText(RegisterCheckStatus.this, "Voter Have Not Register, Proceeding To Register Page", Toast.LENGTH_SHORT).show();
                            intent = new Intent(RegisterCheckStatus.this, Register.class);
                            intent.putExtra("ic",ic_txt);
                            startActivity(intent);
                        }


                    }

                    //If Error
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RegisterCheckStatus.this, "Fail to add data " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }


        }

    }

    //Pop Up Message To Show Error Message
    private void showAlertDialog(String state){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final AlertDialog dialog = alert.create();
        alert.setTitle("Error");
        alert.setMessage(state);
        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                intent = new Intent(RegisterCheckStatus.this, MainActivity.class);
                startActivity(intent);
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
