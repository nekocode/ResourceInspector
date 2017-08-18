package cn.nekocode.resinspector.example;

import android.app.Application;

import cn.nekocode.resinspector.ResourceInspector;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ResourceInspector.initialize(this);
    }
}
