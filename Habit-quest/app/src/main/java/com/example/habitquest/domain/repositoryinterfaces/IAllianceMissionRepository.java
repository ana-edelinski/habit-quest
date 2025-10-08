package com.example.habitquest.domain.repositoryinterfaces;

import com.example.habitquest.domain.model.AllianceMission;
import com.example.habitquest.domain.model.MissionAction;
import com.example.habitquest.utils.RepositoryCallback;

import java.util.List;

public interface IAllianceMissionRepository {
    /** Kreira novu misiju za dati savez */
    void createMission(String allianceId, List<String> memberIds, RepositoryCallback<AllianceMission> callback);

    /** Ažurira napredak člana na osnovu akcije (npr. kupovina, zadatak...) */
    void updateMemberProgress(String missionId, String userId, MissionAction action, RepositoryCallback<Void> callback);

    /** Prati promene na misiji u realnom vremenu */
    void listenMission(String missionId, RepositoryCallback<AllianceMission> callback);

    /** Dohvata aktivnu misiju za savez */
    void getActiveMission(String allianceId, RepositoryCallback<AllianceMission> callback);

    /** Označava kraj misije — dodeljuje nagrade i resetuje status saveza */
    void finishMission(String missionId, boolean victory, long remainingHP, RepositoryCallback<Void> callback);
    void getLastFinishedMission(String allianceId, RepositoryCallback<AllianceMission> callback);
}
