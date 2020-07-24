package com.example.qrcode.Setting;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrcode.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends Fragment {
    private String mCachePath;
    public View mview;
    private RecyclerView mrecyclerView;
    private SettingAdapter adapter;
    private List<SettingBean> mData = new ArrayList<SettingBean>();

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
        String jsonData = SettingTools.readFile("setting.json",mCachePath);
        if (TextUtils.isEmpty(jsonData)) {
            jsonData = SettingTools.getAssetsData(getContext());
        }
        mData = SettingTools.parseJson(jsonData);
        mrecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SettingAdapter(mData);
        mrecyclerView.setAdapter(adapter);
        return mview;
    }
}
