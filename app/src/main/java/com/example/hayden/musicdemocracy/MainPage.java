package com.example.hayden.musicdemocracy;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;

import com.example.hayden.musicdemocracy.MusicService.*;
import android.widget.MediaController.MediaPlayerControl;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import static com.example.hayden.musicdemocracy.R.layout.activity_main_page;

public class MainPage extends AppCompatActivity implements MediaPlayerControl{

    int x = 0;
    private ArrayList<Song> songList;
    private ListView songView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public MusicService musicSrv;

    public Intent playIntent;
    public boolean musicBound=false;

    private MusicController controller;

    private boolean paused=false, playbackPaused=false, firstClick=true, firstClickDur=true;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main_page);

        Log.d("MyApp", "I am here");

        songView = (ListView) findViewById(R.id.song_list);

        songList = new ArrayList<Song>();

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d("it has permission", "should be good");
        } else {
            showAccess();
            Log.d("MyApp", "else statement");
        }

        getSongList();

        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);

        setController();

        //new playlist button

        /*final Button newButton = (Button) findViewById(R.id.newButton);
        newButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (x > 1){
                    newButton.setText("too many times!");
                }
                else{
                    newButton.setText("Clicked");
                }
                x += 1;
            }
        });*/

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void setController(){
        //set the controller up
        controller = new MusicController(this);

        controller.setMediaPlayer(this);
        controller.setEnabled(true);
        controller.setAnchorView(findViewById(R.id.song_list));

        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Log.d("getsongs", "trying to get songs");

        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null); //this is where it crashes on a read permission issue

        Log.d("getsongs", "retrieved data from musicResolver");

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());

        }

    }

    private void showAccess() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Allow Access");
        builder.setMessage("Please allow access to music files");
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        });
        builder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("MainPage Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        controller.hide();
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public void songPicked(View view){
        /*Log.d("Song picked", "in here");
        int check = Integer.parseInt(view.getTag().toString());
        Log.d("song picked value", Integer.toString(check));
        musicSrv.setSong(check);
        Log.d("Song picked", "in here afterwards");
        musicSrv.playSong();
        */
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        if(playbackPaused){
            playbackPaused=false;
            setController();
        }

        controller.show(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //menu item selected
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                musicSrv.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

    //play next
    private void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            playbackPaused=false;
            setController();
        }
        controller.show(0);
    }

    //play previous
    private void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            playbackPaused=false;
            setController();
        }
        controller.show(0);
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {

        if(musicSrv!=null && musicBound && musicSrv.isPng()){ //&& musicSrv.isPng()
            //firstClickDur = false;
            return musicSrv.getDur();
        }
        else if(musicSrv != null && musicBound && firstClickDur == false){ //added afterwards - keep duration displayed on pause
            Log.d("saved duration value", Integer.toString(musicSrv.getDur()));
            return musicSrv.getDur();
        }
        else{
            firstClickDur = false;
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv != null && musicBound && musicSrv.isPng()) {
            Log.d("position of track", Integer.toString(musicSrv.getPosn()));
            //firstClick = false;
            return musicSrv.getPosn();
        }
        else if (musicSrv != null && musicBound && firstClick==false){ //added afterwards - keep position displayed on pause
            Log.d("paused at this point", Integer.toString(musicSrv.getPosn()));
            return musicSrv.getPosn();
        }
        else{
            firstClick = false;
            playbackPaused = false;  //setting this flag makes the position move on the first click
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        Log.d("seek position = ", Integer.toString(pos));
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound) {
            playbackPaused = false;

            //this try catch is used to sleep for milliseconds to allow the ui and mediaplayer to stay synced.
            try {
                Thread.sleep(5);
                Log.d("delayed", "in is playing");
            } catch (InterruptedException e) {
                Log.d("ERROR", "in playing");
                e.printStackTrace();
            }
            return musicSrv.isPng();
        }
        else {
            playbackPaused = true; //modified
            return false;
        }
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    protected void onPause(){
        super.onPause();
        paused=true;
        playbackPaused = true; //added
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused=false;
            playbackPaused = false; //added
        }
    }



    /*@Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }*/
}
