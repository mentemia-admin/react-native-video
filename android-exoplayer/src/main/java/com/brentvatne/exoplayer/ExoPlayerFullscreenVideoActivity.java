package com.brentvatne.exoplayer;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.brentvatne.react.R;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerControlView;

public class ExoPlayerFullscreenVideoActivity extends AppCompatActivity implements ReactExoplayerView.FullScreenDelegate {
    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_ORIENTATION = "extra_orientation";

    private int id;
    private PlayerControlView playerControlView;
    private SimpleExoPlayer player;
    private boolean muted;
    private Player.EventListener isPlayingEventListener;
    private View containerView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getIntent().getIntExtra(EXTRA_ID, -1);
        String orientation = getIntent().getStringExtra(EXTRA_ORIENTATION);
        if ("landscape".equals(orientation)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else if ("portrait".equals(orientation)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        setContentView(R.layout.exo_player_fullscreen_video);
        containerView = findViewById(R.id.enclosing_layout);
        containerView.setKeepScreenOn(true);
        player = ReactExoplayerView.getViewInstance(id).getPlayer();

        ExoPlayerView playerView = findViewById(R.id.player_view);
        playerView.setPlayer(player);
        playerView.setOnClickListener(v -> togglePlayerControlVisibility());

        // See if we can reuse the controls from the inline view
        playerControlView = findViewById(R.id.player_controls);
        playerControlView.setPlayer(player);
        // Set the fullscreen button to "close fullscreen" icon
        ImageView fullscreenIcon = playerControlView.findViewById(R.id.exo_fullscreen_icon);
        fullscreenIcon.setImageResource(R.drawable.fullscreen_exit);
        playerControlView.findViewById(R.id.exo_fullscreen_button)
                .setOnClickListener(v -> ReactExoplayerView.getViewInstance(id).setFullscreen(false));

      /*
       Mentemia change - this was causing the user to have to double click play
      */
     /*    //Handling the playButton click event
        playerControlView.findViewById(R.id.exo_play).setOnClickListener(v -> {
            if (player != null && player.getPlaybackState() == Player.STATE_ENDED) {
                player.seekTo(0);
            }

            ReactExoplayerView.getViewInstance(id).setPausedModifier(false);
        });

        //Handling the pauseButton click event
        playerControlView.findViewById(R.id.exo_pause).setOnClickListener(v -> ReactExoplayerView.getViewInstance(id).setPausedModifier(true)); */

        // Mentemia specific controls
        initMentemiaControls();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ReactExoplayerView.getViewInstance(id) != null) {
        /**
        Mentemia change - Play/Pause the video if it has come back from the background
        **/
            if(ReactExoplayerView.getViewInstance(id).getFullscreenState() && isInBackground)
            {
                player.setPlayWhenReady(!ReactExoplayerView.getViewInstance(id).getPausedState());
                isInBackground = false;
            }
            ReactExoplayerView.getViewInstance(id).syncPlayerState();
            ReactExoplayerView.getViewInstance(id).registerFullScreenDelegate(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        /**
        Mentemia change - only pause if the video is going to background and not when switching to inline. This prevents flickering with the icons
        **/
        if(ReactExoplayerView.getViewInstance(id).getFullscreenState())
        {
            isInBackground = true;
            player.setPlayWhenReady(false);
        }
        if (ReactExoplayerView.getViewInstance(id) != null) {
            ReactExoplayerView.getViewInstance(id).registerFullScreenDelegate(null);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            playerControlView.postDelayed(this::hideSystemUI, 200);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (ReactExoplayerView.getViewInstance(id) != null) {
                ReactExoplayerView.getViewInstance(id).setFullscreen(false);
                return false;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void togglePlayerControlVisibility() {
        if (playerControlView.isVisible()) {
            playerControlView.hide();
        } else {
            playerControlView.show();
        }
    }

    /**
     * Enables regular immersive mode.
     */
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    /**
     * Shows the system bars by removing all the flags
     * except for the ones that make the content appear under the system bars.
     */
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void closeFullScreen() {
        finish();
    }

    /***
     *
     *
     * Mentemia specific code to handle additional controls
     *
     *
     *
     */

    private boolean isInBackground = false;
    private void initMentemiaControls()
    {


        MentemiaControls.setSubtitleControl(playerControlView,ReactExoplayerView.getViewInstance(id).hasTextTracks());
        MentemiaControls.toggleMuteControls(playerControlView,ReactExoplayerView.getViewInstance(id).getMutedState());
        MentemiaControls.toggleSubtitleControl(playerControlView,ReactExoplayerView.getViewInstance(id).getSubtitleState());

        playerControlView.findViewById(R.id.subtitle_btn).setOnClickListener(v -> {
            ReactExoplayerView.getViewInstance(id).toggleSubtitles();
            MentemiaControls.toggleSubtitleControl(playerControlView,ReactExoplayerView.getViewInstance(id).getSubtitleState());
        });

        playerControlView.findViewById(R.id.mute_btn).setOnClickListener(v -> {
            ReactExoplayerView.getViewInstance(id).toggleMute();
            MentemiaControls.toggleMuteControls(playerControlView,ReactExoplayerView.getViewInstance(id).getMutedState());
        });

        isPlayingEventListener = new Player.EventListener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                ReactExoplayerView.getViewInstance(id).setIsPaused(isPlaying);
                if(isPlaying)
                {
                    containerView.setKeepScreenOn(true);
                    playerControlView.setShowTimeoutMs(5000);
                }
                else
                {
                    containerView.setKeepScreenOn(false);
                    playerControlView.setShowTimeoutMs(0);
                }
                playerControlView.show();
            }
        };
        player.addListener(isPlayingEventListener);
    }


}
