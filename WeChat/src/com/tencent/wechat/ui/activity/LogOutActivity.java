package com.tencent.wechat.ui.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.utils.SharedPreferencesUtil;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.ui.widget.CircleImageView;

/**
 * Created by Administrator on 2016/7/6.
 */
public class LogOutActivity extends BaseActivity implements
		View.OnClickListener, CompoundButton.OnCheckedChangeListener {

	private CheckBox mCheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logout);
		FriendVo userIndo = WeChatApplication.getHttpWeChat().getMyInfo()
				.getData();
		CircleImageView imageView = (CircleImageView) findViewById(R.id.user_heading_imageview);
		WeChatApplication.loadImage(userIndo.getHeadImgUrl(), imageView);
		TextView nameTextView = (TextView) findViewById(R.id.user_name_textview);
		nameTextView.setText(userIndo.getNickName());
		findViewById(R.id.logout_btn).setOnClickListener(this);
		findViewById(R.id.back_btn).setOnClickListener(this);
		findViewById(R.id.help_btn).setOnClickListener(this);
		boolean isNotify = SharedPreferencesUtil.getInstance().getIsNotify();
		mCheckBox = (CheckBox) findViewById(R.id.is_notify_check_box);
		mCheckBox.setChecked(isNotify);
		mCheckBox.setOnCheckedChangeListener(this);

		initGuidanceView();
	}

	PopupWindow popupWindow;

	private void initGuidanceView() {
		View view = View.inflate(this, R.layout.dialog_help_guidance, null);
		final WebView webView = (WebView) view
				.findViewById(R.id.guidance_webview);
		ImageButton closeIBtn = ((ImageButton) view
				.findViewById(R.id.close_btn));
		closeIBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popupWindow.dismiss();
			}
		});
		webView.loadUrl("file:///android_asset/guidance.html");
		webView.setBackgroundColor(Color.TRANSPARENT);
		popupWindow = new PopupWindow(view,
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT);
		popupWindow.setFocusable(true);
		popupWindow.setBackgroundDrawable(new ColorDrawable(0xa1000000));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.logout_btn:
			WeChatApplication.getBinder().logout();
			break;
		case R.id.back_btn:
			onBackPressed();
			break;
		case R.id.help_btn:
			showHelpDialog();
			break;
		default:
			break;
		}
	}

	private void showHelpDialog() {
		popupWindow.showAtLocation(mCheckBox, Gravity.CENTER, 0, 0);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		SharedPreferencesUtil.getInstance().saveIsNotify(isChecked);
	}
}
