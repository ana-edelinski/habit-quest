package com.example.habitquest.domain.model;

public class ActiveEffects {
    private double permanentBonus;   // sabira se iz trajnih napitaka
    private double tempBonus;        // jednokratni napici (reset posle borbe)
    private double equipmentBonus;   // sabira se iz odeće
    private int equipmentBattlesLeft; // koliko borbi traje odeća

    public ActiveEffects() {
        permanentBonus = 0;
        tempBonus = 0;
        equipmentBonus = 0;
        equipmentBattlesLeft = 0;
    }

    // kada racunamo efektivni PP
    public int calculateEffectivePp(int basePp) {
        return (int) (basePp * (1 + permanentBonus + tempBonus + equipmentBonus));
    }

    public void addPermanentBonus(double bonus) {
        permanentBonus += bonus;
    }

    public void addTempBonus(double bonus) {
        tempBonus += bonus;
    }

    public void addEquipmentBonus(double bonus) {
        equipmentBonus += bonus;
        equipmentBattlesLeft = 2;
    }

    public void afterBattle() {
        // jednokratni efekti nestaju
        tempBonus = 0;

        // odeća traje 2 borbe
        if (equipmentBattlesLeft > 0) {
            equipmentBattlesLeft--;
            if (equipmentBattlesLeft == 0) {
                equipmentBonus = 0;
            }
        }
    }

    // GETTERI
    public double getPermanentBonus() { return permanentBonus; }
    public double getTempBonus() { return tempBonus; }
    public double getEquipmentBonus() { return equipmentBonus; }
    public int getEquipmentBattlesLeft() { return equipmentBattlesLeft; }
}

