package com.yada.smartpos.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.yada.smartpos.R;
import com.yada.smartpos.activity.App;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.widget.OnWheelChangedListener;
import com.yada.smartpos.widget.OnWheelScrollListener;
import com.yada.smartpos.widget.WheelView;
import com.yada.smartpos.widget.adapters.AbstractWheelTextAdapter;

import java.util.ArrayList;
import java.util.Calendar;

public class DateWheelFragment extends Fragment implements View.OnClickListener {

    private MainActivity mainActivity;

    private WheelView mMonthWheelView;
    private WheelView mDayWheelView;
    private Button dateNext;

    private CalendarTextAdapter mMonthAdapter;
    private CalendarTextAdapter mDayAdapter;

    private String mMonthStr;
    private String mDayStr;

    private ArrayList<String> monthList = new ArrayList<>();
    private ArrayList<String> dayList = new ArrayList<>();

    //常量
    private final int MAX_TEXT_SIZE = 30;
    private final int MIN_TEXT_SIZE = 20;

    public DateWheelFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_wheel, container, false);
        mMonthWheelView = (WheelView) view.findViewById(R.id.month_wv);
        initMonth();

        mDayWheelView = (WheelView) view.findViewById(R.id.day_wv);
        initDay();

        dateNext = (Button) view.findViewById(R.id.dateNext);
        dateNext.setOnClickListener(this);
        return view;
    }

    private void initMonth() {
        Calendar nowCalendar = Calendar.getInstance();
        int nowMonthId = nowCalendar.get(Calendar.MONTH);
        monthList.clear();
        for (int i = 1; i <= 12; i++) {
            if (i < 10) {
                monthList.add("0" + i);
            } else {
                monthList.add("" + i);
            }
        }
        mMonthAdapter = new CalendarTextAdapter(mainActivity, monthList, nowMonthId, MAX_TEXT_SIZE, MIN_TEXT_SIZE);
        mMonthWheelView.setVisibleItems(5);
        mMonthWheelView.setViewAdapter(mMonthAdapter);
        mMonthWheelView.setCurrentItem(nowMonthId);
        mMonthStr = monthList.get(nowMonthId);

        mMonthWheelView.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mMonthAdapter.getItemText(wheel.getCurrentItem());
                // 设置字体大小
                setTextViewStyle(currentText, mMonthAdapter);
                mMonthStr = monthList.get(wheel.getCurrentItem()) + "";
            }
        });

        mMonthWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                String currentText = (String) mMonthAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mMonthAdapter);
            }
        });
    }

    private void initDay() {
        Calendar nowCalendar = Calendar.getInstance();
        int nowDayId = nowCalendar.get(Calendar.DAY_OF_MONTH) - 1;
        dayList.clear();
        setDay(Integer.parseInt(mMonthStr));
        mDayAdapter = new CalendarTextAdapter(mainActivity, dayList, nowDayId, MAX_TEXT_SIZE, MIN_TEXT_SIZE);
        mDayWheelView.setVisibleItems(5);
        mDayWheelView.setViewAdapter(mDayAdapter);
        mDayWheelView.setCurrentItem(nowDayId);
        mDayStr = dayList.get(nowDayId);

        mDayWheelView.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mDayAdapter.getItemText(wheel.getCurrentItem());
                // 设置字体大小
                setTextViewStyle(currentText, mDayAdapter);
                mDayStr = dayList.get(wheel.getCurrentItem()) + "";
            }
        });

        mDayWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                String currentText = (String) mDayAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mDayAdapter);
            }
        });
    }

    /**
     * 设置文字的大小
     *
     * @param currentItemText 文字
     * @param adapter         设配器
     */
    public void setTextViewStyle(String currentItemText, CalendarTextAdapter adapter) {
        ArrayList<View> arrayList = adapter.getTestViews();
        String currentText;
        for (View view : arrayList) {
            TextView textView = (TextView) view;
            currentText = textView.getText().toString();
            if (currentItemText.equals(currentText)) {
                textView.setTextSize(MAX_TEXT_SIZE);
                textView.setTextColor(mainActivity.getResources().getColor(R.color.text_10));
            } else {
                textView.setTextSize(MIN_TEXT_SIZE);
                textView.setTextColor(mainActivity.getResources().getColor(R.color.text_11));
            }
        }
    }

    /**
     * 将改月的所有日期写入数组
     *
     * @param month 月份
     */
    private void setDay(int month) {
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                for (int day = 1; day <= 31; day++) {
                    if (day < 10) {
                        dayList.add("0" + day);
                    } else {
                        dayList.add("" + day);
                    }
                }
                break;
            case 2:
                Calendar nowCalendar = Calendar.getInstance();
                int year = nowCalendar.get(Calendar.YEAR);
                if (isLeapYear(year)) {
                    for (int day = 1; day <= 29; day++) {
                        if (day < 10) {
                            dayList.add("0" + day);
                        } else {
                            dayList.add("" + day);
                        }
                    }
                } else {
                    for (int day = 1; day <= 28; day++) {
                        if (day < 10) {
                            dayList.add("0" + day);
                        } else {
                            dayList.add("" + day);
                        }
                    }
                }
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                for (int day = 1; day <= 30; day++) {
                    if (day < 10) {
                        dayList.add("0" + day);
                    } else {
                        dayList.add("" + day);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 判断是否是闰年
     *
     * @param year 年份
     */
    private boolean isLeapYear(int year) {
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0;
    }

    /**
     * 滚轮的adapter
     */
    private class CalendarTextAdapter extends AbstractWheelTextAdapter {
        ArrayList<String> list;

        protected CalendarTextAdapter(Context context, ArrayList<String> list, int currentItem, int maxSize, int minSize) {
            super(context, R.layout.item_wheel, R.id.tempValue, currentItem, maxSize, minSize);
            this.list = list;
        }

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            return super.getItem(index, cachedView, parent);
        }

        @Override
        public int getItemsCount() {
            return list.size();
        }

        @Override
        protected CharSequence getItemText(int index) {
            return list.get(index) + "";
        }
    }

    @Override
    public void onClick(View v) {
        ((App) mainActivity.getApplication()).getTransData().setOldTransDate(mMonthStr + mDayStr);
        mainActivity.getDateWheelWaitThreat().notifyThread();
    }
}
