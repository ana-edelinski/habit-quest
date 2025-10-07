package com.example.habitquest.domain.model;

public enum MissionAction {
    SHOP_PURCHASE,     // kupovina u prodavnici (max 5 puta)
    BOSS_HIT,          // uspešan udarac bosa (max 10 puta)
    EASY_TASK,         // rešavanje veoma lakih / lakih / normalnih / važnih zadataka (max 10)
    HARD_TASK,         // rešavanje težih zadataka (max 6)
    NO_FAILED_TASKS,   // nema nerešenih zadataka tokom misije (1x)
    MESSAGE_SENT       // poslata poruka u savezu (po danu)


}
