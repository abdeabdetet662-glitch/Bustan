package com.bustan.game;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class LeaderboardActivity extends Activity {

    private FirebaseManager fm;
    private LinearLayout listContainer;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        fm = FirebaseManager.get();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0D0D0D"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 50);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("🏆 المتصدرون");
        title.setTextColor(Color.parseColor("#FFD54F"));
        title.setTextSize(32);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 10);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("أفضل 30 لاعباً في بُستان");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(14);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 40);
        root.addView(sub);

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(listContainer);

        setContentView(scroll);

        fm.signIn(new FirebaseManager.OnDone() {
            @Override public void onSuccess() { startListener(); }
            @Override public void onError(String msg) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }

    private void startListener() {
        reg = fm.listenTopScores(list -> runOnUiThread(() -> render(list)));
    }

    private void render(List<FirebaseManager.ScoreEntry> list) {
        listContainer.removeAllViews();

        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("لا يوجد لاعبون بعد.\nكن الأول!");
            empty.setTextColor(Color.parseColor("#9E9E9E"));
            empty.setTextSize(16);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 60, 0, 0);
            listContainer.addView(empty);
            return;
        }

        String myUid = fm.getUid();
        for (int i = 0; i < list.size(); i++) {
            FirebaseManager.ScoreEntry e = list.get(i);
            boolean isMe = e.uid != null && e.uid.equals(myUid);
            listContainer.addView(buildRow(i + 1, e, isMe));
        }
    }

    private View buildRow(int rank, FirebaseManager.ScoreEntry e, boolean isMe) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(24, 20, 24, 20);

        int bgColor;
        if (isMe) bgColor = Color.parseColor("#0B4F2C");
        else if (rank == 1) bgColor = Color.parseColor("#3E2723");
        else if (rank == 2) bgColor = Color.parseColor("#37474F");
        else if (rank == 3) bgColor = Color.parseColor("#4E342E");
        else bgColor = Color.parseColor("#1A1A1A");

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(20);
        row.setBackground(bg);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        row.setLayoutParams(lp);

        TextView rankView = new TextView(this);
        String rankText;
        if (rank == 1) rankText = "🥇";
        else if (rank == 2) rankText = "🥈";
        else if (rank == 3) rankText = "🥉";
        else rankText = String.valueOf(rank);
        rankView.setText(rankText);
        rankView.setTextColor(Color.WHITE);
        rankView.setTextSize(rank <= 3 ? 34 : 22);
        rankView.setTypeface(null, Typeface.BOLD);
        rankView.setGravity(Gravity.CENTER);
        rankView.setMinWidth(80);
        row.addView(rankView);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        info.setPadding(20, 0, 20, 0);

        TextView nameView = new TextView(this);
        String name = e.name != null ? e.name : "لاعب";
        if (isMe) name += "  (أنت)";
        nameView.setText(name);
        nameView.setTextColor(Color.WHITE);
        nameView.setTextSize(18);
        nameView.setTypeface(null, Typeface.BOLD);
        info.addView(nameView);

        TextView label = new TextView(this);
        label.setText("نقاط");
        label.setTextColor(Color.parseColor("#9E9E9E"));
        label.setTextSize(11);
        info.addView(label);

        row.addView(info);

        TextView scoreView = new TextView(this);
        scoreView.setText(String.valueOf(e.score));
        scoreView.setTextColor(Color.parseColor("#FFD54F"));
        scoreView.setTextSize(24);
        scoreView.setTypeface(null, Typeface.BOLD);
        row.addView(scoreView);

        return row;
    }
}
