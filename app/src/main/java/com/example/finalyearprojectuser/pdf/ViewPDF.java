package com.example.finalyearprojectuser.pdf;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearprojectuser.R;
import com.example.finalyearprojectuser.network.CheckNetwork;
import com.example.finalyearprojectuser.viewinfo.ViewCandidate;
import com.example.finalyearprojectuser.maps.MapsFragment;
import com.example.finalyearprojectuser.party.ViewParty;
import com.example.finalyearprojectuser.results.ViewRealTimeResults;
import com.example.finalyearprojectuser.vote.CheckStatus;
import com.example.finalyearprojectuser.voter.Profile;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class ViewPDF extends AppCompatActivity implements CheckNetwork.ReceiverListener{

    //Vars
    public static List<PDFModel> list;
    private RecyclerView recyclerView;
    private Intent intent;
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
        setContentView(R.layout.activity_view_pdf);

        checkConnection();

        recyclerView = findViewById(R.id.recycleViewForPDF);

        Toast.makeText(ViewPDF.this, "View SPR PDF Page", Toast.LENGTH_SHORT).show();

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

        list = new ArrayList<>();
        list.add(new PDFModel("Rules","https://www.spr.gov.my/sites/default/files/perundangan/pua-386-1981-bm.pdf"));
        list.add(new PDFModel("SPR Plan", "https://www.spr.gov.my/sites/default/files/Pelan%20Reformasi.pdf"));

        recyclerView.setLayoutManager(new GridLayoutManager(this,2));

        ItemClickListener itemClickListener = new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {

                checkConnection();
                Intent intent = new Intent(ViewPDF.this, PDFActivity.class);
                //intent.putExtra("url",list.get(position).getPdfUrl());
                intent.putExtra("position",position);
                startActivity(intent);
            }
        };

        PDFAdapter adapter = new PDFAdapter(list,this,itemClickListener);
        recyclerView.setAdapter(adapter);
    }

    //tabLayout onClick
    private void switchCase(int position) {
        switch (position) {
            case 0:
                intent = new Intent(ViewPDF.this, CheckStatus.class);
                break;

            case 1:
                intent = new Intent(ViewPDF.this, ViewCandidate.class);
                break;

            case 2:
                intent = new Intent(ViewPDF.this, ViewParty.class);
                break;

            case 3:
                intent = new Intent(ViewPDF.this, ViewRealTimeResults.class);
                break;

            case 4:
                intent = new Intent(ViewPDF.this, ViewPDF.class);
                break;

            case 5:
                intent = new Intent(ViewPDF.this, MapsFragment.class);
                break;

            case 6:
                intent = new Intent(ViewPDF.this, Profile.class);
                break;
        }
        startActivity(intent);
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
