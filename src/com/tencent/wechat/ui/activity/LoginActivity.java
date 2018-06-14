package com.tencent.wechat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.wechat.R;
import com.tencent.wechat.ui.fragment.LoginFragment;

/**
 * Created by Administrator on 2016/7/4.
 */
public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private LoginFragment mLoginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_login);
        mLoginFragment = (LoginFragment) createFragment(LoginFragment.class.getName());
        replaceContent(mLoginFragment, R.id.login_layout, "LoginFragment");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
