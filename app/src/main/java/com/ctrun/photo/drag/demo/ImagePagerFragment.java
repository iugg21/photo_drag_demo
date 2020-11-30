package com.ctrun.photo.drag.demo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ctrun.photo.drag.demo.util.ImageUtils;
import com.ctrun.photo.drag.demo.widget.DragClosableLayout;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;

import pl.droidsonroids.gif.GifImageView;

/**
 * @author ctrun on 2016/11/18.
 */
public class ImagePagerFragment extends Fragment {
    private static final String TAG = "ImagePagerFragment";

    private static final String ARG_PARAM1 = "param1";

    protected LayoutInflater mInflater;

    private DragClosableLayout mDragClosableLayout;
    private View image;
    private ProgressBar loading;

    private OnClickImageListener listener;
    private DragClosableLayout.DragCallback dragCallback;

    public void setData(String uriString) {
        uri = uriString;
    }

    private String uri;
    // Gesture Detectors

    public static ImagePagerFragment newInstance(String uri) {
        ImagePagerFragment fragment = new ImagePagerFragment();
        Bundle b = new Bundle();
        b.putString(ARG_PARAM1, uri);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            uri = getArguments().getString(ARG_PARAM1);
        }

        mInflater = LayoutInflater.from(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.common_fragment_image_pager, container, false);

        mDragClosableLayout = view.findViewById(R.id.fl_drag_closable_layout);
        mDragClosableLayout.attachToActivity(getActivity());
        mDragClosableLayout.addDragCallback(dragCallback);
        mDragClosableLayout.setHandleCallback(() -> {
            if (image instanceof SubsamplingScaleImageView) {
                if (((SubsamplingScaleImageView) image).getScale() > ((SubsamplingScaleImageView) image).getMinScale()) {
                    return false;
                }
            }
            return true;
        });
        mDragClosableLayout.findViewById(R.id.fl_content).setOnClickListener(onClickContentLayout);

        loading = view.findViewById(R.id.pb_loading);
        loading.setVisibility(View.INVISIBLE);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!isAdded()) {
            return;
        }

        Glide.with(this)
                .downloadOnly()
                .load(uri)
                .into(new SimpleTarget<File>() {
                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        loading.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        if (!isAdded()) {
                            return;
                        }
                        Toast.makeText(getContext(), "载入图片失败", Toast.LENGTH_SHORT).show();
                        loading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                        if (!isAdded()) {
                            return;
                        }

                        if (ImageUtils.isGIF(resource)) {
                            loading.setVisibility(View.GONE);
                            image = mInflater.inflate(R.layout.common_imageview_gif, null);
                            mDragClosableLayout.setContentView(image);
                            image.setOnClickListener(onClickImage);
                        } else {
                            SubsamplingScaleImageView photoView = (SubsamplingScaleImageView) mInflater.inflate(R.layout.common_imageview_touch, mDragClosableLayout, false);
                            photoView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
                            image = photoView;
                            mDragClosableLayout.setContentView(image);
                            image.setOnClickListener(onClickImage);

                            photoView.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {

                                @Override
                                public void onImageLoaded() {
                                    if (!isAdded()) {
                                        return;
                                    }

                                    loading.setVisibility(View.GONE);
                                }

                                @Override
                                public void onImageLoadError(Exception e) {
                                    if (!isAdded()) {
                                        return;
                                    }

                                    Toast.makeText(getContext(), "载入图片失败", Toast.LENGTH_SHORT).show();
                                    loading.setVisibility(View.GONE);
                                }
                            });

                        }

                        try {
                            if (image instanceof GifImageView) {
                                ((GifImageView) image).setImageURI(Uri.fromFile(resource));
                            } else if (image instanceof SubsamplingScaleImageView) {
                                SubsamplingScaleImageView scaleImageView = (SubsamplingScaleImageView) ImagePagerFragment.this.image;
                                scaleImageView.setImage(ImageSource.uri(resource.getAbsolutePath()));
                                //scaleImageView.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_OUTSIDE);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "", e);
                        }
                    }
                });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnClickImageListener) {
            listener = (OnClickImageListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnClickImageListener");
        }

        if (context instanceof DragClosableLayout.DragCallback) {
            dragCallback = (DragClosableLayout.DragCallback) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        dragCallback = null;
    }

    private final View.OnClickListener onClickContentLayout = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onClickImage();
            }
        }
    };

    private final View.OnClickListener onClickImage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onClickImage();
            }
        }
    };

    @Override
    public void onDestroyView() {
        //回收图片
        if (image != null) {
            if (image instanceof GifImageView) {
                ((GifImageView) image).setImageURI(null);
            /*} else if (image instanceof PhotoView) {
                try {
                    ((BitmapDrawable) ((PhotoView) image).getDrawable()).getBitmap().recycle();
                } catch (Exception e) {
                    LogUtils.e(e);
                }*/
            }
        }
        super.onDestroyView();
    }

    public interface OnClickImageListener {
        void onClickImage();
    }
}
