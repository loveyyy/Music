package com.example.music.ui.activity;

import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music.BR;
import com.example.music.R;
import com.example.music.databinding.RecListInfoActivityBinding;
import com.example.music.http.Api;
import com.example.music.http.ApiResponse;
import com.example.music.http.RxHelper;
import com.example.music.model.BaseRespon;
import com.example.music.model.DownLoadInfo;
import com.example.music.model.DownlodMusciInfo;
import com.example.music.model.PlayingMusicBeens;
import com.example.music.model.RecListInfo;
import com.example.music.server.TaskDispatcher;
import com.example.music.ui.base.BaseAdapter;
import com.example.music.ui.base.BaseActivity;
import com.example.music.ui.custom.CustomDialogFragment;
import com.example.music.ui.custom.PlayerMusicView;
import com.example.music.utils.PlayController;
import com.example.music.viewmodel.RecVM;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Create By morningsun  on 2019-12-10
 */
public class RecListInfoActivity extends BaseActivity<RecListInfoActivityBinding, RecVM> implements PlayerMusicView.showList {
    private RecListInfoActivityBinding recListInfoActivityBinding;
    private RecVM recVM;

    @Override
    public int getLayout() {
        return R.layout.rec_list_info_activity;
    }

    @Override
    public boolean isLight() {
        return false;
    }

    @Override
    protected void initView(RecListInfoActivityBinding bindView) {
        recListInfoActivityBinding = bindView;
        recListInfoActivityBinding.playview.SetShowList(this);
    }

    @Override
    protected void setVM(RecVM vm) {
        recVM = vm;
        recVM.recListInfo.observe(this, new Observer<BaseRespon<RecListInfo>>() {
            @Override
            public void onChanged(final BaseRespon<RecListInfo> rec_list_infoBaseRespon) {
                try {
                    Glide.with(getContext()).load(rec_list_infoBaseRespon.getData().getImg()).into(recListInfoActivityBinding.ivRec);
                    recListInfoActivityBinding.ivRecName.setText(rec_list_infoBaseRespon.getData().getName());
                    recListInfoActivityBinding.ivRecInfo.setText(rec_list_infoBaseRespon.getData().getDesc());
                    recListInfoActivityBinding.ivRecInfo1.setText(rec_list_infoBaseRespon.getData().getInfo());

                    recListInfoActivityBinding.rcvRec.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
                    BaseAdapter<RecListInfo.MusicListBean> beanBaseAdapter = new BaseAdapter<>(getContext(), rec_list_infoBaseRespon.getData().getMusicList(), R.layout.rcv_rec_list_apt, BR.rec_list_info);
                    recListInfoActivityBinding.rcvRec.setAdapter(beanBaseAdapter);
                    beanBaseAdapter.setOnDownLoad(new BaseAdapter.OnDownLoad() {
                        @Override
                        public void OnDownLoadListener(int pos) {
                            //查询链接
                            Api.getInstance().iRetrofit.downloadMusic(rec_list_infoBaseRespon.getData().getMusicList().get(pos).getMusicrid().split("_")[1],
                                    "kuwo", "id", 1, "XMLHttpRequest")
                                    .compose(RxHelper.observableIO2Main(getApplicationContext()))
                                    .subscribe(new ApiResponse<BaseRespon<List<DownlodMusciInfo>>>() {
                                        @Override
                                        public void success(BaseRespon<List<DownlodMusciInfo>> data) {
                                            DownLoadInfo downLoadInfo = new DownLoadInfo();
                                            downLoadInfo.setUrl(data.getData().get(0).getUrl());
                                            downLoadInfo.setFilename(data.getData().get(0).getAuthor() + "-" + data.getData().get(0).getTitle() + ".mp3");
                                            downLoadInfo.setFilepath(Environment.getExternalStorageDirectory().getPath() + File.separator + "mv");

                                            TaskDispatcher.getInstance().enqueue(downLoadInfo);
                                        }

                                    });
                        }
                    });
                    beanBaseAdapter.setOnItemClick(new BaseAdapter.OnItemClick() {
                        @Override
                        public void OnItemClickListener(int pos) {
                            ArrayList<PlayingMusicBeens> playingMusicBeens = new ArrayList<>();
                            for (RecListInfo.MusicListBean listBean : rec_list_infoBaseRespon.getData().getMusicList()) {
                                PlayingMusicBeens playingMusicBeens1 = new PlayingMusicBeens();
                                playingMusicBeens1.setAlbum(listBean.getAlbum());
                                playingMusicBeens1.setAlbumpic(listBean.getAlbumpic());
                                playingMusicBeens1.setDuration(listBean.getDuration());
                                playingMusicBeens1.setMusic_singer(listBean.getArtist());
                                playingMusicBeens1.setMusicid(listBean.getMusicrid());
                                playingMusicBeens1.setMusicname(listBean.getName());
                                playingMusicBeens1.setPic(listBean.getPic());
                                playingMusicBeens1.setRid(String.valueOf(listBean.getRid()));
                                playingMusicBeens.add(playingMusicBeens1);
                            }
                            PlayController.getInstance().setPlayList(playingMusicBeens,pos);
                        }
                    });


                } catch (NullPointerException e) {
                    Toast.makeText(getApplicationContext(), "请返回重试", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    protected void setListener() {

    }

    protected void initData() {
        Intent intent = getIntent();
        recVM.getRecListInfo(intent.getStringExtra("rid"), "1", "30");
    }


    @Override
    public void OnShowList(List<PlayingMusicBeens> playingMusicBeens) {
        CustomDialogFragment customDialogFragment = new CustomDialogFragment(playingMusicBeens, getApplicationContext());
        customDialogFragment.show(getSupportFragmentManager(), "RecListInfo");
    }
}
