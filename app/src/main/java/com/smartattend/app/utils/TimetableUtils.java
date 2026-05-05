package com.smartattend.app.utils;

import com.smartattend.app.model.ScheduleItem;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class TimetableUtils {

    public static List<ScheduleItem> getFullSchedule() {
        List<ScheduleItem> schedule = new ArrayList<>();
        // MON
        schedule.add(new ScheduleItem("MON", "11:40 AM", "GDPI", "NB 106", "Lecture"));
        schedule.add(new ScheduleItem("MON", "12:30 PM", "GDPI", "NB 106", "Lecture"));
        schedule.add(new ScheduleItem("MON", "01:20 PM", "Lunch", "-", ""));
        schedule.add(new ScheduleItem("MON", "03:00 PM", "DAA LAB", "NB 209", "Lab"));
        schedule.add(new ScheduleItem("MON", "03:50 PM", "DAA LAB", "NB 209", "Lab"));

        // TUE
        schedule.add(new ScheduleItem("TUE", "09:10 AM", "Minor", "-", "Lecture"));
        schedule.add(new ScheduleItem("TUE", "10:00 AM", "Minor", "-", "Lecture"));
        schedule.add(new ScheduleItem("TUE", "10:50 AM", "MAD", "NB 102", "Lecture"));
        schedule.add(new ScheduleItem("TUE", "11:40 AM", "MAD", "NB 102", "Lecture"));
        schedule.add(new ScheduleItem("TUE", "12:30 PM", "OP. SYS.", "NB 214", "Lecture"));
        schedule.add(new ScheduleItem("TUE", "01:20 PM", "Lunch", "-", ""));
        schedule.add(new ScheduleItem("TUE", "02:10 PM", "DCCN LAB", "NB 209", "Lab"));
        schedule.add(new ScheduleItem("TUE", "03:00 PM", "DCCN LAB", "NB 209", "Lab"));

        // WED
        schedule.add(new ScheduleItem("WED", "09:10 AM", "CTCS", "NB 105", "Lecture"));
        schedule.add(new ScheduleItem("WED", "10:00 AM", "CTCS", "NB 105", "Lecture"));
        schedule.add(new ScheduleItem("WED", "10:50 AM", "DAA", "NB 106", "Lecture"));
        schedule.add(new ScheduleItem("WED", "11:40 AM", "DBMS", "NB 106", "Lecture"));
        schedule.add(new ScheduleItem("WED", "12:30 PM", "OP. SYS.", "NB 214", "Lecture"));
        schedule.add(new ScheduleItem("WED", "01:20 PM", "Lunch", "-", ""));

        // THU
        schedule.add(new ScheduleItem("THU", "10:50 AM", "DCCN", "NB 214", "Lecture"));
        schedule.add(new ScheduleItem("THU", "11:40 AM", "DCCN", "NB 214", "Lecture"));
        schedule.add(new ScheduleItem("THU", "01:20 PM", "Lunch", "-", ""));
        schedule.add(new ScheduleItem("THU", "02:10 PM", "DBMS LAB", "NB 209", "Lab"));
        schedule.add(new ScheduleItem("THU", "03:00 PM", "DBMS LAB", "NB 209", "Lab"));
        schedule.add(new ScheduleItem("THU", "04:40 PM", "Minor", "-", "Lecture"));

        // FRI
        schedule.add(new ScheduleItem("FRI", "10:00 AM", "DBMS", "NB 106", "Lecture"));
        schedule.add(new ScheduleItem("FRI", "10:50 AM", "DAA", "NB 106", "Lecture"));
        schedule.add(new ScheduleItem("FRI", "12:30 PM", "Lunch", "-", ""));
        schedule.add(new ScheduleItem("FRI", "01:20 PM", "MAD", "NB 105", "Lecture"));
        schedule.add(new ScheduleItem("FRI", "02:10 PM", "OP. SYS.", "NB 209", "Lecture"));
        schedule.add(new ScheduleItem("FRI", "03:00 PM", "OP. SYS.", "NB 209", "Lecture"));
        
        return schedule;
    }

    public static List<ScheduleItem> getScheduleForDay(String day) {
        return getFullSchedule().stream()
                .filter(item -> item.getDay().equalsIgnoreCase(day))
                .collect(Collectors.toList());
    }

    public static String getCurrentDayString() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.MONDAY: return "MON";
            case Calendar.TUESDAY: return "TUE";
            case Calendar.WEDNESDAY: return "WED";
            case Calendar.THURSDAY: return "THU";
            case Calendar.FRIDAY: return "FRI";
            case Calendar.SATURDAY:
            case Calendar.SUNDAY:
            default: return "MON"; // Default to Monday for weekends in demo
        }
    }
}
