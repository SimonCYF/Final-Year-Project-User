package com.example.finalyearprojectuser.party;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearprojectuser.R;
import com.example.finalyearprojectuser.network.CheckNetwork;
import com.example.finalyearprojectuser.viewinfo.ViewCandidate;
import com.example.finalyearprojectuser.maps.MapsFragment;
import com.example.finalyearprojectuser.pdf.ViewPDF;
import com.example.finalyearprojectuser.recycleviewadapter.HorizontalViewAdapterParty;
import com.example.finalyearprojectuser.results.ViewRealTimeResults;
import com.example.finalyearprojectuser.vote.CheckStatus;
import com.example.finalyearprojectuser.voter.Profile;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewParty extends AppCompatActivity implements CheckNetwork.ReceiverListener {

    //Vars
    private Intent intent;
    private TabLayout tabLayout;
    private ArrayList<String> partyName = new ArrayList<>();
    private ArrayList<String> partyImage = new ArrayList<>();
    private ArrayList<String> partyHistory = new ArrayList<>();
    private ArrayList<String> partyPropaganda = new ArrayList<>();
    private DatabaseReference reference;


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
        setContentView(R.layout.activity_view_party);

        checkConnection();

        Toast.makeText(ViewParty.this, "View Available Party Page", Toast.LENGTH_SHORT).show();

        //Database Linking
        reference = FirebaseDatabase.getInstance("https://final-year-project-bc8e1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Party");

        Content content = new Content();
        content.execute();

        //Setting Icons At The Tab Layout
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
                intent = new Intent(ViewParty.this, CheckStatus.class);;
                break;

            case 1:
                intent = new Intent(ViewParty.this, ViewCandidate.class);
                break;

            case 2:
                intent = new Intent(ViewParty.this,ViewParty.class);
                break;

            case 3:
                intent = new Intent(ViewParty.this, ViewRealTimeResults.class);
                break;

            case 4:
                intent = new Intent(ViewParty.this, ViewPDF.class);
                break;

            case 5:
                intent = new Intent(ViewParty.this, MapsFragment.class);
                break;

            case 6:
                intent = new Intent(ViewParty.this, Profile.class);
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
                    partyName.clear();
                    partyImage.clear();
                    partyHistory.clear();
                    partyPropaganda.clear();

                    //Storing Data into ArrayList
                    for (DataSnapshot data : snapshot.getChildren()) {
                        PartyInfo partyInfo = data.getValue(PartyInfo.class);
                        String name_txt = partyInfo.getPartyName();
                        String image_txt = partyInfo.getPartyImage();
                        String history_txt = partyInfo.getPartyHistory();
                        String propaganda_txt = partyInfo.getPartyPropaganda();

                        partyName.add(name_txt);
                        partyImage.add(image_txt);
                        partyHistory.add(history_txt);
                        partyPropaganda.add(propaganda_txt);
                    }
                }

                //If Error
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ViewParty.this, "Fail To Obtain Data " + error, Toast.LENGTH_SHORT).show();
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            initRecycleViewForGetParty();

            return;
        }
    }

    //Adapter to Link Between Party Information with the RecycleView
    private void initRecycleViewForGetParty(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleViewForParty);
        recyclerView.setLayoutManager(layoutManager);
        HorizontalViewAdapterParty adapter = new HorizontalViewAdapterParty(this, partyName, partyImage, partyPropaganda, partyHistory);
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
