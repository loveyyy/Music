package com.example.music.ui.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.music.Interface.State;
import com.example.music.R;
import com.example.music.databinding.PlayerBinding;
import com.example.music.model.PlayInfo;
import com.example.music.model.PlayingMusicBeens;
import com.example.music.server.PlayMusicService;
import com.example.music.ui.MyApplication;
import com.example.music.ui.activity.TextLrc;
import com.example.music.ui.adapter.VP_Paly_Apt;
import com.example.music.utils.PlayController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class PlayerMusicView extends RelativeLayout {
    private PlayerBinding playerBinding;
    private Context context;
    private PlayController playController;
    private showList showList;
    private boolean isScroller = false;
    private VP_Paly_Apt vp_paly_apt;

    public PlayerMusicView(Context context) {
        super(context);
    }

    public PlayerMusicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        playerBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.player,
                this, true);
        playController = PlayController.getInstance();
        this.context = context;
        EventBus.getDefault().register(this);
        vp_paly_apt = new VP_Paly_Apt(context, playController.getPlayList());
        playerBinding.vpPlay.setAdapter(vp_paly_apt);
        setonclick();
    }

    public PlayerMusicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void setonclick() {
        vp_paly_apt.setOnItemClick(new VP_Paly_Apt.onItemClick() {
            @Override
            public void onItemClick(int pos) {
                Intent intent = new Intent();
                intent.putExtra("pos", pos);
                intent.setClass(context, TextLrc.class);
                context.startActivity(intent);
            }
        });

        playerBinding.vpPlay.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (isScroller) {
                    playController.setIndex(position);
                    playController.play();
                    playerBinding.ivPlay.setBackgroundResource(R.drawable.stop);
                    isScroller = false;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                isScroller = true;
            }
        });

        playerBinding.ivPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playController.getPlayList().isEmpty()) {
                    playController.playOrPause();
                } else {
                    ToastUtils.showShort("请选择歌曲进行播放");
                }
            }
        });


        playerBinding.ibtnDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示播放列表
                if (showList != null) {
                    showList.OnShowList(playController.getPlayList());
                }
            }
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void playResult(PlayInfo playInfo) {
        LogUtils.e(playInfo.getState().name());
        switch (playInfo.getState()){
            case PLAYING:
                playerBinding.ivPlay.setBackgroundResource(R.drawable.stop);
                break;
            case PAUSE:
                playerBinding.ivPlay.setBackgroundResource(R.drawable.play);
                break;
            case STOP:
            case BUFFER:
                playerBinding.ivPlay.setBackgroundResource(R.drawable.stop);
                break;
            case ERROR:
                Toast.makeText(MyApplication.getContext(), "播放错误,自动为您播放下一曲", Toast.LENGTH_SHORT).show();
                playController.PlayModel();
                break;
            case FINISH:
                playController.PlayModel();
                break;
            case CHANGE:
                if (vp_paly_apt != null) {
                    vp_paly_apt.notif();
                }
                if (playerBinding.vpPlay.getCurrentItem() != playController.getIndex()) {
                    playerBinding.vpPlay.setCurrentItem(playController.getIndex());
                }
                break;

        }
        playerBinding.cirPro.setProgress(playInfo.getPos() * 100 / (playController.getMusicInfo().getDuration()));
    }


    public interface showList {
        void OnShowList(List<PlayingMusicBeens> playingMusicBeens);
    }

    public void SetShowList(showList showList) {
        this.showList = showList;
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }


}
