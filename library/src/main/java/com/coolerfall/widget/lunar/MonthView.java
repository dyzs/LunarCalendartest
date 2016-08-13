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
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

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
	private int mMarkerIndex = -1;	// useless wait del
	private ArrayList<Integer> mMarkerIndexList;	// useless 保存 marker 的选中的那天

	private float mSolarTextSize;
	private float mLunarTextSize;
	private float mLunarOffset;
	private float mSolarOffset;
	private float mCircleRadius;	// 圆的半径，选择的日期的圆形样式
	private float mCircleMarkerRadius;	// maidou add
	private float mMarkerOffset;	// maidou add
	private ArrayList<String> mMarkersList;	// maidou add
	private HashMap<String, Integer> mMarkerHm;

	private static final int LIGHT_GRAY = 0xffeaeaea;
	private static final int LIGHT_PINK = 0xcce54f60;
	private static final int MARKER_COLOR = Color.MAGENTA; // add maidou

	private Month mMonth;
	private LunarView mLunarView;

	private final Region[][] mMonthWithFourWeeks = new Region[4][DAYS_IN_WEEK];		// 在平面上的一个区域，是用 Rect 组成的
	private final Region[][] mMonthWithFiveWeeks = new Region[5][DAYS_IN_WEEK];
	private final Region[][] mMonthWithSixWeeks = new Region[6][DAYS_IN_WEEK];
	private Paint mPaint;			// 定义画笔
	// private Paint mPaintRect;		// 矩形框的画笔
	private Paint mPaintLittleStar;	// 定义小星星画笔
	private Paint mPaintDivider;	// 垂直分割线画笔

	private Bitmap bitmapStar;		// 星星的bitmap对象
	private Context mContext;

	private boolean flag1;
	private boolean flag2;
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
		mContext = context;
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

		mSolarTextSize = mContext.getResources().getDimension(R.dimen.calendar_solar_text_size);// h / 15f;
		mPaint.setTextSize(mSolarTextSize);
		float solarHeight = mPaint.getFontMetrics().bottom - mPaint.getFontMetrics().top;

		mLunarTextSize = mContext.getResources().getDimension(R.dimen.calendar_lunar_text_size);//mSolarTextSize / 2.5f;
		mPaint.setTextSize(mLunarTextSize);
		float lunarHeight = mPaint.getFontMetrics().bottom - mPaint.getFontMetrics().top;

		mLunarOffset = (Math.abs(mPaint.ascent() + mPaint.descent()) +
				solarHeight + lunarHeight) / 3f + 8f;

		mSolarOffset = mLunarOffset - lunarHeight - 2f;
		mMarkerOffset = mLunarOffset;	// maidou add
		initMonthRegion(mMonthWithFourWeeks, dayWidth, dayHeightInFourWeek);
		initMonthRegion(mMonthWithFiveWeeks, dayWidth, dayHeightInFiveWeek);
		initMonthRegion(mMonthWithSixWeeks, dayWidth, dayHeightInSixWeek);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
		int weeks = mMonth.getWeeksInMonth();
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
		mMarkerHm = mLunarView.getHnMarker();
		canvas.save();
		int weeks = mMonth.getWeeksInMonth();
		Region[][] monthRegion = getMonthRegion();
		Bitmap dateBit = null;
		for (int i = 0; i < weeks; i++) {
			for (int j = 0; j < DAYS_IN_WEEK; j++) {
				draw(canvas, monthRegion[i][j].getBounds(), i, j);
				// String cycleTime = transformMonthDayToString(mMonth.getMonthDay(i, j))
				if (mMarkerHm != null) {
					String date = transformMonthDayToString(mMonth.getMonthDay(i, j));
					if (mMarkerHm.containsKey(date)) {
						if (dateBit != null && dateBit.isRecycled()) {dateBit.recycle();}
						dateBit = getImageFromDrawable(mMarkerHm.get(date));
						canvas.drawBitmap(
								dateBit,
								monthRegion[i][j].getBounds().centerX() + 22f,	// 负数往左
								monthRegion[i][j].getBounds().centerY() - 42f,	// 负数往上
								mPaintLittleStar
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
		mPaint.setTypeface(Typeface.SERIF);
		// 矩形框画笔
//		mPaintRect = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
//		mPaintRect.setStyle(Paint.Style.STROKE);//空心矩形框
//		mPaintRect.setColor(0xffebebeb);
		// 小星星画笔
		mPaintLittleStar = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
		mPaintLittleStar.setTextAlign(Paint.Align.CENTER);
		// 垂直分割线画笔
		mPaintDivider = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
		mPaintDivider.setStyle(Paint.Style.STROKE);
		mPaintDivider.setColor(getResources().getColor(R.color.calendar_normal_lunar_text_color));
		mPaintDivider.setStrokeWidth(0.5f);

		// 判断当前页面中的月份是当前时间的月份，用于初始化选中项
		if (mMonth.isMonthOfToday()) {
			mSelectedIndex = mMonth.getIndexOfToday();
		}

		setBackgroundColor(mLunarView.getMonthBackgroundColor());
//		bitmapStar = getImageFromDrawable(2);
//		mMarkersList = mLunarView.getMarkerList();
//		mMarkerHm = mLunarView.getHnMarker();
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
		Region[][] monthRegion;//  = mMonthWithSixWeeks;
		if (weeks == 4) {	// 当月只有在4周
			monthRegion = mMonthWithFourWeeks;
		} else if (weeks == 5) {
			monthRegion = mMonthWithFiveWeeks;
		} else {
			monthRegion = mMonthWithSixWeeks;
		}
		return monthRegion;
	}

	/* draw all the text in month view*/
	private void draw(Canvas canvas, Rect rect, int xIndex, int yIndex) {
		MonthDay monthDay = mMonth.getMonthDay(xIndex, yIndex);
		drawLeftRightLine(canvas, rect, xIndex, yIndex);		// 分割线
		drawBackground(canvas, rect, monthDay, xIndex, yIndex);	// 选中的背景样式
		drawSolarText(canvas, rect, monthDay);
		drawLunarText(canvas, rect, monthDay);
		// drawRectangle(canvas, rect); 		// 绘制方块边框
	}

	/* draw solar text in month view 画出阳历的 text*/
	private void drawSolarText(Canvas canvas, Rect rect, MonthDay monthDay) {
		if (monthDay == null) {
			return;
		}
		if (!monthDay.isCheckable()) {
			mPaint.setColor(mLunarView.getUnCheckableColor());		// 不可被选中的日期颜色，表示上月下月
		} else if (monthDay.isWeekend()) {
			mPaint.setColor(mLunarView.getHightlightColor());
		} else {
			mPaint.setColor(mLunarView.getSolarTextColor());		// 得到公历的文本颜色
		}
		if (!flag1 && !flag2) {
			mPaint.setColor(Color.WHITE);
		}
		mPaint.setTextSize(mSolarTextSize);
		/* getSolarDay 返回当前日期的公历字符串 */
		canvas.drawText(monthDay.getSolarDay(), rect.centerX(), rect.centerY() + mSolarOffset, mPaint);
	}

	/* draw lunar text in month view 绘制控件的农历文本*/
	private void drawLunarText(Canvas canvas, Rect rect, MonthDay monthDay) {
		if (monthDay == null) {
			return;
		}
		if (!monthDay.isCheckable()) {
			mPaint.setColor(mLunarView.getUnCheckableColor());	// 不可被选中的日期颜色，表示上月下月
		} else if (monthDay.isHoliday()) {
			mPaint.setColor(mLunarView.getHightlightColor());
		} else {    // 普通农历颜色
			mPaint.setColor(mLunarView.getLunarTextColor());
		}
		if (!flag1 && !flag2) {
			mPaint.setColor(Color.WHITE);
		}
		mPaint.setTextSize(mLunarTextSize);
		/* getLunarDay 返回当前日期的农历字符串 */
		canvas.drawText(monthDay.getLunarDay(), rect.centerX(), rect.centerY() + mLunarOffset, mPaint);
	}

	/* draw circle for selected day 绘制选中的圆形 */
	private void drawBackground(Canvas canvas, Rect rect, MonthDay day, int xIndex, int yIndex) {
		// 强制绘制当日背景，并不可变
//		if (day.isToday()) {
//			Drawable background = mLunarView.getTodayBackground();
//			if (background == null) {
//				drawRing(canvas, rect);
//			} else {
//				background.setBounds(rect);
//				background.draw(canvas);
//			}
//			return;
//		}
		/* not today was selected 绘制不是今天，但是是每个月的第一天*/
		flag1 = (mSelectedIndex == -1 && day.isFirstDay());
		if (flag1) {
			mSelectedIndex = xIndex * DAYS_IN_WEEK + yIndex;
			return;
		}

		// 过滤 selIndex 的上月下月
		flag2 = (mSelectedIndex / DAYS_IN_WEEK != xIndex || mSelectedIndex % DAYS_IN_WEEK != yIndex);
		if (flag2) {
			return;
		}
//		画选中圆形
//		mPaint.setColor(LIGHT_GRAY);
//		canvas.drawCircle(rect.centerX(), rect.centerY(), mCircleRadius, mPaint);
		drawSelectedBgStyle(canvas, rect);
	}
	/* draw ring as background of today 绘制日历中今天的红色环形*/
	private void drawRing(Canvas canvas, Rect rect) {
//		mPaint.setColor(Color.RED);
//		canvas.drawCircle(rect.centerX(), rect.centerY(), mCircleRadius, mPaint);
//		mPaint.setColor(mLunarView.getMonthBackgroundColor());
//		canvas.drawCircle(rect.centerX(), rect.centerY(), mCircleRadius - 2, mPaint);
		drawSelectedBgStyle(canvas, rect);
	}
	private void drawSelectedBgStyle(Canvas canvas, Rect rect) {
		mPaint.setColor(LIGHT_PINK);
		RectF rectF = new RectF();
		rectF.set(rect);
		canvas.drawRoundRect(rectF, 5f, 5f, mPaint);// drawRoundRect (rect, float rx, float ry, Paint paint)
	}

	/* draw rect as background of all day*/
	@Deprecated
	private void drawRectangle(Canvas canvas, Rect rect) {
		// canvas.drawRect(rect, mPaintRect);
	}

	private void drawLeftRightLine(Canvas canvas, Rect rect,int xIndex, int yIndex) {
		float startX = rect.left;
		float startY = rect.top + 27f;
		float endX = rect.left;
		float endY = rect.bottom - 27f;
		canvas.drawLine(startX, startY, endX, endY,mPaintDivider);
		if (yIndex == 6) {
			startX = rect.right;
			startY = rect.top + 27f;
			endX = rect.right;
			endY = rect.bottom - 27f;
			canvas.drawLine(startX, startY, endX, endY,mPaintDivider);
		}
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
		invalidate();
	}


	/**
	 * maidou add
	 * 绘制marker日期的背景,Month.getIndexOfDayInCurMonth
	 */
	@Deprecated
	private void drawMarkersBackground(Canvas canvas, int weeks, Region[][] monthRegion) {
		String cycleTime;
		mMarkersList = mLunarView.getMarkerList();
		if (mMarkersList != null) {
			System.out.println("list size:" + mMarkersList.size());
			for (String marker : mMarkersList) {
				for (int i = 0; i < weeks; i++) {
					for (int j = 0; j < DAYS_IN_WEEK; j++) {
						// 将当前日期 MonthDay 转换成字符串
						cycleTime = transformMonthDayToString(mMonth.getMonthDay(i, j));
						if (marker.equals(cycleTime)) {
							canvas.drawBitmap(
//									getImageFromAssetsFile("pic.png"),
									getImageFromDrawable(1),
									monthRegion[i][j].getBounds().centerX() - 35f,	// 负数往左
									monthRegion[i][j].getBounds().centerY() - 15f,	// 负数往上
									mPaint
							);
						}
//						if (mMarkersList != null) {
//							if (mMarkersList.contains(transformMonthDayToString(mMonth.getMonthDay(i, j)))) {
//								canvas.drawBitmap(
//										bitmapStar,
//										monthRegion[i][j].getBounds().centerX() + 22f,	// 负数往左
//										monthRegion[i][j].getBounds().centerY() - 45f,	// 负数往上
//										mPaintLittleStar
//								);
//							}
//						}
					}
				}
			}
		}
	}

	@Deprecated
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

	public static final int TYPE_STAR_SILVER = 1;
	public static final int TYPE_STAR_GOLDEN_NORMAL = 2;
	public static final int TYPE_STAR_GOLDEN_SPECIAL = 3;
	private static final int stars[] = {R.mipmap.pic_star_silvery, R.mipmap.pic_star_golden};
	private Bitmap getImageFromDrawable(int starType) {
		Bitmap image = null;
		switch (starType){
		case TYPE_STAR_SILVER:
			image = BitmapFactory.decodeResource(getResources(),stars[0]);
			break;
		case TYPE_STAR_GOLDEN_NORMAL:
			image = BitmapFactory.decodeResource(getResources(),stars[1]);
			break;
		case TYPE_STAR_GOLDEN_SPECIAL:

			break;
		default:
			image = BitmapFactory.decodeResource(getResources(),stars[0]);
			break;
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





	private String transformMonthDayToString(MonthDay monthDay) {
		return new SimpleDateFormat("yyyy-MM-dd").format(monthDay.getCalendar().getTime());
	}

	public int getCountWeekOfMonth() {
		return mMonth.getWeeksInMonth();
	}

	public void callRefresh(int day) {
		if (mMonth.isMonthOfToday() && day == 0) {
			mSelectedIndex = mMonth.getIndexOfToday();
		} else {
			int selectedDay = day == 0 ? 1 : day;
			mSelectedIndex = mMonth.getIndexOfDayInCurMonth(selectedDay);
		}
		invalidate();
	}
}