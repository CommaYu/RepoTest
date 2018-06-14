package com.tencent.wechat.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.greenrobot.eventbus.EventBus;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.entity.MessageReceiveEvent;
import com.tencent.wechat.common.entity.UnReadMsgVo;
import com.tencent.wechat.http.HttpWeChat;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.ipc.launcher.LauncherBridgeIntentResponse;

/**
 * Author: congqin <br>
 * Data:<br>
 * Description: 消息管理器<br>
 * Note:<br>
 */
public class MessageManager {

	private static final String TAG = "MessageManager";

	/** 显示消息 **/
	private final int MSG_SHOWNOTIFY = 100000;
	
	public static ReadMSGThread readMSGThread;

	private static MessageManager instance;

	private static HttpWeChat mWechat;

	/* 消息map，键为联系人ID，值为消息列表 */
	private ConcurrentMap<String, List<ReceiveMsgVO>> mMsgMap = new ConcurrentHashMap<String, List<ReceiveMsgVO>>();

	/* 会话列表list 里面的好友会被显示在homeMessageFragment */
	private List<FriendVo> mContactList = new ArrayList<FriendVo>();

	/* 当前的聊天对象，如果不在会话界面，会被置为null */
	private String mCurrentContact;

	/* 自己的userName */
	private String myUserName;

	/* 未读消息 */
	private ConcurrentMap<String, UnReadMsgVo> mUnReadMsgMap = new ConcurrentHashMap<String, UnReadMsgVo>();

	private boolean mIsScreenOn = true;
	
	/** 是否继续显示消息  **/
	private boolean continueShow = true;
	
	/** 消息队列  **/
	private BlockingQueue<ReceiveMsgVO> msgQueue =new LinkedBlockingQueue<ReceiveMsgVO>(100);;
	
	private MessageManager(){
		readMSGThread = new ReadMSGThread();
		readMSGThread.start();
	}
	
	public static MessageManager getInstance() {
		if (instance == null) {
			instance = new MessageManager();
			mWechat = WeChatApplication.getHttpWeChat();
		}
		return instance;
	}

	public String getMyUserName() {
		return myUserName;
	}

	public void setMyUserName(String myUserName) {
		this.myUserName = myUserName;
	}

	public void setIsScreenOn(boolean isScreenOn) {
		this.mIsScreenOn = isScreenOn;
	}

	public List<ReceiveMsgVO> getFriendMsg(String uid) {
		return mMsgMap.get(uid);
	}

	public void removeContacts(List<FriendVo> friendVos) {
		mContactList.removeAll(friendVos);
		EventBus.getDefault().post(new MessageReceiveEvent(null, null));
	}

	public void removeContact(FriendVo friendVo) {
		mContactList.remove(friendVo);
		EventBus.getDefault().post(new MessageReceiveEvent(null, null));
	}

	public List<FriendVo> getContactList() {
		return mContactList;
	}

	public void addContact(List<FriendVo> contacts) {
		if (contacts != null) {
			mContactList.addAll(contacts);
		}
	}

	public void addContact(FriendVo contact, boolean isNotify, boolean isAddFirst) {
		if (contact == null) {
			return;
		}
		if (isAddFirst) {
			if (mContactList.contains(contact)) {
				int index = mContactList.indexOf(contact);
				FriendVo oldVO = mContactList.get(index);
				mContactList.remove(oldVO);
				contact.setHasNewMsg(oldVO.isHasNewMsg());
				mContactList.add(0, contact);
			} else {
				mContactList.add(0, contact);
			}
		} else {
			if (mContactList.contains(contact)) {
				int index = mContactList.indexOf(contact);
				FriendVo oldVO = mContactList.get(index);
				mContactList.remove(oldVO);
				contact.setHasNewMsg(oldVO.isHasNewMsg());
				mContactList.add(index, contact);
			} else {
				mContactList.add(contact);
			}
		}

		if (isNotify) {
			EventBus.getDefault().post(new MessageReceiveEvent(null, null));
		}
	}

	public void notifyEventBus() {
		EventBus.getDefault().post(new MessageReceiveEvent(null, null));
	}

	public String getCurrentContact() {
		return mCurrentContact;
	}

	public void setCurrentContact(String currentContact) {
		this.mCurrentContact = currentContact;
	}

	public void readMsg(String uid) {
		// 删除未读消息
		if (mUnReadMsgMap.containsKey(uid)) {
			// 如果存在未读消息
			mUnReadMsgMap.remove(uid);
			notifyUnReadMsgMapChange();
		}
	}

	public ConcurrentMap<String, UnReadMsgVo> getmUnReadMsgMap() {
		return mUnReadMsgMap;
	}

	/**
	 * 添加消息到消息map中，同时通知界面做相应变化，如显示未读消息标识
	 * 
	 * @param weChatMsg
	 * @param isSendMessage
	 */
	private void doAddMessage(ReceiveMsgVO weChatMsg, boolean isSendMessage) {
		Log.d(TAG, "======>doAddMessage()");

		String uid = ""; // 与这条消息关联的好友的username （群消息对应的是群，而非群成员）
		if (isSendMessage) {
			uid = weChatMsg.getToUserName();
		} else {
			uid = weChatMsg.getFromUserName();
		}
		FriendVo sendUser = mWechat.getAllFriendsMap().get(uid);
		if (sendUser == null) {
			Log.e(TAG, "====>sendUser is null");
			return;
		}

		// 当前消息发送人不是自己，并且不处于当前消息发送人对话页面时，视为该联系人有新消息未读
		boolean isHasNewMsg = true;
		if (weChatMsg.getSenderName().equals(myUserName) || uid.equals(mCurrentContact)) {
			isHasNewMsg = false;
		}
		if (isHasNewMsg) {
			if (!mUnReadMsgMap.containsKey(uid)) {
				UnReadMsgVo contactUnreadInfo = new UnReadMsgVo();
				// 如果以前没有该联系人的未读消息，直接添加
				contactUnreadInfo.setmNickName(WeChatMain.getWeChatMain().getAllFriendsMap().get(uid).getNickName());
				contactUnreadInfo.setmUserName(uid);
				contactUnreadInfo
						.setmRemarkName(WeChatMain.getWeChatMain().getAllFriendsMap().get(uid).getRemarkName());
				contactUnreadInfo.setHeadImgUrl(WeChatMain.getWeChatMain().getAllFriendsMap().get(uid).getHeadImgUrl());
				++(contactUnreadInfo.nUnReadNum);
				contactUnreadInfo.setUnReadMsg(weChatMsg);
				mUnReadMsgMap.put(uid, contactUnreadInfo);
			} else {
				// 如果还有该联系人的未读消息，则拿到该联系人的未读消息信息，先保存其中的信息，再做删除重新添加操作
				UnReadMsgVo contactUnreadInfo = mUnReadMsgMap.get(uid);
				contactUnreadInfo.setUnReadMsg(weChatMsg);
				++(contactUnreadInfo.nUnReadNum);
				mUnReadMsgMap.replace(uid, contactUnreadInfo);
			}
			notifyUnReadMsgMapChange();
		}

		sendUser.setHasNewMsg(isHasNewMsg);
		// 会话列表中已经有该好友，将该好友调整至第一位置，如果没有则添加到第一个位置
		if (mContactList.contains(sendUser)) {
			// 将消息存入map中去
			List<ReceiveMsgVO> weChatMsgs = mMsgMap.get(uid);
			if (weChatMsgs == null) {
				weChatMsgs = new ArrayList<ReceiveMsgVO>();
				mMsgMap.put(uid, weChatMsgs);
			}
			weChatMsgs.add(weChatMsg);
			// 将好友调整至第一个位置
			if (mContactList.indexOf(sendUser) != 0) {
				mContactList.remove(sendUser);
				mContactList.add(0, sendUser);
			} else {
				mContactList.get(0).setHasNewMsg(isHasNewMsg);
			}
		} else {
			// 将消息存入map
			List<ReceiveMsgVO> weChatMsgs = new ArrayList<ReceiveMsgVO>();
			weChatMsgs.add(weChatMsg);
			mMsgMap.put(uid, weChatMsgs);
			mContactList.add(0, sendUser);
		}

		EventBus.getDefault().post(new MessageReceiveEvent(uid, weChatMsg));
	}

	/* 通知外部未读消息列表有更新 */
	private void notifyUnReadMsgMapChange() {
		LauncherBridgeIntentResponse.getInstance().showUnReadMsgInLauncher(mUnReadMsgMap);
	}

	/**
	 * 添加消息到消息容器，只能由wechatService调用，其他类应该调用wechatService的添加方法
	 * 
	 * @param addMsgList
	 * @param isSendMsg
	 *            false 代表是从服务器收到的消息（注意 此消息可能是好友发给我的，也可能是我自己手机端发给好友的）<br>
	 *            true 代表是从车机端发出去的消息
	 */
	public void addMessage(List<ReceiveMsgVO> addMsgList, boolean isSendMsg) {
		Log.d(TAG, "addMessage: addMsgList.size=" + addMsgList.size());
		if (addMsgList == null) {
			return;
		}
		for (ReceiveMsgVO weChatMsg : addMsgList) {
			doAddMessage(weChatMsg, isSendMsg);
		}
		if (!isSendMsg) {
			doNotifyOperation(addMsgList);
		}
	}

	/**
	 * 消息提醒的操作，对轰炸消息进行简单处理 
	 */
	private void doNotifyOperation(List<ReceiveMsgVO> addMsgList) {
		if (addMsgList == null || addMsgList.size() == 0) {
			return;
		}
		
		Map<String, ReceiveMsgVO> tmpMap = new HashMap<String, ReceiveMsgVO>();
		for (ReceiveMsgVO msgVO : addMsgList) {
			tmpMap.put(msgVO.getFromUserName(), msgVO);
			try {
				msgQueue.put(msgVO);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
//		for (String key : tmpMap.keySet()) {
//			NotifyManager.getInstance().showNotify(tmpMap.get(key));
//		}
	}
	
	/**
	 * 子线程，显示播报消息
	 *
	 */
	private class ReadMSGThread extends Thread {
		@Override
		public void run() {
			getMsg();
			}
		}
		
	/**
	 * 显示微信消息
	 */
	private void getMsg() {
		Log.d(TAG, "getMsg");
		Object lock = new Object();
		while (true) {
			synchronized (lock) {
				//设置标志位去锁定
				if (getContinueShow()) {
					ReceiveMsgVO msgVO = new ReceiveMsgVO();
					if (null == msgQueue) {
						continue;
					}
					try {
						msgVO = msgQueue.take();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					if (null == msgVO ) {
						continue;
					}
					Message msg = new Message();
					msg.what = MSG_SHOWNOTIFY;
					msg.obj = msgVO;
					uiHandler.sendMessage(msg);
					try {
						Thread.sleep(5*1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
	}
	
	Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_SHOWNOTIFY:
				NotifyManager.getInstance().showNotify((ReceiveMsgVO)msg.obj);
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 获取消息锁
	 * @return
	 */
	public boolean getContinueShow() {
		return continueShow;
	}

	/**
	 * 该方法不能乱用
	 * 设置是否锁定显示消息
	 * @param continueShow
	 */
	public void setContinueShow(boolean continueShow) {
		this.continueShow = continueShow;
	}

	
	public void modifyFriendNewMsg(FriendVo friendVo, boolean isHasNewMsg) {
		int index = mContactList.indexOf(friendVo);
		if (index != -1) {
			mContactList.get(index).setHasNewMsg(isHasNewMsg);
			EventBus.getDefault().post(new MessageReceiveEvent("MODIFY_MSG_STATE", null));
		}
	}

	public void clear() {
		mMsgMap.clear();
		mContactList.clear();
		setMyUserName("");
		setCurrentContact("");
		mUnReadMsgMap.clear();
		notifyUnReadMsgMapChange();
	}

}
