package com.example.map;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;


import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.Button;



import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;



public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private AMap mAMap;
    private Button mDrawButton;

    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private boolean mIsDrawing = false;
    private List<LatLng> mLatLngList = new ArrayList<>();
    private Polyline mPolyline;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化地图
        mMapView = findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mAMap = mMapView.getMap();

        // 初始化定位
        try {
            initLocation();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 检查定位权限
        checkLocationPermission();

        // 初始化按钮
        mDrawButton = findViewById(R.id.btn_draw);
        mDrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsDrawing) {
                    mIsDrawing = false;
                    mDrawButton.setText("开始绘制轨迹");
                    // 清除之前绘制的轨迹
                    if (mPolyline != null) {
                        mPolyline.remove();
                    }
                } else {
                    mIsDrawing = true;
                    mDrawButton.setText("停止绘制轨迹");
                }
            }
        });
    }

    // 初始化定位
    private void initLocation() throws Exception {
        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setInterval(2000);
        mLocationClient.setLocationOption(mLocationOption);

        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        // 定位成功
                        double latitude = aMapLocation.getLatitude();
                        double longitude = aMapLocation.getLongitude();
                        LatLng latLng = new LatLng(latitude, longitude);

                        // 更新地图的定位位置
                        mAMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                        // 添加定位点标记
                        addLocationMarker(latLng);

                        // 判断是否正在绘制轨迹
                        if (mIsDrawing) {
                            // 添加轨迹点
                            mLatLngList.add(latLng);
                            // 绘制轨迹线
                            drawPolyline();
                        }
                    } else {
                        // 定位失败
                        String errorMessage = "定位失败，错误码：" + aMapLocation.getErrorCode() + "，错误信息：" + aMapLocation.getErrorInfo();
                        System.out.println(errorMessage);
                    }
                }
            }
        });

        mLocationClient.startLocation();
    }

    // 添加定位点标记
    private void addLocationMarker(LatLng latLng) {
        if (mMarker != null) {
            mMarker.remove();
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bicycle)); // 使用箭头图标
        mMarker = mAMap.addMarker(markerOptions);
        mMarker.showInfoWindow();
    }

    // 绘制轨迹线的方法
    private void drawPolyline() {
        if (mLatLngList.size() >= 2) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(mLatLngList)
                    .width(10)
                    .color(Color.RED);
            mPolyline = mAMap.addPolyline(polylineOptions);
        }
    }

    // 检查定位权限
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
    }
}



