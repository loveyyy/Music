package com.example.music.ui.base;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.music.R;
import com.example.music.utils.ACache;
import com.jaeger.library.StatusBarUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class BaseActivity<DB extends ViewDataBinding, VM extends BaseVM> extends AppCompatActivity {
    private ACache aCache;
    private VM vm;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT< Build.VERSION_CODES.M){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if(isLight()){
            StatusBarUtil.setLightMode(this);
            StatusBarUtil.setColor(this, this.getResources().getColor(R.color.colorPrimary),0);
        }else{
            StatusBarUtil.setTranslucentForImageViewInFragment(this,0, null);
        }
        //初始化db
        DB db = DataBindingUtil.setContentView(this, getLayout());
        aCache= ACache.get(this);
        //初始化vm
        if (vm == null) {
            Class modelClass;
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                modelClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[1];
            } else {
                //如果没有指定泛型参数，则默认使用BaseViewModel
                modelClass = BaseVM.class;
            }
            vm = (VM) new ViewModelProvider(this).get(modelClass);
        }
        initView(db);
        setVM(vm);
        setListener();
        initData();
        getLifecycle().addObserver(vm);
        db.setLifecycleOwner(this);
    }
    /*
     * 获取layout文件
     */
    public abstract int getLayout();
    /*
     * 设置是否沉浸栏显示
     */
    public abstract boolean isLight();
    /*
     * 初始化dataBinding
     */
    protected abstract void initView(DB bindView);
    /*
     * 初始化ViewModel
     */
    protected abstract void setVM(VM vm);
    /*
     * 设置监听事件
     */
    protected abstract void setListener();
    /*
     * 数据初始化
     */
    protected abstract void initData();


    public Context getContext(){
        return BaseActivity.this;
    }

    public ACache getACache(){
        return aCache;
    }

}
