package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

public class Dashboard extends AppCompatActivity {
    //Initiate variable
    DrawerLayout drawerLayout;
    TextView toolbar_title;
    ImageView changeThemeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tab_layout);
        ViewPager Pager = (ViewPager)findViewById(R.id.viewpager);

        // pass getSupportFragmentManager() as parameter
        tabpagerAdapter myTabpagerAdapter = new tabpagerAdapter(getSupportFragmentManager());
        Pager.setAdapter(myTabpagerAdapter);
        tabLayout.setupWithViewPager(Pager);

        changeThemeButton = findViewById(R.id.change_theme);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar_title = findViewById(R.id.toolbar_id);
        toolbar_title.setText("Music");

        changeThemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void ClickMenu(View view){
        MainActivity.openDrawer(drawerLayout);
    }

    public void ClickLogo(View view){
        MainActivity.closeDrawer(drawerLayout);
    }


    public void ClickHome(View view){
        MainActivity.redirectActivity(this,MainActivity.class);
    }

    public void ClickDashboard(View view){
        recreate();
    }


    public void ClickLogout(View view){
        MainActivity.logout(this);
    }


    protected void onPause(){
        super.onPause();
        MainActivity.closeDrawer(drawerLayout);
    }


}