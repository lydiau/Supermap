package com.example.administrator.myapplication3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.InfoWindowAdapter;
import com.amap.api.maps2d.AMap.OnInfoWindowClickListener;
import com.amap.api.maps2d.AMap.OnMapClickListener;
import com.amap.api.maps2d.AMap.OnMarkerClickListener;
import com.amap.api.maps2d.AMapOptions;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CircleOptions;
import com.amap.api.maps2d.model.GroundOverlayOptions;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.PolygonOptions;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;
import com.amap.api.maps2d.overlay.WalkRouteOverlay;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;
import com.example.administrator.myapplication3.Geocoder.GeocoderActivity;
import com.example.administrator.myapplication3.R;
import com.example.administrator.myapplication3.util.AMapUtil;
import com.example.administrator.myapplication3.util.ToastUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends Activity implements OnMapClickListener,
        OnMarkerClickListener, OnInfoWindowClickListener, InfoWindowAdapter, OnRouteSearchListener,Inputtips.InputtipsListener,TextWatcher {
    private AMap aMap;
    private AutoCompleteTextView startp;// 输入起始点
    private MapView mapView;
    private Context mContext;
    private RouteSearch mRouteSearch;
    private Marker geoMarker;
    private WalkRouteResult mWalkRouteResult;
    private LatLonPoint mStartPoint = new LatLonPoint(39.942295, 116.335891);//起点，116.335891,39.942295
    private LatLonPoint mEndPoint = new LatLonPoint(39.995576, 116.481288);//终点，116.481288,39.995576
    private final int ROUTE_TYPE_WALK = 3;
    private ImageButton btnShow;

    private RelativeLayout mBottomLayout;
    private TextView mRotueTimeDes, mRouteDetailDes;
    private ProgressDialog progDialog = null;// 搜索时进度条
    private AutoCompleteTextView endp;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        mContext = this.getApplicationContext();
        mapView = (MapView) findViewById(R.id.route_map);
        mapView.onCreate(bundle);// 此方法必须重写
        init();
        setfromandtoMarker();
        searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
        startp = (AutoCompleteTextView) findViewById(R.id.startpoint);
        startp.addTextChangedListener(this);// 添加文本输入框监听事件
        endp = (AutoCompleteTextView) findViewById(R.id.endpoint);
        endp.addTextChangedListener(this);
        aMap.getUiSettings().setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
    }

    private void setfromandtoMarker() {
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mStartPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mEndPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        registerListener();
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
        //mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        mRotueTimeDes = (TextView) findViewById(R.id.firstline);
        mRouteDetailDes = (TextView) findViewById(R.id.secondline);

    }

    /**
     * 注册监听
     */
    private void registerListener() {
        aMap.setOnMapClickListener(MainActivity.this);
        aMap.setOnMarkerClickListener(MainActivity.this);
        aMap.setOnInfoWindowClickListener(MainActivity.this);
        aMap.setInfoWindowAdapter(MainActivity.this);

    }

    @Override
    public View getInfoContents(Marker arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public View getInfoWindow(Marker arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onMarkerClick(Marker arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onMapClick(LatLng arg0) {
        // TODO Auto-generated method stub

    }


    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            ToastUtil.show(mContext, "定位中，稍后再试...");
            return;
        }
        if (mEndPoint == null) {
            ToastUtil.show(mContext, "终点未设置");
        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);
        if (routeType == ROUTE_TYPE_WALK) {// 步行路径规划
            WalkRouteQuery query = new WalkRouteQuery(fromAndTo, mode);
            mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
        }
    }

    @Override
    public void onBusRouteSearched(BusRouteResult result, int errorCode) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {

    }

    public class RouteTool extends WalkRouteOverlay {
        public int color;//路线颜色
        public float lineWidth;//路线宽度
        private WalkPath aa;
        private Context c;
        WalkStep var3;
        LatLng var4;
        Marker marker;

        public RouteTool(Context context, AMap aMap, WalkPath walkPath, LatLonPoint latLonPoint, LatLonPoint latLonPoint1) {
            super(context, aMap, walkPath, latLonPoint, latLonPoint1);
            this.c = context;
            this.mAMap = aMap;
            this.aa = walkPath;
        }

        /*修改路线宽度*/
        @Override
        protected float getBuslineWidth() {
            return lineWidth;
        }

        /*修改路线颜色*/
        @Override
        protected int getWalkColor() {
            return color;
        }

        /* 修改终点marker样式*/
        @Override
        protected BitmapDescriptor getEndBitmapDescriptor() {
            BitmapDescriptor reBitmapDescriptor = new BitmapDescriptorFactory().fromResource(R.drawable.start_pink);
            return reBitmapDescriptor;
        }

        /*修改起点marker样式*/
        @Override
        protected BitmapDescriptor getStartBitmapDescriptor() {
            BitmapDescriptor reBitmapDescriptor = new BitmapDescriptorFactory().fromResource(R.drawable.end_green);
            return reBitmapDescriptor;
        }

        /*修改中间点marker样式*/
        @Override
        protected BitmapDescriptor getWalkBitmapDescriptor() {
            BitmapDescriptor reBitmapDescriptor = new BitmapDescriptorFactory().fromResource(R.drawable.dot);
            return reBitmapDescriptor;
        }

        public void setView(int color, float width) {
            this.color = color;
            lineWidth = width;
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        dissmissProgressDialog();
        WalkPath walkPath = null;
        aMap.clear();// 清理地图上的所有覆盖物
        int k = 0;
        int i = 0;
        int j = 0;
        int count = 0;
        int m = 0;
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mWalkRouteResult = result;
                    walkPath = mWalkRouteResult.getPaths()
                            .get(0);
                    List<LatLng> latLngs = new ArrayList<LatLng>();
                    List<LatLng> turnings1 = new ArrayList<LatLng>();
                    List<LatLonPoint> turnings2 = new ArrayList<LatLonPoint>();
                    List<Float> distance = new ArrayList<Float>();
                    latLngs.add(new LatLng(mStartPoint.getLatitude(), mStartPoint.getLongitude()));
                    for (i = 0; i < walkPath.getSteps().size(); i++) {
                        final WalkStep walkStep = walkPath.getSteps().get(i);
                        distance.add(walkStep.getDistance());
                        Collections.sort(distance);
                        for (j = 0; j < walkStep.getPolyline().size() - 1; j++) {
                            if (j == walkStep.getPolyline().size() - 2) {
                                double rou;
                                LatLonPoint kpoint_one = walkStep.getPolyline().get(j);
                                LatLonPoint kpoint_two = walkStep.getPolyline().get(j + 1);
                                LatLonPoint kpoint_three = walkPath.getSteps().get(i + 1).getPolyline().get(0);
                                rou = Math.acos((Math.pow(aMap.getProjection().toScreenLocation(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude())).x, 2)
                                        - (aMap.getProjection().toScreenLocation(new LatLng(kpoint_one.getLatitude(), kpoint_one.getLongitude())).x
                                        + aMap.getProjection().toScreenLocation(new LatLng(kpoint_three.getLatitude(), kpoint_three.getLongitude())).x)
                                        * aMap.getProjection().toScreenLocation(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude())).x
                                        + aMap.getProjection().toScreenLocation(new LatLng(kpoint_one.getLatitude(), kpoint_one.getLongitude())).x
                                        * aMap.getProjection().toScreenLocation(new LatLng(kpoint_three.getLatitude(), kpoint_three.getLongitude())).x
                                        + Math.pow(aMap.getProjection().toScreenLocation(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude())).y, 2)
                                        - (aMap.getProjection().toScreenLocation(new LatLng(kpoint_one.getLatitude(), kpoint_one.getLongitude())).y
                                        + aMap.getProjection().toScreenLocation(new LatLng(kpoint_three.getLatitude(), kpoint_three.getLongitude())).y)
                                        * aMap.getProjection().toScreenLocation(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude())).y
                                        + aMap.getProjection().toScreenLocation(new LatLng(kpoint_one.getLatitude(), kpoint_one.getLongitude())).y
                                        * aMap.getProjection().toScreenLocation(new LatLng(kpoint_three.getLatitude(), kpoint_three.getLongitude())).y)
                                        / (Math.sqrt(Math.pow(aMap.getProjection().toScreenLocation(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude())).x
                                        - aMap.getProjection().toScreenLocation(new LatLng(kpoint_one.getLatitude(), kpoint_one.getLongitude())).x, 2)
                                        + Math.pow(aMap.getProjection().toScreenLocation(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude())).y
                                        - aMap.getProjection().toScreenLocation(new LatLng(kpoint_one.getLatitude(), kpoint_one.getLongitude())).x, 2))
                                        * (Math.sqrt(Math.pow(aMap.getProjection().toScreenLocation(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude())).x
                                        - aMap.getProjection().toScreenLocation(new LatLng(kpoint_three.getLatitude(), kpoint_three.getLongitude())).x, 2)
                                        + Math.pow(aMap.getProjection().toScreenLocation(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude())).y
                                        - aMap.getProjection().toScreenLocation(new LatLng(kpoint_three.getLatitude(), kpoint_three.getLongitude())).x, 2)))));
                                if ((rou > Math.PI * 5 / 6) && (rou <= Math.PI)) {

                                } else {
                                    latLngs.add(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude()));
                                }
                            } else {
                                double rou;
                                LatLonPoint kpoint_one = walkStep.getPolyline().get(j);
                                LatLonPoint kpoint_two = walkStep.getPolyline().get(j + 1);
                                LatLonPoint kpoint_three = walkStep.getPolyline().get(j + 2);
                                rou = Math.acos((Math.pow(aMap.getProjection().toScreenLocation(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude())).x, 2)
                                        - (aMap.getProjection().toScreenLocation(new LatLng(kpoint_one.getLatitude(), kpoint_one.getLongitude())).x
                                        + aMap.getProjection().toScreenLocation(new LatLng(kpoint_three.getLatitude(), kpoint_three.getLongitude())).x)
                                        * aMap.getProjection().toScreenLocation(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude())).x
                                        + aMap.getProjection().toScreenLocation(new LatLng(kpoint_one.getLatitude(), kpoint_one.getLongitude())).x
                                        * aMap.getProjection().toScreenLocation(new LatLng(kpoint_three.getLatitude(), kpoint_three.getLongitude())).x
                                        + Math.pow(aMap.getProjection().toScreenLocation(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude())).y, 2)
                                        - (aMap.getProjection().toScreenLocation(new LatLng(kpoint_one.getLatitude(), kpoint_one.getLongitude())).y
                                        + aMap.getProjection().toScreenLocation(new LatLng(kpoint_three.getLatitude(), kpoint_three.getLongitude())).y)
                                        * aMap.getProjection().toScreenLocation(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude())).y
                                        + aMap.getProjection().toScreenLocation(new LatLng(kpoint_one.getLatitude(), kpoint_one.getLongitude())).y
                                        * aMap.getProjection().toScreenLocation(new LatLng(kpoint_three.getLatitude(), kpoint_three.getLongitude())).y)
                                        / (Math.sqrt(Math.pow(aMap.getProjection().toScreenLocation(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude())).x
                                        - aMap.getProjection().toScreenLocation(new LatLng(kpoint_one.getLatitude(), kpoint_one.getLongitude())).x, 2)
                                        + Math.pow(aMap.getProjection().toScreenLocation(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude())).y
                                        - aMap.getProjection().toScreenLocation(new LatLng(kpoint_one.getLatitude(), kpoint_one.getLongitude())).x, 2))
                                        * (Math.sqrt(Math.pow(aMap.getProjection().toScreenLocation(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude())).x
                                        - aMap.getProjection().toScreenLocation(new LatLng(kpoint_three.getLatitude(), kpoint_three.getLongitude())).x, 2)
                                        + Math.pow(aMap.getProjection().toScreenLocation(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude())).y
                                        - aMap.getProjection().toScreenLocation(new LatLng(kpoint_three.getLatitude(), kpoint_three.getLongitude())).x, 2)))));
                                if (rou > Math.PI * 5 / 6) {
                                    break;
                                } else {
                                    if ((rou <= Math.PI * 120 / 180) &&(j <= walkStep.getPolyline().size() - 30) ) {
                                        aMap.addMarker((new MarkerOptions())
                                                .position(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude()))
                                                .visible(true)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.turning_point)));
                                    }
                                    latLngs.add(new LatLng(kpoint_two.getLatitude(), kpoint_two.getLongitude()));
                                    break;
                                }
                            }
                        }
                    }
                    latLngs.add(new LatLng(mEndPoint.getLatitude(), mEndPoint.getLongitude()));
                    aMap.addPolyline(new PolylineOptions().addAll(latLngs).width(10).color(0xff000000).zIndex(2));
                    aMap.addCircle(new CircleOptions().center(new LatLng(mStartPoint.getLatitude(), mStartPoint.getLongitude())).radius(100000).zIndex(1).fillColor(0xffffffff));
                    RouteTool walkRouteOverlay = new RouteTool(
                        this, aMap, walkPath,
                        mWalkRouteResult.getStartPos(),
                        mWalkRouteResult.getTargetPos());
                //walkRouteOverlay.setView(0xff000000, 10);
                walkRouteOverlay.removeFromMap();
                walkRouteOverlay.addToMap();
                walkRouteOverlay.zoomToSpan();
                //walkRouteOverlay.getTurningPoints();
                mBottomLayout.setVisibility(View.VISIBLE);
                int dis = (int) walkPath.getDistance();
                int dur = (int) walkPath.getDuration();
                String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";
                mRotueTimeDes.setText(des);
                mRouteDetailDes.setVisibility(View.GONE);
                final WalkPath finalWalkPath = walkPath;
                mBottomLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext,
                                WalkRouteDetailActivity.class);
                        intent.putExtra("walk_path", finalWalkPath);
                        intent.putExtra("walk_result",
                                mWalkRouteResult);
                        startActivity(intent);
                    }
                });
            } else if (result != null && result.getPaths() == null) {
                ToastUtil.show(mContext, R.string.no_result);
            }
            } else {
            ToastUtil.show(mContext, R.string.no_result);
        }
        } else {
        ToastUtil.showerror(this.getApplicationContext(), errorCode);
    }
}


    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onRideRouteSearched(RideRouteResult arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String newText = s.toString().trim();
        if (!AMapUtil.IsEmptyOrNullString(newText)) {
            InputtipsQuery inputquery = new InputtipsQuery(newText, "武汉");
            Inputtips inputTips = new Inputtips(MainActivity.this, inputquery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {// 正确返回
            List<String> listString = new ArrayList<String>();
            for (int i = 0; i < tipList.size(); i++) {
                listString.add(tipList.get(i).getName());
            }
            ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
                    getApplicationContext(),
                    R.layout.route_inputs, listString);
            startp.setAdapter(aAdapter);
            endp.setAdapter(aAdapter);
            aAdapter.notifyDataSetChanged();
        } else {
            ToastUtil.showerror(MainActivity.this, rCode);
        }
    }
}



