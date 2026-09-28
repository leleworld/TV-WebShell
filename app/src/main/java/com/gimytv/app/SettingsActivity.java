package com.gimytv.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 设置页面 - 配置网站地址
 * 长按菜单键从 MainActivity 进入
 */
public class SettingsActivity extends Activity {

    public static final String PREFS_NAME = "gimytv_prefs";
    public static final String KEY_HOME_URL = "home_url";
    public static final String DEFAULT_URL = "https://gimytw.cc/";

    private EditText etUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_settings);

        etUrl = findViewById(R.id.etUrl);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);
        TextView tvVersion = findViewById(R.id.tvVersion);

        // 读取当前保存的 URL
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentUrl = prefs.getString(KEY_HOME_URL, DEFAULT_URL);
        etUrl.setText(currentUrl);
        etUrl.setSelection(currentUrl.length()); // 光标移到末尾

        tvVersion.setText("版本：1.0.0");

        // 保存按钮
        btnSave.setOnClickListener(v -> saveAndFinish());

        // 取消按钮
        btnCancel.setOnClickListener(v -> finish());

        // 输入框回车也触发保存
        etUrl.setOnEditorActionListener((v, actionId, event) -> {
            saveAndFinish();
            return true;
        });

        // 默认聚焦到输入框
        etUrl.requestFocus();
    }

    private void saveAndFinish() {
        String url = etUrl.getText().toString().trim();

        // 校验
        if (url.isEmpty()) {
            Toast.makeText(this, "地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 自动补 https://
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        // 确保末尾有 /
        if (!url.endsWith("/")) {
            url = url + "/";
        }

        // 保存
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_HOME_URL, url).apply();

        Toast.makeText(this, "已保存，返回后生效", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // ---- 静态工具方法，供 MainActivity 调用 ----

    /**
     * 从 SharedPreferences 读取用户配置的首页地址
     */
    public static String getHomeUrl(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_HOME_URL, DEFAULT_URL);
    }
}
