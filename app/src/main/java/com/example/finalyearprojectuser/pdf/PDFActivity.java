package com.example.finalyearprojectuser.pdf;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalyearprojectuser.R;
import com.example.finalyearprojectuser.viewinfo.ViewCandidate;
import com.example.finalyearprojectuser.maps.MapsFragment;
import com.example.finalyearprojectuser.party.ViewParty;
import com.example.finalyearprojectuser.results.ViewRealTimeResults;
import com.example.finalyearprojectuser.vote.CheckStatus;
import com.example.finalyearprojectuser.voter.Profile;
import com.google.android.material.tabs.TabLayout;

public class PDFActivity extends AppCompatActivity {

    //Vars
    WebView webView;
    private ProgressBar progressBar;
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

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pdf_display_fragment);

        webView = findViewById(R.id.WV);
        progressBar = findViewById(R.id.pb);
        progressBar.setVisibility(View.VISIBLE);

        //Set Web View Allow To Obtain From the Government Website
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setWebChromeClient(new WebChromeClient());
        Intent intent = getIntent();
        final int position = intent.getIntExtra("position",0);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished(WebView view, String url) {
                webView.loadUrl("javascript:(function() { " +
                        "document.querySelector('[role=\"toolbar\"]').remove();})()");
                progressBar.setVisibility(View.GONE);
            }
        });
        //https://docs.google.com/viewerng/viewer?embedded=true&url=
        webView.loadUrl("https://docs.google.com/gview?embedded=true&url="+ ViewPDF.list.get(position).getPdfUrl());


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
    private void switchCase(int position) {
        switch (position) {
            case 0:
                intent = new Intent(PDFActivity.this, CheckStatus.class);
                break;

            case 1:
                intent = new Intent(PDFActivity.this, ViewCandidate.class);
                break;

            case 2:
                intent = new Intent(PDFActivity.this, ViewParty.class);
                break;

            case 3:
                intent = new Intent(PDFActivity.this, ViewRealTimeResults.class);
                break;

            case 4:
                intent = new Intent(PDFActivity.this, ViewPDF.class);
                break;

            case 5:
                intent = new Intent(PDFActivity.this, MapsFragment.class);
                break;

            case 6:
                intent = new Intent(PDFActivity.this, Profile.class);
                break;
        }
        startActivity(intent);
    }
}