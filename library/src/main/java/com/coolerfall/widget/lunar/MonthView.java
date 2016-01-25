package com.coolerfall.widget.lunar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * 在日历上显示一个月份的阳历和阴历
 * Display one month with solar and lunar date on a calendar.
 *
 * @author Vincent Cheung
 * @since Oct. 12, 2015
 */
@SuppressLint("ViewConstructor")
public class MonthView extends View {
	private static final int DAYS_IN_WEEK = 7;

	private int mSelectedIndex = -1;
	private int mMarkerIndex = -1;
	private ArrayList<Integer> mMarkerIndexList;	// 保存 marker 的选中的那天

	private float mSolarTextSize;
	private float mLunarTextSize;
	private float mLunarOffset;
	private float mCircleRadius;	// 圆的半径，选择的日期的圆形样式
	private float mCircleMarkerRadius;	// maidou add
	private float mMarkerOffset;	// maidou add
	private ArrayList<String> mMarkersList;	// maidou add

	private static final int LIGHT_GRAY = 0xffeaeaea;
	private static final int MARKER_COLOR = Color.MAGENTA; // add maidou

	private Month mMonth;
	private LunarView mLunarView;

	private final Region[][] mMonthWithFourWeeks = new Region[4][DAYS_IN_WEEK];		// 在平面上的一个区域，是用 Rect 组成的
	private final Region[][] mMonthWithFiveWeeks = new Region[5][DAYS_IN_WEEK];
	private final Region[][] mMonthWithSixWeeks = new Region[6][DAYS_IN_WEEK];
	private Paint mPaint;	// 定义画笔
	private Paint mPaintRect;	// 矩形框的画笔
	private Paint mPaintDay;	// 当前日期的画笔
	private Bitmap bitmapStar;	// 星星的bitmap对象

	/**
	 * 传递上下文，月份和农历控件
	 * The constructor of month view.
	 *
	 * @param context   context to use
	 * @param lunarView {@link LunarView}
	 */
	public MonthView(Context context, Month month, LunarView lunarView) {
		super(context);

		mMonth = month;
		mLunarView = lunarView;
		init();
	}

	/* 当view大小发生改变时调用 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		int dayWidth = (int) (w / 7f);
		int dayHeightInFourWeek = (int) (h / 4f);
		int dayHeightInFiveWeek = (int) (h / 5f);
		int dayHeightInSixWeek = (int) (h / 6f);

		mCircleRadius = dayWidth / 2.2f;
		mCircleMarkerRadius = dayWidth / 15.0f;

		mSolarTextSize = h / 15f;
		mPaint.setTextSize(mSolarTextSize);
		float solarHeight = mPaint.getFontMetrics().bottom - mPaint.getFontMetrics().top;

		mLunarTextSize = mSolarTextSize / 2.5f;
		mPaint.setTextSize(mLunarTextSize);
		float lunarHeight = mPaint.getFontMetrics().bottom - mPaint.getFontMetrics().top;

		mLunarOffset = (Math.abs(mPaint.ascent() + mPaint.descent()) +
				solarHeight + lunarHeight) / 3f;
		mMarkerOffset = mLunarOffset;	// maidou add

		initMonthRegion(mMonthWithFourWeeks, dayWidth, dayHeightInFourWeek);
		initMonthRegion(mMonthWithFiveWeeks, dayWidth, dayHeightInFiveWeek);
		initMonthRegion(mMonthWithSixWeeks, dayWidth, dayHeightInSixWeek);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
		int weeks = mMonth.getWeeksInMonth();
		// System.out.println("weeks:====" + weeks);
		// 6f   5f
		if (weeks == 4) {	// 当月只有在4周
			setMeasuredDimension(measureWidth, (int) (measureWidth * 4f / 7f));
		} else if (weeks == 5) {
			setMeasuredDimension(measureWidth, (int) (measureWidth * 5f / 7f));
		} else {
			setMeasuredDimension(measureWidth, (int) (measureWidth * 6f / 7f));
		}
		setMeasuredDimension(measureWidth, (int) (measureWidth * 6f / 7f));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mMonth == null) {
			return;
		}
		canvas.save();
		int weeks = mMonth.getWeeksInMonth();
		Region[][] monthRegion = getMonthRegion();
		for (int i = 0; i < weeks; i++) {
			for (int j = 0; j < DAYS_IN_WEEK; j++) {
				draw(canvas, monthRegion[i][j].getBounds(), i, j);
				String cycleTime = transformMonthdayToString(mMonth.getMonthDay(i, j));
				if (mMarkersList != null) {
					if (mMarkersList.contains(cycleTime)) {
						canvas.drawBitmap(
								bitmapStar,
								monthRegion[i][j].getBounds().centerX() - 35f,	// 负数往左
								monthRegion[i][j].getBounds().centerY() - 15f,	// 负数往上
								mPaint
						);
					}

				}
			}
		}

		// drawMarkersBackground(canvas, weeks, monthRegion);

		canvas.restore();
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				return true;
			case MotionEvent.ACTION_UP:
				handleClickEvent((int) event.getX(), (int) event.getY());
				return true;
			default:
				return super.onTouchEvent(event);
		}
	}

	/* init month view */
	private void init() {
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
		mPaint.setTextAlign(Paint.Align.CENTER);

		mPaintRect = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
		mPaintRect.setStyle(Paint.Style.STROKE);//空心矩形框
		mPaintRect.setColor(0xffebebeb);
		// 0xFF888888 Color.GREY

		mPaintDay = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
//		mPaintDay.setTypeface(Typeface.SERIF);
		mPaintDay.setTextAlign(Paint.Align.CENTER);



		if (mMonth.isMonthOfToday()) {
			mSelectedIndex = mMonth.getIndexOfToday();
		}
		// System.out.println("MonthView method init() getIndexOfToday mSelectedIndex:" + mSelectedIndex);
		setBackgroundColor(mLunarView.getMonthBackgroundColor());
		bitmapStar = getImageFromAssetsFile("pic.png");
		mMarkersList = mLunarView.getMarkerList();
	}

	/* init month region with the width and height of day */
	private void initMonthRegion(Region[][] monthRegion, int dayWidth, int dayHeight) {
		for (int i = 0; i < monthRegion.length; i++) {
			for (int j = 0; j < monthRegion[i].length; j++) {
				Region region = new Region();
				region.set(
						j * dayWidth,
						i * dayHeight,
						dayWidth + j * dayWidth,
						dayWidth + i * dayHeight
				);
				monthRegion[i][j] = region;
			}
		}
	}
	/* get month region for current month 计算当月的区域 */
	private Region[][] getMonthRegion() {
		int weeks = mMonth.getWeeksInMonth();
		Region[][] monthRegion = mMonthWithSixWeeks;
//		if (weeks == 4) {	// 当月只有在4周
//			monthRegion = mMonthWithFourWeeks;
//		} else if (weeks == 5) {
//			monthRegion = mMonthWithFiveWeeks;
//		} else {
//			monthRegion = mMonthWithSixWeeks;
//		}
		return monthRegion;
	}

	/* draw all the text in month view*/
	private void draw(Canvas canvas, Rect rect, int xIndex, int yIndex) {
		MonthDay monthDay = mMonth.getMonthDay(xIndex, yIndex);
		drawBackground(canvas, rect, monthDay, xIndex, yIndex);
		drawSolarText(canvas, rect, monthDay);
		drawLunarText(canvas, rect, monthDay);
		drawRectangle(canvas, rect);
	}

	/* draw solar text in month view 画出阳历的 text*/
	private void drawSolarText(Canvas canvas, Rect rect, MonthDay monthDay) {
		if (monthDay == null) {
			return;
		}
		if (!monthDay.isCheckable()) {
			mPaint.setColor(mLunarView.getUnCheckableColor());		// 未选中的日期颜色
			mPaintDay.setColor(mLunarView.getUnCheckableColor());	// 未选中的日期颜色
		} else if (monthDay.isWeekend()) {
			mPaint.setColor(mLunarView.getHightlightColor());
			mPaintDay.setColor(mLunarView.getHightlightColor());
		} else {
			mPaint.setColor(mLunarView.getSolarTextColor());		// 得到阳历的文本颜色
			mPaintDay.setColor(mLunarView.getSolarTextColor());		// 得到阳历的文本颜色
		}

		mPaint.setTextSize(mSolarTextSize);
		mPaintDay.setTextSize(mSolarTextSize);
		/* getSolarDay 返回当前日期的公历字符串 */
		canvas.drawText(monthDay.getSolarDay(), rect.centerX(), rect.centerY(), mPaintDay);
	}

	/* draw lunar text in month view 绘制控件的农历文本*/
	private void drawLunarText(Canvas canvas, Rect rect, MonthDay monthDay) {
		if (monthDay == null) {
			return;
		}

		if (!monthDay.isCheckable()) {
			mPaint.setColor(mLunarView.getUnCheckableColor());
		} else if (monthDay.isHoliday()) {
			mPaint.setColor(mLunarView.getHightlightColor());
		} else {
			mPaint.setColor(mLunarView.getLunarTextColor());
		}

		mPaint.setTextSize(mLunarTextSize);
		/* getLunarDay 返回当前日期的农历字符串 */
		canvas.drawText(monthDay.getLunarDay(), rect.centerX(), rect.centerY() + mLunarOffset, mPaint);
	}

	/* draw circle for selected day 绘制选中的圆形 */
	private void drawBackground(Canvas canvas, Rect rect, MonthDay day, int xIndex, int yIndex) {
		if (day.isToday()) {
			Drawable background = mLunarView.getTodayBackground();
			if (background == null) {
				drawRing(canvas, rect);
			} else {
				background.setBounds(rect);
				background.draw(canvas);
			}
			return;
		}
		/* not today was selected 绘制不是今天，但是是每个月的第一天*/
		if (mSelectedIndex == -1 && day.isFirstDay()) {
			mSelectedIndex = xIndex * DAYS_IN_WEEK + yIndex;
		}

		if (mSelectedIndex / DAYS_IN_WEEK != xIndex ||
				mSelectedIndex % DAYS_IN_WEEK != yIndex) {
			return;
		}

		mPaint.setColor(LIGHT_GRAY);
		canvas.drawCircle(rect.centerX(), rect.centerY(), mCircleRadius, mPaint);
	}
	/* draw ring as background of today 绘制日历中今天的红色环形*/
	private void drawRing(Canvas canvas, Rect rect) {
		mPaint.setColor(Color.RED);
		canvas.drawCircle(rect.centerX(), rect.centerY(), mCircleRadius, mPaint);
		mPaint.setColor(mLunarView.getMonthBackgroundColor());
		canvas.drawCircle(rect.centerX(), rect.centerY(), mCircleRadius - 2, mPaint);
	}
	/* draw rect as background of all day*/
	private void drawRectangle(Canvas canvas, Rect rect) {
		canvas.drawRect(rect, mPaintRect);
	}

	/* handle date click event 处理日期的点击事件*/
	private void handleClickEvent(int x, int y) {
		Region[][] monthRegion = getMonthRegion();
		for (int i = 0; i < monthRegion.length; i++) {
			for (int j = 0; j < DAYS_IN_WEEK; j++) {
				Region region = monthRegion[i][j];
				if (!region.contains(x, y)) {
					continue;
				}
				MonthDay monthDay = mMonth.getMonthDay(i, j);
				if (monthDay == null) {
					return;
				}
				// DAY_OF_MONTH:如二十号，返回 20
				int day = monthDay.getCalendar().get(Calendar.DAY_OF_MONTH);
				// System.out.println("handleClickEvent day:" + day);
				if (monthDay.isCheckable()) {
					mSelectedIndex = i * DAYS_IN_WEEK + j;
					// System.out.println("handleClickEvent mSelectedIndex:" + mSelectedIndex);
					performDayClick();	// 执行点击
					invalidate();		// 执行重绘
				} else {
					if (monthDay.getDayFlag() == MonthDay.PREV_MONTH_DAY) {	// 如果当前的日期标志等于上一个月
						mLunarView.showPrevMonth(day);		// 显示上一个月
					} else if (monthDay.getDayFlag() == MonthDay.NEXT_MONTH_DAY) {
						mLunarView.showNextMonth(day);
					}
				}
				break;
			}
		}
	}

	/**
	 * 执行点击日历的点击事件
	 * Perform day click event.
	 */
	protected void performDayClick() {
		MonthDay monthDay = mMonth.getMonthDay(mSelectedIndex);
		mLunarView.dispatchDateClickListener(monthDay);
	}

	/**
	 * 设置选中的日期，选中的将会被绘制背景
	 * Set selected day, the selected day will draw background.
	 *
	 * @param day selected day
	 */
	protected void setSelectedDay(int day) {
		if (mMonth.isMonthOfToday() && day == 0) {
			mSelectedIndex = mMonth.getIndexOfToday();
		} else {
			int selectedDay = day == 0 ? 1 : day;
			mSelectedIndex = mMonth.getIndexOfDayInCurMonth(selectedDay);
		}

		invalidate();
		if ((day == 0 && mLunarView.getShouldPickOnMonthChange()) || day != 0) {
			performDayClick();
		}
	}

	protected void setMarkerDay(int day) {
		mMarkerIndex = mMonth.getIndexOfDayInCurMonth(day);
		System.out.println("mMarkerIndex:" + mMarkerIndex);
		invalidate();
	}


	/**
	 * maidou add
	 * 绘制marker日期的背景,Month.getIndexOfDayInCurMonth
	 */
	private void drawMarkersBackground(Canvas canvas, int weeks, Region[][] monthRegion) {
//		if (mMarkerIndex == (xIndex * DAYS_IN_WEEK + yIndex)) {
//			canvas.drawBitmap(
//					getImageFromAssetsFile("pic.png"),
//					rect.centerX() - 35f,    // 负数往左
//					rect.centerY() - 15f,    // 负数往上
//					mPaint
//			);
//		}
		String cycleTime;
		mMarkersList = mLunarView.getMarkerList();
		if (mMarkersList != null) {
			System.out.println("list size:" + mMarkersList.size());
			for (String marker : mMarkersList) {
				for (int i = 0; i < weeks; i++) {
					for (int j = 0; j < DAYS_IN_WEEK; j++) {
						// 将当前日期 MonthDay 转换成字符串
						cycleTime = transformMonthdayToString(mMonth.getMonthDay(i, j));
						if (marker.equals(cycleTime)) {
							//						画小星星
							canvas.drawBitmap(
									getImageFromAssetsFile("pic.png"),
									monthRegion[i][j].getBounds().centerX() - 35f,	// 负数往左
									monthRegion[i][j].getBounds().centerY() - 15f,	// 负数往上
									mPaint
							);
						}
					}
				}
			}
		}
	}

	private Bitmap getImageFromAssetsFile(String fileName) {
		Bitmap image = null;
		AssetManager am = getResources().getAssets();
		try {
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}


	/**
	 * 移除标记信息
	 */
	protected void removeOneMarker(String marker) {
		if (mMarkersList != null) {
			mMarkersList.remove(marker);
		}
		invalidate();
	}

	protected void addOneMarker(String marker) {
		mMarkersList.add(marker);
		invalidate();
	}




	private String transformMonthdayToString(MonthDay monthDay) {
		return new SimpleDateFormat("yyyy-MM-dd").format(monthDay.getCalendar().getTime());
	}

	public int getCountWeekOfMonth() {
		return mMonth.getWeeksInMonth();
	}
}