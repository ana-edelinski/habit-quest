package com.example.habitquest.presentation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habitquest.data.repositories.CategoryRepository;
import com.example.habitquest.domain.model.Category;
import com.example.habitquest.utils.RepositoryCallback;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public class CategoryViewModel extends AndroidViewModel {

    private final CategoryRepository repository;
    private final MutableLiveData<List<Category>> _categories = new MutableLiveData<>();
    public LiveData<List<Category>> categories = _categories;

    private Closeable listenerHandle;

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        repository = new CategoryRepository(application);
    }

    /** Počni da slušaš kategorije u realnom vremenu */
    public void startListening(String firebaseUid, String localUserId) {
        // ako već postoji listener, zatvori ga
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
                // ovde možeš logovati ili postaviti null/error state
            }
        });
    }

    public void createCategory(String firebaseUid, String localUserId, String name, String colorHex) {
        repository.create(firebaseUid, localUserId, name, colorHex, new RepositoryCallback<Category>() {
            @Override
            public void onSuccess(Category result) {
                // ovde možeš da obavestiš UI, npr. Toast ili LiveData success state
            }

            @Override
            public void onFailure(Exception e) {
                // ovde možeš da obavestiš UI o grešci
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
