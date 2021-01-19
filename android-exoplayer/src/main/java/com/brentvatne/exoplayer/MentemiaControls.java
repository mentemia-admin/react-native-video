package com.brentvatne.exoplayer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.brentvatne.react.R;
import com.google.android.exoplayer2.ui.PlayerControlView;

public class MentemiaControls
{
    public static void toggleMuteControls(PlayerControlView playerControlView, boolean muted)
    {
        ImageButton muteBtn = playerControlView.findViewById(R.id.mute_btn);
        if(muted)
        {
            muteBtn.setImageResource(R.drawable.mute);
        }
        else
        {
            muteBtn.setImageResource(R.drawable.unmute);
        }
    }

    public static void setSubtitleControl(PlayerControlView playerControlView, boolean hasTextTracks)
    {
        if(!hasTextTracks)
        {
            ImageButton subtitleView = playerControlView.findViewById(R.id.subtitle_btn);
            subtitleView.setVisibility(View.GONE);
        }
    }

    public static void toggleSubtitleControl(PlayerControlView playerControlView,boolean showSubtitles)
    {
        ImageView subtitleView = playerControlView.findViewById(R.id.subtitle_btn);
        if(showSubtitles)
        {
            subtitleView.setImageResource(R.drawable.subtitles);
        }
        else
        {
            subtitleView.setImageResource(R.drawable.subtitles_off);
        }
    }
}
