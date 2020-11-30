package com.ctrun.photo.drag.demo;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * @author ctrun on 2020/11/30.
 */
public class MainActivity extends AppCompatActivity {

    final int[] mImageResIds = {R.id.iv_image_1, R.id.iv_image_2, R.id.iv_image_3};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_activity_main);

        ArrayList<String> urls = new ArrayList<>();
        urls.add("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=3026939796,485761977&fm=26&gp=0.jpg");
        urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1606731224258&di=9a855e91b0ef192730a1f33e34790bc8&imgtype=0&src=http%3A%2F%2Fimg.ewebweb.com%2Fuploads%2F20190403%2F15%2F1554276138-OkeysTKJBN.jpg");
        urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1606733622577&di=a93caa59b57adce85c9ff8252ab32710&imgtype=0&src=http%3A%2F%2Fn.sinaimg.cn%2Fsinacn11%2F570%2Fw370h200%2F20180610%2Fd190-hcufqif1733929.gif");

        for (int i = 0; i < mImageResIds.length; i++) {
            ImageView imageView = findViewById(mImageResIds[i]);

            Glide.with(this).load(urls.get(i)).into(imageView);

            final int position = i;
            imageView.setOnClickListener((v) -> ImagePagerActivity.start(MainActivity.this, urls, position));
        }

    }

}
