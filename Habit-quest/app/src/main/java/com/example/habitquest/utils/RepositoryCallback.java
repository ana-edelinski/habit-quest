package com.example.habitquest.utils;

/**
 * Generički callback interfejs za asinhrone operacije (Firebase, mreža, sl.).
 * Omogućava da se odvoji success/failure logika od repozitorijuma.
 */
public interface RepositoryCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception e);
}
