package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.repositories.CategoryRepository;
import com.example.habitquest.domain.model.Category;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.utils.RepositoryCallback;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public class CategoryViewModel extends ViewModel {

    private final AppPreferences prefs;
    private final CategoryRepository repository;

    private final MutableLiveData<List<Category>> _categories = new MutableLiveData<>();
    public LiveData<List<Category>> categories = _categories;

    private Closeable listenerHandle;

    public CategoryViewModel(AppPreferences prefs, CategoryRepository repository) {
        this.prefs = prefs;
        this.repository = repository;
    }

    /** Počni da slušaš kategorije u realnom vremenu */
    public void startListening() {
        String firebaseUid = prefs.getFirebaseUid();
        long localUserId = Long.parseLong(prefs.getUserId());

        if (listenerHandle != null) {
            try { listenerHandle.close(); } catch (IOException ignored) {}
        }

        listenerHandle = repository.listenAll(firebaseUid, localUserId, new CategoryRepository.CategoriesListener() {
            @Override
            public void onChanged(List<Category> list) {
                _categories.postValue(list);
            }

            @Override
            public void onError(Exception e) {
                // ovde možeš logovati ili postaviti error state
            }
        });
    }

    public void createCategory(String name, String colorHex) {
        String firebaseUid = prefs.getFirebaseUid();
        long localUserId = Long.parseLong(prefs.getUserId());

        repository.create(firebaseUid, localUserId, name, colorHex, new RepositoryCallback<Category>() {
            @Override
            public void onSuccess(Category result) {
                // možeš emitovati neki LiveData<Event> da obavesti UI
            }

            @Override
            public void onFailure(Exception e) {
                // možeš emitovati LiveData<Error>
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerHandle != null) {
            try { listenerHandle.close(); } catch (IOException ignored) {}
        }
    }
}
