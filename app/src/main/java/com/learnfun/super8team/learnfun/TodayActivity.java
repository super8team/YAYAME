package com.learnfun.super8team.learnfun;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;

public class TodayActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener,AdapterView.OnItemSelectedListener,GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private static int MY_LOCATION_REQUEST_CODE = 2000;
    public static final int REQUEST_CODE_WRITE = 1001;
    private LocationManager locationManager;
    View marker_root_view;
    MarkerOptions myMarker=null;
    private Socket socket=null;
    boolean isPageOpen = false;
    String placeNum="";
    Animation translateLeftAnim;
    Animation translateRightAnim;

    LinearLayout slidingPage01;
    ScrollView scrollPage;

    LatLng SEOUL = new LatLng(35.896687, 128.620512);

    JSONObject sendData,planGPS;
    UserPreferences userPreferences;
    NetworkAsync requestNetwork;
    //ArrayList<LatLng> classOne = new ArrayList();
    ArrayList<Marker> classOneMarker = new ArrayList();
    //ArrayList<LatLng> classTwo = new ArrayList();
    ArrayList<Marker> classTwoMarker = new ArrayList();
    //ArrayList<LatLng> classThree = new ArrayList();
    ArrayList<Marker> classThreeMarker = new ArrayList();

    private Button slidingPageClose,writeHistory;
    ArrayAdapter<String> adapter;
    Spinner spinner;
    Switch histroySwitch;
    boolean mSwc = true;    // 스위치 상태를 기억할 변수

    TextView contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("tag", "온크리에이트 실행");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        slidingPageClose = (Button)findViewById(R.id.slidingPageClose);
        writeHistory = (Button)findViewById(R.id.writeHistory);

        slidingPage01 = (LinearLayout) findViewById(R.id.slidingPage01);
        scrollPage = (ScrollView) findViewById(R.id.scrollPage);

        ImageView image =(ImageView)this.findViewById(R.id.imageView2);
        image.setImageResource(R.drawable.onebin);

        contentView = (TextView)this.findViewById(R.id.contentView);


        translateLeftAnim = AnimationUtils.loadAnimation(this,R.anim.translate_left);
        translateRightAnim = AnimationUtils.loadAnimation(this,R.anim.translate_right);

        SlidingPageAnimationListener animListener = new SlidingPageAnimationListener();
        translateLeftAnim.setAnimationListener(animListener);
        translateRightAnim.setAnimationListener(animListener);

        spinner = (Spinner)findViewById(R.id.spinner);

        histroySwitch = (Switch)findViewById(R.id.historySwitch);
        histroySwitch.setOnCheckedChangeListener(SWITCH);

        userPreferences = UserPreferences.getUserPreferences(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.class_Name,android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){


            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(parent.getContext(),"선택한것은"+parent.getItemAtPosition(position),Toast.LENGTH_SHORT).show();
                mMap.clear();

                if(parent.getItemAtPosition(position).equals("1반")){
                    classOneMarker.clear();
                    socket.emit("class1", "I want to class1's GPS");
                    //addStudentMarker(classOne, "#21ff3f", "1반");
                    if(myMarker!=null) mMap.addMarker(myMarker);

                }else if(parent.getItemAtPosition(position).equals("2반")){
                    classTwoMarker.clear();
                    socket.emit("class2", "I want to class2's GPS");
                    //addStudentMarker(classTwo, "#24d8b4","2반");
                    if(myMarker!=null) mMap.addMarker(myMarker);

                }else{
                    classThreeMarker.clear();
                    socket.emit("class3", "I want to class3's GPS");
                    //addStudentMarker(classThree,"#ebf224","3반");
                    if(myMarker!=null) mMap.addMarker(myMarker);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        chkGpsService(); //GPS sensor on / off check


            try{
                socket = IO.socket("http://163.44.166.91:8000");


                socket.on(Socket.EVENT_CONNECT, listenStartPerson);
                //.on("getclass1", listen_start_person)
                socket.on("getclass", listenGetMessagePerson);

                socket.connect();

            }

            catch(Exception e){

            }


        slidingPageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingPage01.startAnimation(translateRightAnim);
                scrollPage.startAnimation(translateRightAnim);
            }
        });
        writeHistory.setOnClickListener(new View.OnClickListener() { //글쓰기 버튼 클릭시
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteHistoryActivity.class);
                intent.putExtra("placeNum", placeNum);
                startActivityForResult(intent,REQUEST_CODE_WRITE);
            }
        });

    }//oncreate function end

    //GPS 설정 체크
    private boolean chkGpsService() {

        String gps = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        Log.d(gps, "aaaa");

        if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {

            // GPS OFF 일때 Dialog 표시
            AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
            gsDialog.setTitle("위치 서비스 설정");
            gsDialog.setMessage("무선 네트워크 사용, GPS 위성 사용을 모두 체크하셔야 정확한 위치 서비스가 가능합니다.\n위치 서비스 기능을 설정하시겠습니까?");
            gsDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // GPS설정 화면으로 이동
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).create().show();
            return false;

        } else {
            return true;
        }
    }

    public Switch.OnCheckedChangeListener SWITCH = new Switch.OnCheckedChangeListener()

    {

        public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {

            mSwc = isChecked;
            if(isChecked == true){ //처음 화면은 학생들의 위치를 나타내주는 화면
                mMap.clear();
                socket.emit("history", "don't send any gps");
                spinner.setVisibility(View.INVISIBLE); // 화면에 안보임
                classOneMarker.clear();
                classTwoMarker.clear();
                classThreeMarker.clear();
                if(myMarker!=null) mMap.addMarker(myMarker);
                getPlanGPS();

                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            if(!marker.getTitle().equals("현재위치")) {
                                placeNum = marker.getTitle();
                                if (isPageOpen) {
                                    //slidingPage01.startAnimation(translateRightAnim);
                                } else {
                                    slidingPage01.setVisibility(View.VISIBLE);
                                    slidingPage01.startAnimation(translateLeftAnim);
                                    scrollPage.setVisibility(View.VISIBLE);
                                    scrollPage.startAnimation(translateLeftAnim);


                                    //디비에서 히스토리 정보를 가져와서 슬라이드창에 글을 뿌려준다
                                    sendData = new JSONObject();

                                    try {

                                        //recentDate.put("date",getDate());
                                        sendData.put("userId",userPreferences.getUserId());
                                        sendData.put("placeNum",placeNum);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    requestNetwork = new NetworkAsync(TodayActivity.this, "getHistoryContent",  NetworkAsync.POST, sendData);

                                    try {
                                        // 네트워크 통신 후 json 획득
                                        String returnString = requestNetwork.execute().get();
                                        Log.e("planResult", "result is "+returnString);
                                        JSONObject place = new JSONObject(returnString);

                                        //JSONArray planGPSArray = new JSONArray(planGPS.getString("gps"));
                                        JSONObject contentList = new JSONObject(place.getString("place"));


                                        for(int i = 0 ; i < contentList.length();i++){

                                            //제이슨배열을 만든것을 하나씩 제이슨 객체로 만듬
                                            String contentNum = "content" + (i+1);
                                            JSONObject dataJsonObject = contentList.getJSONObject(contentNum);
                                            //JSONObject placeData = new JSONObject(dataJsonObject);

                                            contentView.setText("content : " + dataJsonObject.getString("content") + "\nweather : " +dataJsonObject.getString("weather"));



                                        }



                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }


                                }
                            }
                            return false;
                        }
                    });


            } else{// 히스토리를 나타내는 화면

                mMap.clear();
                spinner.setVisibility(View.VISIBLE); // 화면에보임
                socket.emit("class1", "I want to class1's GPS");
                //addStudentMarker(classOne, "#21ff3f", "1반");
                if(myMarker!=null) mMap.addMarker(myMarker);
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        return false;
                    }
                });
            }


        }

    };


    @Override
    protected void onDestroy() {
        socket.on(Socket.EVENT_DISCONNECT, listenDisconnectPerson);
        super.onDestroy();
    }

    private Emitter.Listener listenStartPerson = new Emitter.Listener() {

        public void call(Object... args) {

            socket.emit("Connection", "connected on server");
            //서버에서 보낸 JSON객체를 사용할 수 있습니다.

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //이곳에 ui 관련 작업을 할 수 있습니다.


                }
            });
        }
    };
    private Emitter.Listener listenGetMessagePerson = new Emitter.Listener() {

        public void call(Object... args) {
            Log.d("loglog", "class1 받았다!!!!!!!!!!!");
            final JSONObject obj = (JSONObject)args[0];


            //서버에서 보낸 JSON객체를 사용할 수 있습니다.

            runOnUiThread(new Runnable() { //발생가능 문제점 : 1반이 선택된 상태에서 1반에 대한 객체가 들어올경우 마커가 중첩되어 찍힐수있음
                @Override
                public void run() {
                    try {
                        //위에서 오브젝트를 받은것을 다시 제이슨배열로 해체
                        JSONArray jsonArray = new JSONArray(obj.getString("class"));
                        //제이슨배열을 만든것을 하나씩 제이슨 객체로 만듬
                        for(int i =0; i < jsonArray.length(); i++){

                            JSONObject dataJsonObject = jsonArray.getJSONObject(i);// 0에 cho 객체가 있음
                            //제이슨 객체안의 데이터를 빼온다
                            Log.d("studentname", dataJsonObject.getString("class"));

                            if(dataJsonObject.getString("class").equals("1")){
                                addStudentMarker(jsonArray, "#21ff3f", "1반");
                            }else if(dataJsonObject.getString("class").equals("2")){
                                addStudentMarker(jsonArray,"#ebf224","2반");
                            }else{
                                addStudentMarker(jsonArray,"#24d8b4","3반");

                            }
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //이곳에 ui 관련 작업을 할 수 있습니다.


                }
            });
        }
    };
    private Emitter.Listener listenDisconnectPerson = new Emitter.Listener() {

        public void call(Object... args) {
            Log.d("loglog", "연결 끊는다");
            socket.disconnect();


            //서버에서 보낸 JSON객체를 사용할 수 있습니다.

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //이곳에 ui 관련 작업을 할 수 있습니다.


                }
            });
        }
    };

    public static String getDate(){
        SimpleDateFormat dateFormat = new  SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        Date date = new Date();
        String strDate = dateFormat.format(date);
        return strDate;
    }

    public void getPlanGPS(){ // 히스토리부분

        //String userid = userPreferences.getUserId();

        sendData = new JSONObject();

        try {

            //recentDate.put("date",getDate());
            sendData.put("userID","Illum");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestNetwork = new NetworkAsync(this, "getPlan",  NetworkAsync.POST, sendData);

        try {
            // 네트워크 통신 후 json 획득
            String returnString = requestNetwork.execute().get();
            Log.e("planResult", "result is "+returnString);
            planGPS = new JSONObject(returnString);

            //JSONArray planGPSArray = new JSONArray(planGPS.getString("gps"));
            JSONObject placeGPS = new JSONObject(planGPS.getString("gps"));
            PolygonOptions rectOptions = new PolygonOptions();
            Log.e("planResult", "result is "+placeGPS);
            for(int i = 0 ; i < placeGPS.length();i++){

                //제이슨배열을 만든것을 하나씩 제이슨 객체로 만듬
                String placeNum = "place" + (i+1);
                JSONObject dataJsonObject = placeGPS.getJSONObject(placeNum);
                //JSONObject placeData = new JSONObject(dataJsonObject);
                Log.d("TAG", dataJsonObject.getString("name"));
                LatLng planGPS = new LatLng(dataJsonObject.getDouble("lat"), dataJsonObject.getDouble("lng"));

                MarkerOptions markerOptions = new MarkerOptions();

                markerOptions.position(planGPS);

                //markerOptions.icon(getMarkerIcon(markerColor)); // change the color of marker

                markerOptions.title(dataJsonObject.getString("no"));

                Marker planMarker = mMap.addMarker(markerOptions);
                //planMarker.setOnClickListener(onButton1Clicked); //마커에 클릭이벤트를 달아 오른쪽에 창이 나오도록 해야함
                rectOptions.add(planGPS);

            }

            // Get back the mutable Polygon
            Polygon polygon = mMap.addPolygon(rectOptions);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                Log.d("TAG", "onMapLoaded 체크퍼미션 실행했다");

                checkLocationPermission();
                //  addImageMarker(); // 새로운 이미지 마커를 박음
            }


        });

    }//onMapReady function end



//
//    protected void onStart() {
//        if(mGoogleApiClient==null){
//            buildGoogleApiClient();
//
//        }
//
//        super.onStart();
//    }

    protected void onStop() {
        //mGoogleApiClient.disconnect();
        super.onStop();
    }






    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            //퍼미션 요청을 위해 UI를 보여줘야 하는지 검사

            Log.d("TAG", "checkLocationPermission" + "이미 퍼미션 획득한 경우");
            locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);


        } else {
            Log.d("TAG", "checkLocationPermission" + "퍼미션 없는경우 권한신청");
            // Show rationale and request permission.
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_LOCATION_REQUEST_CODE);



        }
        return true;
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("TAG", "onRequestPermissionsResult실행");
//        Log.d("permissions", permissions[0]);
//        Log.d("permissions.length", Integer.toString(permissions.length));
//        Log.d("grantResults", Integer.toString(grantResults[0]));
//        Log.d("ACCESS_FINE_LOCATION", Manifest.permission.ACCESS_FINE_LOCATION);
//        Log.d("PERMISSION_GRANTED", Integer.toString(PackageManager.PERMISSION_GRANTED));


        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            Log.d("TAG", "requestCode if문 안에 들어왔다");
            if (permissions.length > 0 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)  &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {



                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TAG", "퍼미션 획득시 마커를 추가 해준다");


                    locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
                    locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);

                    //mMap.setMyLocationEnabled(true);
                    Log.d("TAG", "connect 실행했다");

                }
            } else {
                // Permission was denied. Display an error message.
                Log.d("TAG", "퍼미션취소");

            }
        }
    }//onRequestPermissionsResult end

//    private void setCustomMarkerView() {
//        marker_root_view = LayoutInflater.from(this).inflate(R.layout.activity_maps, null);
//        //tv_marker = (TextView) marker_root_view.findViewById(R.id.tv_marker);
//        }


    private android.location.LocationListener locationListener = new android.location.LocationListener() {


        @Override
        public void onLocationChanged(Location location) {
            //setCustomMarkerView();
            Log.d("TAG", "onLocationChanged에 들어왔다");

            LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            if(myMarker==null){
                addMyMarker(myLatLng);
            }
            else{
                myMarker.position(myLatLng);

            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

//    // View를 Bitmap으로 변환
//    private Bitmap createDrawableFromView(Context context, View view) {
//
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
//        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
//        view.buildDrawingCache();
//        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
//
//        Canvas canvas = new Canvas(bitmap);
//        view.draw(canvas);
//
//        return bitmap;
//    }

    private void addMyMarker(LatLng latLng){
        Log.d("TAG", "마커생성!!!!!!!!!!!!!!!!!");
        //현재 위치에 마커 생성

        myMarker = new MarkerOptions();
        myMarker.position(latLng);
        myMarker.icon(getMarkerIcon("#e21d24")); // change the color of marker(red)
        myMarker.title("현재위치");
        mMap.addMarker(myMarker);

        //지도 상에서 보여주는 영역 이동
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        //mGoogleApiClient.connect();
    }

    private void addStudentMarker(JSONArray arrayList,String markerColor,String className) throws JSONException {
        Log.d("TAG", "학생마커 생성");

        for(int i = 0 ; i < arrayList.length();i++){
            Log.d("TAG", "포문안에 들어왔다");
            //제이슨배열을 만든것을 하나씩 제이슨 객체로 만듬
            JSONObject dataJsonObject = arrayList.getJSONObject(i);
            Log.d("TAG", "제이슨 오브젝트로 만들었다");
            Log.d("TAG", String.valueOf(dataJsonObject.getDouble("lat")));
            LatLng studentGPS = new LatLng(dataJsonObject.getDouble("lat"), dataJsonObject.getDouble("lng"));
            Log.d("TAG", "좌표객체만들었다");
            MarkerOptions markerOptions = new MarkerOptions();
            Log.d("TAG", "마커옵션 만들었다");
            markerOptions.position(studentGPS);
            Log.d("TAG", "마커에 좌표넣었다");
            markerOptions.icon(getMarkerIcon(markerColor)); // change the color of marker
            Log.d("TAG", "아이콘색변경했다");
            markerOptions.title(dataJsonObject.getString("name"));
            Log.d("TAG", "마커이름 바꿨다");
            Marker studentMarker = mMap.addMarker(markerOptions);
            Log.d("TAG", "마커 생성하고 맵에 추가했다");

            if(className.equals("1반")){
                Log.d("tag", "1반 마커배열에 넣음");
                classOneMarker.add(studentMarker);
            }else if(className.equals("2반")){
                Log.d("tag", "2반 마커배열에 넣음");
                classTwoMarker.add(studentMarker);
            }else{
                Log.d("tag", "3반 마커배열에 넣음");
                classThreeMarker.add(studentMarker);
            }
        }


    }

    // method definition //change the color of marker
    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }
//    private void addImageMarker(){
//
////        View marker = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.onbin_marker, null);
////        //현재 위치에 마커 생성
//        ArrayList<LatLng> a = new ArrayList();
//        LatLng latLng1 = new LatLng(35.897532, 128.622696);
//        LatLng latLng2 = new LatLng(35.896664, 128.619985);
//        LatLng latLng3 = new LatLng(35.891765, 128.614243);
//        LatLng latLng4 = new LatLng(35.897884, 128.608298);
//        LatLng latLng5 = new LatLng(35.895238, 128.622741);
//        LatLng latLng6 = new LatLng(35.893233, 128.624808);
//
//        a.add(latLng1); a.add(latLng2); a.add(latLng3);a.add(latLng4);a.add(latLng5);a.add(latLng6);
//
//
////
////        MarkerOptions markerOptions = new MarkerOptions();
////        markerOptions.position(latLng);
////        markerOptions.title("현재위치");
////        // markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker_root_view)));
////        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker)));
////        markerOptions.flat(true);
////        mMap.addMarker(markerOptions);
//        for(int i = 0 ; i < a.size();i++){
//            GroundOverlayOptions newarkMap = new GroundOverlayOptions()
//                    .image(BitmapDescriptorFactory.fromResource(R.drawable.onebin))
//                    .position(a.get(i), 86f, 65f);
//            mMap.addGroundOverlay(newarkMap);
//        }
//        mSydney = mMap.addMarker(new MarkerOptions()
//                .position(SYDNEY)
//                .title("Sydney");
//        mSydney.setTag(0);
//
//    }
@Override
public void onLocationChanged(Location location) {

}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this,"마커가 선택 되었습니다.",Toast.LENGTH_SHORT).show();
        return false;
    }

    private class SlidingPageAnimationListener implements Animation.AnimationListener{

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if(isPageOpen){
                slidingPage01.setVisibility(View.GONE);
                scrollPage.setVisibility(View.GONE);
                isPageOpen = false;

            }else{

                isPageOpen=true;
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_settings){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //색상을 클릭하고 다시 넘어올경우 색상을 저장해야함
        if(requestCode == REQUEST_CODE_WRITE){
            if(resultCode == RESULT_OK){
                String name = intent.getExtras().getString("name");
                Toast toast = Toast.makeText(getBaseContext(),
                        "응답으로 전달된 name: )" + name, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }
}