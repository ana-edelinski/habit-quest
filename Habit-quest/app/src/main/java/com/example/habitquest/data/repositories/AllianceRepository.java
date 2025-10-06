package com.example.habitquest.data.repositories;

import android.content.Context;
import com.example.habitquest.data.remote.AllianceRemoteDataSource;
import com.example.habitquest.domain.model.Alliance;
import com.example.habitquest.utils.RepositoryCallback;

public class AllianceRepository {

    private final AllianceRemoteDataSource remoteDataSource;

    public AllianceRepository(Context context) {
        this.remoteDataSource = new AllianceRemoteDataSource();
    }

    public void createAlliance(Alliance alliance, RepositoryCallback<Void> callback) {
        remoteDataSource.createAlliance(alliance, callback);
    }
}
