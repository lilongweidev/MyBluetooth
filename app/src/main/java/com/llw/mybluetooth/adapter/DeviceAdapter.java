package com.llw.mybluetooth.adapter;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.llw.mybluetooth.R;

import java.util.List;

public class DeviceAdapter extends BaseQuickAdapter<BluetoothDevice, BaseViewHolder> {
    public DeviceAdapter(int layoutResId, @Nullable List<BluetoothDevice> data) {
        super(layoutResId, data);
    }


    @Override
    protected void convert(BaseViewHolder helper, BluetoothDevice item) {

        if (item.getName() == null) {
            helper.setText(R.id.tv_name, "无名");
        } else {
            helper.setText(R.id.tv_name, item.getName());
        }

        ImageView imageView = helper.getView(R.id.iv_device_type);
        getDeviceType(item.getBluetoothClass().getMajorDeviceClass(), imageView);

        switch (item.getBondState()) {
            case 12:
                helper.setText(R.id.tv_bond_state, "已配对");
                break;
            case 11:
                helper.setText(R.id.tv_bond_state, "正在配对...");
                break;
            case 10:
                helper.setText(R.id.tv_bond_state, "未配对");
                break;
        }

        helper.addOnClickListener(R.id.item_device);

    }

    public void changeBondDevice(){
        notifyDataSetChanged();
    }

    private void getDeviceType(int type, ImageView imageView) {
        switch (type) {
            case BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES://耳机
            case BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET://穿戴式耳机
            case BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE://蓝牙耳机
            case BluetoothClass.Device.Major.AUDIO_VIDEO://音频设备
                imageView.setImageResource(R.mipmap.icon_headset);
                break;
            case BluetoothClass.Device.Major.COMPUTER://电脑
                imageView.setImageResource(R.mipmap.icon_computer);
                break;
            case BluetoothClass.Device.Major.PHONE://手机
                imageView.setImageResource(R.mipmap.icon_phone);
                break;
            case BluetoothClass.Device.Major.HEALTH://健康类设备
                imageView.setImageResource(R.mipmap.icon_health);
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_CAMCORDER://照相机录像机
            case BluetoothClass.Device.AUDIO_VIDEO_VCR://录像机
                imageView.setImageResource(R.mipmap.icon_vcr);
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO://车载设备
                imageView.setImageResource(R.mipmap.icon_car);
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER://扬声器
                imageView.setImageResource(R.mipmap.icon_loudspeaker);
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_MICROPHONE://麦克风
                imageView.setImageResource(R.mipmap.icon_microphone);
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO://打印机
                imageView.setImageResource(R.mipmap.icon_printer);
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX://音频视频机顶盒
                imageView.setImageResource(R.mipmap.icon_top_box);
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CONFERENCING://音频视频视频会议
                imageView.setImageResource(R.mipmap.icon_meeting);
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER://显示器和扬声器
                imageView.setImageResource(R.mipmap.icon_tv);
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_GAMING_TOY://游戏
                imageView.setImageResource(R.mipmap.icon_game);
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR://可穿戴设备
                imageView.setImageResource(R.mipmap.icon_wearable_devices);
                break;
            default://其它
                imageView.setImageResource(R.mipmap.icon_bluetooth);
                break;
        }
    }
}
