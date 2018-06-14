package com.tencent.wechat.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.Constant;
import com.tencent.wechat.common.entity.ScanVo;
import com.tencent.wechat.common.utils.JSONUtils;
import com.tencent.wechat.common.utils.WeChatUtil;
import com.tencent.wechat.http.entity.BatchgetcontactRequestVo;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.http.entity.PropVo;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.http.entity.Response;
import com.tencent.wechat.http.entity.ResquestModel;
import com.tencent.wechat.http.entity.SyncKey;
import com.tencent.wechat.http.entity.SyncKey.SyncKeyValue;
import com.tencent.wechat.ipc.BridgeIntentResponse;
import com.tencent.wechat.manager.MessageManager;

/**
 * Author: congqin<br>
 * Data:<br>
 * Description: HttpWeChat的具体实现，内部调用OkhttpUtil发送网络网络请求<br>
 * Note:<br>
 */
public class WeChatMain implements HttpWeChat {
	private static WeChatMain instance = new WeChatMain();

	public static WeChatMain getWeChatMain() {
		return instance;
	}

	private static final String TAG = "WeChatMain";

	/*
	 * 群组map,键为群username，值为群Vo 包含那些未加入通讯录的群
	 * 注意：只有当一个群的信息是完整的，即调用updateRoomInfo填充memberList 后，该群才会被加入以下map
	 */
	private Map<String, FriendVo> mGroupFriendMap;

	/*
	 * 初次登录网页后获得的部分最近联系人（即会话列表页面初始所展示的好友） 以后新增的会话联系人（包括新加的群）并不会被加入到该列表
	 */
	private List<FriendVo> mContactFriends;

	/*
	 * 好友列表页面所包含的好友，不包括未加入通讯录的群
	 */
	private List<FriendVo> mAllFriends;

	/*
	 * 所有联系人map. 比如自己被邀请至群聊，但并不把该群加入通讯录，则该群作为一个friendVO 会被加入到mAllFriendsMap,
	 * 且这种情况下，该vo会被加入MessageManager中的list 但不会被加入到上面的mContactFriends。
	 */
	private Map<String, FriendVo> mAllFriendsMap;

	private String uuid;

	private PropVo prop;

	private String referer;

	private SyncKey syncKey;

	private FriendVo myInfo;

	private String myId;

	private boolean init = false;

	private String pgv_pvi;

	private String pgv_si;

	private String deviceId;

	private void init() {
		uuid = "";
		prop = new PropVo();
		referer = "";
		syncKey = null;
		myInfo = new FriendVo();
		myId = "";
		init = false;
		mGroupFriendMap = new HashMap<String, FriendVo>();
		mContactFriends = new ArrayList<FriendVo>();
		mAllFriends = new ArrayList<FriendVo>();
		mAllFriendsMap = new HashMap<String, FriendVo>();

		pgv_pvi = WeChatUtil.getPgvPvi();
		pgv_si = WeChatUtil.getPgvSi();
		deviceId = WeChatUtil.getDeviceID();
		OkHttpUtil.clearCookie();
	}

	@Override
	public String getQvode() throws IOException, InterruptedException {
		logOut();
		init();

		String qcodeUrl = WeChatUtil.getProp("GET_UUID", null);

		Map<String, String> params = new HashMap<String, String>();
		params.put("appid", "wx782c26e4c19acffb");
		params.put("fun", "new");
		params.put("lang", "zh_CN");
		params.put("_", "" + System.currentTimeMillis());
		String result = OkHttpUtil.getStringSync(qcodeUrl, params);
		uuid = RegexUtils.getUuid(result);
		return WeChatUtil.getProp("GET_QCODE", null) + uuid;
	}

	/**
	 * code： 200: 成功 201：扫描成功，但未点确认 408：未扫描 400：未知 500： login poll srv exception
	 * 
	 * @throws IOException
	 * @throws JSONException
	 * @throws InterruptedException
	 */
	@Override
	public ScanVo getLoginState() throws IOException, JSONException,
			InterruptedException {

		ScanVo scanVo = new ScanVo();
		init = false;
		// if (null == uuid) {
		// scanVo.setCode(0);
		// scanVo.setContent("uid不能为空");
		// return scanVo;
		// }

		String statusUrl = WeChatUtil.getProp("GET_LOGIN_STATUS", null);

		Map<String, String> params = new HashMap<String, String>();
		params.put("loginicon", "true");
		params.put("tip", "0"); // tip 需要设为0
		params.put("uuid", uuid);
		params.put("_", System.currentTimeMillis() + "");

		// 状态未发送改变时，该方法阻塞25s
		String status = OkHttpUtil.getStringSync(statusUrl, params, null);
		Log.d(TAG, "status===>" + status);

		String retCode = RegexUtils.retStatus(status);
		Log.d(TAG, "retCode===>" + retCode);

		if ("200".equals(retCode)) {
			int ret = -1;
			ret = dealCode200(status);
			if (0 == ret) {
				scanVo.setCode(Constant.LOGIN_STATE_SUCCESS);
				scanVo.setContent("登录成功");
				init = true;
			} else {
				scanVo.setCode(Constant.LOGIN_STATE_QVODE_INVALID);
				scanVo.setContent("二维码过期请重新获取二维码");
				Log.i(TAG, "getLoginState ret!=0  二维码过期请重新获取二维码");
			}

		} else if ("201".equals(retCode)) { // 扫描但还未点击确认登录
			scanVo.setCode(Constant.LOGIN_STATE_SCANNED);
			scanVo.setContent(RegexUtils.getHeadImage(status));
		} else if ("408".equals(retCode)) { // 未扫描
			scanVo.setCode(Constant.LOGIN_STATE_NOT_SCAN);
			scanVo.setContent("未扫描");
		} else {
			scanVo.setCode(Constant.LOGIN_STATE_QVODE_INVALID);
			scanVo.setContent("二维码过期请重新获取二维码");
			Log.i(TAG, "getLoginState retCode!=200  二维码过期请重新获取二维码");
		}
		return scanVo;
	}

	private int dealCode200(String status) throws IOException, JSONException {
		String keyUrl = RegexUtils.getRedirectUrl(status);
		String queryStr = keyUrl.split("\\?")[1];
		String[] vs = queryStr.split("&");
		String trick = "";
		String scan = "";
		for (String v : vs) {
			String[] kv = v.split("=");
			if ("ticket".equals(kv[0])) {
				trick = kv[1];
			} else if ("scan".equals(kv[0])) {
				scan = (kv[1]);
			}
		}

		referer = initAllKey(keyUrl);

		if (TextUtils.isEmpty(referer)) {
			Log.i(TAG, "referer is null ");
			return 1;
		}
		prop.setUuid(uuid);
		prop.setTicket(trick);
		prop.setScan(scan);

		String initContent = initConnect(null);
		Log.i(TAG, initContent);

		// FileUtil.writeToFile("/sdcard/wechatresult/initContent" +
		// System.currentTimeMillis(), initContent);

		JSONObject jo = new JSONObject(initContent);

		Integer retCode = jo.getJSONObject("BaseResponse").getInt("Ret");
		if (null == retCode) {
			return 1;
		}
		if (0 == retCode) { // 返回0 直接处理
			delInitContent(jo);
			return 0;
		}
		logOut();
		return 1;
	}

	// xml 请求重定向
	private static Pattern redirectUrls = Pattern
			.compile("(?<=(\\<redirecturl\\>))[^<]+");

	// js需要跳转
	private static Pattern jsRedirectUrls = Pattern
			.compile("(?<=(href=\"))[^\"]+");

	private String initAllKey(String url) throws IOException {
		HttpCall c = new HttpCall();

		Log.d(TAG, "initAllKey: referer=" + referer);
		String ret = OkHttpUtil.getStringSync(url, null, null);
		Log.i(TAG, "loginpage:" + url + "  ret:" + ret);
		Log.d(TAG, "==============" + ret.indexOf("redirecturl"));
		if (ret.indexOf("redirecturl") != -1) {
			Matcher m = redirectUrls.matcher(ret);
			if (m.find()) {
				String redirectUrl = m.group();
				ret = OkHttpUtil.getStringSync(redirectUrl, null);
				Log.i(TAG, "loginpage:" + redirectUrl + "  ret:" + ret);
			} else {
				return "";
			}
		} else {
			Log.d(TAG, "initAllKey: ret do not contain redirecturl");
		}
		boolean local2 = false;
		if (ret.indexOf("window.location.href") != -1) {
			local2 = true;
			Matcher m = jsRedirectUrls.matcher(ret);
			if (m.find()) {
				String redirectUrl = m.group();
				ret = OkHttpUtil.getStringSync(redirectUrl, null);
			} else {
				return "";
			}
		} else {
			Log.d(TAG, "initAllKey: ret do not contain window.location.href");
		}

		this.prop = RegexUtils.readProp(ret);
		if (null == prop) {
			prop = new PropVo();
			return "";
		}
		String refer = (local2 ? "https://wx2.qq.com" : url) + c.getRedirUrl();
		Log.d(TAG, "initAllKey: c.getRedirUrl()=" + c.getRedirUrl());
		Log.d(TAG, "initAllKey: refer=" + refer);
		return refer;

	}

	private String initConnect(String urls) throws IOException {

		String url = TextUtils.isEmpty(urls) ? WeChatUtil.getProp(
				"GET_INIT_URL", referer) : urls;
		Log.d(TAG, "initConnect|url=" + url);

		ResquestModel r = new ResquestModel(prop, deviceId);
		String content = r.toJson();
		Map<String, String> headers = new HashMap<String, String>();

		headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
		String ret = OkHttpUtil.postJsonSync(url, null, content, headers);

		Log.d(TAG, "initConnect: content=" + content);

		Log.i(TAG, url);
		Log.i(TAG, "content" + content);
		Log.i(TAG, "初始化聊天界面成功");

		return ret;
	}

	@Override
	public Response<List<FriendVo>> getLateConnectUser() {
		Response<List<FriendVo>> resp = new Response<List<FriendVo>>();
		resp.setData(mContactFriends);
		return resp;
	}

	@Override
	public Response<List<FriendVo>> getAllConnectUser() {
		Response<List<FriendVo>> resp = new Response<List<FriendVo>>();
		resp.setData(mAllFriends);
		return resp;
	}

	public Map<String, FriendVo> getAllFriendsMap() {
		return mAllFriendsMap;
	}

	public Map<String, FriendVo> getGroupFriendsMap() {
		return mGroupFriendMap;
	}

	private final static Pattern selector = Pattern
			.compile("(?<=(selector:\"))[^\"]+");

	public Integer synKey() throws IOException, JSONException {
		int code = -1;
		try {
			List<SyncKeyValue> jarr = this.syncKey.getList();
			StringBuilder synckey = new StringBuilder();
			for (SyncKeyValue syncKeyValue : jarr) {
				synckey.append(syncKeyValue.getKey().toString()).append("_")
						.append(syncKeyValue.getVal().toString()).append("|");
			}
			synckey.deleteCharAt(synckey.length() - 1);
			Log.d(TAG, "synKey: =======referer=" + referer);

			String host = (referer.indexOf("wx2") != -1) ? "https://webpush2.weixin.qq.com"
					: "https://webpush.weixin" + ".qq.com";

			String url = host + WeChatUtil.getProp("GET_CHECK_MSG", "");
			Map<String, String> synMap = new HashMap<String, String>();

			synMap.put("r", "" + System.currentTimeMillis());
			synMap.put("sid", prop.getWxsid());
			synMap.put("skey", prop.getSkey());
			synMap.put("synckey", synckey.toString());
			synMap.put("uin", prop.getWxuin());

			String rrt = OkHttpUtil.getStringSync(url, synMap, null);

			Log.i(TAG, "rrt====>" + rrt);
			if (rrt.indexOf("retcode:\"0\"") == -1) {
				code = -1;
			}
			Matcher mt = selector.matcher(rrt);
			if (mt.find()) {
				code = Integer.valueOf(mt.group());
			}
			if (rrt.contains("retcode:\"1100\"")
					|| rrt.contains("retcode:\"1101\"")
					|| rrt.contains("retcode:\"1102\"")) {
				code = 1100;
			}

		} catch (Exception e) {
			String errMessage = e.getMessage();
			if (!TextUtils.isEmpty(errMessage)) {
				Log.e(TAG, e.getMessage());
			}
		}
		return code;
	}

	@Override
	public List<ReceiveMsgVO> getChatMsg() throws IOException, JSONException {

		if (TextUtils.isEmpty(referer)) {
			return null;
		}
		String msgUrl = WeChatUtil.getProp("GET_MESSAGE", referer) + "&sid="
				+ prop.getWxsid() + "&skey=" + prop.getSkey();
		ResquestModel rm = new ResquestModel(prop, deviceId);
		rm.add("SyncKey", this.syncKey);
		rm.add("rr", System.currentTimeMillis());

		String ret = OkHttpUtil.postJsonSync(msgUrl, null, rm.toJson(), null);

		// FileUtil.writeToFile("/sdcard/wechatresult/getChatMsg" +
		// System.currentTimeMillis(), ret);

		JSONObject jo = new JSONObject(ret);
		JSONObject baseReq = jo.getJSONObject("BaseResponse");
		if (null == baseReq || 0 != baseReq.getInt("Ret")) {
			return null;
		}
		if (null != jo.getJSONObject("SyncKey")) {
			syncKey = JSONUtils
					.fromJson(jo.getString("SyncKey"), SyncKey.class);
		}

		// 处理删除好友或退出群聊
		List<FriendVo> delContactList = JSONUtils.fromJson(
				jo.getString("DelContactList"),
				new TypeToken<List<FriendVo>>() {
				});
		if (delContactList != null && !delContactList.isEmpty()) {
			for (FriendVo friendVo : delContactList) {
				String userName = friendVo.getUserName();
				mGroupFriendMap.remove(userName);
				getAllFriendsMap().remove(userName);
				MessageManager.getInstance().removeContacts(delContactList);
			}
			mAllFriends.removeAll(delContactList);

		}

		boolean isModify = false;
		// 处理群聊成员变化，接受好友添加请求
		List<FriendVo> modContactList = JSONUtils.fromJson(
				jo.getString("ModContactList"),
				new TypeToken<List<FriendVo>>() {
				});
		if (modContactList != null && !modContactList.isEmpty()) {
			isModify = true;
			for (FriendVo friendVo : modContactList) {
				String userName = friendVo.getUserName();
				if (userName.startsWith("@@")) {
					// 群
					if (mGroupFriendMap.containsKey(userName)) {
						// 该群已经在列表中
						List<FriendVo> currentMembers = friendVo
								.getMemberList();
						List<FriendVo> oldMembers = mGroupFriendMap.get(
								userName).getMemberList();
						List<FriendVo> resultMembers = new ArrayList<FriendVo>();
						List<String> userList = new ArrayList<String>();
						for (FriendVo member : currentMembers) {
							String memberUserName = member.getUserName();
							if (oldMembers.contains(member)) {
								// 原来的成员中有，则获取原来的该成员信息
								int index = oldMembers.indexOf(member);
								resultMembers.add(oldMembers.get(index));
							} else {
								// 原来的成员中没有，则先看好友map中有没有该好友信息，如果也没有则需要网络获取
								if (mAllFriendsMap.containsKey(memberUserName)) {
									resultMembers.add(mAllFriendsMap
											.get(memberUserName));
								} else {
									// 需要网络获取
									userList.add(memberUserName);
								}
							}
						}
						// 当有成员需要网络获取时
						if (!userList.isEmpty()) {
							List<BatchgetcontactRequestVo> prams = new ArrayList<BatchgetcontactRequestVo>();
							for (String s : userList) {
								BatchgetcontactRequestVo requestVO = new BatchgetcontactRequestVo();
								requestVO.setUserName(s);
								prams.add(requestVO);
							}
							List<FriendVo> friendVos = getBatchContact(prams);
							if (friendVos != null) {
								resultMembers.addAll(friendVos);
							}
						}
						friendVo.setMemberCount(resultMembers.size());
						friendVo.setMemberList(resultMembers);

					} else {
						// 该群没有在列表中
						List<BatchgetcontactRequestVo> prams = new ArrayList<BatchgetcontactRequestVo>();
						BatchgetcontactRequestVo requestVO = new BatchgetcontactRequestVo();
						requestVO.setUserName(userName);
						prams.add(requestVO);
						Log.d(TAG,
								"该群没有在列表中: before getBatchContact,friendVo meberlist.size="
										+ friendVo.getMemberCount());

						List<FriendVo> friendVos = getBatchContact(prams);
						if (friendVos != null && friendVos.size() > 0) {
							friendVo = friendVos.get(0);
						}
						Log.d(TAG,
								"该群没有在列表中: after getBatchContact,friendVo meberlist.size="
										+ friendVo.getMemberCount());
					}
					String headImgUrl = friendVo.getHeadImgUrl();
					friendVo.setHeadImgUrl(getFullUrl(headImgUrl));
					List<FriendVo> currentMembers = friendVo.getMemberList();
					if (!currentMembers.contains(myInfo)) {
						mAllFriendsMap.remove(userName);
						mGroupFriendMap.remove(userName);
						MessageManager.getInstance().removeContact(friendVo);
					} else {
						mAllFriendsMap.put(userName, friendVo);
						mGroupFriendMap.put(userName, friendVo);
						MessageManager.getInstance().addContact(friendVo,
								false, false);
					}
				} else {
					// 好友 重新请求好友信息
					List<BatchgetcontactRequestVo> prams = new ArrayList<BatchgetcontactRequestVo>();
					BatchgetcontactRequestVo requestVO = new BatchgetcontactRequestVo();
					requestVO.setUserName(userName);
					prams.add(requestVO);
					List<FriendVo> friendVos = getBatchContact(prams);
					FriendVo friendVo1 = friendVos.get(0);
					if (WeChatUtil.isNotFriend(friendVo1)) {
						continue;
					}
					friendVo1.setHeadImgUrl(getFullUrl(friendVo1
							.getHeadImgUrl()));
					mAllFriendsMap.put(userName, friendVo1);
					if (mAllFriends.contains(friendVo1)) {
						mAllFriends.remove(friendVo1);
					}
					mAllFriends.add(friendVo1);
					MessageManager.getInstance().addContact(friendVo1, false,
							false);
				}
			}
			MessageManager.getInstance().notifyEventBus();
		}
		if (isModify) {
			uploadContact();
		}

		// 获取添加消息数据
		List<ReceiveMsgVO> addMsgList = JSONUtils.fromJson(
				jo.getString("AddMsgList"),
				new TypeToken<List<ReceiveMsgVO>>() {
				});

		List<ReceiveMsgVO> resultMsgList = new ArrayList<ReceiveMsgVO>();
		if (addMsgList != null && !addMsgList.isEmpty()) {
			// 有新消息时
			for (final ReceiveMsgVO receiveMsgVO : addMsgList) {
				boolean ignore = false;
				String fromeUser = receiveMsgVO.getFromUserName();
				String toUser = receiveMsgVO.getToUserName();
				String content = receiveMsgVO.getContent();
				int msgType = receiveMsgVO.getMsgType();
				String msgId = receiveMsgVO.getMsgId();
				boolean sendByMyself = false;
				if (fromeUser.equals(myId)) {
					// 该消息是自己在手机端发送的，这时候需要把fromeUser和toUser互换
					// 因为往
					// MessageManager中添加msg时，isSendMsg传false,此时，uid会从fromUser中获取。
					String temp = fromeUser;
					fromeUser = toUser;
					toUser = temp;
					receiveMsgVO.setFromUserName(fromeUser);
					receiveMsgVO.setToUserName(toUser);
					receiveMsgVO.setSenderName(myId);
					sendByMyself = true;
				} else {
					receiveMsgVO.setSenderName(fromeUser);
				}

				if (fromeUser.startsWith("@@")) {// 群消息，需要设置群消息标识，获取到发送人，截取发送信息
					receiveMsgVO.setGroupMsg(true);
					if (content.contains(":<br/>")
							&& !content.contains("&pictype=location")) {
						String[] r = content.split(":<br/>");
						if (r.length < 2) {
							continue;
						}
						String senderName = r[0];
						receiveMsgVO.setSenderName(senderName);
						receiveMsgVO.setContent(r[1]);
					} else if (content.contains(":<br/>")
							&& content.contains("&pictype=location")) { // 分享的地图
						String[] r = content.split(":<br/>");
						if (sendByMyself) {
							receiveMsgVO.setContent(r[0]);
							receiveMsgVO
									.setImageUrl("https://wx.qq.com" + r[1]);
						} else {
							receiveMsgVO.setSenderName(r[0]);
							receiveMsgVO.setContent(r[1]);
							receiveMsgVO
									.setImageUrl("https://wx.qq.com" + r[2]);
						}

						receiveMsgVO.setMsgType(Constant.MSGTYPE_GET_LOCATION);
					}

					if (!mGroupFriendMap.containsKey(fromeUser)) {// 当群组map中没有该群组时，需要获取该群组信息
						List<BatchgetcontactRequestVo> prams = new ArrayList<BatchgetcontactRequestVo>();
						BatchgetcontactRequestVo requestVO = new BatchgetcontactRequestVo();
						requestVO.setUserName(fromeUser);
						prams.add(requestVO);
						getBatchContact(prams);
					}
				} else {// 好友消息
					receiveMsgVO.setGroupMsg(false);
					if (content.contains(":<br/>")
							&& content.contains("&pictype=location")) { // 分享的地图
						String[] r = content.split(":<br/>");
						String text_map = r[0];
						receiveMsgVO.setContent(text_map);
						receiveMsgVO.setImageUrl("https://wx.qq.com" + r[1]);
						receiveMsgVO.setMsgType(Constant.MSGTYPE_GET_LOCATION);
					}

					FriendVo fromFriend = mAllFriendsMap.get(fromeUser);
					if (!mAllFriendsMap.containsKey(fromeUser)
							|| fromFriend.getVerifyFlag() != 0) {
						continue;
					}
				}

				Log.i(TAG,
						"receiveMsgVO.getContent() = "
								+ receiveMsgVO.getContent()
								+ "|receiveMsgVO.getMsgType()="
								+ receiveMsgVO.getMsgType());
				// 对不同消息类型进行处理
				switch (receiveMsgVO.getMsgType()) {

				case Constant.MSGTYPE_TEXT:// 文字消息
					String msgContent = receiveMsgVO.getContent();

					String carVoiceUrl = WeChatUtil.getCarVoiceUrl(msgContent);
					String carPoiUrl = WeChatUtil.getCarPoiUrl(msgContent);
					String voicePath = null;

					if (!TextUtils.isEmpty(carVoiceUrl)) {
						byte[] bytes = OkHttpUtil
								.getByteSync(carVoiceUrl, null);
						voicePath = WeChatUtil.saveFile(
								Constant.VOICE_FILE_PATH, "/" + msgId + ".mp3",
								bytes);
					}

					if (!TextUtils.isEmpty(voicePath)) {// 这条文本消息是
														// 另一台车机发送的语音，进行转换
						receiveMsgVO.setVoiceUrl(voicePath);
						receiveMsgVO.setMsgType(Constant.MSGTYPE_VOICE);
						receiveMsgVO.setContent("【语音】");
						if (!sendByMyself) {
							receiveMsgVO.setRead_status(false);
						}
					} else if (!TextUtils.isEmpty(carPoiUrl)) {// 这条文本消息是
																// 另一台车机发送的位置，进行转换
						receiveMsgVO.setUrl(carPoiUrl);
						receiveMsgVO.setMsgType(Constant.MSGTYPE_GET_LOCATION);
						receiveMsgVO.setContent(WeChatApplication.getContext()
								.getString(R.string.mark_position)
								+ WeChatUtil.removeCarPoi(receiveMsgVO
										.getContent()));
					} else {
						receiveMsgVO.setContent(msgContent.replaceAll("<br/>",
								"\n"));
					}
					break;

				case Constant.MSGTYPE_VOICE:// 语音消息
											// 下载并保存音频文件，将文件路径保存在voiceUrl字段中
					if (!sendByMyself) {
						receiveMsgVO.setContent(WeChatApplication.getContext()
								.getString(R.string.mark_voice));
						String voiceUrl = WeChatUtil.getProp("VOID_URL",
								referer)
								+ "?msgid="
								+ msgId
								+ "&skey="
								+ prop.getSkey();
						byte[] b = getResource(voiceUrl).getData();
						String _voicePath = WeChatUtil.saveFile(
								Constant.VOICE_FILE_PATH, "/" + msgId + ".amr",
								b);
						receiveMsgVO.setVoiceUrl(_voicePath);
						receiveMsgVO.setRead_status(false);
					} else {
						// 对于自己手机端发送的语音，车机端用文本代替
						receiveMsgVO.setContent(WeChatApplication.getContext()
								.getString(R.string.mark_voice));
						receiveMsgVO.setMsgType(Constant.MSGTYPE_TEXT);
						statusNotify();
					}
					break;

				case Constant.MSGTYPE_EMOTICON:// 动画表情
					if (!sendByMyself) {
						receiveMsgVO.setContent(WeChatApplication.getContext()
								.getString(R.string.mark_emot_icon)
								+ receiveMsgVO.getContent());
						String imageUrl = WeChatUtil.getProp("IMAGE_URL",
								referer)
								+ "?msgid="
								+ msgId
								+ "&skey="
								+ prop.getSkey();
						byte[] b = getResource(imageUrl).getData();
						String imagePath = WeChatUtil.saveFile(
								Constant.IMAGE_FILE_PATH,
								"/" + System.currentTimeMillis() + ".gif", b);
						receiveMsgVO.setImageUrl(imagePath);
					} else {// 对于自己手机端发送的动画表情，车机端用文本代替
						receiveMsgVO.setContent(WeChatApplication.getContext()
								.getString(R.string.mark_emot_icon));
						receiveMsgVO.setMsgType(Constant.MSGTYPE_TEXT);
						statusNotify();
					}
					break;

				case Constant.MSGTYPE_IMAGE:// 图片消息 获取到图片地址，保存到imageUrl字段中
					if (!sendByMyself) {
						receiveMsgVO.setContent(WeChatApplication.getContext()
								.getString(R.string.mark_image));
						String imageUrl = WeChatUtil.getProp("IMAGE_URL",
								referer)
								+ "?msgid="
								+ msgId
								+ "&skey="
								+ prop.getSkey();
						receiveMsgVO.setImageUrl(imageUrl);
					} else {
						// 对于自己手机端发送的照片，车机端用文本代替
						receiveMsgVO.setContent(WeChatApplication.getContext()
								.getString(R.string.mark_image));
						receiveMsgVO.setMsgType(Constant.MSGTYPE_TEXT);
						statusNotify();
					}
					break;

				case Constant.MSGTYPE_SHARE: // 自己手机端发送的分享，无法同步
					// app分享消息 获取图片信息、title及内容
					if (!sendByMyself) {
						if ("微信转账".equals(receiveMsgVO.getFileName())) {
							receiveMsgVO.setMsgType(Constant.MSGTYPE_SYS);
							receiveMsgVO.setContent("收到一笔微信转账，请在手机上查看！");
						} else {
							receiveMsgVO.setMsgType(Constant.MSGTYPE_SYS);
							receiveMsgVO.setContent("收到分享，请在手机上查看");
						}
					} else {
						ignore = true;
					}

					break;

				case Constant.MSGTYPE_VIDEO:// 视频
					if (!sendByMyself) {
						receiveMsgVO.setContent(WeChatApplication.getContext()
								.getString(R.string.tip_receive_video));
					} else {
						// 对于自己手机端发送的视频，车机端用文本代替
						String videoImageUrl1 = WeChatUtil.getProp("IMAGE_URL",
								referer)
								+ "?MsgId="
								+ msgId
								+ "&skey="
								+ prop.getSkey() + "&type=slave";
						receiveMsgVO.setImageUrl(videoImageUrl1);
						receiveMsgVO.setContent(WeChatApplication.getContext()
								.getString(R.string.mark_video));
						receiveMsgVO.setMsgType(Constant.MSGTYPE_TEXT);
						String videoUrl1 = WeChatUtil.getProp("VIDEO_URL",
								referer)
								+ "?msgid="
								+ msgId
								+ "&skey="
								+ prop.getSkey();
						receiveMsgVO.setVideoUrl(videoUrl1);
						statusNotify();
					}
					break;

				case Constant.MSGTYPE_MICROVIDEO:// 微视频
					if (!sendByMyself) {
						receiveMsgVO.setContent(WeChatApplication.getContext()
								.getString(R.string.tip_receive_video));

					} else {
						// 对于自己手机端发送的微视频，车机端用文本代替
						String videoImageUrl = WeChatUtil.getProp("IMAGE_URL",
								referer)
								+ "?MsgId="
								+ msgId
								+ "&skey="
								+ prop.getSkey() + "&type=slave";
						receiveMsgVO.setImageUrl(videoImageUrl);
						receiveMsgVO.setContent(WeChatApplication.getContext()
								.getString(R.string.mark_mcr_video));

						receiveMsgVO.setMsgType(Constant.MSGTYPE_TEXT);
					}
					break;

				case Constant.MSGTYPE_STATUSNOTIFY:// 通知消息
					ignore = true;
					Log.d(TAG,
							"sys-MSGTYPE_STATUSNOTIFY = "
									+ receiveMsgVO.getContent());
					statusNotify();
					break;

				case Constant.MSGTYPE_SYS:// 系统消息
					Log.d(TAG, "sys-msg = " + receiveMsgVO.getContent());
					receiveMsgVO.setMsgType(Constant.MSGTYPE_SYS);
					receiveMsgVO.setContent(WeChatUtil.removeHref(receiveMsgVO
							.getContent()));
					break;

				case Constant.MSGTYPE_GET_LOCATION:// 共享位置
					if (!sendByMyself) {
						receiveMsgVO.setContent(WeChatApplication.getContext()
								.getString(R.string.mark_position)
								+ receiveMsgVO.getContent());
						String imageUrl = WeChatUtil.getProp("LOC_IMAGE_URL",
								referer)
								+ "?msgid="
								+ msgId
								+ "&url=xxx&pictype=location";
						receiveMsgVO.setImageUrl(imageUrl);
					} else {
						// 对于自己手机端发送的位置，车机端用文本代替
						receiveMsgVO.setContent(WeChatApplication.getContext()
								.getString(R.string.mark_position));
						receiveMsgVO.setMsgType(Constant.MSGTYPE_TEXT);
					}
					break;

				case Constant.MSGTYPE_SHARECARD:// 名片
					if (!sendByMyself) {
						receiveMsgVO.setContent("收到名片，请在手机上查看");
						receiveMsgVO.setMsgType(Constant.MSGTYPE_SYS);
					} else {
						// 对于自己手机端发送的名片，车机端用文本代替
						receiveMsgVO.setContent("【名片】");
						receiveMsgVO.setMsgType(Constant.MSGTYPE_TEXT);
					}
					break;

				default:
					ignore = true;
					break;
				}
				if (!ignore) {
					resultMsgList.add(receiveMsgVO);
				}
			}
			return resultMsgList;
		}
		return null;
	}

	/**
	 * 批量获取联系人，比如某个好友更新了昵称，则调用以下方法，又如群成员发生了变化，也需要调用以下方法更新成员信息
	 * 
	 * @param prams
	 * @return 更新后的成员信息
	 * @throws IOException
	 * @throws JSONException
	 */
	public List<FriendVo> getBatchContact(List<BatchgetcontactRequestVo> prams)
			throws IOException, JSONException {
		Log.d(TAG, "=============>getBatchContact");
		// 组装请求参数
		Map<String, Object> attribute = new HashMap<String, Object>();
		Map<String, Object> baseRequest = new HashMap<String, Object>();
		baseRequest.put("Uin", prop.getWxuin());
		baseRequest.put("Sid", prop.getWxsid());
		baseRequest.put("Skey", prop.getSkey());
		baseRequest.put("DeviceID", getLocalId());
		attribute.put("BaseRequest", baseRequest);
		attribute.put("List", prams);
		attribute.put("Count", prams.size());
		// 请求地址
		String getRoomInfoUrl = WeChatUtil
				.getProp("GET_BATCH_CONTACT", referer)
				+ "&r="
				+ System.currentTimeMillis()
				+ "&pass_ticket="
				+ prop.getPassTicket();

		String getRoomInfoRet = OkHttpUtil.postJsonSync(getRoomInfoUrl, null,
				JSONUtils.toJson(attribute), null);

		// FileUtil.writeToFile("/sdcard/wechatresult/updateRoomInfo_attribute"
		// + System.currentTimeMillis(), JSONUtils
		// .toJson(attribute));
		// FileUtil.writeToFile("/sdcard/wechatresult/updateRoomInfo_getRoomInfoRet"
		// + System.currentTimeMillis(),
		// getRoomInfoRet);

		JSONObject userFriendInfoObj = new JSONObject(getRoomInfoRet);
		if (0 != userFriendInfoObj.getJSONObject("BaseResponse").getInt("Ret")) {
			return null;
		}
		List<FriendVo> friendVos = JSONUtils.fromJson(
				userFriendInfoObj.getString("ContactList"),
				new TypeToken<List<FriendVo>>() {
				});
		if (friendVos != null && !friendVos.isEmpty()) {
			for (FriendVo friendVo : friendVos) {
				String userName = friendVo.getUserName();
				String headImageUrl = friendVo.getHeadImgUrl();
				friendVo.setHeadImgUrl(getFullUrl(headImageUrl));
				if (userName.startsWith("@@")) {
					// 当查询到的好友信息是群组时，获取群组成员的信息
					List<FriendVo> memberList = friendVo.getMemberList();

					if (memberList != null && !memberList.isEmpty()) {
						// 查询群成员信息
						List<BatchgetcontactRequestVo> requestVOs = new ArrayList<BatchgetcontactRequestVo>();
						for (FriendVo vo : memberList) {
							String name = vo.getUserName();

							BatchgetcontactRequestVo requestVO = new BatchgetcontactRequestVo();
							requestVO.setUserName(name);
							requestVO.setEncryChatRoomId(friendVo
									.getEncryChatRoomId());
							requestVOs.add(requestVO);
						}

						int page = 0;
						if (requestVOs.size() % 50 == 0) {
							page = requestVOs.size() / 50;
						} else {
							page = requestVOs.size() / 50 + 1;
						}
						int size = requestVOs.size();
						List<FriendVo> members = new ArrayList<FriendVo>();
						for (int i = 0; i < page; i++) {
							int end = (i + 1) * 50;
							if ((i + 1) * 50 >= size) {
								end = size;
							}
							List<BatchgetcontactRequestVo> subrequestVOs = requestVOs
									.subList(i * 50, end);
							List<FriendVo> subMembers = getBatchContact(subrequestVOs);
							if (subMembers != null && !subMembers.isEmpty()) {
								members.addAll(subMembers);
							}
						}
						friendVo.setMemberList(members);
					}

					FriendVo oldFriendVo = mAllFriendsMap.get(userName);
					if (oldFriendVo != null) {
						friendVo.setNickName(oldFriendVo.getNickName());
					}

					if (!mGroupFriendMap.containsKey(userName)) {
						mGroupFriendMap.put(userName, friendVo);
					}
					mAllFriendsMap.put(userName, friendVo);
				}
			}
		}
		return friendVos;
	}

	@Override
	public Response<Integer> sendChatMsg(ReceiveMsgVO msg) throws IOException,
			JSONException {
		Response<Integer> response = new Response<Integer>();
		if (!init) { // 没有初始化
			response.setRetCode(1);
			return response;
		}

		Map<String, Object> e = new HashMap<String, Object>();
		int msgType = msg.getMsgType();
		e.put("Type", msgType);
		e.put("FromUserName", myId);
		e.put("ToUserName", msg.getToUserName());
		String localId = getLocalId();
		e.put("LocalID", localId);
		e.put("ClientMsgId", localId);
		String timeCurrent = System.currentTimeMillis() + "";
		Log.d(TAG, "timeCurrent。length = " + timeCurrent.length() + "    "
				+ timeCurrent);
		if (timeCurrent.length() > 9) {
			e.put("CreateTime", timeCurrent.substring(0, 9));
		}
		String url = WeChatUtil.getProp("SEND_MESSAGE", referer);
		switch (msgType) {
		case Constant.MSGTYPE_TEXT:// 文字
			e.put("Content", msg.getContent());
			url = WeChatUtil.getProp("SEND_MESSAGE", referer);
			Log.d(TAG, "sendChatMsg: url=" + url);
			break;
		case Constant.MSGTYPE_IMAGE:// 图片
			e.put("MediaId", msg.getMediaId());
			url = WeChatUtil.getProp("SEND_IMAGE_MSG", referer) + "&"
					+ "pass_ticket=" + prop.getPassTicket();
			break;
		default:
			break;
		}

		ResquestModel sm = new ResquestModel(prop, deviceId);
		sm.add("Msg", e);

		String ret = OkHttpUtil.postJsonSync(url, null, sm.toJson(), null);

		JSONObject jo = new JSONObject(ret);
		JSONObject baseVo = jo.getJSONObject("BaseResponse");
		if (null == baseVo) {
			response.setRetCode(1);
			init = false;
			return response;
		}
		int retCode = baseVo.getInt("Ret");
		if (retCode == 1201) {
			response.setData(1);
		} else if (retCode == 0) {
			response.setData(0);
		} else {
			response.setData(1);
		}
		return response;
	}

	@Override
	public Response<byte[]> getResource(String url) throws IOException {
		Response<byte[]> ret = new Response<byte[]>();
		if (url.indexOf("webwxgetvoice") != -1) {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Accept",
					"audio/webm,audio/ogg,audio/wav,audio/*;q=0.9,application/ogg;q=0.7,video/*;"
							+ "q=0.6,*/*;q=0.5");
			ret.setData(OkHttpUtil.getByteSync(url, null, headers));
		} else if (url.indexOf("webwxgetvideo") != -1) {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Accept-Encoding", "identity;q=1, *;q=0");
			headers.put("Range", "bytes=0-");
			ret.setData(OkHttpUtil.getByteSync(url, null, headers));
		} else {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Accept", "image/png,image/*;q=0.8,*/*;q=0.5");
			ret.setData(OkHttpUtil.getByteSync(url, null, headers));
		}
		return ret;
	}

	private int delInitContent(JSONObject jo) throws JSONException, IOException {
		syncKey = JSONUtils.fromJson(jo.getString("SyncKey"), SyncKey.class);
		myInfo = JSONUtils.fromJson(jo.getString("User"), FriendVo.class);
		String headImgUrl = myInfo.getHeadImgUrl();
		myInfo.setHeadImgUrl(getFullUrl(headImgUrl));
		myId = myInfo.getUserName();
		mAllFriends.add(myInfo);
		mAllFriendsMap.put(myId, myInfo);
		String chatSet = jo.getString("ChatSet");

		String[] chatsArr = chatSet.split(",");

		Set<String> chats = new HashSet<String>();

		for (String chat : chatsArr) {
			chats.add(chat);
		}
		List<FriendVo> contactList = JSONUtils.fromJson(
				jo.getString("ContactList"), new TypeToken<List<FriendVo>>() {
				});

		if (contactList != null && !contactList.isEmpty()) {

			for (FriendVo friendVo : contactList) {
				String userName = friendVo.getUserName();
				if (userName.startsWith("@@")
						&& (friendVo.getMemberCount() == 0 || !friendVo
								.getMemberList().contains(myInfo))) {
					continue;
				}
				if (WeChatUtil.isNotFriend(friendVo)) {
					continue;
				}
				String headImgurl = getFullUrl(friendVo.getHeadImgUrl());
				friendVo.setHeadImgUrl(headImgurl);

				if (!mContactFriends.contains(friendVo)) {
					mContactFriends.add(friendVo);
				}
				if (!mAllFriendsMap.containsKey(friendVo)) {
					mAllFriendsMap.put(userName, friendVo);
				}
			}
		}
		return 0;
	}

	class HttpCall implements HttpCallback {

		private String redirUrl;

		@Override
		public void before(HttpClient client, HttpUriRequest request) {
		}

		@Override
		public void after(HttpClient client, HttpUriRequest request,
				HttpResponse response) {
			Header[] hs = response.getHeaders("Location");
			if (null != hs && hs.length >= 1) {
				redirUrl = hs[0].getValue();
			}

		}

		public String getRedirUrl() {
			return redirUrl;
		}

	}

	private static String getLocalId() {
		return System.currentTimeMillis() + "0" + WeChatUtil.getRandomNum(3);
	}

	@Override
	public Response<FriendVo> getMyInfo() {
		Response<FriendVo> resp = new Response<FriendVo>();
		if (init == false) { // 还没有初始化
			resp.setRetCode(1);
			resp.setData(new FriendVo());
			return resp;
		}
		resp.setData(myInfo);
		return resp;
	}

	@Override
	public void logOut() throws IOException {
		if (TextUtils.isEmpty(referer)) {
			return;
		}
		String url = WeChatUtil.getProp("LOG_OUT", referer)
				+ "?redirect=1&type=0&skey=" + prop.getSkey() + "&sid="
				+ prop.getWxsid();
		String ret = OkHttpUtil.getStringSync(url, null);
	}

	/**
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public Integer getAllFriend() throws IOException, JSONException {
		Log.d(TAG, "getAllFriend: =====");
		if (null == prop) {
			Log.d(TAG, "getAllFriend() prop is null");
			return -1;
		}
		String getFriend = WeChatUtil.getProp("GET_CONTACT", referer)
				+ "?skey=" + prop.getSkey() + "&r="
				+ System.currentTimeMillis();

		String ret = OkHttpUtil.getStringSync(getFriend, null, null);

		// FileUtil.writeToFile("/sdcard/wechatresult/getAllFriend" +
		// System.currentTimeMillis(), ret);

		JSONObject userFriendInfoObj = new JSONObject(ret);

		if (0 != userFriendInfoObj.getJSONObject("BaseResponse").getInt("Ret")) {
			return 1;
		}
		List<FriendVo> memberList = JSONUtils.fromJson(
				userFriendInfoObj.getString("MemberList"),
				new TypeToken<List<FriendVo>>() {
				});
		if (memberList != null && !memberList.isEmpty()) {
			for (FriendVo friendVo : memberList) {
				if (WeChatUtil.isNotFriend(friendVo)) {
					continue;
				}
				String headImg = getFullUrl(friendVo.getHeadImgUrl());
				friendVo.setHeadImgUrl(headImg);
				mAllFriends.add(friendVo);
				if (!mAllFriendsMap.containsKey(friendVo.getUserName())) {
					mAllFriendsMap.put(friendVo.getUserName(), friendVo);
				}
			}
		}

		uploadContact();
		return 0;
	}

	private void uploadContact() {
		Log.d(TAG, "uploadContact: ==== " + mAllFriendsMap.size());
		Collection<FriendVo> c = mAllFriendsMap.values();
		BridgeIntentResponse.getInstance().uploadContact(c);

	}

	/**
	 * 状态通知，如当手机端发送语音时，收到心跳包返回2，如果不去获取该音频，则心跳包不停的返回2，调用以下方法则可避免。
	 */
	private void statusNotify() {
		String webwxstatusnotifyUrl = WeChatUtil.getProp("NOTIFY_PHONE",
				referer);

		ResquestModel r = new ResquestModel(prop, deviceId);
		r.add("ClientMsgId", System.currentTimeMillis());
		r.add("Code", 3);
		r.add("FromUserName", myId);
		r.add("ToUserName", myId);

		try {
			String content = r.toJson();
			String ret = OkHttpUtil.postJsonSync(webwxstatusnotifyUrl, null,
					content, null);
			Log.i(TAG, "ret:" + ret + "|" + "content:" + content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getFullUrl(String url) {
		if (url.startsWith("http")) {
			return url;
		}
		String host = WeChatUtil.getHost(referer);
		String headImg = "";
		if (url.startsWith("/")) {
			headImg = host + url.substring(1);
		} else {
			headImg = host + url;
		}
		return headImg;
	}
}
