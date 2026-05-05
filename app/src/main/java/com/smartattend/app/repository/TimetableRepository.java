package com.smartattend.app.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smartattend.app.model.ScheduleItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimetableRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<List<ScheduleItem>> getTimetable(String batchId) {
        return db.collection("timetables").document(batchId)
                .get()
                .continueWith(task -> {
                    List<ScheduleItem> list = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        List<Map<String, Object>> scheduleMaps = (List<Map<String, Object>>) doc.get("schedule");
                        
                        if (scheduleMaps != null) {
                            for (Map<String, Object> map : scheduleMaps) {
                                ScheduleItem item = new ScheduleItem(
                                        (String) map.get("day"),
                                        (String) map.get("time"),
                                        (String) map.get("subject"),
                                        (String) map.get("room"),
                                        (String) map.get("type")
                                );
                                list.add(item);
                            }
                        }
                    }
                    return list;
                });
    }

    public void seedDatabase(android.content.Context context) {
        String batchId = "CSE_IV_SEM_I";
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("batchName", "2024 Batch - IV Sem - CSE I");
        data.put("lastUpdated", System.currentTimeMillis());

        List<java.util.Map<String, String>> schedule = new ArrayList<>();
        
        // MONDAY
        schedule.add(createSlot("MON", "11:40 AM", "GDPI", "NB 106", "Lecture"));
        schedule.add(createSlot("MON", "12:30 PM", "GDPI", "NB 106", "Lecture"));
        schedule.add(createSlot("MON", "03:00 PM", "DAA LAB", "NB 209", "Lab"));
        schedule.add(createSlot("MON", "03:50 PM", "DAA LAB", "NB 209", "Lab"));

        // TUESDAY
        schedule.add(createSlot("TUE", "09:10 AM", "Minor", "-", "Lecture"));
        schedule.add(createSlot("TUE", "10:00 AM", "Minor", "-", "Lecture"));
        schedule.add(createSlot("TUE", "10:50 AM", "MAD", "NB 102", "Lecture"));
        schedule.add(createSlot("TUE", "11:40 AM", "MAD", "NB 102", "Lecture"));
        schedule.add(createSlot("TUE", "12:30 PM", "OP. SYS.", "NB 214", "Lecture"));
        schedule.add(createSlot("TUE", "02:10 PM", "DCCN LAB", "NB 209", "Lab"));
        schedule.add(createSlot("TUE", "03:00 PM", "DCCN LAB", "NB 209", "Lab"));

        // WEDNESDAY
        schedule.add(createSlot("WED", "09:10 AM", "CTCS", "NB 105", "Lecture"));
        schedule.add(createSlot("WED", "10:00 AM", "CTCS", "NB 105", "Lecture"));
        schedule.add(createSlot("WED", "10:50 AM", "DAA", "NB 106", "Lecture"));
        schedule.add(createSlot("WED", "11:40 AM", "DBMS", "NB 106", "Lecture"));
        schedule.add(createSlot("WED", "12:30 PM", "OP. SYS.", "NB 214", "Lecture"));

        // THURSDAY
        schedule.add(createSlot("THU", "10:50 AM", "DCCN", "NB 214", "Lecture"));
        schedule.add(createSlot("THU", "11:40 AM", "DCCN", "NB 214", "Lecture"));
        schedule.add(createSlot("THU", "02:10 PM", "DBMS LAB", "NB 209", "Lab"));
        schedule.add(createSlot("THU", "03:00 PM", "DBMS LAB", "NB 209", "Lab"));
        schedule.add(createSlot("THU", "04:40 PM", "Minor", "-", "Lecture"));

        // FRIDAY
        schedule.add(createSlot("FRI", "10:00 AM", "DBMS", "NB 106", "Lecture"));
        schedule.add(createSlot("FRI", "10:50 AM", "DAA", "NB 106", "Lecture"));
        schedule.add(createSlot("FRI", "01:20 PM", "MAD", "NB 105", "Lecture"));
        schedule.add(createSlot("FRI", "02:10 PM", "OP. SYS.", "NB 209", "Lecture"));
        schedule.add(createSlot("FRI", "03:00 PM", "OP. SYS.", "NB 209", "Lecture"));

        data.put("schedule", schedule);
        db.collection("timetables").document(batchId).set(data)
            .addOnSuccessListener(aVoid -> {
                android.util.Log.d("Firestore", "Timetable Seeded Successfully!");
                android.widget.Toast.makeText(context, "Timetable Uploaded!", android.widget.Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("Firestore", "Error seeding timetable: " + e.getMessage());
                android.widget.Toast.makeText(context, "Upload Failed: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            });
    }

    private java.util.Map<String, String> createSlot(String day, String time, String sub, String room, String type) {
        java.util.Map<String, String> slot = new java.util.HashMap<>();
        slot.put("day", day);
        slot.put("time", time);
        slot.put("subject", sub);
        slot.put("room", room);
        slot.put("type", type);
        return slot;
    }
}
