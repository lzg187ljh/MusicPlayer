package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //Initiate variable
    DrawerLayout drawerLayout;

    ImageView play, prev, next, imageView;
    TextView songTitleView,songArtistView;
    SeekBar mSeekBarTime, mSeekBarVol;
    static MediaPlayer mMediaPlayer;
    private Runnable runnable;
    private AudioManager mAudioManager;
    static int currentIndex = 0;
    int position = 0;
    boolean fromList;
    boolean after_rotate;
    DatabaseReference myRef;
    // creating ArrayLists to store our songs
    final ArrayList<Music> songs = new ArrayList<Music>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawer_layout);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // initializing views
        play = findViewById(R.id.play);
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);
        songTitleView = findViewById(R.id.songTitle);
        songArtistView = findViewById(R.id.songAritists);
        imageView = findViewById(R.id.songCover);
        mSeekBarTime = findViewById(R.id.seekBar);
        mSeekBarVol = findViewById(R.id.seekBarVol);
        fromList = false;
        after_rotate = false;

        // SharedPreference get data
        SharedPreferences sharedPreferences = this.getSharedPreferences("test", MODE_PRIVATE);
        Log.v("sharedp test",sharedPreferences.getString("name",""));
        after_rotate = sharedPreferences.getBoolean("after_rotate",false);
        position = sharedPreferences.getInt("Position",0);
        currentIndex = sharedPreferences.getInt("index",0);

        // getting values from playlist.java
        Intent intent = getIntent();
        if(intent.getStringExtra(playlist.EXTRA_MESSAGE)!=null){
            currentIndex = Integer.parseInt(intent.getStringExtra(playlist.EXTRA_MESSAGE));
            // reset position
            position = 0;
            fromList = true;
        }

        //fetch data from firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // literate snapshot child
                for(DataSnapshot ds: snapshot.getChildren()){
//                    songTitles.add(ds.child("songtitles").getValue(String.class));
//                    songArtists.add(ds.child("songartists").getValue(String.class));
//                    songUrls.add(ds.child("songurls").getValue(String.class));
//                    imgUrls.add(ds.child("imgurls").getValue(String.class));
                songs.add(new Music(
                        ds.child("songtitles").getValue(String.class),
                        ds.child("songartists").getValue(String.class),
                        ds.child("songurls").getValue(String.class),
                        ds.child("imgurls").getValue(String.class)
                ));
            }
                Log.v("onCreate : List size", String.valueOf(songs.size()));

                try{
                    if(mMediaPlayer.isPlaying()){
                        mMediaPlayer.reset();
                    }
                }catch (Exception e){}

                initializeMusicPlayer();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        // seekbar volume

        int maxV = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curV = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mSeekBarVol.setMax(maxV);
        mSeekBarVol.setProgress(curV);

        mSeekBarVol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        //above seekbar volume
        //


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSeekBarTime.setMax(mMediaPlayer.getDuration());
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    play.setImageResource(R.drawable.ic_play);
                } else {
                    mMediaPlayer.start();
                    play.setImageResource(R.drawable.ic_pause);
                }

                seekBarUpdate();

            }
        });


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer != null) {
                    play.setImageResource(R.drawable.ic_pause);
                }

                if (currentIndex < songs.size() - 1) {
                    currentIndex++;
                } else {
                    currentIndex = 0;
                }

                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }

                changeMusic();
            }
        });


        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mMediaPlayer != null) {
                    play.setImageResource(R.drawable.ic_pause);
                }

                if (currentIndex > 0) {
                    currentIndex--;
                } else {
                    currentIndex = songs.size() - 1;
                }
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }

                changeMusic();
            }
        });
    }

    private void initializeMusicPlayer() {
        // intializing mediaplayer
        try {
            mMediaPlayer = new MediaPlayer();

            //Log.v("onCreate : currentIndex", String.valueOf(currentIndex));
            //Log.v("onCreate : Url", songUrls.get(currentIndex));
            songTitleView.setText(songs.get(currentIndex).getSongTitles());
            songArtistView.setText(songs.get(currentIndex).getSongArtist());
            mMediaPlayer.setDataSource(songs.get(currentIndex).getSongUrl());
            new DownLoadImageTask(imageView).execute(songs.get(currentIndex).getImgUrl());
            mMediaPlayer.prepare();
            mMediaPlayer.seekTo(position);
            if(fromList==true || after_rotate==true){
                mSeekBarTime.setMax(mMediaPlayer.getDuration());
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    play.setImageResource(R.drawable.ic_play);
                } else {
                    mMediaPlayer.start();
                    play.setImageResource(R.drawable.ic_pause);
                }

                seekBarUpdate();
            }

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.v("onCompletion", "onCompletion");
                    if (mMediaPlayer != null) {
                        play.setImageResource(R.drawable.ic_pause);
                    }

                    if (currentIndex < songs.size() - 1) {
                        currentIndex++;
                    } else {
                        currentIndex = 0;
                    }

                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                    }

                    changeMusic();
                    seekBarUpdate();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void changeMusic() {
        // intializing mediaplayer
        try {
            mMediaPlayer = new MediaPlayer();
            //Log.v("onCreate : currentIndex", String.valueOf(currentIndex));
            //Log.v("onCreate : Url", songUrls.get(currentIndex));
            songTitleView.setText(songs.get(currentIndex).getSongTitles());
            songArtistView.setText(songs.get(currentIndex).getSongArtist());
            mMediaPlayer.setDataSource(songs.get(currentIndex).getSongUrl());
            new DownLoadImageTask(imageView).execute(songs.get(currentIndex).getImgUrl());
            mMediaPlayer.prepare();
            mMediaPlayer.start();

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mMediaPlayer != null) {
                        play.setImageResource(R.drawable.ic_pause);
                    }

                    if (currentIndex < songs.size() - 1) {
                        currentIndex++;
                    } else {
                        currentIndex = 0;
                    }

                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                    }

                    changeMusic();
                    seekBarUpdate();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void ClickMenu(View view){
        openDrawer(drawerLayout);
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        //open drawer layout
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void ClickLogo(View view){
        closeDrawer(drawerLayout);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            //when drawer is open, close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void ClickHome(View view){
        recreate();
    }

    public void ClickDashboard(View view){
        // redirect activity
        redirectActivity(this,Dashboard.class);
    }

    public void ClickSearch(View view){
        // redirect activity
        redirectActivity(this, Search.class);
    }

    public void ClickLogout(View view){
        logout(this);
    }

    public static void logout(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure to logout ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // finish activity
                activity.finishAffinity();
                System.exit(0);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //show dialog
        builder.show();
    }

    public static void redirectActivity(Activity activity, Class aClass) {
        Intent intent = new Intent(activity,aClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    protected void onPause(){
        super.onPause();
        closeDrawer(drawerLayout);
    }

    // player controller below
    // AsyncTask for downloading resource
    private class DownLoadImageTask extends AsyncTask<String,Void, Bitmap> {
        ImageView imageView;

        public DownLoadImageTask(ImageView imageView){
            this.imageView = imageView;
        }

        /*
            doInBackground(Params... params)
                Override this method to perform a computation on a background thread.
         */
        protected Bitmap doInBackground(String...urls){
            String urlOfImage = urls[0];
            Bitmap logo = null;
            try{
                InputStream is = new URL(urlOfImage).openStream();
                /*
                    decodeStream(InputStream is)
                        Decode an input stream into a bitmap.
                 */
                logo = BitmapFactory.decodeStream(is);
            }catch(Exception e){ // Catch the download exception
                e.printStackTrace();
            }
            return logo;
        }

        /*
            onPostExecute(Result result)
                Runs on the UI thread after doInBackground(Params...).
         */
        protected void onPostExecute(Bitmap result){
            imageView.setImageBitmap(result);
        }
    }



    private void seekBarUpdate() {
        // seekbar duration
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mSeekBarTime.setMax(mMediaPlayer.getDuration());
                mMediaPlayer.start();
            }
        });

        mSeekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mMediaPlayer.seekTo(progress);
                    mSeekBarTime.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mMediaPlayer != null) {
                    try {
                        if (mMediaPlayer.isPlaying()) {
                            Message message = new Message();
                            message.what = mMediaPlayer.getCurrentPosition();
                            handler.sendMessage(message);
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @SuppressLint("Handler Leak") Handler handler = new Handler () {
        @Override
        public void handleMessage  (Message msg) {
            mSeekBarTime.setProgress(msg.what);
        }
    };

    // Plan B: save state using SharedPreferences while rotate
    protected void onStop() {
        SharedPreferences sharedPreferences = getSharedPreferences("test", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("index",currentIndex);
        editor.putInt("Position", mMediaPlayer.getCurrentPosition());
        editor.putBoolean("after_rotate", true);
        Log.v("onStop: ", "SharedPreferences");
        editor.commit();
        super.onStop();
    }

    // Plan A: save state using onSaveInstanceState while rotate
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//
//        super.onSaveInstanceState(savedInstanceState);
//        savedInstanceState.putInt("index",currentIndex);
//        savedInstanceState.putInt("Position", mMediaPlayer.getCurrentPosition());
//        Log.v("OnSave Position: ", String.valueOf(mMediaPlayer.getCurrentPosition()));
//    }
//    @Override
//
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//
//        super.onRestoreInstanceState(savedInstanceState);
//        after_rotate = true;
//        position = savedInstanceState.getInt("Position");
//        currentIndex = savedInstanceState.getInt("index");
//        Log.v("OnRestore index: ", String.valueOf(currentIndex));
//        Log.v("OnRestore Position: ", String.valueOf(position));
//
//    }
}