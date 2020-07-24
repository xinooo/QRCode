package com.example.qrcode.Setting;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrcode.R;
import com.example.qrcode.zxing.activity.CaptureActivity;
import com.example.qrcode.zxing.camera.AutoFocusCallback;
import com.example.qrcode.zxing.camera.CameraManager;

import java.util.List;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.ViewHolder> {

    private List<SettingBean> mData;
    private AutoFocusCallback autoFocusCallback  = new AutoFocusCallback();

    public SettingAdapter(List<SettingBean> Data){
        this.mData = Data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final SettingBean data = mData.get(position);
        holder.tv_id.setText(data.getid());
        if(!data.getnote().equals("")){
            holder.tv_note.setVisibility(View.VISIBLE);
            holder.tv_note.setText(data.getnote());
        }
        holder.box.setChecked(false);
        holder.box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String s = holder.tv_id.getText().toString();
                switch (s){
                    case "播放提示音":
                        Log.e("AA",s+isChecked);
                        break;
                    case "复制到剪贴板":
                        Log.e("AA",s+isChecked);
                        break;
                    case "自动对焦":
                        Log.e("AA",s+isChecked);
                        CaptureActivity.handler.isFocus = isChecked;
                        if(isChecked){
                            CameraManager.get().requestAutoFocus(CaptureActivity.handler, R.id.auto_focus);
                        }else {
                            autoFocusCallback.setHandler(null, 0);
                        }
                        break;
                    case "确定焦点":
                        Log.e("AA",s+isChecked);
                        break;
                    case "自动打开网页":
                        Log.e("AA",s+isChecked);
                        break;
                    case "反色":
                        Log.e("AA",s+isChecked);
                        break;
                }
                data.setisCheck(isChecked);
                SettingTools.saveJsonData(mData,CaptureActivity.mCachePath);
            }
        });
        holder.box.setChecked(data.getisCheck());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout r1;
        private TextView tv_id,tv_note;
        private CheckBox box;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            r1 = (RelativeLayout) itemView.findViewById(R.id.r1);
            tv_id = (TextView) itemView.findViewById(R.id.tv_id);
            tv_note = (TextView) itemView.findViewById(R.id.tv_note);
            box = (CheckBox) itemView.findViewById(R.id.box);
        }
    }
}
