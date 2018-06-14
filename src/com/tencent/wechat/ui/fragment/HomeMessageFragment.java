package com.tencent.wechat.ui.fragment;

import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ScrollerCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.wechat.R;
import com.tencent.wechat.common.entity.MessageReceiveEvent;
import com.tencent.wechat.common.entity.SessionInfo;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.listener.IOnKeyDownListener;
import com.tencent.wechat.listener.OnRecyclerItemClickListener;
import com.tencent.wechat.manager.MessageManager;
import com.tencent.wechat.ui.activity.WechatActivity;
import com.tencent.wechat.ui.activity.SessionActivity;
import com.tencent.wechat.ui.adapter.HomeMessageRecyclerViewAdapter;

/**
 * Created by Administrator on 2016/7/4.
 */
public class HomeMessageFragment extends Fragment implements
		OnRecyclerItemClickListener, IOnKeyDownListener {

	private static final String TAG = HomeMessageFragment.class.getSimpleName();

	/**
	 * 会话好友列表
	 */
	private RecyclerView mRecyclerView;

	private LinearLayoutManager mLinearLayoutManager;

	/**
	 * 所依附Activity
	 */
	private WechatActivity mFragmentActivity;

	private HomeMessageRecyclerViewAdapter mAdapter;

	private List<FriendVo> mFriends;

	private int mCheckPosition;

	private ScrollerCompat mScroller;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mFragmentActivity = (WechatActivity) getActivity();
		mFragmentActivity.setIOnKeyDownListener(this);
		View view = inflater.inflate(R.layout.fragment_home_message, container,
				false);
		setupViews(view);
		EventBus.getDefault().register(this);

		return view;
	}

	private void setupViews(View view) {
		mRecyclerView = (RecyclerView) view
				.findViewById(R.id.home_message_fragment_recyclerview);
		mLinearLayoutManager = new LinearLayoutManager(mFragmentActivity);
		// 设置布局 管理器
		mRecyclerView.setLayoutManager(mLinearLayoutManager);
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		// 设置为垂直布局，这也是默认的
		mLinearLayoutManager.setOrientation(OrientationHelper.HORIZONTAL);
		mFriends = MessageManager.getInstance().getContactList();
		mAdapter = new HomeMessageRecyclerViewAdapter(mFragmentActivity,
				mFriends);
		mAdapter.setOnItemClickListener(this);
		mRecyclerView.setAdapter(mAdapter);
	}

	@Override
	public void onRecyclerItemClick(int position) {
		SessionActivity.startMe(mFragmentActivity, new SessionInfo(mFriends
				.get(position).getUserName()), false);
		if (mCheckPosition != position) {
			mCheckPosition = position;
			mAdapter.setCheckPosition(mCheckPosition);
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(MessageReceiveEvent event) {
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onKeyDown(int keyCode) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT: // 上一个
			mCheckPosition--;
			if (mCheckPosition < 0) {
				mCheckPosition = 0;
				return;
			}
			mAdapter.setCheckPosition(mCheckPosition);
			mLinearLayoutManager.scrollToPosition(mCheckPosition);
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT: // 下一个
			mCheckPosition++;
			if (mCheckPosition > mFriends.size() - 1) {
				mCheckPosition = mFriends.size() - 1;
				return;
			}
			mAdapter.setCheckPosition(mCheckPosition);
			mLinearLayoutManager.scrollToPosition(mCheckPosition);
			break;
		case KeyEvent.KEYCODE_ENTER: // OK
			onRecyclerItemClick(mCheckPosition);
			break;
		}
	}
}
