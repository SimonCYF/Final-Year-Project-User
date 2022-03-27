package com.example.finalyearprojectuser.blockchain;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.finalyearprojectuser.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.GsonBuilder;

public class DisplayBlockchain extends AppCompatActivity {

    //vars
    private TabLayout tabLayout;
    private Intent intent;
    private FirebaseAuth mAuth;
    private TextView textView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //get sharedPreferences data
        @SuppressLint("WrongConstant")
        SharedPreferences sp = getSharedPreferences("SharedPref", MODE_PRIVATE);

        String msg = sp.getString("block","");

        String blockDisplay = new GsonBuilder().setPrettyPrinting().create().toJson(msg);
        textView.setText(blockDisplay);
    }



}
