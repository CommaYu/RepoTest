package com.tencent.wechat.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.tencent.wechat.common.utils.AppManager;

/**
 * Created by Administrator on 2016/7/5.
 */
public class BaseActivity extends FragmentActivity {

	private FragmentManager mFragmentManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFragmentManager = getSupportFragmentManager();
		AppManager.getInstance().addActivity(this);
	}

	/**
	 * 创建fragment
	 * 
	 * @param fragmentName
	 * @param bundle
	 * @return Fragment
	 */
	public Fragment createFragment(String fragmentName, Bundle bundle) {
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		Fragment fragment = (bundle == null) ? Fragment.instantiate(this,
				fragmentName) : Fragment
				.instantiate(this, fragmentName, bundle);
		ft.commitAllowingStateLoss();
		return fragment;
	}

	/**
	 * 创建fragment
	 * 
	 * @param fragmentName
	 * @return Fragment
	 */
	public Fragment createFragment(String fragmentName) {
		return createFragment(fragmentName, null);
	}

	/**
	 * 添加fragment
	 * 
	 * @param containerViewId
	 * @param fragment
	 * @param tag
	 */
	public void addFragment(int containerViewId, Fragment fragment, String tag) {
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		if (tag == null) {
			ft.add(containerViewId, fragment);
		} else {
			ft.add(containerViewId, fragment, tag);
		}
		ft.addToBackStack(null);
		ft.commitAllowingStateLoss();
	}

	/**
	 * 添加fragment
	 * 
	 * @param containerViewId
	 * @param fragment
	 */
	public void addFragment(int containerViewId, Fragment fragment) {
		addFragment(containerViewId, fragment, null);
	}

	/**
	 * 替换fragment
	 * 
	 * @param fragment
	 * @param containerViewId
	 * @param tag
	 */
	public void replaceContent(Fragment fragment, int containerViewId,
			String tag) {
		replaceContent(fragment, containerViewId, tag, false);
	}

	/**
	 * 替换fragment
	 * 
	 * @param fragment
	 * @param containerViewId
	 * @param tag
	 * @param isToBackStack
	 */
	public void replaceContent(final Fragment fragment, int containerViewId,
			final String tag, boolean isToBackStack) {
		FragmentTransaction ft = mFragmentManager.beginTransaction().replace(
				containerViewId, fragment, tag);
		if (isToBackStack) {
			ft.addToBackStack(null);
		}
		ft.commitAllowingStateLoss();
	}

	@Override
	public void onBackPressed() {
		AppManager.getInstance().finishActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getInstance().finishActivity(this);
	}
}
