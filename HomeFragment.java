package com.zybooks.lotterychecker;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HomeFragment extends Fragment {
    String date;
    private static final String CHANNEL_ID = "LCNC";
    private MaterialTimePicker timePicker;
    private Calendar calendar;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Button setAlarm;
    private static final String PAST_POWERBALL_PAGE = "https://www.texaslottery.com/export/sites/lottery/Games/Powerball/Winning_Numbers/";
    private static final String PAST_MEGAMILLION_PAGE = "https://www.lotteryusa.com/mega-millions/year";
    private static final String POWERBALL_PAGE = "https://www.lotteryusa.com/powerball/";
    private static final String MEGAMILLION_PAGE = "https://www.lotteryusa.com/mega-millions/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        createNotificationChannel();

        LotteryDB lotteryDB = LotteryDB.getInstance(getContext());

        TextView pbTickets = view.findViewById(R.id.powerball_tickets);
        TextView megaTickets = view.findViewById(R.id.megamillion_tickets);
        TextView powerballNumbers = view.findViewById(R.id.powerball_numbers);
        TextView megamillionNumbers = view.findViewById(R.id.megamillion_numbers);

        calendar = Calendar.getInstance();
        setAlarm = view.findViewById(R.id.alarm);
        setAlarm.setOnClickListener(l -> {
            timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select Time")
                    .build();

            timePicker.show(getActivity().getSupportFragmentManager(), "Tag");
            timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                    calendar.set(Calendar.MINUTE, timePicker.getMinute());
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                }
            });

        });
        setAlarm(calendar.getTimeInMillis());

        StringBuilder powerString = new StringBuilder();
        StringBuilder megaString = new StringBuilder();
        List<Ticket> tickets = lotteryDB.getTickets(true);
        for (Ticket ticket:tickets) {
            if (ticket.getMega()) {
                megaString.append(ticket).append("\n");
            } else {
                powerString.append(ticket).append("\n");
            }
        }
        pbTickets.setText(powerString);
        megaTickets.setText(megaString);

        StringBuilder powerDrawingString = new StringBuilder();
        StringBuilder megaDrawingString = new StringBuilder();
        List<Drawing> drawings = lotteryDB.getDrawings(true);
        for (Drawing drawing: drawings) {
            if (drawing.getMega()) {
                megaDrawingString.append(drawing).append("\n");
            } else {
                powerDrawingString.append(drawing).append("\n");
            }
        }
        powerballNumbers.setText(powerDrawingString);
        megamillionNumbers.setText(megaDrawingString);
        TextView drawingView = view.findViewById(R.id.drawingDB);
        Button updateButton = view.findViewById(R.id.update_button);
        updateButton.setOnClickListener(l -> {
            ExecutorService powerballExecutor = Executors.newFixedThreadPool(3);
            Handler powerballHandler = new Handler(Looper.getMainLooper());
            powerballExecutor.execute(() -> {
                org.jsoup.nodes.Document doc;
                org.jsoup.nodes.Document doc2;
                // Get Powerball data
                try {
                    doc = Jsoup.connect(POWERBALL_PAGE).get();
                    doc2 = Jsoup.connect(PAST_POWERBALL_PAGE).get();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Drawing powerballDrawing = getNumbers(doc, false);
                Elements pbElements = doc2.select("td");

                // Get Megamillion data
                try {
                    doc = Jsoup.connect(MEGAMILLION_PAGE).get();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Drawing megamillionDrawing = getNumbers(doc, true);

                powerballHandler.post(() -> {
                    powerballNumbers.setText(powerballDrawing.toString());
                    long result = lotteryDB.addDrawing(powerballDrawing);
                    if (result > 0) {Toast.makeText(getContext(), "Result added", Toast.LENGTH_SHORT).show();}
                    else {Toast.makeText(getContext(), "Result already exists", Toast.LENGTH_SHORT).show();}

                    megamillionNumbers.setText(megamillionDrawing.toString());
                    result = lotteryDB.addDrawing(megamillionDrawing);
                    if (result > 0) {Toast.makeText(getContext(), "Result added", Toast.LENGTH_SHORT).show();}
                    else {Toast.makeText(getContext(), "Result already exists", Toast.LENGTH_SHORT).show();}

                });
            });

            powerballExecutor.shutdown();


        });


        Button showDrawings = view.findViewById(R.id.showDrawings);
        showDrawings.setOnClickListener(l -> {
            List<Drawing> drawingList = lotteryDB.getDrawings(false);
            StringBuilder drawingString = new StringBuilder();
            for (Drawing drawing: drawingList) {
                drawingString.append(drawing.toString()).append("\n");
            }
            drawingView.setText(lotteryDB.getPastDrawings());

        });

        Button clearTable = view.findViewById(R.id.clearTable);
        clearTable.setOnClickListener(l -> lotteryDB.clearDrawings());

        return view;
    }

    public Drawing getNumbers(org.jsoup.nodes.Document doc, boolean mega) {
        List<Integer> pballNumbers = new ArrayList<>();
        int pballBall = 0;
        String pballDate = "";
        String pballJackpot = "";
        Elements pbContent = doc.getElementsByClass("c-ball c-ball--default c-result__item");
        Elements powerball;
        if (mega) {
            powerball = doc.getElementsByClass("c-ball   c-ball--yellow c-ball--");
        } else {
            powerball = doc.getElementsByClass("c-ball   c-ball--red c-ball--");
        }
        Elements pbDate = doc.getElementsByClass("c-result-card__title");
        Elements pbJackpot = doc.getElementsByClass("c-next-draw-card__cash-prize-value");
        for (int i = 0; i < 5; ++i) {
            Element content = pbContent.get(i);
            pballNumbers.add(Integer.parseInt(content.text()));
        }
        if (powerball.first() != null) {pballBall = Integer.parseInt(powerball.first().text());}
        if (pbDate.first() != null) {pballDate = pbDate.first().text();}
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("eeee, MMM dd, yyyy");
        LocalDate pballLocalDate = LocalDate.parse(pballDate, inputFormatter);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        pballDate = pballLocalDate.format(dateFormatter);



        if (pbJackpot.first() != null) {pballJackpot = pbJackpot.first().text();}

        return new Drawing(pballDate, pballNumbers, pballBall, pballJackpot, mega);
    }


    private void setAlarm(long time) {
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(getContext(), Alarm.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}