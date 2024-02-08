package com.zybooks.lotterychecker;

import androidx.annotation.NonNull;

public class Ticket {
    private long id;
    private String startDate;
    private String endDate;
    private int[] numbers;
    private int powerMegaBall;
    private int win;
    private boolean mega;

    public Ticket() {}

    public Ticket(long id, String startDate, String endDate, int[] numbers, int powerMegaBall, boolean mega, int win) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numbers = numbers;
        this.powerMegaBall = powerMegaBall;
        this.mega = mega;
        this.win = win;
    }

    public Ticket (String startDate, String endDate, int[] numbers, int powerMegaBall, boolean mega, int win) {
        this(-1, startDate, endDate, numbers, powerMegaBall, mega, 0);
    }

    public Ticket(String startDate, String endDate, int[] numbers, int powerMegaBall, boolean mega) {
        this(-1, startDate, endDate, numbers, powerMegaBall, mega, 0);
    }


    public long getId() {return this.id;}
    public String getStartDate() {return this.startDate;}

    public String getEndDate() {return this.endDate;}

    public int[] getNumbers() {return this.numbers;}

    public int getPowerMegaBall() {return this.powerMegaBall;}

    public boolean getMega() {return this.mega;}

    public int getWin() {return win;}

    @NonNull
    public String toString() {
        return numbers[0] + " " + numbers[1] + " " + numbers[2] + " " + numbers[3]
                + " " + numbers[4] + " " + powerMegaBall + "| Ending: " + endDate
                + " | ID: " + id + " Win: " + win;
    }
}
