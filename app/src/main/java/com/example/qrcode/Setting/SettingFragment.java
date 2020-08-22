package com.example.qrcode.Setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrcode.R;

import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends Fragment {
    private String mCachePath;
    public View mview;
    private RecyclerView mrecyclerView;
    private SettingAdapter adapter;
    private List<SettingBean> mData = new ArrayList<SettingBean>();

    //Toolbar
    private ImageView leftbutton,rightbutton;
    private TextView title;
    private LinearLayout toolbar;

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCachePath = getActivity().getFilesDir().getPath() + "/cache";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mview = inflater.inflate(R.layout.settingfragment, container, false);
        mrecyclerView = (RecyclerView)mview.findViewById(R.id.recyclerview);
        //Toolbar
        title = (TextView)mview.findViewById(R.id.scanner_toolbar_title);
        leftbutton = (ImageView) mview.findViewById(R.id.scanner_toolbar_leftbutton);
        rightbutton = (ImageView) mview.findViewById(R.id.scanner_toolbar_rightbutton);
        toolbar = (LinearLayout) mview.findViewById(R.id.include);
        leftbutton.setImageDrawable(getActivity().getResources().getDrawable(R.mipmap.ic_menu_back));
        rightbutton.setVisibility(View.GONE);
        title.setText(getString(R.string.menu4));
        title.setTextColor(getActivity().getResources().getColor(R.color.white));
        toolbar.setBackgroundColor(getActivity().getResources().getColor(R.color.colorPrimary));
        mData = SettingTools.getSettingData(getContext(),mCachePath);
        mrecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SettingAdapter(mData);
        mrecyclerView.setAdapter(adapter);
        leftbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack(); //返回
            }
        });
        return mview;
    }

}
