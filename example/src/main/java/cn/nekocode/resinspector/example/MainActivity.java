package cn.nekocode.resinspector.example;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cn.nekocode.resinspector.ResourceInspector;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ResourceInspector.wrap(newBase));
    }
}
