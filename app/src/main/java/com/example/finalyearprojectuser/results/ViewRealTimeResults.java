package com.example.finalyearprojectuser.results;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearprojectuser.R;
import com.example.finalyearprojectuser.candidate.CandidateInfo;
import com.example.finalyearprojectuser.network.CheckNetwork;
import com.example.finalyearprojectuser.viewinfo.ViewCandidate;
import com.example.finalyearprojectuser.maps.MapsFragment;
import com.example.finalyearprojectuser.party.ViewParty;
import com.example.finalyearprojectuser.pdf.ViewPDF;
import com.example.finalyearprojectuser.recycleviewadapter.VerticalViewAdapterResult;
import com.example.finalyearprojectuser.senator.SenatorInfo;
import com.example.finalyearprojectuser.vote.CheckStatus;
import com.example.finalyearprojectuser.voter.Profile;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.reginald.editspinner.EditSpinner;

import java.util.ArrayList;

public class ViewRealTimeResults extends AppCompatActivity implements CheckNetwork.ReceiverListener {

    //Vars
    private String state[] = {"DEFAULT","JOHOR", "KEDAH", "KELANTAN", "MELACCA", "NEGERI SEMBILAN", "PAHANG", "PENANG", "PERAK", "PERLIS", "SABAH", "SARAWAK", "SELANGOR", "TERENGGANU", "KUALA LUMPUR", "LABUAN", "PUTRAJAYA"};
    private Integer integer = 0;
    private DatabaseReference reference, selectedReference, senatorReference;
    private EditSpinner spinner;
    private SwitchCompat decisionSwitch;
    private boolean checkStateStatus = false;
    private TextView textView;
    private Intent intent;
    private String selectedState = null;
    private ArrayAdapter<String> stateList;
    private TabLayout tabLayout;

    //Candidate Array List
    private ArrayList<String> candidateName = new ArrayList<>();
    private ArrayList<String> candidateImage = new ArrayList<>();
    private ArrayList<String> candidatePropaganda = new ArrayList<>();
    private ArrayList<String> candidateState = new ArrayList<>();
    private ArrayList<String> candidateParty = new ArrayList<>();
    private ArrayList<String> selectedStateCandidateName = new ArrayList<>();
    private ArrayList<Integer> candidateVote = new ArrayList<Integer>();

    //Senator Array List
    private ArrayList<String> senatorName = new ArrayList<>();
    private ArrayList<String> senatorImage = new ArrayList<>();
    private ArrayList<String> senatorPostcode = new ArrayList<>();
    private ArrayList<String> senatorState = new ArrayList<>();
    private ArrayList<String> senatorParty = new ArrayList<>();
    private ArrayList<Integer> senatorVote = new ArrayList<Integer>();

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
        setContentView(R.layout.activity_view_results);

        checkConnection();

        //Linking
        textView = findViewById(R.id.resultsChange);
        spinner = findViewById(R.id.stateSpinner);
        decisionSwitch = findViewById(R.id.viewResultsSwitch);

        //Database Linking
        reference = FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Candidate");
        senatorReference = FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Senator");
        displayCandidateData();

        Toast.makeText(ViewRealTimeResults.this,"Default Vote Results View", Toast.LENGTH_LONG).show();

        //Linking Edit Spinner
        stateList = new ArrayAdapter<String>(ViewRealTimeResults.this, android.R.layout.simple_spinner_item, state);
        stateList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(stateList);

        //Spinner On Click
        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                checkConnection();
                selectedState = spinner.getText().toString();
                Toast.makeText(ViewRealTimeResults.this,selectedState,Toast.LENGTH_SHORT).show();

                getAllData();

            }
        });



        //Decision Switch On Switch
        decisionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkConnection();
                if (isChecked) {
                    integer = 2;
                    textView.setText("Senator");

                    if(selectedState == null){
                        displaySenatorData();

                    }else {
                        getAllData();
                    }

                }else{
                    integer = 1;
                    textView.setText("Parliament");

                    if(selectedState == null){
                        displayCandidateData();

                    }else {
                        getAllData();
                    }

                }

            }
        });


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
    }

    //tabLayout onClick
    private void switchCase(int position){
        switch (position){
            case 0:
                intent = new Intent(ViewRealTimeResults.this, CheckStatus.class);
                break;

            case 1:
                intent = new Intent(ViewRealTimeResults.this, ViewCandidate.class);
                break;

            case 2:
                intent = new Intent(ViewRealTimeResults.this, ViewParty.class);
                break;

            case 3:
                intent = new Intent(ViewRealTimeResults.this, ViewRealTimeResults.class);
                break;

            case 4:
                intent = new Intent(ViewRealTimeResults.this, ViewPDF.class);
                break;

            case 5:
                intent = new Intent(ViewRealTimeResults.this, MapsFragment.class);
                break;

            case 6:
                intent = new Intent(ViewRealTimeResults.this, Profile.class);
                break;

        }
        startActivity(intent);
    }

    //Function To Get Senator Data
    private void getAllData(){

        //Default + Parliament
        if(selectedState.equals("DEFAULT") && integer == 1) {

            displayCandidateData();

            //State + Parliament
        } else if(integer == 1){

            checkStateStatus = true;
            Content content = new Content();
            content.execute();

            //Default + Senator
        } else if(selectedState.equals("DEFAULT") && integer == 2){

            displaySenatorData();

            //State + Senator
        }else if (integer == 2){

            senatorReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    //Clear Previous Data
                    senatorName.clear();
                    senatorImage.clear();
                    senatorState.clear();
                    senatorPostcode.clear();
                    senatorParty.clear();
                    senatorVote.clear();

                    //To Store Into ArrayList
                    for (DataSnapshot data : snapshot.getChildren()){
                        SenatorInfo senatorInfo = data.getValue(SenatorInfo.class);
                        String name_txt = senatorInfo.getSenatorName();
                        String image_txt = senatorInfo.getSenatorImage();
                        String state_txt = senatorInfo.getSenatorState();
                        String postcode_txt = senatorInfo.getSenatorPostcode();
                        Integer vote_txt = senatorInfo.getSenatorTotalVotes();

                        if (state_txt.equals(selectedState)) {
                            senatorName.add(name_txt);
                            senatorImage.add(image_txt);
                            senatorState.add(state_txt);
                            senatorPostcode.add(postcode_txt);
                            senatorParty.add("");
                            senatorVote.add(vote_txt);
                        }


                    }

                    //If The ArrayList is Empty
                    if(senatorName.isEmpty()){
                        showAlertDialog("Senator In "+ selectedState);

                    }

                    initRecycleViewForGetSenator();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

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
                    candidateVote.clear();

                    //Storing Data into ArrayList
                    for (DataSnapshot data : snapshot.getChildren()) {
                        CandidateInfo candidateInfo = data.getValue(CandidateInfo.class);
                        String name_txt = candidateInfo.getCandidateName();
                        String image_txt = candidateInfo.getCandidateImage();
                        String propaganda_txt = candidateInfo.getCandidatePropaganda();
                        String party_txt = candidateInfo.getCandidateParty();
                        String state_txt = candidateInfo.getCandidateState();
                        int vote_txt = candidateInfo.getCandidateTotalVotes();

                         if(checkStateStatus == true) {
                            if (state_txt.equals(selectedState)) {
                                candidateName.add(name_txt);
                                candidateImage.add(image_txt);
                                candidateState.add(state_txt);
                                candidatePropaganda.add(propaganda_txt);
                                candidateParty.add(party_txt);
                                candidateVote.add(vote_txt);
                            }
                        }
                    }

                    if(candidateName.isEmpty()){
                        showAlertDialog("Candidate In "+selectedState);

                    }
                }

                //If Error
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showAlertDialog(selectedState);
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

    //Pop Up Message To Show No Candidate In That State
    private void showAlertDialog(String state){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final AlertDialog dialog = alert.create();
        alert.setTitle("Error");
        alert.setMessage("No Available "+ state);
        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               dialog.dismiss();
            }
        });
        alert.create().show();
    }

    //All Candidate Data
    private void displayCandidateData(){
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Clear Previous Data
                candidateName.clear();
                candidateImage.clear();
                candidatePropaganda.clear();
                candidateParty.clear();
                candidateState.clear();
                candidateVote.clear();

                //Storing Data into ArrayList
                for (DataSnapshot data : snapshot.getChildren()) {
                    CandidateInfo candidateInfo = data.getValue(CandidateInfo.class);
                    String name_txt = candidateInfo.getCandidateName();
                    String image_txt = candidateInfo.getCandidateImage();
                    String propaganda_txt = candidateInfo.getCandidatePropaganda();
                    String party_txt = candidateInfo.getCandidateParty();
                    String state_txt = candidateInfo.getCandidateState();
                    int vote_txt = candidateInfo.getCandidateTotalVotes();

                    //Adding Into Arrays
                    candidateName.add(name_txt);
                    candidateImage.add(image_txt);
                    candidateState.add(state_txt);
                    candidatePropaganda.add(propaganda_txt);
                    candidateParty.add(party_txt);
                    candidateVote.add(vote_txt);

                }

                initRecycleViewForGetCandidate();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //All Senator Data
    private void displaySenatorData(){
        senatorReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Clear Previous Data
                senatorName.clear();
                senatorImage.clear();
                senatorState.clear();
                senatorPostcode.clear();
                senatorParty.clear();
                senatorVote.clear();

                //To Store Into ArrayList
                for (DataSnapshot data : snapshot.getChildren()){
                    SenatorInfo senatorInfo = data.getValue(SenatorInfo.class);
                    String name_txt = senatorInfo.getSenatorName();
                    String image_txt = senatorInfo.getSenatorImage();
                    String state_txt = senatorInfo.getSenatorState();
                    String postcode_txt = senatorInfo.getSenatorPostcode();
                    Integer vote_txt = senatorInfo.getSenatorTotalVotes();

                    senatorName.add(name_txt);
                    senatorImage.add(image_txt);
                    senatorState.add(state_txt);
                    senatorPostcode.add(postcode_txt);
                    senatorParty.add("");
                    senatorVote.add(vote_txt);
                }

                initRecycleViewForGetSenator();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Adapter to Link Between Candidate Information with the RecycleView
    private void initRecycleViewForGetCandidate(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleViewForResult);
        recyclerView.setLayoutManager(layoutManager);
        VerticalViewAdapterResult adapter = new VerticalViewAdapterResult(this, candidateName, candidateImage, candidateState, candidatePropaganda, candidateParty, candidateVote);
        recyclerView.setAdapter(adapter);
    }


    //Adapter to Link Between Senator Information with the RecycleView
    private void initRecycleViewForGetSenator(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleViewForResult);
        recyclerView.setLayoutManager(layoutManager);
        VerticalViewAdapterResult adapter = new VerticalViewAdapterResult(this, senatorName, senatorImage, senatorState, senatorParty, senatorPostcode, senatorVote);
        recyclerView.setAdapter(adapter);
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

