package com.tencent.wechat.ui.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.entity.SessionInfo;
import com.tencent.wechat.common.utils.RegexUtil;
import com.tencent.wechat.http.HttpWeChat;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.listener.IOnKeyDownListener;
import com.tencent.wechat.listener.OnRecyclerItemClickListener;
import com.tencent.wechat.ui.activity.WechatActivity;
import com.tencent.wechat.ui.activity.SessionActivity;
import com.tencent.wechat.ui.adapter.HomeFriendsRecyclerViewAdapter;
import com.tencent.wechat.ui.widget.LetterBar;

/**
 * Created by Administrator on 2016/7/5.
 */
public class HomeFriendsFragment extends Fragment implements
		OnRecyclerItemClickListener, LetterBar.OnLetterSelectListener,
		IOnKeyDownListener {

	// 索引条字符“群”
	private static final String GROUP_ALPHABET = "群";
	// 索引条字符“#”
	private static final String OTHER_ALPHABET = "#";

	private WechatActivity mFragmentActivity;

	private RecyclerView mRecyclerView;

	private HomeFriendsRecyclerViewAdapter mAdapter;

	private GridLayoutManager mGridLayoutManager;

	private HttpWeChat mWechat = WeChatApplication.getHttpWeChat();

	private LetterBar mLetterBar;

	// 全部好友
	private List<FriendVo> mFriends;
	// 群组列表
	private List<FriendVo> mGroupFriends;
	// 其他好友列表
	private List<FriendVo> mOtherFriends;
	// 普通好友列表
	private List<FriendVo> mNormalFriends;

	private int mCheckPosition;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mFragmentActivity = (WechatActivity) getActivity();
		mFragmentActivity.setIOnKeyDownListener(this);
		mFriends = new ArrayList<FriendVo>();
		mGroupFriends = new ArrayList<FriendVo>();
		mOtherFriends = new ArrayList<FriendVo>();
		mNormalFriends = new ArrayList<FriendVo>();
		View view = inflater.inflate(R.layout.fragment_home_friends, container,
				false);
		setupViews(view);
		view.post(new Runnable() {
			@Override
			public void run() {
				loadData();
			}
		});
		return view;
	}

	private void setupViews(View view) {
		mLetterBar = (LetterBar) view.findViewById(R.id.letter_bar);
		mLetterBar.setOnLetterSelectListener(this);
		mRecyclerView = (RecyclerView) view
				.findViewById(R.id.home_friends_fragment_recyclerview);
		mGridLayoutManager = new GridLayoutManager(mFragmentActivity, 2,
				LinearLayoutManager.HORIZONTAL, false);
		mRecyclerView.setLayoutManager(mGridLayoutManager);
		mAdapter = new HomeFriendsRecyclerViewAdapter(mFragmentActivity,
				mFriends);
		mAdapter.setOnItemClickListener(this);
		mRecyclerView.setAdapter(mAdapter);
	}

	private void loadData() {
		setupFriendsData();
		mAdapter.notifyDataSetChanged();
	}

	private void setupFriendsData() {
		List<FriendVo> contacts = mWechat.getAllConnectUser().getData();
		if (contacts != null) {
			mFriends.addAll(contacts);
			// 对好友进行排序，将群组排在最前面剩下的按拼音排序，简拼为字母以外开头的，排在最后面
			Collections.sort(mFriends, new Comparator<FriendVo>() {

				@Override
				public int compare(FriendVo o1, FriendVo o2) {
					String py1 = TextUtils.isEmpty(o1.getRemarkPYInitial()) ? o1
							.getPYInitial() : o1.getRemarkPYInitial();
					String py2 = TextUtils.isEmpty(o2.getRemarkPYInitial()) ? o2
							.getPYInitial() : o2.getRemarkPYInitial();
					String userId1 = o1.getUserName();
					String userId2 = o2.getUserName();

					boolean isFirstpy1 = TextUtils.isEmpty(py1) ? false
							: RegexUtil.isLetter(py1.substring(0, 1));
					boolean isFirstpy2 = TextUtils.isEmpty(py2) ? false
							: RegexUtil.isLetter(py2.substring(0, 1));
					if (!isFirstpy1) {
						py1 = "~";
					}
					if (!isFirstpy2) {
						py2 = "~";
					}
					if (userId1.startsWith("@@") && userId2.startsWith("@@")) {
						return py1.compareTo(py2);
					} else if (userId1.startsWith("@@")
							&& !userId2.startsWith("@@")) {
						return -1;
					} else if (!userId1.startsWith("@@")
							&& userId2.startsWith("@@")) {
						return 1;
					} else {
						return py1.compareTo(py2);
					}
				}
			});
			// 将好友进行分组，分为群组、普通好友、其他好友
			for (FriendVo connectUser : mFriends) {
				String userId = connectUser.getUserName();
				if (userId.startsWith("@@")) {
					// 群组
					mGroupFriends.add(connectUser);
				} else {
					// 好友
					String py = TextUtils.isEmpty(connectUser
							.getRemarkPYInitial()) ? connectUser.getPYInitial()
							: connectUser.getRemarkPYInitial();
					boolean isFirstpy = TextUtils.isEmpty(py) ? false
							: RegexUtil.isLetter(py.substring(0, 1));
					if (isFirstpy) {
						mNormalFriends.add(connectUser);
					} else {
						mOtherFriends.add(connectUser);
					}
				}
			}
		}
	}

	public int getPosition(String letter) {
		for (int i = 0, count = mNormalFriends.size(); i < count; i++) {
			FriendVo connectUser = mNormalFriends.get(i);
			String currentPY = TextUtils.isEmpty(connectUser
					.getRemarkPYInitial()) ? connectUser.getPYInitial()
					: connectUser.getRemarkPYInitial();
			String currentStr = TextUtils.isEmpty(currentPY) ? "" : currentPY
					.substring(0, 1);
			if (GROUP_ALPHABET.equals(letter)) {
				return 0;
			} else if (OTHER_ALPHABET.equals(letter)) {
				return mGroupFriends.size() + mNormalFriends.size();
			} else if (currentStr.equals(letter)) {
				return i + mGroupFriends.size();
			}
		}
		return -1;
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

	@Override
	public void onLetterSelect(String letter) {
		int selectPosition = getPosition(letter);
		if (selectPosition != -1) {
			Log.d("好友列表", "第" + selectPosition + "个");
			mGridLayoutManager.scrollToPositionWithOffset(selectPosition, 0);
		}
	}

	@Override
	public void onKeyDown(int keyCode) {
		switch (keyCode) {
		case 21: // 上一个
			mCheckPosition--;
			if (mCheckPosition < 0) {
				mCheckPosition = 0;
				return;
			}
			mAdapter.setCheckPosition(mCheckPosition);
			mGridLayoutManager.scrollToPosition(mCheckPosition);
			break;
		case 22: // 下一个
			mCheckPosition++;
			if (mCheckPosition > mFriends.size() - 1) {
				mCheckPosition = mFriends.size() - 1;
				return;
			}
			mAdapter.setCheckPosition(mCheckPosition);
			mGridLayoutManager.scrollToPosition(mCheckPosition);
			break;
		case 66: // OK
			onRecyclerItemClick(mCheckPosition);
			break;
		}
	}
}
