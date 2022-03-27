package com.example.finalyearprojectuser.vote;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalyearprojectuser.R;
import com.example.finalyearprojectuser.network.CheckNetwork;
import com.example.finalyearprojectuser.viewinfo.ViewCandidate;
import com.example.finalyearprojectuser.maps.MapsFragment;
import com.example.finalyearprojectuser.party.ViewParty;
import com.example.finalyearprojectuser.pdf.ViewPDF;
import com.example.finalyearprojectuser.recycleviewadapter.VerticalViewAdapterCandidateVote;
import com.example.finalyearprojectuser.voter.Profile;
import com.example.finalyearprojectuser.voter.ProfileTest;
import com.example.finalyearprojectuser.voter.VoterInfo;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CheckStatus extends AppCompatActivity implements CheckNetwork.ReceiverListener{

    //Vars
    private Intent intent;
    private Integer integer = 0, buttonInteger = 0;
    private Boolean check = false;
    private TabLayout tabLayout;
    private ArrayList<String> test = new ArrayList<>();
    private ExtendedFloatingActionButton submitButton;
    private CircleImageView circleImageView;
    private String icNum, selectedCandidateName, name, ic, state, voteStatus = "empty";
    private TextView displayState, displayName, displayStatus, displayIcNum, displayVote;
    private SharedPreferences sharedPreferences, statusSharedPreferences;
    private DatabaseReference reference, userReference;
    public static boolean task1Finished = false;
    VoterInfo voterInfo;
    private ProgressBar progressBar;
    private VerticalViewAdapterCandidateVote verticalViewAdapterVote;
    private Boolean loopCheck = false;

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
        setContentView(R.layout.activity_vote_check_status);

        checkConnection();

        final MediaPlayer mp = MediaPlayer.create(this, R.raw.button);

        //Obtain Shared Preferences
        //sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        icNum = sharedPreferences.getString("voterIcNumber", "");

        //Database Linking
        reference = FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Voter");

        //Linking
        displayIcNum = findViewById(R.id.voteIcNum);
        displayName = findViewById(R.id.voteName);
        displayState = findViewById(R.id.voteState);
        displayStatus = findViewById(R.id.voteStatus);
        displayVote = findViewById(R.id.textViewVerifyVoterStatus);
        circleImageView = findViewById(R.id.voteCandidateStatus);
        submitButton = findViewById(R.id.proceedToVotePageFloatingActionButton);
        intent = new Intent(CheckStatus.this, CandidateSelectionAfterCheckStatus.class);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.getTabAt(0).setIcon(ICONS[0]);
        tabLayout.getTabAt(1).setIcon(ICONS[1]);
        tabLayout.getTabAt(2).setIcon(ICONS[2]);
        tabLayout.getTabAt(3).setIcon(ICONS[3]);
        tabLayout.getTabAt(4).setIcon(ICONS[4]);
        tabLayout.getTabAt(5).setIcon(ICONS[5]);
        tabLayout.getTabAt(6).setIcon(ICONS[6]);

        displayIcNum.setText(icNum);
        Log.d("ic",icNum);

        getData();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) { switchCase(tab.getPosition()); }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switchCase(tab.getPosition());
            }
        });

        //Submit Button On Click
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.start();
                checkConnection();
                buttonInteger = 1;
                verify();
            }
        });
    }

    private void verify(){
        if(buttonInteger == 1 && integer ==1){
            startActivity(intent);
            Toast.makeText(CheckStatus.this, "Proceeding To Voting Page", Toast.LENGTH_SHORT).show();

        }else if(buttonInteger == 1 && integer == 2){

            showAlertDialog();
        }
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
                    String voteStatus_txt = voterInfo.getVoterVoteStatus();
                    String postcode_txt = voterInfo.getVoterPostcode();

                    if(ic_txt.equals(icNum)){
                        state = state_txt;
                        voteStatus = voteStatus_txt;
                        name = name_txt;
                        displayIcNum.setText(ic_txt);
                        displayState.setText(state);
                        displayStatus.setText(voteStatus);
                        displayName.setText(name);


                        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                        // Creating an Editor object to edit(write to the file)
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        // Storing the key and its value as the data fetched from edittext
                        editor.putString("voterState",state_txt);
                        editor.putString("voterPostcode",postcode_txt);
                        editor.commit();

                        progress.dismiss();

                        if (voteStatus.equals("1")) {
                            integer = 1;
                            circleImageView.setImageResource(R.drawable.ic_baseline_check_24);
                            displayVote.setText("Vote Available");
                            displayStatus.setText("AVAILABLE");
                            Toast.makeText(CheckStatus.this, "Vote Available", Toast.LENGTH_SHORT).show();
                        } else if (voteStatus.equals("0")) {
                            integer = 2;
                            circleImageView.setImageResource(R.drawable.ic_baseline_cancel_24);
                            displayVote.setText("Vote Unavailable");
                            displayStatus.setText("UN-AVAILABLE");
                            Toast.makeText(CheckStatus.this, "You Have Already Voted", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //tabLayout onClick
    private void switchCase(int position){
        switch (position){
            case 0:
                intent = new Intent(CheckStatus.this, CheckStatus.class);
                break;

            case 1:
                intent = new Intent(CheckStatus.this, ViewCandidate.class);
                break;

            case 2:
                intent = new Intent(CheckStatus.this, ViewParty.class);
                break;

            case 4:
                intent = new Intent(CheckStatus.this, ViewPDF.class);
                break;

            case 5:
                intent = new Intent(CheckStatus.this, MapsFragment.class);
                break;

            case 6:
                intent = new Intent(CheckStatus.this, Profile.class);
                break;
        }
        startActivity(intent);
    }

    //Display Alert Box
    private void showAlertDialog(){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Invalid Options");
        alert.setMessage("You Have Already Voted!");
        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.create().show();
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
