package com.learnfun.super8team.learnfun.Content;


import android.location.Location;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.learnfun.super8team.learnfun.Activity.ContentActivity;
import com.learnfun.super8team.learnfun.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by KIM on 2017-05-12.
 */

public class Content {
    public static boolean CONTENT_USED = false;
    public static int ONLINE_CONTENT_NUM;
    private String name;
    private int number;
    private String vertical;
    private String horizontal;
    private boolean visionable;
    private boolean clickable;
    private boolean disable;
    private Location location; // 위경도 데이터를 가지고 있을 객체
    private JSONArray scriptCode;
    private EditText editview;
    private boolean hasEditview = false;
    private ArrayList<ContentView> views = new ArrayList<>();
    private JSONObject jobj;
    private ContentActivity contentActivity;
//    private ArrayList<ImgView> imgView = new ArrayList<>();
//    private ArrayList<TxtView> txtView = new ArrayList<>();
//    private ArrayList<BtnView> btnView = new ArrayList<>();
    private int[] imgviews = {R.id.img_1,R.id.img_2,R.id.img_3,R.id.img_4};
    private int[] btnviews = {R.id.btn_1,R.id.btn_2,R.id.btn_3,R.id.btn_4};

    private LinearLayout outform;
    private LinearLayout inform;

    //생성자 초기설정
    public Content(JSONObject jobj, ContentActivity contentActivity) throws JSONException, InterruptedException {
        this.contentActivity = contentActivity;
        this.jobj        = jobj;
        this.name        = jobj.getString("name");
        this.number      = jobj.getInt("number");
        this.vertical   = jobj.getString("vertical");
        this.horizontal = jobj.getString("horizontal");
        this.visionable = jobj.getBoolean("visionable");
        this.clickable  = jobj.getBoolean("clickable");
        this.disable    = jobj.getBoolean("disable");
        String str = jobj.getString("location");
        Log.i("뭐냐이거",str);
        String temp[] = str.substring(1,str.length()-1).split(",");
        Log.i("위치값은 어떻게/?",temp[0]);
        Log.i("위치값은 어떻게/?",temp[1]);
        // 현재 입력된 위경도 데이터로 로케이션 객체 생성
        this.location = new Location("Content");
        this.location.setLatitude(Double.parseDouble(temp[0]));
        this.location.setLongitude(Double.parseDouble(temp[1]));

        Log.i("컨텐츠 멤버 값 입력완료 -----", this.name);
        Log.i("vision 값 입력완료 -----", String.valueOf(this.visionable));
        Log.i("ciick 값 입력완료 -----", String.valueOf(this.clickable));
        //컨텐츠 배열들을 ArrayList에 저장
        //이미지 컨텐츠 배열
        for(int i=0;i<jobj.getJSONArray("image").length();i++){
            ImgView imgView = new ImgView(jobj.getJSONArray("image").getJSONObject(i), contentActivity.findViewById(imgviews[i]),this.number,contentActivity);

            this.views.add(imgView);

        }

        for(int i=0;i<jobj.getJSONArray("text").length();i++){
//            Log.i("텍스트 컨텐츠 길이 ::",Integer.toString(jobj.getJSONArray("text").length()));
            if(jobj.getJSONArray("text").getJSONObject(i).getInt("id") == 1){
                //텍스트 컨텐츠 헤더
//                Log.i("텍스트 컨텐츠 헤더 값 ::",Integer.toString(jobj.getJSONArray("text").getJSONObject(i).getInt("id")));
                TxtView txtView = new TxtView(jobj.getJSONArray("text").getJSONObject(i), contentActivity.findViewById(R.id.header_text),this.number);

                this.views.add(txtView);
            }else{
                //텍스트 컨텐츠 바텀
//                Log.i("텍스트 컨텐츠 바텀 값 ::",Integer.toString(jobj.getJSONArray("text").getJSONObject(i).getInt("id")));
                TxtView txtView = new TxtView(jobj.getJSONArray("text").getJSONObject(i), contentActivity.findViewById(R.id.bottom_text),this.number);

                this.views.add(txtView);
            }
        }
//        Log.i("텍스트 컨텐츠 배열 저장완료 -----",views.toString());
        //에디트뷰 있을시
        if(jobj.has("edit")){
            hasEditview = true;
            editview = (EditText) contentActivity.findViewById(R.id.edit_text);
            editview.setText(jobj.getJSONObject("edit").getString("text"));
            editview.setHint(jobj.getJSONObject("edit").getString("hint"));
            editview.setTextSize(jobj.getJSONObject("edit").getInt("size"));
        }

        //버튼 컨텐츠 배열
        for(int i=0;i<jobj.getJSONArray("button").length();i++){
//            Log.i("버튼 컨텐츠 길이 ::",Integer.toString(jobj.getJSONArray("button").length()));
            BtnView btnView = new BtnView(jobj.getJSONArray("button").getJSONObject(i), contentActivity.findViewById(btnviews[i]),this.number);

            this.views.add(btnView);
        }
//        Log.i("버튼 컨텐츠 배열 저장완료 -----",views.toString());
        //전체적인 컨텐츠 위치 설정
        outform = (LinearLayout) contentActivity.findViewById(R.id.horizon_layout);
        LinearLayout.LayoutParams params_horizontal = (LinearLayout.LayoutParams) outform.getLayoutParams();
        //호라이즌 폼 설정
        switch (this.horizontal){
            case "left":
                params_horizontal.gravity = Gravity.LEFT;
                break;
            case "center":
                params_horizontal.gravity = Gravity.CENTER;
                break;
            case "right":
                params_horizontal.gravity = Gravity.RIGHT;
                break;
            default:
                params_horizontal.gravity = Gravity.CENTER;
                break;
        }
        outform.setLayoutParams(params_horizontal);

        //버티컬 폼 설정
        inform  = (LinearLayout) contentActivity.findViewById(R.id.vertical_layout);
        LinearLayout.LayoutParams params_vertical = (LinearLayout.LayoutParams) inform.getLayoutParams();

        switch (this.vertical){
            case "top":
                params_vertical.gravity = Gravity.TOP;
                break;
            case "center":
                params_vertical.gravity = Gravity.CENTER;
                break;
            case "bottom":
                params_vertical.gravity = Gravity.BOTTOM;
                break;
            default:
                params_vertical.gravity = Gravity.CENTER;
                break;
        }

        inform.setLayoutParams(params_vertical);

    }//end of constructor

    //컨텐츠 실행 조건체크-쓰레드로 체크될 것
    public boolean checkCondition(double lat, double lng){
        double val = 0.000100;
        //컨텐츠 반경안에 접근하면 참 반환 but 컨텐츠가 살아있어야함
//        Log.i("위도      ", String.valueOf(latitude+val > lat && latitude-val < lat));
//        Log.i("나의 위도      ", String.valueOf(lat));
//        Log.i("위도 큰값     ", String.valueOf(latitude+val));
//        Log.i("위도 작은값     ", String.valueOf(latitude-val));
//        Log.i("경도      ", String.valueOf(latitude-val < lat && longitude+val > lng));
        if( (location.getLatitude()+val > lat && location.getLatitude()-val < lat) && (location.getLongitude()-val < lng && location.getLongitude()+val > lng) ){
            return true;
        }else{
            return false;
        }
    }
    //컨텐트 뷰 표시
    public void setContentView() throws InterruptedException {
        if(!CONTENT_USED){
            CONTENT_USED = true;
            ONLINE_CONTENT_NUM = this.number;
            //컨텐츠 뷰 활성화
            for(int i=0;i<views.size();i++){
                Log.e("Content", "visible");
                views.get(i).setContentView();
            }
            //액션 스크립트 등록
            addScript();
            if (hasEditview) editview.setVisibility(View.VISIBLE);
            ImageButton exitBtn = (ImageButton) contentActivity.findViewById(R.id.exitBtn);
            exitBtn.setVisibility(View.VISIBLE);
        }

    }
    //컨텐츠 그냥 종료
    public void closeContent(){
        for(int i=0;i<views.size();i++){
            //뷰 비활성화
            views.get(i).unsetContentView();
            //액션 스크립트 초기화
            views.get(i).actionClear();
        }
        if (hasEditview) editview.setVisibility(View.GONE);
        ImageButton exitBtn = (ImageButton) contentActivity.findViewById(R.id.exitBtn);
        exitBtn.setVisibility(View.GONE);
        CONTENT_USED = false;
    }

    //컨텐트 뷰 비활성화
    public void unsetContentView(){
        for(int i=0;i<views.size();i++){
            //뷰 비활성화
            views.get(i).unsetContentView();
            //액션 스크립트 초기화
            views.get(i).actionClear();
        }
        if (hasEditview) {
            editview.setVisibility(View.GONE);
            editview.setText("");
        }
        ImageButton exitBtn = (ImageButton) contentActivity.findViewById(R.id.exitBtn);
        exitBtn.setVisibility(View.GONE);
        CONTENT_USED = false;
        this.disable = true;
        this.visionable = false;
        this.clickable = false;
        //DB에 있는 명세(현재컨텐츠의 값) 수정 하는 코드 작성할 것
        contentActivity.getDB().update(this.name,false,false,true);
    }
    //컨텐츠 이름 받아오기
    public String getContentName(){
        return this.name;
    }

    //컨텐츠 생명여부 받아오기
    public boolean getContentDisable(){
        return this.disable;
    }
    public void setContentDisable(boolean flag){ this.disable = flag; }

    public boolean getClickable() { return this.clickable; }
    public void setContentClickable(boolean flag){ this.clickable = flag; }

    public boolean getVisionable() { return this.visionable; }
    public void setContentVisionable(boolean flag){ this.visionable = flag; }

    public Location getContentLocation() {
        return location;
    }

    public int getNumber() { return this.number; }

    private void addScript() {
        //스크립트 읽어들이기

        try {
            if (disable == false) {
                Log.i("스크립트 ", jobj.getString("name"));
                scriptCode = jobj.getJSONArray("script");
                for (int i = 0; i < scriptCode.length(); i++) {
                    //존재하는 스크립트수만큼 반복
                    //이름이 같은 뷰에 해당 타입의 액션을 추가한다.
//                    Log.i("제이슨 길이","ㅁㅁ"+scriptCode.length());
                    //스크립트 명세 추출
                    String type = scriptCode.getJSONObject(i).getString("type");
//                    Log.i("제이슨 길이","떳냐");
                    //타입에 따른 액션스크립트 구분
                    if (type.equals("CLICK")) {
                        String name = scriptCode.getJSONObject(i).getString("name");
                        JSONObject action = scriptCode.getJSONObject(i).getJSONObject("action");
//                        Log.i("액션 제이슨값  ",action.toString());
                        //컨텐츠 ArrayList에서 이름이 같은 컨텐츠 검색
                        for (int j = 0; j < views.size(); j++) {
//                            Log.i("제이슨 네임  ",name);
//                            Log.i("뷰 네임 ", views.get(j).name);
                            if (name.equals(views.get(j).name) && ONLINE_CONTENT_NUM == views.get(j).contentNum) {
                                //이름이 같은 컨텐츠 발견시 액션코드를 해당 컨텐츠에 삽입
                                //반복종료
                                Log.i("9SS", views.get(j).toString());
                                views.get(j).setClickAction(action, contentActivity);

                                break;
                            }
                        }
                    } else if (type.equals("CHECKEDIT")) {
                        String answer = scriptCode.getJSONObject(i).getString("answer");
                        String name = scriptCode.getJSONObject(i).getString("name");
                        JSONObject ooo = scriptCode.getJSONObject(i).getJSONObject("true");
                        JSONObject xxx = scriptCode.getJSONObject(i).getJSONObject("false");
                        Log.i("체크", " 뷰 사이즈 " + views.size());
                        for (int j = 0; j < views.size(); j++) {
                            Log.i("체크1-------", name);
                            Log.i("체크1-------", views.get(j).name);
                            if (name.equals(views.get(j).name) && ONLINE_CONTENT_NUM == views.get(j).contentNum) {
                                Log.i("9SS", views.get(j).toString() );
                                views.get(j).setCheckEditAction(editview, answer, ooo, xxx, contentActivity);

                                break;
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

