package com.mylunartest.dyzs.lunartest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;


import com.coolerfall.widget.lunar.LunarView;
import com.coolerfall.widget.lunar.MonthDay;
import com.coolerfall.widget.lunar.MonthView;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


/**
 * @author maidou
 * hypothesis insert marker date like ‘2015-12-12’
 * transfrom to {@link MonthDay}
 * The constructor of month day.
 * MonthDay(Calendar calendar)
 */
public class CalendarActivity extends Activity implements View.OnClickListener{

    private Context mContext;
    private TextView mTvDate;
    private LunarView mLunarView;
    private String reg = "[0-9]{2}";
    private static final int ON_DATE_SELECTED = 61;
    private static final int GET_ALL_MARKER = 10;

    private ArrayList<String> mMarkers = new ArrayList<>();
    private TextView mTopSolarMonth, mTopWeekday, mTopYear;
    private static final String[] CHINESE_WEEK = {"日", "一", "二", "三", "四", "五", "六"};
    private static final String[] STR_ARR = {
            "2016-01-30","2016-02-05", "2016-02-09", "2016-02-18",
    };

    // 保存
    private HashMap<String, Integer> hmMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        mContext = this;

        initView();
        initData();
    }
    private void initView() {
        mTvDate = (TextView) findViewById(R.id.tv_date);
        mLunarView = (LunarView) findViewById(R.id.lunar_view_main);
        mTopSolarMonth = (TextView) findViewById(R.id.tv_big_solar_month);
        mTopWeekday = (TextView) findViewById(R.id.tv_calendar_weekday);
        mTopYear = (TextView) findViewById(R.id.tv_calendar_year);

        findViewById(R.id.iv_to_last_month).setOnClickListener(this);
        findViewById(R.id.iv_to_next_month).setOnClickListener(this);
    }
    // 添加 marker
    private void initData() {
        for (String str:STR_ARR) {
            mMarkers.add(str);
        }
        hmMarkers = new HashMap<>();
        // parseAsyncData();

        // count is the number of get data
        hmMarkers.put("2016-01-20", getStarType(1));
        hmMarkers.put("2016-01-27", getStarType(5));
        hmMarkers.put("2016-01-28", getStarType(2));
        hmMarkers.put("2016-02-01", getStarType(1));
        hmMarkers.put("2016-02-12", getStarType(5));
        mLunarView.setHmMarker(hmMarkers);
        mLunarView.setOnDatePickListener(new LunarView.OnDatePickListener() {
            @Override
            public void onDatePick(LunarView view, MonthDay monthDay) {
                int year = monthDay.getCalendar().get(Calendar.YEAR);
                int month = monthDay.getCalendar().get(Calendar.MONTH) + 1;
                int day = monthDay.getCalendar().get(Calendar.DAY_OF_MONTH);

                String strMonth = month + "";
                String strDay = day + "";
                if (!strMonth.matches(reg)) {
                    strMonth = "0" + month;
                }

                if (!strDay.matches(reg)) {
                    strDay = "0" + day;
                }
                String lunarMonth = monthDay.getLunar().getLunarMonth();
                String lunarDay = monthDay.getLunar().getLunarDay();
                mTvDate.setText(String.format("%d-%s-%s  %s月%s", year, strMonth, strDay, lunarMonth, lunarDay));
                mTopSolarMonth.setText(month + "");
                mTopWeekday.setText(getWeekDay(year, month, day));
                mTopYear.setText(year + "年");


                mTvDate.setText(String.format("%d-%d-%d  %s月%s", year, month, day, lunarMonth, lunarDay));
                if (mLunarView.getInterceptFirstTimeDatePick()) {
                    mLunarView.setInterceptFirstTimeDatePick(!mLunarView.getInterceptFirstTimeDatePick());
                    return;
                }
                ToastUtil.makeText(CalendarActivity.this,
                        String.format("%d-%d-%d  %s月%s", year, month, day, lunarMonth, lunarDay));
                String dateFormat = String.format("%d-%d-%d", year, month, day);
            }
        });


        // 初始化加载时调用一次, 因为在 pageSelected 的时候会预先 invalidate, 所以不需要加载 callRefresh 方法
        String today = DateUtil.getCurrentDate();
        parseAsyncData(DateUtil.getMonthTimeMillisOffset(today, -1), System.currentTimeMillis());
        mLunarView.setOnPageSelectedListener(new LunarView.onPageSelectedListener() {
            @Override
            public void resetMarkerData(int position) {
                String year = DateUtil.getYearByAdapterPos(position);
                String month = DateUtil.getMonthByAdapterPos(position);
                long fromTime = DateUtil.getMonthTimeMillisOffset(year, month, null, -1);// 当前月的上一月
                long toTime = DateUtil.getMonthTimeMillisOffset(year, month, null, 2);   // 当前月的两月
                parseAsyncData(fromTime, toTime);
            }
        });
    }

    private void parseAsyncData(long fTime, long tTime) {
        Date d1 = new Date();
        d1.setYear(2014 - 1900);
        d1.setMonth(10);
        d1.setDate(15);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("==>" + sdf.format(d1));

        Date d2 = new Date();
        d1.setYear(2017 - 1900);
        d1.setMonth(10);
        d1.setDate(15);

        long fromTime = d1.getTime();
        long toTime = d2.getTime();

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
//            case R.id.tv_add:
//                if (mCurDate != null) {
//                    mLunarView.addMarker(mCurDate);
//                    System.out.println("add excetued");
//                } else {
//                    ToastUtil.makeText(mContext, "请选择日期");
//                }
//                break;
//            case R.id.tv_del:
//                if (mMarkers.contains(mCurDate)) {
//                    mMarkers.remove(mCurDate);
//                    mLunarView.removeOneMarker(mCurDate);
//                    ToastUtil.makeText(mContext, "成功删除：" + mCurDate);
//                } else {
//                    ToastUtil.makeText(mContext, "当前日期没有日程安排");
//                }
//                break;
//            case R.id.del_all:
//                ToastUtil.makeText(mContext, "删除全部");
//                mLunarView.removeAllMarkers(mMarkers);
//                mMarkers = null;
//                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mLunarView.setMarkerList(mMarkers);
//        mLunarView.setHmMarker(hmMarkers);
    }

    // 通过 count 计算返回的星星类型
    private int getStarType(int count) {
        if (count < 3) {
            return MonthView.TYPE_STAR_SILVER;
        }
        return MonthView.TYPE_STAR_GOLDEN_NORMAL;
    }

    // 获取 weekday
    private String getWeekDay(int year, int month, int day) {
        String ret = "周";
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month -1, day);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
        case Calendar.SUNDAY:
            ret += "日";
            break;
        case Calendar.MONDAY:
            ret += "一";
            break;
        case Calendar.TUESDAY:
            ret += "二";
            break;
        case Calendar.WEDNESDAY:
            ret += "三";
            break;
        case Calendar.THURSDAY:
            ret += "四";
            break;
        case Calendar.FRIDAY:
            ret += "五";
            break;
        case Calendar.SATURDAY:
            ret += "六";
            break;
        default:
            ret += "日";
            break;
        }
        return ret;
    }
}