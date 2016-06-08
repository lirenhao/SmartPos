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

public class TimeWheelFragment extends Fragment implements View.OnClickListener {

    private MainActivity mainActivity;

    private WheelView mHourWheelView;
    private WheelView mMinuteWheelView;
    private WheelView mSecondWheelView;
    private Button timeNext;

    private CalendarTextAdapter mHourAdapter;
    private CalendarTextAdapter mMinuteAdapter;
    private CalendarTextAdapter mSecondAdapter;

    private String mHourStr;
    private String mMinuteStr;
    private String mSecondStr;

    private ArrayList<String> hourList = new ArrayList<>();
    private ArrayList<String> minuteList = new ArrayList<>();
    private ArrayList<String> secondList = new ArrayList<>();

    //常量
    private final int MAX_TEXT_SIZE = 30;
    private final int MIN_TEXT_SIZE = 20;

    public TimeWheelFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_wheel, container, false);
        mHourWheelView = (WheelView) view.findViewById(R.id.hour_wv);
        initHour();

        mMinuteWheelView = (WheelView) view.findViewById(R.id.minute_wv);
        initMinute();

        mSecondWheelView = (WheelView) view.findViewById(R.id.second_wv);
        initSecond();

        timeNext = (Button) view.findViewById(R.id.timeNext);
        timeNext.setOnClickListener(this);
        return view;
    }

    private void initHour() {
        Calendar nowCalendar = Calendar.getInstance();
        int nowHourId = nowCalendar.get(Calendar.HOUR_OF_DAY);
        hourList.clear();
        for (int i = 0; i < 24; i++) {
            if (i < 10) {
                hourList.add("0" + i);
            } else {
                hourList.add("" + i);
            }
        }
        mHourAdapter = new CalendarTextAdapter(mainActivity, hourList, nowHourId, MAX_TEXT_SIZE, MIN_TEXT_SIZE);
        mHourWheelView.setVisibleItems(5);
        mHourWheelView.setViewAdapter(mHourAdapter);
        mHourWheelView.setCurrentItem(nowHourId);
        mHourStr = hourList.get(nowHourId);

        mHourWheelView.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mHourAdapter.getItemText(wheel.getCurrentItem());
                // 设置字体大小
                setTextViewStyle(currentText, mHourAdapter);
                mHourStr = hourList.get(wheel.getCurrentItem()) + "";
            }
        });

        mHourWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                String currentText = (String) mHourAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mHourAdapter);
            }
        });
    }

    private void initMinute() {
        Calendar nowCalendar = Calendar.getInstance();
        int nowMinuteId = nowCalendar.get(Calendar.MINUTE);
        minuteList.clear();
        for (int i = 0; i < 60; i++) {
            if (i < 10) {
                minuteList.add("0" + i);
            } else {
                minuteList.add("" + i);
            }
        }
        mMinuteAdapter = new CalendarTextAdapter(mainActivity, minuteList, nowMinuteId, MAX_TEXT_SIZE, MIN_TEXT_SIZE);
        mMinuteWheelView.setVisibleItems(5);
        mMinuteWheelView.setViewAdapter(mMinuteAdapter);
        mMinuteWheelView.setCurrentItem(nowMinuteId);
        mMinuteStr = minuteList.get(nowMinuteId);

        mMinuteWheelView.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mMinuteAdapter.getItemText(wheel.getCurrentItem());
                // 设置字体大小
                setTextViewStyle(currentText, mMinuteAdapter);
                mMinuteStr = minuteList.get(wheel.getCurrentItem()) + "";
            }
        });
    }

    private void initSecond() {
        Calendar nowCalendar = Calendar.getInstance();
        int nowSecondId = nowCalendar.get(Calendar.SECOND);
        secondList.clear();
        for (int i = 0; i < 60; i++) {
            if (i < 10) {
                secondList.add("0" + i);
            } else {
                secondList.add("" + i);
            }
        }
        mSecondAdapter = new CalendarTextAdapter(mainActivity, secondList, nowSecondId, MAX_TEXT_SIZE, MIN_TEXT_SIZE);
        mSecondWheelView.setVisibleItems(5);
        mSecondWheelView.setViewAdapter(mSecondAdapter);
        mSecondWheelView.setCurrentItem(nowSecondId);
        mSecondStr = secondList.get(nowSecondId);

        mSecondWheelView.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mSecondAdapter.getItemText(wheel.getCurrentItem());
                // 设置字体大小
                setTextViewStyle(currentText, mSecondAdapter);
                mSecondStr = secondList.get(wheel.getCurrentItem()) + "";
            }
        });

        mSecondWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                String currentText = (String) mSecondAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mSecondAdapter);
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
        ((App) mainActivity.getApplication()).getTransData().setOldTransTime(mHourStr + mMinuteStr + mSecondStr);
        mainActivity.getTimeWheelWaitThreat().notifyThread();
    }
}
