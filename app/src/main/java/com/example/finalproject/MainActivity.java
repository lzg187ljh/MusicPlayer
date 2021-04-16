package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.palette.graphics.Palette;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.View;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    //Initiate variable
    DrawerLayout drawerLayout;
    LinearLayout linearLayout;

    ImageView play, prev, next, imageView, play_mode, changeThemeButton;
    ImageView toolbar_home,toolbar_theme;
    TextView songTitleView,songArtistView,toolbar_text;
    SeekBar mSeekBarTime, mSeekBarVol;
    static MediaPlayer mMediaPlayer;
    private Runnable runnable;
    private AudioManager mAudioManager;
    int playModeCounter = 0;
    static int currentIndex = 0;
    int position = 0;
    boolean fromList;
    boolean after_rotate;
    boolean isLoopPlayback;
    boolean isSingleCycle;
    boolean isRandomCycle;
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
        play_mode = findViewById(R.id.play_mode);
        songTitleView = findViewById(R.id.songTitle);
        songArtistView = findViewById(R.id.songAritists);
        imageView = findViewById(R.id.songCover);
        mSeekBarTime = findViewById(R.id.seekBar);
        mSeekBarVol = findViewById(R.id.seekBarVol);
        changeThemeButton = findViewById(R.id.change_theme);
        fromList = false;
        after_rotate = false;

        // SharedPreference get data
        SharedPreferences sharedPreferences = this.getSharedPreferences("test", MODE_PRIVATE);
        after_rotate = sharedPreferences.getBoolean("after_rotate",false);
        position = sharedPreferences.getInt("Position",0);
        currentIndex = sharedPreferences.getInt("index",0);
        isLoopPlayback = sharedPreferences.getBoolean("isLoopPlayback",true);
        isSingleCycle = sharedPreferences.getBoolean("isSingleCycle",false);
        isRandomCycle = sharedPreferences.getBoolean("isRandomCycle",false);

        // getting values from playlist.java
        Intent intent = getIntent();
        if(intent.getStringExtra(playlist.EXTRA_MESSAGE)!=null){
            currentIndex = Integer.parseInt(intent.getStringExtra(playlist.EXTRA_MESSAGE));
            Log.v("currentIndex: ", String.valueOf(currentIndex));
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
                int index_=0;
                for(DataSnapshot ds: snapshot.getChildren()){
                    songs.add(new Music(
                            index_,
                            ds.child("songtitles").getValue(String.class),
                            ds.child("songartists").getValue(String.class),
                            ds.child("songurls").getValue(String.class),
                            ds.child("imgurls").getValue(String.class)
                    ));
                    index_++;
                }

                try{
                    if(mMediaPlayer.isPlaying()){
                        // after click on home button
                        fromList = true;
                        // ********bug here, can't really get the real current position
                        if(!fromList){
                            position = mMediaPlayer.getCurrentPosition();
                        }

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

                if(isLoopPlayback){
                    if (currentIndex < songs.size() - 1) {
                        currentIndex++;
                    } else {
                        currentIndex = 0;
                    }
                }

                if(isSingleCycle){
                    // do nothing
                }

                if(isRandomCycle){
                    Random rand = new Random();
                    int newInt;
                    while( (newInt = rand.nextInt(songs.size())) == currentIndex) {
                        //Keep looping
                    }
                    currentIndex = newInt;
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

                if(isLoopPlayback){
                    if (currentIndex > 0) {
                        currentIndex--;
                    } else {
                        currentIndex = songs.size()-1;
                    }
                }

                if(isSingleCycle){
                    // do nothing
                }

                if(isRandomCycle){
                    Random rand = new Random();
                    int newInt;
                    while( (newInt = rand.nextInt(songs.size())) == currentIndex) {
                        //Keep looping
                    }
                    currentIndex = newInt;
                }
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }

                changeMusic();
            }
        });

        play_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(playModeCounter){
                    case 0:
                        isLoopPlayback=false;
                        isRandomCycle=true;
                        isSingleCycle=false;
                        play_mode.setImageResource(R.drawable.shuffle);
                        break;
                    case 1:
                        isLoopPlayback=false;
                        isRandomCycle=false;
                        isSingleCycle=true;
                        play_mode.setImageResource(R.drawable.repeat_one);
                        break;
                    case 2:
                        isLoopPlayback=true;
                        isRandomCycle=false;
                        isSingleCycle=false;
                        play_mode.setImageResource(R.drawable.playlist_play);
                        break;
                    default:
                        isLoopPlayback=false;
                        isRandomCycle=true;
                        isSingleCycle=false;
                        play_mode.setImageResource(R.drawable.shuffle);
                        playModeCounter=0;
                        break;
                }
                playModeCounter++;
            }
        });

        changeThemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public Bitmap createImage(int width, int height, int color, float radius) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);
        Bitmap output = Bitmap.createBitmap(bitmap);
        RenderScript rs = RenderScript.create(MainActivity.this);
        ScriptIntrinsicBlur gaussianBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, output);
        gaussianBlur.setRadius(radius);
        gaussianBlur.setInput(allIn);
        gaussianBlur.forEach(allOut);
        allOut.copyTo(output);
        rs.destroy();;
        return bitmap;
    }


    private void changeBackground(String imgurl) throws IOException {
        Log.d("cbg",imgurl);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        URL url = new URL(imgurl);
        Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

        Palette palette = Palette.from(image).generate();
        // Pick one of the swatches
        Palette.Swatch vibrant = palette.getVibrantSwatch();

        if (vibrant != null) {
            // Set the background color of a layout based on the vibrant color
            linearLayout = findViewById(R.id.background_main);
            //linearLayout.setBackgroundColor(vibrant.getRgb());
            BitmapDrawable background = new BitmapDrawable(createImage(100,100,vibrant.getRgb(),25));
            linearLayout.setBackgroundDrawable(background);
            songTitleView.setTextColor(vibrant.getTitleTextColor());
            songArtistView.setTextColor(vibrant.getTitleTextColor());
        }
    }

    private void initializeMusicPlayer() {
        // intializing mediaplayer
        try {
            changeBackground(songs.get(currentIndex).getImgUrl());

            mMediaPlayer = new MediaPlayer();
            songTitleView.setText(songs.get(currentIndex).getSongTitles());
            songArtistView.setText(songs.get(currentIndex).getSongArtist());
            mMediaPlayer.setDataSource(songs.get(currentIndex).getSongUrl());
            new DownLoadImageTask(imageView).execute(songs.get(currentIndex).getImgUrl());
            mMediaPlayer.prepare();
            mMediaPlayer.seekTo(position);
            if(fromList==true || after_rotate==true){
                Log.d("fromList: ", String.valueOf(fromList));
                Log.d("after_rotate", String.valueOf(after_rotate));
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
            changeBackground(songs.get(currentIndex).getImgUrl());

            mMediaPlayer = new MediaPlayer();
            songTitleView.setText(songs.get(currentIndex).getSongTitles());
            songArtistView.setText(songs.get(currentIndex).getSongArtist());
            mMediaPlayer.setDataSource(songs.get(currentIndex).getSongUrl());
            new DownLoadImageTask(imageView).execute(songs.get(currentIndex).getImgUrl());
            mMediaPlayer.prepare();
            mMediaPlayer.start();

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d("song is ended","here");
                    if (mMediaPlayer != null) {
                        play.setImageResource(R.drawable.ic_pause);
                    }

                    if(isLoopPlayback){
                        if (currentIndex < songs.size() - 1) {
                            currentIndex++;
                        } else {
                            currentIndex = 0;
                        }
                    }

                    if(isSingleCycle){
                        // do nothing
                    }

                    if(isRandomCycle){
                        Random rand = new Random();
                        int newInt;
                        while( (newInt = rand.nextInt(songs.size())) == currentIndex) {
                            //Keep looping
                        }
                        currentIndex = newInt;
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
        editor.putBoolean("isLoopPlayback",isLoopPlayback);
        editor.putBoolean("isRandomCycle", isRandomCycle);
        editor.putBoolean("isSingleCycle", isSingleCycle);
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