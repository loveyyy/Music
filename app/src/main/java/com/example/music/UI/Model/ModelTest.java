package com.example.music.UI.Model;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Beens.MusicBeen;
import com.example.music.Beens.PlayingMusicBeens;
import com.example.music.Beens.SearchBeens;
import com.example.music.UI.Activity.MainHostActivity;
import com.example.music.UI.Adapter.Search_MusicAdapter;
import com.example.music.Utils.Rereofit.Api;
import com.example.music.Utils.Rereofit.ApiResponse;
import com.example.music.Utils.Rereofit.ApiSubscribe;

public class ModelTest extends ViewModel {
    private static MutableLiveData<SearchBeens> searchBeensLiveData;
    private ModelTest(){
        searchBeensLiveData = new MutableLiveData<>();
    }
    public MutableLiveData<SearchBeens> getImage() {
        return searchBeensLiveData;
    }

    public  void getmusic(final Context context, String accesstoken){
        Api.getInstance().iRetrofit.search("http://www.kuwo.cn/api/www/search/searchMusicBykeyWord",
                accesstoken,1,30)
                .compose(ApiSubscribe.<SearchBeens>io_main()).subscribe(new ApiResponse<SearchBeens>(context) {
            @Override
            public void success(final SearchBeens data) {
                searchBeensLiveData.setValue(data);
            }
        });
    }

}
