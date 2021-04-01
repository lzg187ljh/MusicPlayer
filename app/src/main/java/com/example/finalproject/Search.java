package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.View;

public class Search extends AppCompatActivity {
    //Initiate variable
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        drawerLayout = findViewById(R.id.drawer_layout);
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
        MainActivity.redirectActivity(this,Dashboard.class);
    }

    public void ClickSearch(View view){
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