package com.example.ZenWake.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zenwake.R;
import com.example.ZenWake.models.Alarm;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private List<Alarm> alarms;
    private OnAlarmClickListener listener;

    public interface OnAlarmClickListener {
        void onAlarmClick(Alarm alarm);
        void onToggleAlarm(Alarm alarm, boolean isEnabled);
        void onDeleteAlarm(Alarm alarm);
    }

    public AlarmAdapter(List<Alarm> alarms, OnAlarmClickListener listener) {
        this.alarms = alarms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarm = alarms.get(position);
        holder.bind(alarm, listener);
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        private TextView timeText;
        private TextView amPmText;
        private TextView repeatText;
        private TextView challengeText;
        private ImageButton deleteButton;
        private SwitchCompat alarmSwitch;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.timeText);
            amPmText = itemView.findViewById(R.id.amPmText);
            repeatText = itemView.findViewById(R.id.repeatText);
            challengeText = itemView.findViewById(R.id.challengeText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            alarmSwitch = itemView.findViewById(R.id.alarmSwitch);
        }

        public void bind(final Alarm alarm, final OnAlarmClickListener listener) {
            // Set time
            String time = String.format("%d:%02d",
                    alarm.getHour() > 12 ? alarm.getHour() - 12 : alarm.getHour(),
                    alarm.getMinute());
            timeText.setText(time);

            // Set AM/PM
            amPmText.setText(alarm.getHour() >= 12 ? "PM" : "AM");

            // Set repeat days
            repeatText.setText(alarm.getRepeatingDaysDisplay());

            // Set challenge type - capitalize first letter
            String challenge = alarm.getChallengeType();
            if (challenge != null && !challenge.isEmpty()) {
                challenge = challenge.substring(0, 1).toUpperCase() + challenge.substring(1);
                challengeText.setText(challenge + " Challenge");
            } else {
                challengeText.setText("Math Challenge");
            }

            // Set switch state
            alarmSwitch.setChecked(alarm.isEnabled());

            // Click listeners
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onAlarmClick(alarm);
                }
            });

            alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    listener.onToggleAlarm(alarm, isChecked);
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDeleteAlarm(alarm);
                }
            });
        }
    }
}