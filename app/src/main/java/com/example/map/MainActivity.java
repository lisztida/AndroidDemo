package com.example.map;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.DPoint;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.PolygonOptions;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.ServiceSettings;
import com.example.map.entity.BikeInfo;
import com.example.map.entity.Point;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.util.Log;

import org.json.JSONArray;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;




public class MainActivity extends AppCompatActivity implements AMapLocationListener, GeoFenceListener {
    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_LOCATION = 1001;
    private static final String GEOFENCE_BROADCAST_ACTION = "com.example.geofence.broadcast";

    private MapView mapView;
    private AMap aMap;
    private AMapLocationClient locationClient;
    private Marker locationMarker;
    private Polyline polyline;
    private LatLng lastLocation;
    private Button drawButton;
    private Button centerButton;

    private Button repairButton;

    private boolean isDrawing = false;
    private boolean isCentered = false;
    private List<LatLng> trackPoints;

    private List<DPoint> fencePoints;

    private boolean isAlerted = false;
    private GeoFenceClient mGeoFenceClient;


    private ArrayList<Point> points = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();
    private boolean isPointProcessed = false;


    //获取登录信息
    private TextView loginuser;
    private String loginUsername;


    //自行车信息保存列表
    private List<BikeInfo> bikeInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("shit",Thread.currentThread().getName());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 接收参数
        Intent intent = this.getIntent();
        loginUsername = intent.getStringExtra("username");
        loginuser = findViewById(R.id.hello);
        loginuser.setText("欢迎！"+loginUsername);


        // 初始化地图
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();

        // 初始化按钮
        drawButton = findViewById(R.id.btn_draw);
        centerButton = findViewById(R.id.btn_center);
        repairButton = findViewById(R.id.btn_repair);

        // 请求定位权限
        try {
            ServiceSettings.updatePrivacyShow(this,true,true);
            ServiceSettings.updatePrivacyAgree(this,true);
            requestLocationPermission();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //获取各点信息
        this.initData((info) -> {
            Log.i("nb", Thread.currentThread().getName());
            int westNum = 0 ,southNum = 0,stadiumNum = 0,east1Num = 0,east2Num = 0;
            bikeInfos = info;
            System.out.println(bikeInfos);
            for(BikeInfo bikeInfo:bikeInfos){
                if (bikeInfo.getCurrentLocation().equals("西门") && bikeInfo.isAvailable()){
                    westNum ++;
                }else if(bikeInfo.getCurrentLocation().equals("体育场") && bikeInfo.isAvailable()){
                    stadiumNum ++;
                }else if(bikeInfo.getCurrentLocation().equals("南门") && bikeInfo.isAvailable()){
                    southNum ++;
                }else if(bikeInfo.getCurrentLocation().equals("东一门") && bikeInfo.isAvailable()){
                    east1Num ++;
                }else if(bikeInfo.getCurrentLocation().equals("东二门") && bikeInfo.isAvailable()){
                    east2Num ++;
                }
            }

            // 添加点到列表
            LatLng latLng1 = new LatLng(39.95133, 116.336834);
            Marker marker1 = aMap.addMarker(new MarkerOptions()
                    .position(latLng1)
                    .title("共享单车西门")
                    .snippet("数量：" + westNum)
                    .draggable(true));
            Point point1 = new Point(latLng1, "西门", westNum);
            point1.setMarker(marker1);
            points.add(point1);
            markers.add(marker1);

            LatLng latLng2 = new LatLng(39.952132,116.340084);
            Marker marker2 = aMap.addMarker(new MarkerOptions()
                    .position(latLng2)
                    .title("共享单车体育场")
                    .snippet("数量：" + stadiumNum)
                    .draggable(true));
            Point point2 = new Point(latLng2, "体育场", stadiumNum);
            point1.setMarker(marker2);
            points.add(point2);
            markers.add(marker2);

            LatLng latLng3 = new LatLng(39.94922,116.341495);
            Marker marker3 = aMap.addMarker(new MarkerOptions()
                    .position(latLng3)
                    .title("共享单车南门")
                    .snippet("数量：" + southNum)
                    .draggable(true));
            Point point3 = new Point(latLng3, "南门", southNum);
            point1.setMarker(marker3);
            points.add(point3);
            markers.add(marker3);

            LatLng latLng4 = new LatLng(39.952587,116.343882);
            Marker marker4 = aMap.addMarker(new MarkerOptions()
                    .position(latLng4)
                    .title("共享单车东一门")
                    .snippet("数量：" + east1Num)
                    .draggable(true));
            Point point4 = new Point(latLng4, "东一门", east1Num);
            point1.setMarker(marker4);
            points.add(point4);
            markers.add(marker4);

            LatLng latLng5 = new LatLng(39.95253,116.347691);
            Marker marker5 = aMap.addMarker(new MarkerOptions()
                    .position(latLng5)
                    .title("共享单车东二门")
                    .snippet("数量：" + east2Num)
                    .draggable(true));
            Point point5 = new Point(latLng1, "东二门", east2Num);
            point1.setMarker(marker5);
            points.add(point5);
            markers.add(marker5);

        });




        aMap.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                // 在这里添加处理点击 InfoWindow 的逻辑
                String title = marker.getTitle();
                String snippet = marker.getSnippet();
                // 在这里处理点击 InfoWindow 后的操作，比如显示更详细的信息等
            }
        });




        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng currentPosition = new LatLng(lastLocation.latitude, lastLocation.longitude);
                for (Point point : points) {
                    //判断是否在范围内
                    if (isNearbyPoint(currentPosition, point.getLatLng(), 50)) {

                        if (!isDrawing) {
                            //这个状态是还没有解锁，把按钮上的内容变成停车
                            drawButton.setText("停车");
                            //显示报修按钮
                            repairButton.setVisibility(View.VISIBLE);
                            //获取借车人信息
                            String userId = loginUsername;
                            //借走车辆
                            //获取地点
                            String currentLocation = point.getTitle();
                            int bicycleId = 0;
                            for(BikeInfo bikeInfo:bikeInfos){
                                //System.out.println("!!!!!!" + bikeInfo.getCurrentLocation() + "!!!!!!");
                                if(bikeInfo.getCurrentLocation().equals(currentLocation) && bikeInfo.isAvailable()){
                                    bicycleId = bikeInfo.getId();
                                    break;
                                    //获取第一个符合条件的车
                                }
                            }
                            int finalBicycleId = bicycleId;
                            //获取时间戳
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                            String date = df.format(new Date());
                            String rideBikeUrl = "http://172.25.104.246:8030/rideBike?id=" + finalBicycleId + "&userId=" + userId + "&rentalTime=" + date;
                            Thread t = new Thread(() -> {
                                try {
                                    OkHttpClient client = new OkHttpClient();
                                    RequestBody body = RequestBody.create("{\"username\":\"aaa\", \"password\":\"as\"}", MediaType.get("application/json; charset=utf-8"));
                                    Request request = new Request.Builder()
                                            .url(rideBikeUrl)
                                            .post(body)
                                            .build();
                                    try(Response response = client.newCall(request).execute()){
                                        //只是调用接口
                                    }catch (Exception e){
                                        System.out.println(e);
                                    }
                                }catch (Exception e){
                                    System.out.println(e);
                                }

                            });
                            t.start();
                            Toast.makeText(MainActivity.this, "借车成功!", Toast.LENGTH_SHORT).show();
                            point.setQuantity(point.getQuantity() - 1);
                            trackPoints = new ArrayList<>();
                            isDrawing=true;
                        } else {
                            //这个状态是正在骑行，按下后将取消骑行，所以将text设置为解锁
                            drawButton.setText("解锁");
                            repairButton.setVisibility(View.GONE);
                            //获取地点
                            String currentLocation = point.getTitle();
                            //获取骑车用户信息
                            String userId = loginUsername;
                            //获取时间戳
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                            String date = df.format(new Date());
                            String returnBikeUrl = "http://172.25.104.246:8030/returnBike?userId=" + userId + "&returnTime=" + date + "&currentLocation=" + currentLocation;
                            Thread t = new Thread(() -> {
                                try{
                                    OkHttpClient client = new OkHttpClient();
                                    RequestBody body = RequestBody.create("{\"username\":\"aaa\", \"password\":\"as\"}", MediaType.get("application/json; charset=utf-8"));
                                    Request request = new Request.Builder()
                                            .url(returnBikeUrl)
                                            .post(body)
                                            .build();
                                    try(Response response = client.newCall(request).execute()){
                                        //只是调用接口
                                    }catch (Exception e){
                                        System.out.println(e);
                                    }
                                }catch (Exception e){
                                    System.out.println(e);
                                }
                            });
                            t.start();
                            Toast.makeText(MainActivity.this, "还车成功!", Toast.LENGTH_SHORT).show();
                            point.setQuantity(point.getQuantity() + 1);
                            // 执行停止轨迹的操作
                            if (polyline != null) {
                                polyline.remove();
                                polyline=null;
                            }
                            isDrawing=false;
                        }

                        // 更新标记和信息窗口
                        Marker marker = getMarkerByPoint(point);
                        if (marker != null) {
                            marker.setSnippet("数量：" + point.getQuantity());
                            marker.showInfoWindow();
                        }
                        isPointProcessed = true;
                        break; // 结束循环，只处理第一个满足条件的点
                    }
                }

                // 如果不在任何点附近
                if (!isPointProcessed) {
                    Toast.makeText(MainActivity.this, "您不在停车点附近，无法操作", Toast.LENGTH_SHORT).show();
                }
            }
        });


        centerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                centerMap();
            }
        });

        repairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng currentPosition = new LatLng(lastLocation.latitude, lastLocation.longitude);
                for (Point point : points) {
                    //判断是否在范围内
                    if (isNearbyPoint(currentPosition, point.getLatLng(), 50)) {
                        //这个状态是正在骑行，按下后将取消骑行，所以将text设置为解锁
                        drawButton.setText("解锁");
                        repairButton.setVisibility(View.GONE);
                        //获取地点
                        String currentLocation = point.getTitle();
                        //获取骑车用户信息
                        String userId = loginUsername;
                        //获取时间戳
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                        String date = df.format(new Date());
                        String repairBikeUrl = "http://172.25.104.246:8030/repairBike?userId=" + userId + "&returnTime=" + date + "&currentLocation=" + currentLocation;
                        Thread t = new Thread(() -> {
                            try{
                                OkHttpClient client = new OkHttpClient();
                                RequestBody body = RequestBody.create("{\"username\":\"aaa\", \"password\":\"as\"}", MediaType.get("application/json; charset=utf-8"));
                                Request request = new Request.Builder()
                                        .url(repairBikeUrl)
                                        .post(body)
                                        .build();
                                try(Response response = client.newCall(request).execute()){
                                    //只是调用接口
                                }catch (Exception e){
                                    System.out.println(e);
                                }
                            }catch (Exception e){
                                System.out.println(e);
                            }
                        });
                        t.start();
                        Toast.makeText(MainActivity.this, "报修成功!", Toast.LENGTH_SHORT).show();
                        // 执行停止轨迹的操作
                        if (polyline != null) {
                            polyline.remove();
                            polyline=null;
                        }
                        isDrawing=false;

                        // 更新标记和信息窗口
                        Marker marker = getMarkerByPoint(point);
                        if (marker != null) {
                            marker.setSnippet("数量：" + point.getQuantity());
                            marker.showInfoWindow();
                        }
                        isPointProcessed = true;
                        break; // 结束循环，只处理第一个满足条件的点
                    }
                }
                // 如果不在任何点附近
                if (!isPointProcessed) {
                    Toast.makeText(MainActivity.this, "您不在停车点附近，无法操作", Toast.LENGTH_SHORT).show();
                }

            }
        });



        // 初始化地理围栏客户端
        mGeoFenceClient = new GeoFenceClient(getApplicationContext());
        mGeoFenceClient.setGeoFenceListener(this);

        // 添加围栏触发的广播接收器
        IntentFilter filter = new IntentFilter(GEOFENCE_BROADCAST_ACTION);
        registerReceiver(mGeoFenceReceiver, filter);

        // 设置PendingIntent
        mGeoFenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);

        // 初始化围栏坐标点
        fencePoints = new ArrayList<>();
        fencePoints.add(new DPoint(39.953739, 116.339645));
        fencePoints.add(new DPoint(39.953016, 116.336094));
        fencePoints.add(new DPoint(39.950388, 116.337124));
        fencePoints.add(new DPoint(39.948722, 116.339366));
        fencePoints.add(new DPoint(39.948979, 116.3415));
        fencePoints.add(new DPoint(39.948075, 116.341822));
        fencePoints.add(new DPoint(39.948782, 116.34356));
        fencePoints.add(new DPoint(39.949226, 116.343367));
        fencePoints.add(new DPoint(39.950624, 116.348345));
        fencePoints.add(new DPoint(39.956085, 116.3468));
        fencePoints.add(new DPoint(39.954525, 116.339602));

        // 创建围栏
        createGeoFence();
    }

    //创建获取数据接口
    interface i {
        public void f(List<BikeInfo> info);
    }

    //获取共享单车数据
    private void initData(i ii){

        Thread t = new Thread(() -> {
            Log.i("shitNetwork",Thread.currentThread().getName());
            String getBikeUrl = "http://172.25.104.246:8030/getBikeInfo";
            try{
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(getBikeUrl)
                        .build();
                try(Response response = client.newCall(request).execute()){
                    JSONArray bikeList = new JSONArray(response.body().string());
                    String bikeJson = bikeList.toString();
                    //System.out.println("!!!!!" + bikeList + "!!!!!");
                    Gson gson = new Gson();
                    List<BikeInfo> bikeInfos1 = gson.fromJson(bikeJson,new TypeToken<List<BikeInfo>>(){}.getType());
                    ii.f(bikeInfos1);
                }catch (Exception e){
                    System.out.println(e);
                }
            } catch (Exception e){
                System.out.println(e);
            }

        });
        t.start();
    }

    private Marker getMarkerByPoint(Point point) {
        int index = points.indexOf(point);
        if (index != -1 && index < markers.size()) {
            return markers.get(index);
        }
        return null;
    }

    // 判断当前位置是否在指定位置附近
    private boolean isNearbyPoint(LatLng currentLocation, LatLng targetLocation, double radius) {
        float[] results = new float[1];
        Location.distanceBetween(
                currentLocation.latitude,
                currentLocation.longitude,
                targetLocation.latitude,
                targetLocation.longitude,
                results);

        return results[0] <= radius;
    }

    private void requestLocationPermission() throws Exception {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 权限已经授予，开始定位
            startLocation();
        } else {
            // 请求定位权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 定位权限已授予，开始定位
                try {
                    startLocation();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                // 定位权限被拒绝，显示提示信息
                Toast.makeText(this, "定位权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocation() throws Exception {
        // 创建定位客户端
        locationClient = new AMapLocationClient(this);
        locationClient.setLocationListener(this);

        // 配置定位参数
        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationOption.setInterval(2000);
        locationClient.setLocationOption(locationOption);

        // 启动定位
        locationClient.startLocation();
    }

    private void centerMap() {
        if (lastLocation != null) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 20f));
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
            // 定位成功
            double latitude = aMapLocation.getLatitude();
            double longitude = aMapLocation.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            lastLocation = latLng;


            BitmapDescriptor bicycleIcon = BitmapDescriptorFactory.fromResource(R.drawable.bicycle);
            BitmapDescriptor walkIcon = BitmapDescriptorFactory.fromResource(R.drawable.walk);
            // 更新定位点
            // 更新定位点
            if (locationMarker == null) {
                // 创建定位点
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .icon(isDrawing ? bicycleIcon : walkIcon);
                locationMarker = aMap.addMarker(markerOptions);
            } else {
                // 移动定位点
                locationMarker.setPosition(latLng);
                // 切换图标
                locationMarker.setIcon(isDrawing ? bicycleIcon : walkIcon);
            }


            if (isDrawing) {
                // 绘制轨迹
                centerMap();
                if (polyline == null) {
                    Log.d(TAG,"开始绘制轨迹");
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .width(10f)
                            .color(Color.RED);
                    polyline = aMap.addPolyline(polylineOptions);
                }

                trackPoints.add(latLng);
                polyline.setPoints(trackPoints);

                if (isCentered) {
                    // 将地图中心移动到定位点
                    centerMap();
                    isCentered = false;
                }
            }

            // 输出定位信息到 Logcat
            String locationInfo = "定位成功\n" +
                    "经度：" + longitude + "\n" +
                    "纬度：" + latitude + "\n" +
                    "地址：" + aMapLocation.getAddress();
            Log.d(TAG, locationInfo);

            // 检查是否在围栏内
            boolean isInFence = contains(latLng, fencePoints);
            if (!isInFence&&isDrawing) {
                // 离开围栏，触发警报
                if (!isAlerted) {
                    Log.d(TAG,"离开围栏区域");
                    Toast.makeText(this, "离开围栏区域", Toast.LENGTH_SHORT).show();
                }
            } else if(isInFence){
                // 进入围栏，重置警报状态
                isAlerted = false;
            }
        } else {
            // 定位失败
            String errorInfo = "定位失败\n" +
                    "错误码：" + aMapLocation.getErrorCode() + "\n" +
                    "错误信息：" + aMapLocation.getErrorInfo();
            Log.e(TAG, errorInfo);
            Toast.makeText(this, "定位失败", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean contains(LatLng point, List<DPoint> polygon) {
        if (polygon == null || polygon.isEmpty()) {
            return false;
        }

        int intersectCount = 0;
        for (int i = 0; i < polygon.size() - 1; i++) {
            if (rayCrossesSegment(point, polygon.get(i), polygon.get(i + 1))) {
                intersectCount++;
            }
        }

        return (intersectCount % 2) == 1;
    }

    private boolean rayCrossesSegment(LatLng point, DPoint p1, DPoint p2) {
        double px = point.longitude;
        double py = point.latitude;
        double p1x = p1.getLongitude();
        double p1y = p1.getLatitude();
        double p2x = p2.getLongitude();
        double p2y = p2.getLatitude();

        if (p1y == p2y) {
            return false;
        }

        if (py < Math.min(p1y, p2y) || py > Math.max(p1y, p2y)) {
            return false;
        }

        double x = p1x + (py - p1y) * (p2x - p1x) / (p2y - p1y);

        return x > px;
    }

    private void createGeoFence() {
        if (fencePoints != null && fencePoints.size() >= 3) {
            PolygonOptions polygonOptions = new PolygonOptions();
            for (DPoint point : fencePoints) {
                polygonOptions.add(new LatLng(point.getLatitude(), point.getLongitude()));
            }
            aMap.addPolygon(polygonOptions.strokeColor(Color.RED).fillColor(Color.argb(100, 255, 0, 0)));

            mGeoFenceClient.addGeoFence(fencePoints, "自有业务ID");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
        mapView.onDestroy();
        unregisterReceiver(mGeoFenceReceiver);
    }

    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (GEOFENCE_BROADCAST_ACTION.equals(action)) {
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        // 获取围栏ID
                        String fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID);
                        // 获取围栏行为
                        int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
                        if (status == GeoFence.STATUS_OUT) {
                            // 离开围栏，触发警报
                            Toast.makeText(context, "离开围栏区域", Toast.LENGTH_SHORT).show();
                            isAlerted = true;
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onGeoFenceCreateFinished(List<GeoFence> geoFenceList, int errorCode, String s) {
        if (errorCode == GeoFence.ADDGEOFENCE_SUCCESS) {
            // 围栏创建成功
            Log.i(TAG, "围栏创建成功");
        } else {
            // 围栏创建失败
            Log.e(TAG, "围栏创建失败，错误码：" + errorCode);
        }
    }
}
