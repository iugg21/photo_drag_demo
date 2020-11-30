package com.ctrun.photo.drag.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.ctrun.photo.drag.demo.widget.DragClosableLayout;
import com.ctrun.photo.drag.demo.widget.IndicatorView;

import java.util.ArrayList;

/**
 * @author ctrun on 2016/11/18.
 * 图片查看
 */
@SuppressWarnings({"FieldCanBeLocal"})
public class ImagePagerActivity extends AppCompatActivity implements ImagePagerFragment.OnClickImageListener, DragClosableLayout.DragCallback {
    public static final String EXTRA_ENABLE_EDIT = "EXTRA_ENABLE_EDIT";
    public static final String EXTRA_CURRENT_POSITION = "EXTRA_CURRENT_POSITION";
    public static final String EXTRA_URLS = "EXTRA_URLS";
    public static final String EXTRA_DEL_URLS = "EXTRA_DEL_URLS";

    public static void start(Context context, String url) {
        ArrayList<String> urls = new ArrayList<>();
        urls.add(url);

        start(context, urls, 0);
    }

    public static void start(Context context, ArrayList<String> urls, int position) {
        Intent intent = new Intent(context, ImagePagerActivity.class);
        intent.putExtra(ImagePagerActivity.EXTRA_URLS, urls);
        intent.putExtra(ImagePagerActivity.EXTRA_CURRENT_POSITION, position);
        context.startActivity(intent);
    }

    private ViewPager mPager;
    private ImagePagerAdapter mAdapter;
    private ImageView mBtnDelete;
    private IndicatorView mIndicatorView;

    /**
     * 当前显示的图片位置
     */
    private int mCurrentPosition;
    /**
     * 需要显示的图片URL集合
     */
    private ArrayList<String> mUrls;
    private boolean mEnableEdit;
    private ArrayList<String> mDelUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.common_activity_image_pager);

        mCurrentPosition = getIntent().getIntExtra(EXTRA_CURRENT_POSITION, 0);
        mEnableEdit = getIntent().getBooleanExtra(EXTRA_ENABLE_EDIT, false);
        mUrls = getIntent().getStringArrayListExtra(EXTRA_URLS);

        setupViews();
    }

    private void setupViews() {
        mBtnDelete = findViewById(R.id.iv_delete);
        mPager = findViewById(R.id.pager);
        if (mEnableEdit) {
            mBtnDelete.setVisibility(View.VISIBLE);
        } else {
            mBtnDelete.setVisibility(View.GONE);
        }
        mIndicatorView = findViewById(R.id.indicatorView);

        mIndicatorView.setCount(mUrls.size(), 0);
        mBtnDelete.setOnClickListener(mOnClickDelete);

        mAdapter = new ImagePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mPager.addOnPageChangeListener(mOnPageChange);
        mPager.setCurrentItem(mCurrentPosition);
    }

    View.OnClickListener mOnClickDelete = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            final int selectPos = mPager.getCurrentItem();
            AlertDialog.Builder builder = new AlertDialog.Builder(ImagePagerActivity.this);
            builder.setTitle("提示");
            builder.setMessage("确定删除？");
            builder.setPositiveButton("确定", (dialog, which) -> {
                String url = mUrls.remove(selectPos);
                mDelUrls.add(url);
                if (mUrls.isEmpty()) {
                    onBackPressed();
                } else {
                    mAdapter.notifyDataSetChanged();
                    mCurrentPosition = mPager.getCurrentItem();
                    mIndicatorView.setCount(mUrls.size(), mCurrentPosition);
                    mPager.setCurrentItem(mCurrentPosition);
                }
                dialog.dismiss();
            });
            builder.setNegativeButton("取消", null);
            builder.show();
        }
    };

    private ViewPager.OnPageChangeListener mOnPageChange = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            mIndicatorView.setSelect(position);
        }
    };

    @Override
    public void onStartDrag() {
        if (mEnableEdit) {
            mBtnDelete.setVisibility(View.INVISIBLE);
        }
        mIndicatorView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onReleasedToPendingResume() {

    }

    @Override
    public void onReleasedToResume() {
        if (mEnableEdit) {
            mBtnDelete.setVisibility(View.VISIBLE);
        }
        mIndicatorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onReleasedToPendingClose() {

    }

    @Override
    public void onReleasedToClose() {

    }

    @Override
    public void onClickImage() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (mDelUrls.isEmpty()) {
            setResult(RESULT_CANCELED);
        } else {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEL_URLS, mDelUrls);
            setResult(RESULT_OK, intent);
        }
        finish();
        overridePendingTransition(0, R.anim.common_zoom_out_fast);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_CURRENT_POSITION, mPager.getCurrentItem());
    }

    class ImagePagerAdapter extends FragmentStatePagerAdapter {

        ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return ImagePagerFragment.newInstance(mUrls.get(i));
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImagePagerFragment fragment = (ImagePagerFragment) super.instantiateItem(container, position);
            fragment.setData(mUrls.get(position));
            return fragment;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mUrls.size();
        }
    }
}