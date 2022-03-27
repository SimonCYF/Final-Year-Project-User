package com.example.finalyearprojectuser.voter;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.airbnb.lottie.animation.content.Content;
import com.example.finalyearprojectuser.MainActivity;
import com.example.finalyearprojectuser.R;
import com.example.finalyearprojectuser.maps.MapsFragment;
import com.example.finalyearprojectuser.party.ViewParty;
import com.example.finalyearprojectuser.pdf.ViewPDF;
import com.example.finalyearprojectuser.results.ViewRealTimeResults;
import com.example.finalyearprojectuser.viewinfo.ViewCandidate;
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

public class ProfileTest extends AppCompatActivity {

    //Vars
    private String icValue, voterName, voterState, voterHpNum, displayState, displayHpNum;
    private String stateList[] = {"Johor", "Kedah", "Kelantan", "Melacca", "Negeri Sembilan", "Pahang", "Penang", "Perak", "Perlis", "Sabah", "Sarawak", "Selangor", "Terengganu", "Kuala Lumpur", "Labuan", "Putrajaya"};
    private ArrayAdapter<String> stateListDisplay;
    private Intent intent;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private TextInputEditText newHpNum;
    private EditSpinner newState;
    private Button updateState, updateHpNum, logout, popUpConfirm, popUpCancel;
    private SharedPreferences sharedPreferences;
    private TextView name, state, hpNum, icNum, popUpHpNum, popUpState;
    private DatabaseReference reference;
    private TabLayout tabLayout;
    private final int[] ICONS = new int[]{
            R.drawable.ic_baseline_how_to_vote_24,
            R.drawable.ic_baseline_supervised_user_circle_24,
            R.drawable.ic_baseline_flag_circle_24,
            R.drawable.ic_baseline_confirmation_number_24,
            R.drawable.ic_baseline_map_24,
            R.drawable.ic_baseline_self_improvement_24
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toast.makeText(ProfileTest.this, "View Profile Page", Toast.LENGTH_SHORT).show();

        //Linking
        name = findViewById(R.id.profileName);
        state = findViewById(R.id.profileState);
        hpNum = findViewById(R.id.profileHpNum);
        icNum = findViewById(R.id.profileIcNum);
        updateState = findViewById(R.id.updateStateButton);
        updateHpNum = findViewById(R.id.updateHpNumButton);
        logout = findViewById(R.id.logoutButton);

        //Shared Preferences
        //Obtain Shared Preferences
        sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        icValue = sharedPreferences.getString("voterIcNumber","");

        //Database Linking
        reference = FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Voter").child(icValue);

        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading Data");
        progress.setMessage("Wait While Loading... Please Be Patient");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();


        Content content = new Content();
        content.execute();

        progress.dismiss();

        tabLayout  = findViewById(R.id.tabLayout);
        tabLayout.getTabAt(0).setIcon(ICONS[0]);
        tabLayout.getTabAt(1).setIcon(ICONS[1]);
        tabLayout.getTabAt(2).setIcon(ICONS[2]);
        tabLayout.getTabAt(3).setIcon(ICONS[3]);
        tabLayout.getTabAt(4).setIcon(ICONS[4]);
        tabLayout.getTabAt(5).setIcon(ICONS[5]);

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
                changePhoneNumber();
            }
        });

        updateState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeState();
            }
        });

    }

    //tabLayout onClick
    private void switchCase(int position) {
        switch (position) {
            case 0:
                intent = new Intent(ProfileTest.this, CheckStatus.class);
                break;

            case 1:
                intent = new Intent(ProfileTest.this, ViewCandidate.class);
                break;

            case 2:
                intent = new Intent(ProfileTest.this, ViewParty.class);
                break;

            case 3:
                intent = new Intent(ProfileTest.this, ViewRealTimeResults.class);
                break;

            case 4:
                intent = new Intent(ProfileTest.this, ViewPDF.class);
                break;

            case 5:
            intent = new Intent(ProfileTest.this, MapsFragment.class);
                break;

            case 6:
                intent = new Intent(ProfileTest.this, Profile.class);
                break;
        }
        startActivity(intent);
    }

    //Logout
    private void showAlertDialog(){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Logout");
        alert.setMessage("Are you sure you want to logout?");
        alert.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ProfileTest.this,"Logged Out",Toast.LENGTH_SHORT).show();
                //mAuth.signOut();
                intent = new Intent(ProfileTest.this, MainActivity.class);
                startActivity(intent);
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                intent = new Intent(ProfileTest.this, Profile.class);
                startActivity(intent);
            }
        });
        alert.create().show();
    }

    //Function To Allow Things To Run In Background Obtaining From Database
    private class Content extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    //Storing Data into ArrayList
                    VoterInfo voterInfo = snapshot.getValue(VoterInfo.class);
                    assert voterInfo != null;

                    voterName = voterInfo.getVoterName();
                    voterState = voterInfo.getVoterState();
                    voterHpNum = voterInfo.getVoterHpNumber();

                    Log.d("hey",voterHpNum+voterInfo+voterState+voterName);

                    icNum.setText(icValue);
                    name.setText(voterName);
                    state.setText(voterState);
                    hpNum.setText(voterHpNum);
                }

                //If Error
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileTest.this, "Fail To Obtain Data " + error, Toast.LENGTH_SHORT).show();
                }
            });


            return  null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            return;
        }
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

        //Linking Edit Spinner
        stateListDisplay = new ArrayAdapter<String>(ProfileTest.this, android.R.layout.simple_spinner_item, stateList);
        stateListDisplay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newState.setAdapter(stateListDisplay);

        dialogBuilder.setView(contactPopUp);
        dialog = dialogBuilder.create();
        dialog.show();

        getStateData(new Callback() {
            @Override
            public void onCallBack(String result) {
                popUpState.setText(result);
                displayState = result;
            }
        });

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
                }else if(selectedState.equals(displayState)){
                    Toast.makeText(ProfileTest.this, "Same State As Previous", Toast.LENGTH_SHORT).show();
                }else{
                    reference.child("voterState").setValue(selectedState);
                    Toast.makeText(ProfileTest.this, "Successfully Update State", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    intent = new Intent(ProfileTest.this, ProfileTest.class);
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

        dialogBuilder.setView(contactPopUp);
        dialog = dialogBuilder.create();
        dialog.show();

        getPhoneData(new Callback() {
            @Override
            public void onCallBack(String result) {
                popUpHpNum.setText(result);
                displayHpNum = result;
            }
        });

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
                }else if(phone_txt.equals(displayHpNum)){
                    Toast.makeText(ProfileTest.this, "Same Phone Number As Previous", Toast.LENGTH_SHORT).show();
                }else{
                    reference.child("voterHpNumber").setValue(phone_txt);
                    Toast.makeText(ProfileTest.this, "Successfully Update Phone Number", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                intent = new Intent(ProfileTest.this, ProfileTest.class);
                    startActivity(intent);
                }
            }
        });

    }

    //Function To Obtain Data From The Database
    private void getPhoneData(@NonNull Callback callback){

        reference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String hp = snapshot.child("voterHpNumber").getValue(String.class);
                callback.onCallBack(hp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Function To Obtain Data From The Database
    private void getStateData(@NonNull Callback callback){

        reference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String state = snapshot.child("voterState").getValue(String.class);
                callback.onCallBack(state);
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
}
