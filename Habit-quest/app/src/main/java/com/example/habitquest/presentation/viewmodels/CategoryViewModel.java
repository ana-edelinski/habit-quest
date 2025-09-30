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

    private final MutableLiveData<String> _message = new MutableLiveData<>();
    public LiveData<String> message = _message;


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

        repository.isColorAvailable(firebaseUid, colorHex, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean available) {
                if (available) {
                    repository.create(firebaseUid, localUserId, name, colorHex, new RepositoryCallback<Category>() {
                        @Override
                        public void onSuccess(Category result) {
                            _message.postValue("Category created successfully.");
                        }

                        @Override
                        public void onFailure(Exception e) {
                            _message.postValue("Error creating category: " + e.getMessage());
                        }
                    });
                } else {
                    _message.postValue("Color already taken. Choose another one.");
                }
            }

            @Override
            public void onFailure(Exception e) {
                _message.postValue("Error checking color availability: " + e.getMessage());
            }
        });
    }

    public void deleteCategory(Category category) {
        repository.delete(prefs.getFirebaseUid(),
                Long.parseLong(prefs.getUserId()),
                category.getId(),
                new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        _message.postValue("Category deleted successfully.");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (e instanceof IllegalStateException) {
                            _message.postValue(e.getMessage()); // "Cannot delete category: active tasks exist"
                        } else {
                            _message.postValue("Error deleting category: " + e.getMessage());
                        }
                    }
                });
    }


    public void updateCategory(Category category) {
        repository.isColorAvailable(prefs.getFirebaseUid(), category.getColorHex(),
                new RepositoryCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean available) {
                        if (available) {
                            repository.update(prefs.getFirebaseUid(), category, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    _message.postValue("Category updated successfully.");
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    _message.postValue("Error updating category: " + e.getMessage());
                                }
                            });
                        } else {
                            _message.postValue("Color already taken. Choose another one.");
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        _message.postValue("Error checking color availability: " + e.getMessage());
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
