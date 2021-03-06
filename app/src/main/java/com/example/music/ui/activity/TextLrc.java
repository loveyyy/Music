package com.example.music.ui.activity;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.music.R;
import com.example.music.databinding.LrcBinding;
import com.example.music.model.BaseRespon;
import com.example.music.model.LrcBeen;
import com.example.music.model.PlayInfo;
import com.example.music.model.PlayingMusicBeens;
import com.example.music.ui.MyApplication;
import com.example.music.ui.base.BaseActivity;
import com.example.music.utils.PlayController;
import com.example.music.utils.imageutils.GildeCilcleImageUtils;
import com.example.music.viewmodel.LrcVM;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * Created by Administrator on 2018/7/3.
 */

public class TextLrc extends BaseActivity<LrcBinding, LrcVM> implements PlayController.OnMusicChange {
    private LrcBinding lrcBinding;
    private LrcVM lrcVM;
    private PlayController playController;
    private PlayingMusicBeens playingMusicBeens;
    private Animation animation;

    @Override
    public int getLayout() {
        return R.layout.lrc;
    }

    @Override
    public boolean isLight() {
        return false;
    }

    @Override
    protected void initView(LrcBinding bindView) {
        lrcBinding = bindView;
        playController = PlayController.getInstance();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void setVM(LrcVM vm) {
        lrcVM = vm;

        lrcVM.lrc.observe(this, new Observer<BaseRespon<LrcBeen>>() {
            @Override
            public void onChanged(BaseRespon<LrcBeen> baseRespon) {
                lrcBinding.lrctext.init(baseRespon.getData().getLrclist());
                RequestOptions requestOptions = new RequestOptions().transform(new GildeCilcleImageUtils());
                Glide.with(getApplicationContext()).load(playingMusicBeens.getAlbumpic()).apply(requestOptions).into(lrcBinding.ivBac);
            }
        });
    }

    @Override
    protected void setListener() {

        lrcBinding.ibLrcLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playingMusicBeens = playController.playNext();
                lrcVM.getLrc(playingMusicBeens.getRid());
            }
        });
        lrcBinding.ibLrcNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playingMusicBeens = playController.playNext();
                lrcVM.getLrc(playingMusicBeens.getRid());
            }
        });
        lrcBinding.ibLrcPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playController.playOrPause();
            }
        });
    }

    @Override
    protected void initData() {
        playingMusicBeens = playController.getMusicInfo();
        lrcVM.getLrc(playingMusicBeens.getRid());
         animation = AnimationUtils.loadAnimation(getContext(),R.anim.rotate);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void playResult(PlayInfo playInfo) {
        switch (playInfo.getState()){
            case PLAYING:
                lrcBinding.ibLrcPlay.setBackgroundResource(R.drawable.ic_lrc_stop);
                if(lrcBinding.ivBac.getAnimation() == null){
                    if(animation!=null){
                        lrcBinding.ivBac.startAnimation(animation);//开始动画
                    }
                }
                break;
            case PAUSE:
                lrcBinding.ibLrcPlay.setBackgroundResource(R.drawable.ic_lrc_play);
                lrcBinding.ivBac.clearAnimation();
                break;
            case STOP:
            case BUFFER:
                lrcBinding.ibLrcPlay.setBackgroundResource(R.drawable.ic_lrc_stop);
                break;
            case ERROR:
                Toast.makeText(MyApplication.getContext(), "播放错误,自动为您播放下一曲", Toast.LENGTH_SHORT).show();
                playController.PlayModel();
                break;
            case FINISH:
                playController.PlayModel();
                break;
        }
        lrcBinding.lrctext.setPro(playInfo.getPos());
    }

    @Override
    public void Change() {
        playingMusicBeens = playController.getMusicInfo();
        lrcVM.getLrc(playingMusicBeens.getRid());
    }
}
