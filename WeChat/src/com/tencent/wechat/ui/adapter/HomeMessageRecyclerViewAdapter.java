package com.tencent.wechat.ui.adapter;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
 * Created by Administrator on 2016/7/4.
 */
public class HomeMessageRecyclerViewAdapter extends
		RecyclerView.Adapter<HomeMessageRecyclerViewAdapter.MyViewHolder> {

	private List<FriendVo> mFriends;

	private Context mContext;

	private OnRecyclerItemClickListener mOnItemClickListener;

	private int mCheckPosition;

	public HomeMessageRecyclerViewAdapter(Context context,
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
				R.layout.item_recycle_home_message, parent, false);
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
		if (friendVo.isHasNewMsg()) {
			holder.newMsgView.setVisibility(View.VISIBLE);
		} else {
			holder.newMsgView.setVisibility(View.INVISIBLE);
		}
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
		boolean isNotify = WeChatUtil.isFriendNotify(friendVo);
		if (!isNotify) {
			holder.notifyImageView.setVisibility(View.VISIBLE);
		} else {
			holder.notifyImageView.setVisibility(View.GONE);
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

	class MyViewHolder extends RecyclerView.ViewHolder {
		View itemView;
		ImageView imageView;
		TextView textView;
		RoundedLinearLayout imageLayout;
		View newMsgView;
		LinearLayout checkLayout;
		ImageView notifyImageView;

		public MyViewHolder(View v) {
			super(v);
			itemView = v;
			imageView = (ImageView) v.findViewById(R.id.head_imageview);
			textView = (TextView) v.findViewById(R.id.user_name_textview);
			newMsgView = v.findViewById(R.id.new_msg_icon);
			checkLayout = (LinearLayout) v.findViewById(R.id.item_check_layout);
			imageLayout = (RoundedLinearLayout) v
					.findViewById(R.id.imageview_layout);
			imageLayout.setmClipMode(RoundedLinearLayout.MODE_TOP_ONLY);
			imageLayout.setRadius(10);
			notifyImageView = (ImageView) v
					.findViewById(R.id.notify_icon_imageview);
		}
	}
}
