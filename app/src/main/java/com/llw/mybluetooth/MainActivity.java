package com.llw.mybluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.llw.mybluetooth.adapter.DeviceAdapter;
import com.llw.mybluetooth.util.StatusBarUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static int REQUEST_ENABLE_BLUETOOTH = 1;

    BluetoothAdapter bluetoothAdapter;

    private TextView scanDevices;
    private LinearLayout loadingLay;
    private RecyclerView rv;
    private BluetoothReceiver bluetoothReceiver;

    private RxPermissions rxPermissions;

    DeviceAdapter mAdapter;
    List<BluetoothDevice> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StatusBarUtil.StatusBarLightMode(this);//状态栏黑色字体

        initView();//初始化控件

        checkVersion();//检查版本
    }

    /**
     * 初始化蓝牙配置
     */
    private void initBlueTooth() {
        IntentFilter intentFilter = new IntentFilter();//创建一个IntentFilter对象
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);//获得扫描结果
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//绑定状态变化
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//开始扫描
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//扫描结束
        bluetoothReceiver = new BluetoothReceiver();//实例化广播接收器
        registerReceiver(bluetoothReceiver, intentFilter);//注册广播接收器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//获取蓝牙适配器
    }

    /**
     * 检查Android版本
     */
    private void checkVersion() {
        if (Build.VERSION.SDK_INT >= 23) {//6.0或6.0以上
            permissionsRequest();//动态权限申请
        } else {//6.0以下
            initBlueTooth();//初始化蓝牙配置
        }
    }

    /**
     * 动态权限申请
     */
    private void permissionsRequest() {//使用这个框架使用了Lambda表达式，设置JDK版本为 1.8或者更高
        rxPermissions = new RxPermissions(this);//实例化这个权限请求框架，否则会报错
        rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {//申请成功
                        initBlueTooth();//初始化蓝牙配置
                    } else {//申请失败
                        showMsg("权限未开启");
                    }
                });
    }

    /**
     * 初始化控件
     */
    private void initView() {
        loadingLay = findViewById(R.id.loading_lay);
        scanDevices = findViewById(R.id.scan_devices);
        rv = findViewById(R.id.rv);
        scanDevices.setOnClickListener(this);
    }

    /**
     * 结果返回
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                showMsg("蓝牙打开成功");
            } else {
                showMsg("蓝牙打开失败");
            }
        }
    }

    /**
     * 消息提示
     *
     * @param msg 消息内容
     */
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.scan_devices) {
            if (bluetoothAdapter != null) {
                if (bluetoothAdapter.isEnabled()) {//打开
                    //开始扫描周围的蓝牙设备,如果扫描到蓝牙设备，通过广播接收器发送广播
                    if (mAdapter != null) {
                        list.clear();
                        mAdapter.notifyDataSetChanged();
                    }
                    bluetoothAdapter.startDiscovery();
                } else {//未打开
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
                }
            } else {
                showMsg("你的设备不支持蓝牙");
            }
        }
    }

    /**
     * 获取已绑定设备
     */
    private void getBondedDevice() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {//如果获取的结果大于0，则开始逐个解析
            for (BluetoothDevice device : pairedDevices) {
                if (list.indexOf(device) == -1) {//防止重复添加
                    if (device.getName() != null) {//过滤掉设备名称为null的设备
                        list.add(device);
                    }
                }
            }
        }
    }


    //广播接收器
    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND://扫描到设备
                    showDevicesData(context, intent);
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED://设备绑定状态发生改变
                    mAdapter.changeBondDevice();
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED://开始扫描
                    loadingLay.setVisibility(View.VISIBLE);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED://扫描结束
                    loadingLay.setVisibility(View.GONE);
                    break;
            }
        }

    }

    /**
     * 显示蓝牙设备信息
     *
     * @param context
     * @param intent
     */
    private void showDevicesData(Context context, Intent intent) {
        getBondedDevice();//获取已绑定的设备
        //获取周围蓝牙设备
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (list.indexOf(device) == -1) {//防止重复添加

            if (device.getName() != null) {//过滤掉设备名称为null的设备
                list.add(device);
            }
        }
        mAdapter = new DeviceAdapter(R.layout.item_device_list, list);
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(mAdapter);

        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                //点击时获取状态，如果已经配对过了就不需要在配对
                if (list.get(position).getBondState() == BluetoothDevice.BOND_NONE) {
                    createOrRemoveBond(1, list.get(position));//开始匹配
                } else {
                    showDialog("确定要取消配对吗？", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //取消配对
                            createOrRemoveBond(2, list.get(position));//取消匹配
                        }
                    });
                }
            }
        });
    }

    //提示弹窗
    private void showDialog(String dialogTitle, @NonNull DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialogTitle);
        builder.setPositiveButton("确定", onClickListener);
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //卸载广播接收器
        unregisterReceiver(bluetoothReceiver);
    }

    /**
     * 创建或者取消匹配
     *
     * @param type 处理类型 1 匹配  2  取消匹配
     * @param device 设备
     */
    private void createOrRemoveBond(int type, BluetoothDevice device) {
        Method method = null;
        try {
            switch (type) {
                case 1://开始匹配
                    method = BluetoothDevice.class.getMethod("createBond");
                    method.invoke(device);
                    break;
                case 2://取消匹配
                    method = BluetoothDevice.class.getMethod("removeBond");
                    method.invoke(device);
                    list.remove(device);//清除列表中已经取消了配对的设备
                    break;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }


}
