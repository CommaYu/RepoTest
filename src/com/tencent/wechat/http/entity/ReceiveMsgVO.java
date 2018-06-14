package com.tencent.wechat.http.entity;

import java.io.Serializable;

/**
 * 接收信息VO
 *
 * @author hdzhang
 */
public class ReceiveMsgVO implements Serializable {

    private static final long serialVersionUID = -4678573581003052817L;

    //消息ID
    private String MsgId;
    //消息来源
    private String FromUserName;
    //接收人
    private String ToUserName;
    //发送人，对于群消息，FromUserName为群id,sendername为发送者id
    private String senderName;
    //消息类型
    private Integer MsgType;
    //消息内容
    private String Content;
    //APP分享消息title
    private String FileName;
    //APP分享消息内容
    private String FileNameContent;
    //多媒体文件ID
    private String MediaId;
    //链接地址
    private String Url;
    //本地音频地址，如果收到音频消息，则下载音频文件，保存在以下的本地sd卡
    private String voiceUrl;
    //图片地址
    private String imageUrl;
    //视频地址
    private String videoUrl;
    //多媒体资源本地地址
    private String localMediaUrl;
    //通知者
    private String StatusNotifyUserName;
    //是否群消息
    private boolean isGroupMsg;
    //消息发送状态 1:发送成功，2：发送中，3：发送失败
    private Integer sendState;
    //消息发送/接收时间
    private Long msgTime;

    //消息的时间戳
    private long CreateTime;

    //接收的消息是否已读
    public boolean read_status = true;

    //如果是语音显示长度
    private Long VoiceLength;

    //对于发送的消息，是否成功发送  默认为true
    private boolean sendSuccess=true;

    public boolean isSendSuccess() {
        return sendSuccess;
    }

    public void setSendSuccess(boolean sendSuccess) {
        this.sendSuccess = sendSuccess;
    }

    public Long getVoiceLength() {
        return VoiceLength;
    }

    public void setVoiceLength(Long voiceLength) {
        VoiceLength = voiceLength;
    }

    public Long getMsgTime() {
        return msgTime;
    }

    public void setMsgTime(Long msgTime) {
        this.msgTime = msgTime;
    }

    public Integer getSendState() {
        return sendState;
    }

    public void setSendState(Integer sendState) {
        this.sendState = sendState;
    }

    public boolean isRead_status() {
        return read_status;
    }

    public void setRead_status(boolean read_status) {
        this.read_status = read_status;
    }

    public String getMsgId() {
        return MsgId;
    }

    public void setMsgId(String msgId) {
        MsgId = msgId;
    }

    public String getFromUserName() {
        return FromUserName;
    }

    public void setFromUserName(String fromUserName) {
        FromUserName = fromUserName;
    }

    public String getToUserName() {
        return ToUserName;
    }

    public void setToUserName(String toUserName) {
        ToUserName = toUserName;
    }

    public Integer getMsgType() {
        return MsgType;
    }

    public void setMsgType(Integer msgType) {
        MsgType = msgType;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public String getMediaId() {
        return MediaId;
    }

    public void setMediaId(String mediaId) {
        MediaId = mediaId;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getStatusNotifyUserName() {
        return StatusNotifyUserName;
    }

    public void setStatusNotifyUserName(String statusNotifyUserName) {
        StatusNotifyUserName = statusNotifyUserName;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public boolean isGroupMsg() {
        return isGroupMsg;
    }

    public void setGroupMsg(boolean isGroupMsg) {
        this.isGroupMsg = isGroupMsg;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFileNameContent() {
        return FileNameContent;
    }

    public void setFileNameContent(String fileNameContent) {
        FileNameContent = fileNameContent;
    }

    public String getLocalMediaUrl() {
        return localMediaUrl;
    }

    public void setLocalMediaUrl(String localMediaUrl) {
        this.localMediaUrl = localMediaUrl;
    }

    public long getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(long createTime) {
        CreateTime = createTime;
    }

}
