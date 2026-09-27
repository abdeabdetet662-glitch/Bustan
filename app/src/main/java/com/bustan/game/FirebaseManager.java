package com.bustan.game;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManager {

    private static FirebaseManager instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public interface OnDone {
        void onSuccess();
        void onError(String msg);
    }

    private FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static FirebaseManager get() {
        if (instance == null) instance = new FirebaseManager();
        return instance;
    }

    public void signIn(OnDone cb) {
        if (auth.getCurrentUser() != null) { cb.onSuccess(); return; }
        auth.signInAnonymously()
            .addOnSuccessListener(r -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public String getUid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
    }

    public void saveScore(String playerName, int score, OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", getUid());
        data.put("name", playerName);
        data.put("score", score);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("bustan_scores").document(getUid()).set(data)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public static class ScoreEntry {
        public String uid;
        public String name;
        public int score;
    }

    public interface ScoresListener {
        void onScores(List<ScoreEntry> list);
    }

    public ListenerRegistration listenTopScores(ScoresListener l) {
        return db.collection("bustan_scores")
            .orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                List<ScoreEntry> list = new ArrayList<>();
                for (QueryDocumentSnapshot d : snap) {
                    ScoreEntry s = new ScoreEntry();
                    s.uid = d.getId();
                    s.name = d.getString("name");
                    Long sc = d.getLong("score");
                    s.score = sc != null ? sc.intValue() : 0;
                    list.add(s);
                }
                l.onScores(list);
            });
    }
}
