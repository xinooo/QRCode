package com.example.qrcode.Setting;

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

import java.util.List;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.ViewHolder> {

    private List<SettingBean> mData;
    private OnOperationListener operationListener;
    private boolean autofocus = false;

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
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final SettingBean data = mData.get(position);
        holder.tv_id.setText(data.getid());
        if(!data.getnote().equals("")){
            holder.tv_note.setVisibility(View.VISIBLE);
            holder.tv_note.setText(data.getnote());
        }
        holder.box.setChecked(data.getisCheck());
        holder.box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String s = holder.tv_id.getText().toString();
                SettingTools.settingChange(s,isChecked);
                data.setisCheck(isChecked);
                if(position == 2){
                    //若自動對焦關閉，則確定焦點也關閉且無法選擇
                    operationListener.setCheck(position,isChecked);
                }
                SettingTools.saveJsonData(mData,CaptureActivity.mCachePath);
            }
        });
        if(position == 2 && !data.getisCheck()){
            autofocus = true;
        }
        //若自動對焦關閉，則確定焦點也關閉且無法選擇
        if(position == 3 && autofocus){
            holder.box.setEnabled(false);
            holder.box.setChecked(false);
            autofocus = false;
        }
    }

    public void setOnOperationListener(OnOperationListener listener) {
        this.operationListener = listener;
    }
    public interface OnOperationListener {
        void setCheck(int position, boolean isChecked);
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
