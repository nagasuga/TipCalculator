package com.diaero.tipcalculator;

import java.text.DecimalFormat;

public class Calculator {
    private static final String TAG = "MainActivity";
    private static Calculator instance = null;
    private static TipData data;
    private static DecimalFormat decFormat;

    protected Calculator() {
        // Exists only to defeat instantiation.
    }

    public static Calculator getInstance() {
        if (instance == null) {
            instance = new Calculator();
            data = new TipData();
            decFormat = new DecimalFormat("#.##");
        }
        return instance;
    }

    public TipData calculateFromBill(double bill, int people, int tipPercent) {
        data.bill = bill;
        data.people = people;
        data.tipPercent = tipPercent;

        data.tip = getTipAmount(bill, tipPercent);
        data.total = bill + data.tip;

        data.eachBill = bill / people;
        data.eachTip = data.tip / people;
        data.eachTotal = data.eachBill + data.eachTip;

        return data;
    }

    private Double getTipAmount(double bill, int tipPercent) {
        return Double.valueOf(decFormat.format(bill * (tipPercent / 100.0)));
    }

    private Double getBill(double total, int tipPercent) {
        return Double.valueOf(decFormat.format(total / (1 + (tipPercent / 100.0))));
    }
}
