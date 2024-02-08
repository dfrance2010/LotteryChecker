package com.zybooks.lotterychecker;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;
import java.util.List;

public class Drawing {
    private String date;
    private List<Integer> numbers;
    private int powerMegaBall;
    private String jackpot;
    private boolean mega;

    public Drawing() {
    }

    public Drawing(String date, List<Integer> numbers, int megaball,
                   String jackpot, boolean mega) {
        this.date = date;
        this.numbers = numbers;
        this.powerMegaBall = megaball;
        this.jackpot = jackpot;
        this.mega = mega;
    }

    public Drawing(List<Integer> numbers, int powerMegaBall, boolean mega) {
        date = "";
        this.numbers = numbers;
        this.powerMegaBall = powerMegaBall;
        jackpot = "";
        this.mega = mega;
    }

    public String getDate() {
        return this.date;
    }

    public List<Integer> getNumbers() {
        return this.numbers;
    }

    public int getPowerMegaBall() {
        return this.powerMegaBall;
    }

    public String getJackpot() {
        return this.jackpot;
    }
    public boolean getMega() {return this.mega;}

    public String convertJackpot(String jackpotIn) {
        String jackpotOut = jackpotIn.replace("$", "");
        String unit;

        try {
            unit = jackpotOut.substring(jackpotOut.length() - 7);
            jackpotOut = jackpotOut.replace(unit, "");
        } catch (StringIndexOutOfBoundsException e) {
            return jackpotIn;
        }

        double jackpotDouble = jackpotToNumber(jackpotOut, unit);
        DecimalFormat df = new DecimalFormat("###,###,###,###");

        return "$" + df.format(jackpotDouble);
    }

    public double jackpotToNumber(String jackpotIn, String unit) {
        double jackpotDouble = Double.parseDouble(jackpotIn);

        jackpotDouble *= 0.63;

        if (unit.equals("Million")) {
            jackpotDouble *= 1000000;
        } else {
            jackpotDouble *= 1000000000;
        }

        return jackpotDouble;
    }

    @NonNull
    public String toString() {
        return "Next Jackpot: " + convertJackpot(jackpot) + "\n\nNumbers for " + date + ":\n"
                + numbers.get(0) + " " + numbers.get(1) + " " + numbers.get(2) + " "
                + numbers.get(3) + " " +numbers.get(4)+ " " + powerMegaBall;
    }
}
