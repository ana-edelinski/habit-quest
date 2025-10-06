package com.example.habitquest.data.repositories;

import android.content.Context;

import com.example.habitquest.data.remote.AllianceRemoteDataSource;
import com.example.habitquest.domain.model.Alliance;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AllianceRepository {

    private final AllianceRemoteDataSource remoteDataSource;
    private final Context context;

    public AllianceRepository(Context context) {
        this.context = context.getApplicationContext();
        this.remoteDataSource = new AllianceRemoteDataSource();
    }

    public void createAlliance(Alliance alliance, RepositoryCallback<Void> callback) {
        remoteDataSource.createAlliance(alliance, callback);
    }

    public void acceptAllianceInvite(String allianceId, RepositoryCallback<Void> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String userId = user.getUid();
        String username = user.getDisplayName() != null ? user.getDisplayName() : "User";

        remoteDataSource.acceptAllianceInvite(context, allianceId, userId, username, callback);
    }

    public void rejectAllianceInvite(String allianceId, RepositoryCallback<Void> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String userId = user.getUid();
        remoteDataSource.rejectAllianceInvite(allianceId, userId, callback);
    }
}
