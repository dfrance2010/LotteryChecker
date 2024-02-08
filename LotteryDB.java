package com.zybooks.lotterychecker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LotteryDB extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "lottery.db";
    private static final String POWERBALL_PAGE = "https://www.lotteryusa.com/powerball/year";
    private static final String MEGAMILLION_PAGE = "https://www.lotteryusa.com/mega-millions/year";
    private static LotteryDB instance;
    private String returnstring;
    private static final HashMap<String, Integer> POWERBALL_PAYOUT = new HashMap<>();
    static {
        POWERBALL_PAYOUT.put("0-0", 0);
        POWERBALL_PAYOUT.put("0-1", 4);
        POWERBALL_PAYOUT.put("1-0", 0);
        POWERBALL_PAYOUT.put("1-1", 4);
        POWERBALL_PAYOUT.put("2-0", 0);
        POWERBALL_PAYOUT.put("2-1", 7);
        POWERBALL_PAYOUT.put("3-0", 7);
        POWERBALL_PAYOUT.put("3-1", 100);
        POWERBALL_PAYOUT.put("4-0", 100);
        POWERBALL_PAYOUT.put("4-1", 50000);
        POWERBALL_PAYOUT.put("5-0", 1000000);
        POWERBALL_PAYOUT.put("5-1", 1);
    }

    static final HashMap<String, Integer> MEGAMILLION_PAYOUT = new HashMap<>();
    static {
        MEGAMILLION_PAYOUT.put("0-0", 0);
        MEGAMILLION_PAYOUT.put("0-1", 2);
        MEGAMILLION_PAYOUT.put("1-0", 0);
        MEGAMILLION_PAYOUT.put("1-1", 4);
        MEGAMILLION_PAYOUT.put("2-0", 0);
        MEGAMILLION_PAYOUT.put("2-1", 10);
        MEGAMILLION_PAYOUT.put("3-0", 10);
        MEGAMILLION_PAYOUT.put("3-1", 200);
        MEGAMILLION_PAYOUT.put("4-0", 500);
        MEGAMILLION_PAYOUT.put("4-1", 10000);
        MEGAMILLION_PAYOUT.put("5-0", 1000000);
        MEGAMILLION_PAYOUT.put("5-1", 1);
    }

    private LotteryDB(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    public static LotteryDB getInstance(Context context) {
        if (instance == null) {
            instance = new LotteryDB(context);
        }
        return instance;
    }

    private static final class TicketTable {
        private static final String TABLE = "lottery_table";
        private static final String COL_TICKET_ID = "ticket_id";
        private static final String COL_START_DATE = "start_date";
        private static final String COL_END_DATE = "end_date";
        private static final String COL_TYPE = "type";
        private static final String COL_NUM_1 = "number_1";
        private static final String COL_NUM_2 = "number_2";
        private static final String COL_NUM_3 = "number_3";
        private static final String COL_NUM_4 = "number_4";
        private static final String COL_NUM_5 = "number_5";
        private static final String COL_POWER_MEGA = "power_mega_ball";
        private static final String COL_WIN = "win";

    }

    private static final class DrawingTable {
        private static final String TABLE = "drawing_table";
        private static final String COL_DRAWING_ID = "drawing_id";
        private static final String COL_DATE = "date";
        private static final String COL_TYPE = "type";
        private static final String COL_NUM_1 = "number_1";
        private static final String COL_NUM_2 = "number_2";
        private static final String COL_NUM_3 = "number_3";
        private static final String COL_NUM_4 = "number_4";
        private static final String COL_NUM_5 = "number_5";
        private static final String COL_POWER_MEGA = "power_mega_ball";
        private static final String COL_JACKPOT = "jackpot";

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TicketTable.TABLE + "(" +
                TicketTable.COL_TICKET_ID + " integer primary key autoincrement, " +
                TicketTable.COL_START_DATE + " text, " +
                TicketTable.COL_END_DATE + " text," +
                TicketTable.COL_TYPE + " text," +
                TicketTable.COL_NUM_1+ " integer," +
                TicketTable.COL_NUM_2+ " integer," +
                TicketTable.COL_NUM_3+ " integer," +
                TicketTable.COL_NUM_4+ " integer," +
                TicketTable.COL_NUM_5+ " integer," +
                TicketTable.COL_POWER_MEGA+ " integer," +
                TicketTable.COL_WIN+ " integer);");

        db.execSQL("create table " + DrawingTable.TABLE + "(" +
                DrawingTable.COL_DRAWING_ID + " integer primary key autoincrement, " +
                DrawingTable.COL_DATE + " text, " +
                DrawingTable.COL_TYPE + " text," +
                DrawingTable.COL_NUM_1+ " integer," +
                DrawingTable.COL_NUM_2+ " integer," +
                DrawingTable.COL_NUM_3+ " integer," +
                DrawingTable.COL_NUM_4+ " integer," +
                DrawingTable.COL_NUM_5+ " integer," +
                DrawingTable.COL_POWER_MEGA+ " integer," +
                DrawingTable.COL_JACKPOT+ " text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists " + TicketTable.TABLE);
        db.execSQL("drop table if exists " + DrawingTable.TABLE);
        onCreate(db);
    }

    public long addTicket(Ticket ticket) {
        String type;
        if (ticket.getMega()) {
            type = "mega_ticket";
        } else {type = "power_ticket";}

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(TicketTable.COL_START_DATE, ticket.getStartDate());
        values.put(TicketTable.COL_END_DATE, ticket.getEndDate());
        values.put(TicketTable.COL_TYPE, type);
        values.put(TicketTable.COL_NUM_1, ticket.getNumbers()[0]);
        values.put(TicketTable.COL_NUM_2, ticket.getNumbers()[1]);
        values.put(TicketTable.COL_NUM_3, ticket.getNumbers()[2]);
        values.put(TicketTable.COL_NUM_4, ticket.getNumbers()[3]);
        values.put(TicketTable.COL_NUM_5, ticket.getNumbers()[4]);
        values.put(TicketTable.COL_POWER_MEGA, ticket.getPowerMegaBall());
        values.put(TicketTable.COL_WIN, 0);


        return db.insert(TicketTable.TABLE, null, values);
    }

    public long addDrawing(@NonNull Drawing drawing) {
        SQLiteDatabase dbw = getWritableDatabase();
        ContentValues values = new ContentValues();

        String type;
        if (drawing.getMega()) {type = "mega_drawing";} else {type = "power_drawing";}

        SQLiteDatabase dbr = getReadableDatabase();
        String sql = "SELECT " + DrawingTable.COL_DATE + ", " +
                DrawingTable.COL_JACKPOT + " FROM " + DrawingTable.TABLE +
                " WHERE " + DrawingTable.COL_TYPE + " = '" + type + "'" +
                "ORDER BY " + DrawingTable.COL_DRAWING_ID + " DESC LIMIT 1;";
        Cursor cursor = dbr.rawQuery(sql, new String[]{});

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            LocalDate lastDate;
            lastDate = LocalDate.parse(cursor.getString(0));
            String jackpot = cursor.getString(1);
            if (jackpot.isEmpty()) {
                values.put(DrawingTable.COL_JACKPOT, drawing.getJackpot());
                long result = dbw.update(DrawingTable.TABLE, values, DrawingTable.COL_DATE + "=?",
                        new String[] {drawing.getDate()});
                return result;
            }
            LocalDate currentDate = LocalDate.parse(drawing.getDate());
            if (currentDate.isBefore(lastDate) || currentDate.equals(lastDate)) {
                return -1;
            }
        }

        cursor.close();


        values.put(DrawingTable.COL_DATE, drawing.getDate());
        values.put(DrawingTable.COL_TYPE, type);
        values.put(DrawingTable.COL_NUM_1, drawing.getNumbers().get(0));
        values.put(DrawingTable.COL_NUM_2, drawing.getNumbers().get(1));
        values.put(DrawingTable.COL_NUM_3, drawing.getNumbers().get(2));
        values.put(DrawingTable.COL_NUM_4, drawing.getNumbers().get(3));
        values.put(DrawingTable.COL_NUM_5, drawing.getNumbers().get(4));
        values.put(DrawingTable.COL_POWER_MEGA, drawing.getPowerMegaBall());
        values.put(DrawingTable.COL_JACKPOT, drawing.getJackpot());

        return dbw.insert(DrawingTable.TABLE, null, values);
    }

    public List<Drawing> getDrawings(boolean current) {
        List<Drawing> drawings = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String sql;
        if (current) {
            sql = "SELECT * FROM " + DrawingTable.TABLE + " ORDER BY date DESC LIMIT 2;";
        } else {
            sql = "SELECT * FROM " + DrawingTable.TABLE + " ORDER BY date DESC;";
        }

        Cursor cursor = db.rawQuery(sql, new String[]{});

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String date = cursor.getString(1);
                String type = cursor.getString(2);
                List<Integer> numbers = new ArrayList<Integer>();
                numbers.add(cursor.getInt(3));
                numbers.add(cursor.getInt(4));
                numbers.add(cursor.getInt(5));
                numbers.add(cursor.getInt(6));
                numbers.add(cursor.getInt(7));
                int powerMegaBall = cursor.getInt(8);
                String jackpot = cursor.getString(9);
                boolean mega = !type.equals("power_drawing");
                Drawing drawing = new Drawing(date, numbers, powerMegaBall, jackpot, mega);
                drawings.add(drawing);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return drawings;
    }

    public List<Ticket> getTickets(boolean current) {
        List<Ticket> tickets = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String sql;
        if (current) {
            sql ="SELECT *, DATE() as date " +
                    "FROM " + TicketTable.TABLE +
                    " WHERE date <= DATE(" + TicketTable.COL_END_DATE + ", '+1 day');";
        } else {
            sql = "SELECT *, DATE() as date " +
                    "FROM " + TicketTable.TABLE +
                    " WHERE date > DATE(" + TicketTable.COL_END_DATE + ");";
        }

        Cursor cursor = db.rawQuery(sql, new String[]{});

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String startDate = cursor.getString(1);
                String endDate = cursor.getString(2);
                String type = cursor.getString(3);
                int[] numbers = {cursor.getInt(4), cursor.getInt(5), cursor.getInt(6),
                        cursor.getInt(7), cursor.getInt(8)};
                int powerMegaBall = cursor.getInt(9);
                int win = cursor.getInt(10);
                boolean mega = !type.equals("power_ticket");
                Ticket ticket = new Ticket(id, startDate, endDate, numbers, powerMegaBall, mega, win);
                tickets.add(ticket);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return tickets;
    }

    public int checkTicket(Ticket ticket) {
        int win = 0;

        SQLiteDatabase dbr = getReadableDatabase();
        SQLiteDatabase dbw = getWritableDatabase();

        List<Drawing> drawings = new ArrayList<>();

        String sql = "SELECT " + DrawingTable.COL_NUM_1 + ", " +
                DrawingTable.COL_NUM_2 + ", " +
                DrawingTable.COL_NUM_3 + ", " +
                DrawingTable.COL_NUM_4 + ", " +
                DrawingTable.COL_NUM_5 + ", " +
                DrawingTable.COL_POWER_MEGA + ", " +
                DrawingTable.COL_TYPE + ", " +
                "DATE('" + ticket.getStartDate() + "') as start_date, " +
                "DATE('" + ticket.getEndDate() + "') as end_date" +
                " FROM " + DrawingTable.TABLE +
                " WHERE start_date <= DATE(" + DrawingTable.COL_DATE + ") AND " +
                "end_date >= DATE(" + DrawingTable.COL_DATE + ");";

        Cursor cursor = dbr.rawQuery(sql, new String[]{});
        if (cursor.moveToFirst()) {
            do {
                List<Integer> numbers = new ArrayList<>();
                numbers.add(cursor.getInt(0));
                numbers.add(cursor.getInt(1));
                numbers.add(cursor.getInt(2));
                numbers.add(cursor.getInt(3));
                numbers.add(cursor.getInt(4));
                int powerMegaBall = cursor.getInt(5);
                String type = cursor.getString(6);
                boolean mega = !type.equals("power_drawing");
                Drawing drawing = new Drawing(numbers, powerMegaBall, mega);
                drawings.add(drawing);
            } while (cursor.moveToNext());
        }
        cursor.close();

        for(Drawing drawing: drawings) {
            int numMatch = 0;
            int pmbMatch = 0;
            for (int i = 0; i < 5; ++i) {
                if (drawing.getNumbers().contains(ticket.getNumbers()[i])) {
                    numMatch++;
                }
                if (drawing.getPowerMegaBall() == ticket.getPowerMegaBall()) {
                    pmbMatch = 1;
                }
            }
            String mapKey = numMatch + "-" + pmbMatch;
            if (ticket.getMega()) {
                win += MEGAMILLION_PAYOUT.get(mapKey);
            } else {win += POWERBALL_PAYOUT.get(mapKey);}
        }

        ContentValues winValue = new ContentValues();
        winValue.put(TicketTable.COL_WIN, win);
        dbw.update(TicketTable.TABLE, winValue, TicketTable.COL_TICKET_ID + "=?",
                new String[] {String.valueOf(ticket.getId())});

        return win;
    }


    public void deleteTicket(int id) {
        SQLiteDatabase db = getWritableDatabase();

        String sql = "DELETE " + "FROM " + TicketTable.TABLE + " WHERE "
                + TicketTable.COL_TICKET_ID + " = " + id;

        db.execSQL(sql);
    }

    public void clearDrawings() {
        SQLiteDatabase db = getWritableDatabase();

        String sql = "DELETE " + "FROM " + DrawingTable.TABLE;

        db.execSQL(sql);
    }

    public String getPastDrawings() {

        List<Drawing> drawingList = new ArrayList<>();
        ExecutorService powerballExecutor = Executors.newFixedThreadPool(3);
        Handler powerballHandler = new Handler(Looper.getMainLooper());
        powerballExecutor.execute(() -> {
            org.jsoup.nodes.Document powerDoc;
            try {
                powerDoc = Jsoup.connect(POWERBALL_PAGE).get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Elements pbDate = powerDoc.getElementsByClass("c-result-card__title");
            returnstring = "dumb";
//        for (int i = 0; i < 10; ++i) {
//            newstring += pbDate.get(i).text() + "\n";
//        }
//            try {
//                megaDoc = Jsoup.connect(MEGAMILLION_PAGE).get();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            String megastring = getNumbers(megaDoc, true);

//            powerballHandler.post(() -> {
//                returnstring = "newstring";
//            });

        });

        powerballExecutor.shutdown();

        return returnstring;
    }

    private String getNumbers(org.jsoup.nodes.Document doc, boolean mega) {
//        List<Drawing> drawingList = new ArrayList<>();
//        List<Integer> pballNumbers = new ArrayList<>();
//        int pballBall = 0;
//        String pballDate = "";
//        Elements pbContent = doc.getElementsByClass("c-ball c-ball--default c-result__item");
//        Elements powerball;
//        if (mega) {
//            powerball = doc.getElementsByClass("c-ball   c-ball--yellow c-ball--");
//        } else {
//            powerball = doc.getElementsByClass("c-ball   c-ball--red c-ball--");
//        }
//        for (int i = 0; i < 5; ++i) {
//            Element content = pbContent.get(i);
//            pballNumbers.add(Integer.parseInt(content.text()));
//        }
//        if (powerball.first() != null) {pballBall = Integer.parseInt(powerball.first().text());}
//        if (pbDate.first() != null) {pballDate = pbDate.first().text();}
//        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("eeee, MMM dd, yyyy");
//        LocalDate pballLocalDate = LocalDate.parse(pballDate, inputFormatter);
//        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        pballDate = pballLocalDate.format(dateFormatter);

        Elements pbDate = doc.getElementsByClass("c-result-card__title");
        String newstring = "";
//        for (int i = 0; i < 10; ++i) {
//            newstring += pbDate.get(i).text() + "\n";
//        }
        newstring = pbDate.toString();

        return newstring;
    }
}
