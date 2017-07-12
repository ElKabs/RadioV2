package com.example.kabs_.radiov2;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.DebugUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import org.apache.commons.net.time.TimeTCPClient;
import android.support.fragment.*;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static com.example.kabs_.radiov2.MainActivity.ACTION_1;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnClickListener{

    String url = "http://whooshserver.net:8629/live";
    MediaPlayer mediaPlayer;
    boolean reproduciendo = false;
    ImageButton bt;

    private SimpleExoPlayer player;

    Uri radioUri = Uri.parse(url);
    private int onStatusResume = 0;
    private static final int NOTIFICATION_ID = 1234;

    public static final String ACTION_1 = "action_1";
    public static final String ACTION_2 = "action_2";

    MediaSource mediaSource;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        bt = (ImageButton)findViewById(R.id.play);
        bt.setOnClickListener(this);

        inicializarRepro();
    }

    @Override
    public void onClick(View v) {
        try {
            if(!reproduciendo) {
                reproducir2();
                reproduciendo = true;
                bt.setImageResource(android.R.drawable.ic_media_pause);
            }
            else{
                pausar();
                reproduciendo = false;
                bt.setImageResource(android.R.drawable.ic_media_play);
            }
            Log.d("MainActivity", "Reproduciendo...");
        } catch (Exception e) {
            Log.e("StreamAudioDemo", e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatemen

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(reproduciendo) {
            crearNotif();
        }
    }

    public void crearNotif(){
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notif36)
                        .setContentTitle("Noticias del Llano")
                        .setContentText("Toca para volver");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP|   Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setOngoing(true);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(001, mBuilder.build());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    public void borrarNotif(){
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        pausar();
        borrarNotif();
    }


    public void inicializarRepro(){
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);

        TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);

        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "mediaPlayerSample"), defaultBandwidthMeter);

        mediaSource = new ExtractorMediaSource(Uri.parse(url), dataSourceFactory, extractorsFactory, null, null);

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.face) {
            Intent i = new Intent(this, Main2Activity.class);
            startActivity(i);
            //aqui hay que buscar la manera de iniciar la activity main2 osea la de face
            //para probar si funciona el webView
            //depronto falla por el link pero e Stack hay links que si funcionan
            //si funciona ya terminamos la app porque es hacer lo mismo en Twitter y la pag
            //startActivity(); -aun no entiendo como funciona este metodo

        } else if (id == R.id.nav_gallery) {
            Intent i = new Intent(this, Main3Activity.class);
            startActivity(i);
        } else if (id == R.id.nav_slideshow) {
            Intent i = new Intent(this, Main5Activity.class);
            startActivity(i);

        } else if (id == R.id.nav_manage) {
            Intent i = new Intent(this, Instagram.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void pausar(){
        player.stop();
    }

    public void reproducir2(){
        player.prepare(mediaSource);

        player.setPlayWhenReady(true);
    }

    public static class NotificationActionService extends IntentService {
        MainActivity main = new MainActivity();
        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            String action = intent.getAction();

            if (ACTION_1.equals(action)) {
                main.pausar();
            }
        }
    }
}



