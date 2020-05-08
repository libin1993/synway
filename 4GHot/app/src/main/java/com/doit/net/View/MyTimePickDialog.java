package com.doit.net.View;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;


import com.doit.net.ucsi.R;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/********************* 这个类由网上拷贝再修改而来 **********************/

/**
 * 日期时间选择控件 使用方法： private EditText inputDate;//需要设置的日期时间文本编辑框 private String
 * initDateTime="2012年9月3日 14:44",//初始日期时间值 在点击事件中使用：
 * inputDate.setOnClickListener(new OnClickListener() {
 *
 * @Override public void onClick(View v) { MyTimePickDialog
 *           dateTimePicKDialog=new
 *           MyTimePickDialog(SinvestigateActivity.this,initDateTime);
 *           dateTimePicKDialog.dateTimePicKDialog(inputDate);
 *
 *           } });
 *
 * @author
 */
public class MyTimePickDialog implements OnDateChangedListener, OnTimeChangedListener {
    private DatePicker datePicker;
    private TimePicker timePicker;
    private AlertDialog ad;
    private String dateTime;
    private String initDateTime;
    private Activity activity;

    /**
     * 日期时间弹出选择框构造函数
     *
     * @param activity
     *            ：调用的父activity
     * @param initDateTime
     *            初始日期时间值，作为弹出窗口的标题和日期时间初始值
     */
    public MyTimePickDialog(Activity activity, String initDateTime) {
        this.activity = activity;
        this.initDateTime = initDateTime;

    }

    public void init(DatePicker datePicker, TimePicker timePicker) {
        Calendar calendar = Calendar.getInstance();
        if (!(null == initDateTime || "".equals(initDateTime))) {
            calendar = this.getCalendarByInintData(initDateTime);
        } else {
            initDateTime = calendar.get(Calendar.YEAR) + "年"
                    + calendar.get(Calendar.MONTH) + "月"
                    + calendar.get(Calendar.DAY_OF_MONTH) + "日 "
                    + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                    + calendar.get(Calendar.MINUTE) + ":"
                    + calendar.get(Calendar.SECOND);
        }

        datePicker.init(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                this);
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        //不支持秒
    }

    /**
     * 弹出日期时间选择框方法
     *
     * @param inputDate
     *            :为需要设置的日期时间文本编辑框
     * @return
     */
    public AlertDialog dateTimePicKDialog(final EditText inputDate) {
        LinearLayout dateTimeLayout = (LinearLayout) activity
                .getLayoutInflater().inflate(R.layout.date_time, null);
        datePicker = (DatePicker) dateTimeLayout.findViewById(R.id.dpDatePicker);
        timePicker = (TimePicker) dateTimeLayout.findViewById(R.id.tpTimePicker);
        init(datePicker, timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(this);
        setDatePickerBkg(datePicker);
        setTimepickerBkg(timePicker);

        ad = new AlertDialog.Builder(activity, R.style.MyDialogBkg)
                .setView(dateTimeLayout)
                .setTitle(initDateTime)
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        inputDate.setText(dateTime);
                    }
                })
                .setNeutralButton("清空", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        inputDate.setText("");
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();

        ad.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(activity.getResources().getColor(R.color.darkorange));
        ad.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(activity.getResources().getColor(R.color.darkorange));
        ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(activity.getResources().getColor(R.color.darkorange));

        onDateChanged(null, 0, 0, 0);
        return ad;
    }

    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        onDateChanged(null, 0, 0, 0);
    }

    public void onDateChanged(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
        // 获得日历实例
        Calendar calendar = Calendar.getInstance();

        calendar.set(datePicker.getYear(), datePicker.getMonth(),
                datePicker.getDayOfMonth(), timePicker.getCurrentHour(),
                timePicker.getCurrentMinute(),0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //必须格式化成这个格式才成使用数据库查询

        dateTime = sdf.format(calendar.getTime());
        ad.setTitle(dateTime);
    }

    /**
     * 实现将初始日期时间2012年07月02日 16:45 拆分成年 月 日 时 分 秒,并赋值给calendar
     *
     * @param initDateTime
     *            初始日期时间值 字符串型
     * @return Calendar
     */
    private Calendar getCalendarByInintData(String initDateTime) {
        Calendar calendar = Calendar.getInstance();

        //将传入的yyyy-MM-dd HH:mm:ss格式转化为yyyy年MM月dd日 HH:mm:ss格式
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateTmp = null;
        try {
            dateTmp = (Date) simpleDateFormat.parse(initDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        String strDate = simpleDateFormat.format(dateTmp);
        //UtilBaseLog.printLog(strDate);


        // 将初始日期时间2012年07月02日 16:45 拆分成年 月 日 时 分 秒
        String date = spliteString(strDate, "日", "index", "front"); // 日期
        String time = spliteString(strDate, "日", "index", "back"); // 时间

        String yearStr = spliteString(date, "年", "index", "front"); // 年份
        String monthAndDay = spliteString(date, "年", "index", "back"); // 月日

        String monthStr = spliteString(monthAndDay, "月", "index", "front"); // 月
        String dayStr = spliteString(monthAndDay, "月", "index", "back"); // 日

        String hourStr = spliteString(time, ":", "index", "front"); // 时
        String minuteStr = spliteString(time, ":", "index", "back"); // 分
        //String secondsStr = spliteString(time, ":", "front", "index"); // 秒
        //由于TimePicker不支持秒，所以就不提取了

        int currentYear = Integer.valueOf(yearStr.trim()).intValue();
        int currentMonth = Integer.valueOf(monthStr.trim()).intValue() - 1;
        int currentDay = Integer.valueOf(dayStr.trim()).intValue();
        int currentHour = Integer.valueOf(hourStr.trim()).intValue();
        int currentMinute = Integer.valueOf(minuteStr.trim()).intValue();
        //int currentSecond = Integer.valueOf(secondsStr.trim()).intValue();

        calendar.set(currentYear, currentMonth, currentDay, currentHour,
                currentMinute);
        return calendar;
    }

    /**
     * 截取子串
     *
     * @param srcStr
     *            源串
     * @param pattern
     *            匹配模式
     * @param indexOrLast
     * @param frontOrBack
     * @return
     */
    public static String spliteString(String srcStr, String pattern,
                                      String indexOrLast, String frontOrBack) {
        String result = "";
        int loc = -1;
        if (indexOrLast.equalsIgnoreCase("index")) {
            loc = srcStr.indexOf(pattern); // 取得字符串第一次出现的位置
        } else {
            loc = srcStr.lastIndexOf(pattern); // 最后一个匹配串的位置
        }
        if (frontOrBack.equalsIgnoreCase("front")) {
            if (loc != -1)
                result = srcStr.substring(0, loc); // 截取子串
        } else {
            if (loc != -1)
                result = srcStr.substring(loc + 1, srcStr.length()); // 截取子串
        }
        return result;
    }

    /**
     * 设置时间选择器的分割线颜色
     *
     * @param datePicker
     */
    private void setDatePickerBkg(DatePicker datePicker) {
        // Divider changing:

        // 获取 mSpinners
        LinearLayout llFirst = (LinearLayout) datePicker.getChildAt(0);

        // 获取 NumberPicker
        LinearLayout mSpinners = (LinearLayout) llFirst.getChildAt(0);
        for (int i = 0; i < mSpinners.getChildCount(); i++) {
            NumberPicker picker = (NumberPicker) mSpinners.getChildAt(i);

            Field[] pickerFields = NumberPicker.class.getDeclaredFields();
            for (Field pf : pickerFields) {
                if (pf.getName().equals("mSelectionDivider")) {
                    pf.setAccessible(true);
                    try {
                        pf.set(picker, new ColorDrawable(activity.getResources().getColor(R.color.darkgrey)));//设置分割线颜色
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    //break;
                }
                if (pf.getName().equals("mSelectionDividerHeight")) {   //设置高度
                    pf.setAccessible(true);
                    try {
                        int result = 2;  //要设置的高度
                        pf.set(picker, result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                picker.invalidate();
            }
        }
    }

    private void setTimepickerBkg(TimePicker time_picker){
        Resources system = Resources.getSystem();
        int hour_numberpicker_id = system.getIdentifier("hour", "id", "android");
        int minute_numberpicker_id = system.getIdentifier("minute", "id", "android");
        //int ampm_numberpicker_id = system.getIdentifier("amPm", "id", "android");

        NumberPicker hour_numberpicker = (NumberPicker) time_picker.findViewById(hour_numberpicker_id);
        NumberPicker minute_numberpicker = (NumberPicker) time_picker.findViewById(minute_numberpicker_id);
        //NumberPicker ampm_numberpicker = (NumberPicker) time_picker.findViewById(ampm_numberpicker_id);

        setNumberPickerDividerColor(hour_numberpicker);
        setNumberPickerDividerColor(minute_numberpicker);
        //set_numberpicker_text_colour(ampm_numberpicker);
    }


    private void setNumberPickerDividerColor(NumberPicker numberPicker) {
        NumberPicker picker = numberPicker;
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    //设置分割线的颜色值
                    pf.set(picker, new ColorDrawable(activity.getResources().getColor(R.color.darkgrey)));
                } catch (IllegalArgumentException | Resources.NotFoundException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                //break;
            }

            if (pf.getName().equals("mSelectionDividerHeight")) {   //设置高度
                pf.setAccessible(true);
                try {
                    int result = 1;  //要设置的高度
                    pf.set(picker, result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            picker.invalidate();
        }
    }

}

