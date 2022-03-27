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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearprojectuser.R;
import com.example.finalyearprojectuser.blockchain.Block;
import com.example.finalyearprojectuser.candidate.CandidateInfo;
import com.example.finalyearprojectuser.network.CheckNetwork;
import com.example.finalyearprojectuser.viewinfo.ViewCandidate;
import com.example.finalyearprojectuser.maps.MapsFragment;
import com.example.finalyearprojectuser.party.ViewParty;
import com.example.finalyearprojectuser.pdf.ViewPDF;
import com.example.finalyearprojectuser.recycleviewadapter.VerticalViewAdapterCandidateVote;
import com.example.finalyearprojectuser.recycleviewadapter.VerticalViewAdapterSenatorVote;
import com.example.finalyearprojectuser.results.ViewRealTimeResults;
import com.example.finalyearprojectuser.senator.SenatorInfo;
import com.example.finalyearprojectuser.voter.Profile;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.GsonBuilder;
import com.reginald.editspinner.EditSpinner;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class CandidateSelectionAfterCheckStatus extends AppCompatActivity implements CheckNetwork.ReceiverListener {

    //Vars
    private RecyclerView recyclerView;
    private Intent intent;
    private SwitchCompat switchOptions;
    private ExtendedFloatingActionButton submitButton;
    private ImageView displayImage;
    private String icNum, selectedCandidateName = null, selectedSenatorName = null, storeCandidateName, storeCandidateName2, storeSenatorName, storeSenatorName2, blockchainHash, candidateCurrentState, voterPostcode;
    private Integer voteCount, updatedVoteCount, senatorVoteCount, updatedSenatorVoteCount;
    private String [] test;
    private EditSpinner spinner;
    private String spinnerOptions[] = {};
    private ArrayAdapter<String> spinnerList;
    private TextView displayIc, displayState, displaySwitch, textView;
    private SharedPreferences sharedPreferences, candidateSharedPreferences, blockSharedPreferences;
    private Integer switchResults = 0;

    //Candidate List
    private ArrayList<String> candidateName = new ArrayList<>();
    private ArrayList<String> candidateImage = new ArrayList<>();
    private ArrayList<String> candidatePropaganda = new ArrayList<>();
    private ArrayList<String> candidateState = new ArrayList<>();
    private ArrayList<String> candidateParty = new ArrayList<>();

    //Senator List
    private ArrayList<String> senatorName = new ArrayList<>();
    private ArrayList<String> senatorImage = new ArrayList<>();
    private ArrayList<String> senatorPostcode = new ArrayList<>();
    private ArrayList<String> senatorState = new ArrayList<>();
    private ArrayList<String> senatorParty = new ArrayList<>();

    private DatabaseReference reference, userReference, candidateReference, senatorReference, senatorVotesReference;
    private VerticalViewAdapterCandidateVote verticalViewAdapterVote;
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

    //Blockchain Vars
    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static int difficulty = 2;
    public static int count = 0;

    //Fingerprint Vars
    private static final int REQUEST_CODE = 10;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    @RequiresApi(api = Build.VERSION_CODES.R)

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_page);
        executor = ContextCompat.getMainExecutor(this);

        final MediaPlayer mp = MediaPlayer.create(this, R.raw.button);

        //Linking
        textView = findViewById(R.id.textViewForVote);
        recyclerView = findViewById(R.id.recycleViewForVote);
        displayState = findViewById(R.id.textViewForVote);
        submitButton = findViewById(R.id.submitFloatingActionButton);
        displaySwitch = findViewById(R.id.voteResultsChange);
        switchOptions = findViewById(R.id.voteSwitch);

        //Share Preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        icNum = sharedPreferences.getString("voterIcNumber", "");
        candidateCurrentState = sharedPreferences.getString("voterState","");
        voterPostcode = sharedPreferences.getString("voterPostcode","");

        //Candidate Shared Preferences
        candidateSharedPreferences = getSharedPreferences("SelectedCandidate", MODE_PRIVATE);
        candidateSharedPreferences.edit().remove("candidateSelected").commit();
        candidateSharedPreferences.edit().remove("senatorSelected").commit();


        //Switch On Switch
        switchOptions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkConnection();
                if (isChecked){
                    switchResults = 1;
                    displaySwitch.setText("Senate");
                    textView.setText("Available Senate With Same Postcode Candidate Display Here");
                }else {
                    switchResults = 2;
                    displaySwitch.setText("Parliament");
                    textView.setText("Available Parliament Candidate Display Here");
                }
                checkSwitch();
            }
        });

        //displayIc.setText(icNum);
        displayState.setText(candidateCurrentState+" Parliament Candidate");

        //Database Linking
        reference = FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Candidate");
        userReference = FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Voter").child(icNum);
        userReference.keepSynced(true);

        Content content = new Content();
        content.execute();

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

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.start();
                checkConnection();
                selectedCandidateName = candidateSharedPreferences.getString("candidateSelected","");
                selectedSenatorName = candidateSharedPreferences.getString("senatorSelected","");

                Log.d("name", selectedCandidateName);
                Log.d("name", selectedSenatorName);
                if(selectedCandidateName.equals("")){
                    showAlertDialog("Parliament Candidate");
                }else if(selectedSenatorName.equals("")){
                    showAlertDialog("Senator");
                } else{

                    ProgressDialog progressdialog = new ProgressDialog(CandidateSelectionAfterCheckStatus.this);
                    progressdialog.setMessage("Please Wait....");
                    progressdialog.show();
                    progressdialog.setCancelable(false);
                    progressdialog.setCanceledOnTouchOutside(false);

                    //Remove [ ] From Shared Preferences
                    storeCandidateName = selectedCandidateName.toString().replace("[", "");
                    storeCandidateName2 = storeCandidateName.toString().replace("]", "");

                    storeSenatorName = selectedSenatorName.toString().replace("[", "");
                    storeSenatorName2 = storeSenatorName.toString().replace("]", "");

                    //Database Linkage
                    candidateReference = FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Candidate").child(storeCandidateName2);
                    senatorVotesReference = FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Senator").child(storeSenatorName2);

                    candidateSharedPreferences.edit().remove("candidateSelected").commit();
                    candidateSharedPreferences.edit().remove("senatorSelected").commit();

                    biometricPrompt = new BiometricPrompt(CandidateSelectionAfterCheckStatus.this,
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

                            Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();


                            getCandidateVoteCount(new Callback(){
                                @Override
                                public void onCallBack(Integer result) {
                                    voteCount = result;
                                    Log.d("voteCount", String.valueOf(voteCount));
                                    updatedVoteCount = voteCount + 1;
                                    Log.d("voteCountUpdate", String.valueOf(updatedVoteCount));
                                    candidateReference.child("candidateTotalVotes").setValue(updatedVoteCount);
                                }
                            });

                            getSenatorVoteCount(new Callback() {
                                @Override
                                public void onCallBack(Integer result) {
                                    senatorVoteCount = result;
                                    Log.d("voteCount", String.valueOf(senatorVoteCount));
                                    updatedSenatorVoteCount = senatorVoteCount + 1;
                                    Log.d("voteCountUpdate", String.valueOf(updatedSenatorVoteCount));
                                    senatorVotesReference.child("senatorTotalVotes").setValue(updatedSenatorVoteCount);

                                }
                            });
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            //Toast.makeText(getApplicationContext(), "Updating "+storeCandidateName2+ "Votes Count", Toast.LENGTH_SHORT).show();


                            userReference.child("voterVoteStatus").setValue("0");
                            userReference.child("voterVoteSenator").setValue(storeSenatorName2);
                            userReference.child("voterVoteCandidate").setValue(storeCandidateName2);
                            if (count == 0) {
                                blockchain.add(new Block(storeCandidateName2, "0"));
                                blockchain.get(count).mineBlock(difficulty);
                                count += 1;
                            } else {
                                blockchain.add(new Block(storeCandidateName2, blockchain.get(blockchain.size() - 1).hash));
                                blockchain.get(count).mineBlock(difficulty);

                                count += 1;
                            }

                            progressdialog.dismiss();

                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(getApplicationContext(), "Storing Into Blockchain", Toast.LENGTH_SHORT).show();


                            String blockDisplay = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
                            Log.d("blockchain", blockDisplay);
                            //startActivity(intent);

                            progressdialog.dismiss();

                            Log.d("tets", String.valueOf(blockchainHash));
                            intent = new Intent(CandidateSelectionAfterCheckStatus.this, CheckStatus.class);
                            startActivity(intent);

                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                    //Create Alert Box
                    promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Scan Your Fingerprint To Proceed To Confirm Votes")
                            .setSubtitle("Use Only Enrolled Left Or Right Thumb")
                            .setDescription("Fingerprint")
                            .setNegativeButtonText("Cancel")
                            .build();

                    biometricPrompt.authenticate(promptInfo);

                }
            }
        });

    }

    //Verify Switch
    private void checkSwitch(){
        if (switchResults == 1){
            getSenatorName();
        }else{
            Content content = new Content();
            content.execute();
        }
    }

    //Display Alert Box
    private void showAlertDialog(String input){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Incomplete Selection");
        alert.setMessage("Please Select A "+input);
        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.create().show();
    }

    //Display Alert Box
    private void showAlertDialogForInvalid(String input){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Error");
        alert.setMessage(input);
        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.create().show();
    }

    //Edit Spinner Obtain Senator List
    private void getSenatorName(){

        ArrayList<String> senatorList = new ArrayList<>();
        senatorReference = FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Senator");

        senatorReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senatorList.clear();
                senatorName.clear();
                senatorState.clear();
                senatorParty.clear();
                senatorPostcode.clear();
                senatorImage.clear();

                for(DataSnapshot data: snapshot.getChildren()) {
                    SenatorInfo senatorInfo = data.getValue(SenatorInfo.class);
                    String name_txt = senatorInfo.getSenatorName();
                    String senator_image_txt = senatorInfo.getSenatorImage();
                    String senator_state_txt = senatorInfo.getSenatorState();
                    String senator_postcode_txt = senatorInfo.getSenatorPostcode();

                    if(senator_state_txt.equals(candidateCurrentState) && senator_postcode_txt.equals(voterPostcode)){
                        senatorList.add(name_txt);
                        senatorName.add(name_txt);
                        senatorImage.add(senator_image_txt);
                        senatorPostcode.add(senator_postcode_txt);
                        senatorState.add(senator_state_txt);
                        senatorParty.add("");
                    }

                }

                //If The ArrayList is Empty
                if(senatorName.isEmpty()){
                    showAlertDialogForInvalid("No Available Senator At "+ voterPostcode +" In "+ candidateCurrentState);

                }

                initRecycleViewForGetSenator();
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
                intent = new Intent(CandidateSelectionAfterCheckStatus.this, CheckStatus.class);
                break;

            case 1:
                intent = new Intent(CandidateSelectionAfterCheckStatus.this, ViewCandidate.class);
                break;

            case 2:
                intent = new Intent(CandidateSelectionAfterCheckStatus.this, ViewParty.class);
                break;

            case 3:
                intent = new Intent(CandidateSelectionAfterCheckStatus.this, ViewRealTimeResults.class);
                break;

            case 4:
                intent = new Intent(CandidateSelectionAfterCheckStatus.this, ViewPDF.class);
                break;

            case 5:
                intent = new Intent(CandidateSelectionAfterCheckStatus.this,MapsFragment.class);
                break;

            case 6:
                intent = new Intent(CandidateSelectionAfterCheckStatus.this, Profile.class);
                break;

        }
        startActivity(intent);
    }

    //Function To Allow Things To Run In Background Obtaining From Database
    private class Content extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    //Clear Previous Data
                    candidateName.clear();
                    candidateImage.clear();
                    candidatePropaganda.clear();
                    candidateParty.clear();
                    candidateState.clear();

                    //Storing Data into ArrayList
                    for (DataSnapshot data : snapshot.getChildren()) {
                        CandidateInfo candidateInfo = data.getValue(CandidateInfo.class);
                        String name_txt = candidateInfo.getCandidateName();
                        String image_txt = candidateInfo.getCandidateImage();
                        String propaganda_txt = candidateInfo.getCandidatePropaganda();
                        String party_txt = candidateInfo.getCandidateParty();
                        String state_txt = candidateInfo.getCandidateState();

                        if (state_txt.equals(candidateCurrentState)) {
                            candidateName.add(name_txt);
                            candidateImage.add(image_txt);
                            candidateState.add(state_txt);
                            candidatePropaganda.add(propaganda_txt);
                            candidateParty.add(party_txt);
                        }
                    }

                    //If The ArrayList is Empty
                    if(candidateName.isEmpty()){
                        showAlertDialogForInvalid("No Available  Parliament Candidate In "+ candidateCurrentState);

                    }
                }

                //If Error
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(CandidateSelectionAfterCheckStatus.this, "Fail To Obtain Data " + error, Toast.LENGTH_SHORT).show();
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            initRecycleViewForGetCandidate();

            return;
        }
    }

    private void getCandidateVoteCount(@NonNull Callback call){

        candidateReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int candidateVoteCount = snapshot.child("candidateTotalVotes").getValue(Integer.class);
                call.onCallBack(candidateVoteCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSenatorVoteCount(@NonNull Callback call){

        senatorVotesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int senatorVoteCount = snapshot.child("senatorTotalVotes").getValue(Integer.class);
                call.onCallBack(senatorVoteCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Adapter to Link Between Candidate Information with the RecycleView
    private void initRecycleViewForGetCandidate(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleViewForVote);
        recyclerView.setLayoutManager(layoutManager);
        VerticalViewAdapterCandidateVote adapter = new VerticalViewAdapterCandidateVote(this, candidateName, candidateImage, candidateState, candidatePropaganda, candidateParty);
        recyclerView.setAdapter(adapter);
    }

    //Adapter to Link Between Senator Information with the RecycleView
    private void initRecycleViewForGetSenator(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleViewForVote);
        recyclerView.setLayoutManager(layoutManager);
        VerticalViewAdapterSenatorVote adapter = new VerticalViewAdapterSenatorVote(this, senatorName, senatorImage, senatorState, senatorParty, senatorPostcode);
        recyclerView.setAdapter(adapter);
    }

    //Callback Interface
    public interface Callback{
        void onCallBack(Integer result);
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
