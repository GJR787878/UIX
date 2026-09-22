package io.github.gjr787878.uix;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gjr.glassbutton.GlassCapsuleButton;

public class SubActivity extends AppCompatActivity {
    private SharedPreferences sp;
    private float density;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        density = getResources().getDisplayMetrics().density;
        sp = getSharedPreferences("uix", MODE_PRIVATE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        // 顶部 48dp 让位状态栏（§3.7）
        root.setPadding(Math.round(24 * density), Math.round(48 * density),
                Math.round(24 * density), Math.round(32 * density));

        // 自绘返回按钮（§3.7）
        TextView backBtn = new TextView(this);
        backBtn.setText("← 返回");
        backBtn.setTextColor(Color.WHITE);
        backBtn.setTextSize(16);
        backBtn.setPadding(0, 0, 0, Math.round(24 * density));
        backBtn.setOnClickListener(v -> finish());
        root.addView(backBtn);

        // 标题
        TextView title = new TextView(this);
        title.setText("二级界面 · 三个选项");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setPadding(0, 0, 0, Math.round(32 * density));
        root.addView(title);

        // 三个子选项开关
        for (int i = 1; i <= 3; i++) {
            final int idx = i;
            TextView label = new TextView(this);
            label.setText("子选项" + idx);
            label.setTextColor(0xFFCCCCCC);
            label.setTextSize(14);
            label.setPadding(0, Math.round(12 * density), 0, Math.round(8 * density));
            root.addView(label);

            GlassCapsuleButton btn = new GlassCapsuleButton(this);
            boolean on = sp.getBoolean("sub_" + idx, false);
            btn.setText(on ? "开" : "关");
            btn.setGlassSelected(on);
            btn.setOnClickListener(v -> {
                boolean now = !sp.getBoolean("sub_" + idx, false);
                sp.edit().putBoolean("sub_" + idx, now).apply();
                ((GlassCapsuleButton) v).setText(now ? "开" : "关");
                ((GlassCapsuleButton) v).setGlassSelected(now);
                Toast.makeText(this, "子选项" + idx + ": " + (now ? "开" : "关"), Toast.LENGTH_SHORT).show();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = Math.round(12 * density);
            root.addView(btn, lp);
        }

        setContentView(root);
    }
}
