package com.tencent.wechat.ui.adapter;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.utils.ExpressionUtil;
import com.tencent.wechat.common.utils.WeChatUtil;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.listener.OnRecyclerItemClickListener;
import com.tencent.wechat.ui.widget.RoundedLinearLayout;

/**
 * Created by Administrator on 2016/7/5.
 */
public class HomeFriendsRecyclerViewAdapter extends
		RecyclerView.Adapter<HomeFriendsRecyclerViewAdapter.MyViewHolder> {

	// 好友列表 数据
	private List<FriendVo> mFriends;

	private Context mContext;

	// item点击
	private OnRecyclerItemClickListener mOnItemClickListener;
	// 当前选中位置
	private int mCheckPosition;

	public HomeFriendsRecyclerViewAdapter(Context context,
			List<FriendVo> friends) {
		this.mContext = context;
		mFriends = friends;
	}

	public void setCheckPosition(int position) {
		this.mCheckPosition = position;
		notifyDataSetChanged();
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.item_recycleview_home_friends, parent, false);
		MyViewHolder holder = new MyViewHolder(view);
		return holder;
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, final int position) {
		FriendVo friendVo = mFriends.get(position);
		String name = WeChatUtil.getFriendName(friendVo);
		CharSequence nameSpannableString = ExpressionUtil.parseEmoji(mContext,
				name);

		holder.textView.setText(nameSpannableString);
		WeChatApplication.loadImage(friendVo.getHeadImgUrl(), holder.imageView);
		if (mOnItemClickListener != null) {
			holder.checkLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mOnItemClickListener.onRecyclerItemClick(position);
				}
			});
		}

		if (mCheckPosition == position) {
			holder.checkLayout
					.setBackgroundResource(R.drawable.home_round_dark_check_background);
		} else {
			holder.checkLayout
					.setBackgroundResource(R.drawable.home_round_dark_background);
		}
	}

	@Override
	public int getItemCount() {
		return mFriends.size();
	}

	public void setOnItemClickListener(
			OnRecyclerItemClickListener onItemClickListener) {
		this.mOnItemClickListener = onItemClickListener;
	}

	class MyViewHolder extends ViewHolder {
		View itemView;
		ImageView imageView;
		TextView textView;
		RoundedLinearLayout imageLayout;
		LinearLayout checkLayout;

		public MyViewHolder(View v) {
			super(v);
			itemView = v;
			imageView = (ImageView) v.findViewById(R.id.head_imageview);
			textView = (TextView) v.findViewById(R.id.user_name_textview);
			checkLayout = (LinearLayout) v.findViewById(R.id.item_check_layout);
			imageLayout = (RoundedLinearLayout) v
					.findViewById(R.id.imageview_layout);
			imageLayout.setmClipMode(RoundedLinearLayout.MODE_TOP_ONLY);
			imageLayout.setRadius(10);
		}
	}
}
