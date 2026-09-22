package io.github.gjr787878.uix;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gjr.glassbutton.GlassNavBar;
import com.gjr.glassbutton.GlassCapsuleButton;
import com.gjr.glassbutton.GlassRadioButton;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sp;
    private FrameLayout root;
    private LinearLayout content;
    private ScrollView scrollView;
    private GlassNavBar nav;
    private float density;
    private boolean isTablet;

    private static final int TAB1 = 0;
    private static final int TAB2 = 1;
    private static final int TAB3 = 2;
    private int currentTab = TAB1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        density = getResources().getDisplayMetrics().density;
        sp = getSharedPreferences("uix", MODE_PRIVATE);

        // 检测平板（§3.4：smallestScreenWidthDp >= 600）
        Configuration config = getResources().getConfiguration();
        isTablet = config.smallestScreenWidthDp >= 600;

        buildLayout();
        setupNav();
        switchTab(TAB1);
    }

    private void buildLayout() {
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        scrollView = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        int padTop = Math.round(48 * density);
        int padLeft = Math.round(24 * density);
        int padRight = Math.round(24 * density);
        int padBottom;

        if (isTablet) {
            padLeft = Math.round(140 * density);
            padBottom = Math.round(32 * density);
        } else {
            padBottom = Math.round(140 * density);
        }
        content.setPadding(padLeft, padTop, padRight, padBottom);
        scrollView.addView(content);
        root.addView(scrollView);

        setContentView(root);
    }

    private void setupNav() {
        nav = new GlassNavBar(this);
        nav.addItem(createIcon(), "导航一");
        nav.addItem(createIcon(), "导航二");
        nav.addItem(createIcon(), "导航三");
        nav.setSelected(0);
        nav.setOnItemSelectedListener(this::switchTab);

        FrameLayout.LayoutParams navParams;
        if (isTablet) {
            nav.setOrientation(LinearLayout.VERTICAL);
            nav.setSideWidthDp(72f);
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            navParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    screenHeight / 2);
            navParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            navParams.leftMargin = Math.round(20 * density);
        } else {
            navParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            navParams.gravity = Gravity.BOTTOM;
            navParams.leftMargin = Math.round(16 * density);
            navParams.rightMargin = Math.round(16 * density);
            navParams.bottomMargin = 0;
        }
        root.addView(nav, navParams);

        // 导航栏磨砂渐变背景（带一点泛白）
        GradientDrawable navBg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xD04A4A52, 0xB85A5A62}); // 顶部82%不透明泛白深灰 → 底部72%更泛白
        navBg.setShape(GradientDrawable.RECTANGLE);
        navBg.setCornerRadius(28 * density);
        navBg.setStroke(Math.round(1 * density), 0x55FFFFFF); // 1dp淡白描边
        nav.setBackground(navBg);
    }

    private void switchTab(int index) {
        currentTab = index;
        content.removeAllViews();
        if (index == TAB1) buildTab1();
        else if (index == TAB2) buildTab2();
        else buildTab3();
    }

    // ==================== 导航一：10 个选项 ====================
    private void buildTab1() {
        addTitle("导航一 · 十个选项");

        // 1-3：开关按钮
        addSwitch("选项一", "opt1");
        addSwitch("选项二", "opt2");
        addSwitch("选项三", "opt3");

        // 4：弹窗选项（5 个子选项）
        addSectionLabel("选项四（弹窗单选）");
        addAction("btn4", sp.getString("opt4", "选项 A"), v -> showDialog4());

        // 5：全屏二级界面
        addSectionLabel("选项五（二级界面）");
        addAction("btn5", "打开二级界面", v -> startActivity(new Intent(this, SubActivity.class)));

        // 6：三语言切换
        addSectionLabel("选项六（语言）");
        String[] langs = {"中文", "English", "Русский"};
        int langIdx = sp.getInt("lang", 0);
        addAction("btn6", langs[langIdx], v -> {
            int next = (sp.getInt("lang", 0) + 1) % 3;
            sp.edit().putInt("lang", next).apply();
            ((GlassCapsuleButton) v).setText(langs[next]);
            Toast.makeText(this, "语言: " + langs[next], Toast.LENGTH_SHORT).show();
        });

        // 7：页面内三个互斥单选按钮组
        addSectionLabel("选项七（单选组）");
        String[] radioOpts = {"模式 A", "模式 B", "模式 C"};
        int radioSel = sp.getInt("opt7", 0);
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            GlassRadioButton rb = new GlassRadioButton(this);
            rb.setText(radioOpts[i]);
            rb.setId(700 + i);
            rb.setChecked(radioSel == i);
            rb.setOnClickListener(v -> {
                sp.edit().putInt("opt7", idx).apply();
                // 单选组互斥
                for (int j = 0; j < 3; j++) {
                    GlassRadioButton b = content.findViewWithTag("opt7_rb" + j);
                    if (b != null) b.setChecked(j == idx);
                }
            });
            rb.setTag("opt7_rb" + i);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = Math.round(12 * density);
            content.addView(rb, lp);
        }

        // 8：透明度滑杆
        addSectionLabel("选项八（透明度）");
        int curAlpha = sp.getInt("opt8", 80);
        TextView alphaValue = new TextView(this);
        alphaValue.setText("透明度: " + curAlpha + "%");
        alphaValue.setTextColor(0xFF0A84FF);
        alphaValue.setTextSize(14);
        alphaValue.setPadding(0, 0, 0, Math.round(8 * density));
        content.addView(alphaValue);

        SeekBar seek = new SeekBar(this);
        seek.setMax(100);
        seek.setProgress(curAlpha);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (!fromUser) return;
                sp.edit().putInt("opt8", progress).apply();
                alphaValue.setText("透明度: " + progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
        content.addView(seek);

        // 9：文本输入
        addSectionLabel("选项九（文本输入）");
        addAction("btn9", sp.getString("opt9", "点击输入文本"), v -> showInputDialog9());

        // 10：颜色选择器
        addSectionLabel("选项十（颜色选择）");
        String[] colors = {"蓝色", "绿色", "红色"};
        int colorIdx = sp.getInt("opt10", 0);
        addAction("btn10", colors[colorIdx], v -> showColorDialog10(colors));
    }

    private void showDialog4() {
        String[] items = {"选项 A", "选项 B", "选项 C", "选项 D", "选项 E"};
        String cur = sp.getString("opt4", "选项 A");
        int checked = 0;
        for (int i = 0; i < items.length; i++) if (items[i].equals(cur)) checked = i;

        // 自定义弹窗布局（深色 + 玻璃按钮）
        LinearLayout dialogRoot = new LinearLayout(this);
        dialogRoot.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(20 * density);
        dialogRoot.setPadding(pad, Math.round(20 * density), pad, Math.round(16 * density));
        dialogRoot.setBackgroundColor(0xFF1C1C1E);

        android.widget.RadioGroup rg = new android.widget.RadioGroup(this);
        rg.setOrientation(android.widget.RadioGroup.VERTICAL);
        for (int i = 0; i < items.length; i++) {
            android.widget.RadioButton rb = new android.widget.RadioButton(this);
            rb.setText(items[i]);
            rb.setTextColor(Color.WHITE);
            rb.setId(800 + i);
            rb.setChecked(i == checked);
            rg.addView(rb);
        }
        dialogRoot.addView(rg);

        // 两个并列玻璃胶囊按钮
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(0, Math.round(16 * density), 0, 0);

        GlassCapsuleButton cancelBtn = new GlassCapsuleButton(this);
        cancelBtn.setText("取消");
        LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        cancelLp.rightMargin = Math.round(8 * density);
        btnRow.addView(cancelBtn, cancelLp);

        GlassCapsuleButton okBtn = new GlassCapsuleButton(this);
        okBtn.setText("确定");
        LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        okLp.leftMargin = Math.round(8 * density);
        btnRow.addView(okBtn, okLp);

        dialogRoot.addView(btnRow);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogRoot)
                .create();
        dialog.show();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        okBtn.setOnClickListener(v -> {
            int sel = rg.getCheckedRadioButtonId() - 800;
            if (sel >= 0 && sel < items.length) {
                sp.edit().putString("opt4", items[sel]).apply();
                ((GlassCapsuleButton) content.findViewWithTag("btn4")).setText(items[sel]);
            }
            dialog.dismiss();
        });
    }

    private void showInputDialog9() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(sp.getString("opt9", ""));
        input.setTextColor(Color.WHITE);

        LinearLayout dialogRoot = new LinearLayout(this);
        dialogRoot.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(20 * density);
        dialogRoot.setPadding(pad, Math.round(20 * density), pad, Math.round(16 * density));
        dialogRoot.setBackgroundColor(0xFF1C1C1E);
        dialogRoot.addView(input);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(0, Math.round(16 * density), 0, 0);

        GlassCapsuleButton cancelBtn = new GlassCapsuleButton(this);
        cancelBtn.setText("取消");
        LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        cancelLp.rightMargin = Math.round(8 * density);
        btnRow.addView(cancelBtn, cancelLp);

        GlassCapsuleButton okBtn = new GlassCapsuleButton(this);
        okBtn.setText("确定");
        LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        okLp.leftMargin = Math.round(8 * density);
        btnRow.addView(okBtn, okLp);

        dialogRoot.addView(btnRow);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogRoot)
                .create();
        dialog.show();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        okBtn.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            sp.edit().putString("opt9", text).apply();
            ((GlassCapsuleButton) content.findViewWithTag("btn9")).setText(text.isEmpty() ? "点击输入文本" : text);
            dialog.dismiss();
        });
    }

    private void showColorDialog10(String[] colors) {
        String cur = sp.getString("opt10", "蓝色");
        int checked = 0;
        for (int i = 0; i < colors.length; i++) if (colors[i].equals(cur)) checked = i;

        LinearLayout dialogRoot = new LinearLayout(this);
        dialogRoot.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(20 * density);
        dialogRoot.setPadding(pad, Math.round(20 * density), pad, Math.round(16 * density));
        dialogRoot.setBackgroundColor(0xFF1C1C1E);

        android.widget.RadioGroup rg = new android.widget.RadioGroup(this);
        rg.setOrientation(android.widget.RadioGroup.VERTICAL);
        for (int i = 0; i < colors.length; i++) {
            android.widget.RadioButton rb = new android.widget.RadioButton(this);
            rb.setText(colors[i]);
            rb.setTextColor(Color.WHITE);
            rb.setId(900 + i);
            rb.setChecked(i == checked);
            rg.addView(rb);
        }
        dialogRoot.addView(rg);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(0, Math.round(16 * density), 0, 0);

        GlassCapsuleButton cancelBtn = new GlassCapsuleButton(this);
        cancelBtn.setText("取消");
        LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        cancelLp.rightMargin = Math.round(8 * density);
        btnRow.addView(cancelBtn, cancelLp);

        GlassCapsuleButton okBtn = new GlassCapsuleButton(this);
        okBtn.setText("确定");
        LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        okLp.leftMargin = Math.round(8 * density);
        btnRow.addView(okBtn, okLp);

        dialogRoot.addView(btnRow);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogRoot)
                .create();
        dialog.show();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        okBtn.setOnClickListener(v -> {
            int sel = rg.getCheckedRadioButtonId() - 900;
            if (sel >= 0 && sel < colors.length) {
                sp.edit().putString("opt10", colors[sel]).apply();
                ((GlassCapsuleButton) content.findViewWithTag("btn10")).setText(colors[sel]);
            }
            dialog.dismiss();
        });
    }

    // ==================== 导航二：22 个开关 ====================
    private void buildTab2() {
        addTitle("导航二 · 二十二个开关");
        for (int i = 1; i <= 20; i++) {
            addSwitch("开关" + numToCn(i), "sw2_" + i);
        }
        // 21：版本号
        addSectionLabel("当前版本");
        TextView versionTv = new TextView(this);
        versionTv.setText("v1.0.0");
        versionTv.setTextColor(0xFF0A84FF);
        versionTv.setTextSize(14);
        content.addView(versionTv);

        // 22：检查更新
        addSectionLabel("检查更新");
        addAction("btnCheckUpdate", "检查更新", v ->
                Toast.makeText(this, "已是最新版", Toast.LENGTH_SHORT).show());
    }

    // ==================== 导航三：10 个开关 + 描述 ====================
    private void buildTab3() {
        addTitle("导航三 · 十个开关");
        for (int i = 1; i <= 10; i++) {
            addSwitchWithDesc("这是第" + numToCn(i) + "个开关的二十字描述文字内容示例", "sw3_" + i);
        }
    }

    // ==================== 工具方法 ====================
    private void addTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(24);
        tv.setPadding(0, 0, 0, Math.round(32 * density));
        content.addView(tv);
    }

    private void addSectionLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFFCCCCCC);
        tv.setTextSize(14);
        tv.setPadding(0, Math.round(24 * density), 0, Math.round(12 * density));
        content.addView(tv);
    }

    private GlassCapsuleButton addSwitch(String label, String key) {
        addSectionLabel(label);
        GlassCapsuleButton btn = new GlassCapsuleButton(this);
        boolean on = sp.getBoolean(key, false);
        btn.setText(on ? "开" : "关");
        btn.setGlassSelected(on);
        btn.setOnClickListener(v -> {
            boolean now = !sp.getBoolean(key, false);
            sp.edit().putBoolean(key, now).apply();
            ((GlassCapsuleButton) v).setText(now ? "开" : "关");
            ((GlassCapsuleButton) v).setGlassSelected(now);
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = Math.round(12 * density);
        content.addView(btn, lp);
        return btn;
    }

    private GlassCapsuleButton addAction(String tag, String text, View.OnClickListener listener) {
        GlassCapsuleButton btn = new GlassCapsuleButton(this);
        btn.setTag(tag);
        btn.setText(text);
        btn.setOnClickListener(listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = Math.round(12 * density);
        content.addView(btn, lp);
        return btn;
    }

    private void addSwitchWithDesc(String desc, String key) {
        TextView descTv = new TextView(this);
        descTv.setText(desc);
        descTv.setTextColor(0xFFCCCCCC);
        descTv.setTextSize(14);
        descTv.setPadding(0, Math.round(12 * density), 0, Math.round(8 * density));
        content.addView(descTv);

        GlassCapsuleButton btn = new GlassCapsuleButton(this);
        boolean on = sp.getBoolean(key, false);
        btn.setText(on ? "开" : "关");
        btn.setGlassSelected(on);
        btn.setOnClickListener(v -> {
            boolean now = !sp.getBoolean(key, false);
            sp.edit().putBoolean(key, now).apply();
            ((GlassCapsuleButton) v).setText(now ? "开" : "关");
            ((GlassCapsuleButton) v).setGlassSelected(now);
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = Math.round(12 * density);
        content.addView(btn, lp);
    }

    private String numToCn(int n) {
        String[] cn = {"一","二","三","四","五","六","七","八","九","十",
                       "十一","十二","十三","十四","十五","十六","十七","十八","十九","二十"};
        return cn[n-1];
    }

    private GradientDrawable createIcon() {
        GradientDrawable icon = new GradientDrawable();
        icon.setShape(GradientDrawable.OVAL);
        icon.setColor(Color.WHITE);
        return icon;
    }
}
