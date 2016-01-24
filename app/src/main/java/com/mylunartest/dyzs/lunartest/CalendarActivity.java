package com.mylunartest.dyzs.lunartest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import com.coolerfall.widget.lunar.LunarView;
import com.coolerfall.widget.lunar.MonthDay;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


/**
 * @author maidou
 * hypothesis insert marker date like ‘2015-12-12’
 * transfrom to {@link MonthDay}
 * The constructor of month day.
 * MonthDay(Calendar calendar)
 */
public class CalendarActivity extends Activity implements View.OnClickListener{

    private TextView mTvDate;
    private LunarView mLunarView;
    private ArrayList<String> mMarkers = new ArrayList<>();
    private String mCurDate;
    private Context mContext;
    private JSONObject jsonObj;
    private HashMap<String,Object> hashMap;
    private String reg = "[0-9]{2}";
    // "2015-11-30":[]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        mContext = this;
        jsonObj = new JSONObject();
        String jsonStr = "[{'2015-11-30':'[{'icon1':url,'icon2':url2},{},{}]'},{},{},{}]";



        initView();
//        mMarkers.add("2015-11-30");
//        mMarkers.add("2015-12-16");
//        mMarkers.add("2015-12-22");
//        mMarkers.add("2015-09-21");
        mMarkers.add("2016-01-01");
        mMarkers.add("2016-01-04");
        mMarkers.add("2016-01-07");
        mMarkers.add("2016-01-09");
        mMarkers.add("2016-02-09");
        mMarkers.add("2016-02-15");
        mMarkers.add("2016-02-22");
        initData();
    }

    // 添加 marker
    private void initData() {
        mLunarView.setOnMarkerListener(mMarkerListener);
        mLunarView.setOnDatePickListener(new LunarView.OnDatePickListener() {
            @Override
            public void onDatePick(LunarView view, MonthDay monthDay) {
                int year = monthDay.getCalendar().get(Calendar.YEAR);
                int month = monthDay.getCalendar().get(Calendar.MONTH) + 1;
                int day = monthDay.getCalendar().get(Calendar.DAY_OF_MONTH);
                String lunarMonth = monthDay.getLunar().getLunarMonth();
                String lunarDay = monthDay.getLunar().getLunarDay();

                String strMonth = month + "";
                String strDay = day + "";
                if (!strMonth.matches(reg)) {
                    strMonth = "0" + month;
                }

                if (!strDay.matches(reg)) {
                    strDay = "0" + day;
                }
                System.out.println("month:" + month);


                mTvDate.setText(String.format("%d-%d-%d  %s月%s", year, month, day, lunarMonth, lunarDay));
                if (mLunarView.getInterceptFirstTimeDatePick()) {
                    mLunarView.setInterceptFirstTimeDatePick(!mLunarView.getInterceptFirstTimeDatePick());
                    return;
                }
                mCurDate = String.format("%d-%d-%d", year, month, day);
                ToastUtil.makeText(CalendarActivity.this,
                        String.format("%d-%s-%s  %s月%s", year, strMonth, strDay, lunarMonth, lunarDay));
//                sendMainHandlerMessage(ON_DATE_SELECTED, mCurDate);
            }
        });
    }

    private void initView() {
        mTvDate = (TextView) findViewById(R.id.tv_date);
        mLunarView = (LunarView) findViewById(R.id.lunar_view_main);
        findViewById(R.id.iv_to_last_month).setOnClickListener(this);
        findViewById(R.id.iv_to_next_month).setOnClickListener(this);
        findViewById(R.id.tv_add).setOnClickListener(this);
        findViewById(R.id.tv_del).setOnClickListener(this);
        findViewById(R.id.del_all).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_to_last_month:
                mLunarView.showPrevMonth();
                break;
            case R.id.iv_to_next_month:
                mLunarView.showNextMonth();
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_add:
                if (mCurDate != null) {
                    mLunarView.addMarker(mCurDate);
                    System.out.println("add excetued");
                } else {
                    ToastUtil.makeText(mContext, "请选择日期");
                }
                break;
            case R.id.tv_del:
                if (mMarkers.contains(mCurDate)) {
                    mMarkers.remove(mCurDate);
                    mLunarView.removeOneMarker(mCurDate);
                    ToastUtil.makeText(mContext, "成功删除：" + mCurDate);
                } else {
                    ToastUtil.makeText(mContext, "当前日期没有日程安排");
                }
                break;
            case R.id.del_all:
                ToastUtil.makeText(mContext, "删除全部");
                mLunarView.removeAllMarkers(mMarkers);
                mMarkers = null;
                break;
        }
    }

    private MarkerListener mMarkerListener = new MarkerListener();
    class MarkerListener implements LunarView.OnMarkerListener {
        @Override
        public ArrayList<String> getInitMarkers() {
            return mMarkers;
        }
    }
}
