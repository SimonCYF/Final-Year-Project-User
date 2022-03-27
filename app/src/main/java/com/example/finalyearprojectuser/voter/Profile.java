package com.example.finalyearprojectuser.voter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.example.finalyearprojectuser.viewinfo.ViewCandidate;
import com.example.finalyearprojectuser.maps.MapsFragment;
import com.example.finalyearprojectuser.party.ViewParty;
import com.example.finalyearprojectuser.pdf.ViewPDF;
import com.example.finalyearprojectuser.results.ViewRealTimeResults;
import com.example.finalyearprojectuser.vote.CheckStatus;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.reginald.editspinner.EditSpinner;

import java.util.Objects;

public class Profile extends AppCompatActivity implements CheckNetwork.ReceiverListener{

    //Vars
    private String icValue, voterName, voterState, voterHpNum, displayState, displayHpNum;
    private String stateList[] = {"DEFAULT", "JOHOR", "KEDAH", "KELANTAN", "MELACCA", "NEGERI SEMBILAN", "PAHANG", "PENANG", "PERAK", "PERLIS", "SABAH", "SARAWAK", "SELANGOR", "TERENGGANU", "KUALA LUMPUR", "LABUAN", "PUTRAJAYA"};
    private ArrayAdapter<String> stateListDisplay;
    private Intent intent;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private TextInputEditText newHpNum;
    private EditSpinner newState;
    private Button updateState, updateHpNum, logout, popUpConfirm, popUpCancel;
    private SharedPreferences sharedPreferences;
    private TextView name, state, hpNum, icNum, popUpHpNum, popUpState, address;
    private DatabaseReference reference, userReference;
    private TabLayout tabLayout;
    private final int[] ICONS = new int[]{
            R.drawable.ic_baseline_how_to_vote_24,
            R.drawable.ic_baseline_supervised_user_circle_24,
            R.drawable.ic_baseline_flag_circle_24,
            R.drawable.ic_baseline_confirmation_number_24,
            R.drawable.ic_baseline_picture_as_pdf_24,
            R.drawable.ic_baseline_map_24,
            R.drawable.ic_baseline_self_improvement_24
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        checkConnection();

        Toast.makeText(Profile.this, "View Profile Page", Toast.LENGTH_SHORT).show();

        //Linking
        name = findViewById(R.id.profileName);
        state = findViewById(R.id.profileState);
        hpNum = findViewById(R.id.profileHpNum);
        icNum = findViewById(R.id.profileIcNum);
        address = findViewById(R.id.profileAddress);
        updateState = findViewById(R.id.updateStateButton);
        updateHpNum = findViewById(R.id.updateHpNumButton);
        logout = findViewById(R.id.logoutButton);

        //Shared Preferences
        //Obtain Shared Preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        icValue = sharedPreferences.getString("voterIcNumber", "");


        //Database Linking
        reference = FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Voter");
        userReference= FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Voter").child(icValue);

        //Get Voter Data
        getData();


        tabLayout  = findViewById(R.id.tabLayout);
        tabLayout.getTabAt(0).setIcon(ICONS[0]);
        tabLayout.getTabAt(1).setIcon(ICONS[1]);
        tabLayout.getTabAt(2).setIcon(ICONS[2]);
        tabLayout.getTabAt(3).setIcon(ICONS[3]);
        tabLayout.getTabAt(4).setIcon(ICONS[4]);
        tabLayout.getTabAt(5).setIcon(ICONS[5]);
        tabLayout.getTabAt(6).setIcon(ICONS[6]);

        //tabLayout On Click
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) { switchCase(tab.getPosition()); }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switchCase(tab.getPosition());
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });

        updateHpNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkConnection();changePhoneNumber();
            }
        });

        updateState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnection();changeState();
            }
        });

    }


    //Get Voter Data
    private void getData(){

        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading Data");
        progress.setMessage("Wait While Loading... Please Be Patient");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot data : snapshot.getChildren()){
                    VoterInfo voterInfo = data.getValue(VoterInfo.class);
                    assert  voterInfo !=  null;

                    String ic_txt = voterInfo.getVoterIcNum();
                    String name_txt = voterInfo.getVoterName();
                    String state_txt = voterInfo.getVoterState();
                    String phone_txt = voterInfo.getVoterHpNumber();
                    String postcode_txt = voterInfo.getVoterPostcode();
                    String address_txt = voterInfo.getVoterAddress();

                    if(ic_txt.equals(icValue)){
                        voterState = state_txt;
                        voterName = name_txt;
                        voterHpNum = phone_txt;

                        //Set Value To Text View
                        icNum.setText(icValue);
                        name.setText(voterName);
                        state.setText(voterState);
                        hpNum.setText(voterHpNum);
                        address.setText(address_txt + ", "+postcode_txt);

                        progress.dismiss();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Function To Change Voter Current State
    private void changeState(){
        dialogBuilder = new AlertDialog.Builder(this);
        final View contactPopUp = getLayoutInflater().inflate(R.layout.activity_profile_change_state, null);

        //Linking
        newState = (EditSpinner) contactPopUp.findViewById(R.id.newStateSpinner);
        popUpState = (TextView) contactPopUp.findViewById(R.id.currentState);
        popUpConfirm = (Button) contactPopUp.findViewById(R.id.btnConfirm);
        popUpCancel = (Button) contactPopUp.findViewById(R.id.btnCancel);

        popUpState.setText(voterState);

        //Linking Edit Spinner
        stateListDisplay = new ArrayAdapter<String>(Profile.this, android.R.layout.simple_spinner_item, stateList);
        stateListDisplay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newState.setAdapter(stateListDisplay);

        dialogBuilder.setView(contactPopUp);
        dialog = dialogBuilder.create();
        dialog.show();

        popUpCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        popUpConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedState = newState.getText().toString();

                if (selectedState.isEmpty()){
                    newState.setError("New State Is Require");
                    newState.requestFocus();
                }else if(selectedState.equals(voterState)){
                    Toast.makeText(Profile.this, "Same State As Previous", Toast.LENGTH_SHORT).show();
                }else{
                    userReference.child("voterState").setValue(selectedState);
                    Toast.makeText(Profile.this, "Successfully Update State", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    intent = new Intent(Profile.this, Profile.class);
                    startActivity(intent);
                }

            }
        });

    }

    //Function To Change Voter Current Phone Number
    private void changePhoneNumber(){
        dialogBuilder = new AlertDialog.Builder(this);
        final View contactPopUp = getLayoutInflater().inflate(R.layout.activity_profile_change_phone_number, null);

        //Linking
        popUpHpNum = (TextView) contactPopUp.findViewById(R.id.currentPhoneNumber);
        newHpNum = (TextInputEditText) contactPopUp.findViewById(R.id.newPhoneNumber);
        popUpConfirm = (Button) contactPopUp.findViewById(R.id.btnConfirm);
        popUpCancel = (Button) contactPopUp.findViewById(R.id.btnCancel);

        //Set Only Integers
        newHpNum.setFilters(new InputFilter[]{new IntegerCheck(0f, 99999999999.0f)});

        popUpHpNum.setText(voterHpNum);
        dialogBuilder.setView(contactPopUp);
        dialog = dialogBuilder.create();
        dialog.show();

        popUpCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        popUpConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone_txt = Objects.requireNonNull(newHpNum.getText()).toString().trim();

                if (phone_txt.isEmpty()){
                    newHpNum.setError("New Phone Number Is Require");
                    newHpNum.requestFocus();
                }else if(phone_txt.equals(voterHpNum)){
                    Toast.makeText(Profile.this, "Same Phone Number As Previous", Toast.LENGTH_SHORT).show();
                }else if(phone_txt.length() < 10) {
                    Toast.makeText(Profile.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();

                } else{
                    userReference.child("voterHpNumber").setValue(phone_txt);
                    Toast.makeText(Profile.this, "Successfully Update Phone Number", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    intent = new Intent(Profile.this, Profile.class);
                    startActivity(intent);
                }
            }
        });

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

    //tabLayout onClick
    private void switchCase(int position) {
        switch (position) {
            case 0:
                intent = new Intent(Profile.this, CheckStatus.class);
                break;

            case 1:
                intent = new Intent(Profile.this, ViewCandidate.class);
                break;

            case 2:
                intent = new Intent(Profile.this, ViewParty.class);
                break;

            case 3:
                intent = new Intent(Profile.this, ViewRealTimeResults.class);
                break;

            case 4:
                intent = new Intent(Profile.this, ViewPDF.class);
                break;

            case 5:
                intent = new Intent(Profile.this, MapsFragment.class);
                break;

            case 6:
                intent = new Intent(Profile.this, Profile.class);
                break;
        }
        startActivity(intent);
    }

    //Logout
    private void showAlertDialog(){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final AlertDialog dialog = alert.create();
        alert.setTitle("Logout");
        alert.setMessage("Are you sure you want to logout?");
        alert.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(Profile.this,"Logged Out",Toast.LENGTH_SHORT).show();
                //mAuth.signOut();
                intent = new Intent(Profile.this, MainActivity.class);
                startActivity(intent);
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
