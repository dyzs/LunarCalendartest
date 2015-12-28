package com.mylunartest.dyzs.lunartest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import com.coolerfall.widget.lunar.LunarView;
import com.coolerfall.widget.lunar.MonthDay;
import com.mylunartest.dyzs.lunartest.R;


import java.util.ArrayList;
import java.util.Calendar;


/**
 * @author maidou
 * hypothesis insert marker date like ‘2015-12-12’
 * transfrom to {@link MonthDay},hwo to do it
 * The constructor of month day.
 * MonthDay(Calendar calendar)
 */
public class CalendarActivity extends Activity implements View.OnClickListener{

    private TextView mTvDate;
    private LunarView mLunarView;
    private ArrayList<String> mMarkers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        initView();
        mMarkers.add("2015-11-30");
        mMarkers.add("2015-12-16");
        mMarkers.add("2015-12-22");
        mMarkers.add("2015-09-21");
        mMarkers.add("2016-01-01");
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
                mTvDate.setText(String.format("%d-%d-%d  %s月%s", year, month, day, lunarMonth, lunarDay));
                if (mLunarView.getInterceptFirstTimeDatePick()) {
                    mLunarView.setInterceptFirstTimeDatePick(!mLunarView.getInterceptFirstTimeDatePick());
                    return;
                }
                String curDate = String.format("%d-%d-%d", year, month, day);
                ToastUtil.makeText(CalendarActivity.this,
                        String.format("%d-%d-%d  %s月%s", year, month, day, lunarMonth, lunarDay));
//                String dateFormat = String.format("%d-%d-%d", year, month, day);
//                sendMainHandlerMessage(ON_DATE_SELECTED, dateFormat);
            }
        });
    }

    private void initView() {
        mTvDate = (TextView) findViewById(R.id.tv_date);
        mLunarView = (LunarView) findViewById(R.id.lunar_view_main);
        findViewById(R.id.iv_to_last_month).setOnClickListener(this);
        findViewById(R.id.iv_to_next_month).setOnClickListener(this);
        findViewById(R.id.btn_add_mark).setOnClickListener(this);
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
            case R.id.btn_add_mark:
                mMarkers.remove("2015-11-30");
                for (String mark:mMarkers) {
                    System.out.println("mark:" + mark);
                }
                mLunarView.setOnMarkerListener(mMarkerListener);
                mLunarView.postInvalidate();
                System.out.println("postInvalidate excetued");
                break;
        }
    }

    private void addOneMarker(String marker) {
//      mLunarView.notifyMarkerAddListener(marker);
    }

    private void loadMarkerScheduler(String curDate) {

    }


    private MarkerListener mMarkerListener = new MarkerListener();
    class MarkerListener implements LunarView.OnMarkerListener {
        @Override
        public ArrayList<String> getInitMarkers() {
            return mMarkers;
        }
    }







//    private void doubleClick() {
//        if (mMarkers.contains("2015-09-21")) {
//            mLunarView.modifyCurMarker("2015-09-21","修改备注信息，blahblah");
//            return;
//        }
//        mLunarView.addMarker("2015-09-21");
//    }


}
