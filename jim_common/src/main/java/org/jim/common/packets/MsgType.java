package org.jim.common.packets;

public enum MsgType {
  /**
   * <pre>
   *歷史記錄類型
   * </pre>
   *
   * <code>MSG_TYPE_TEXT = 0;</code>
   */
  MSG_TYPE_HISTORY(-1),
  /**
   * <pre>
   *文本
   * </pre>
   *
   * <code>MSG_TYPE_TEXT = 0;</code>
   */
  MSG_TYPE_TEXT(0),
  /**
   * <pre>
   *图片
   * </pre>
   *
   * <code>MSG_TYPE_IMG = 1;</code>
   */
  MSG_TYPE_IMG(1),
  /**
   * <pre>
   *语音
   * </pre>
   *
   * <code>MSG_TYPE_VOICE = 2;</code>
   */
  MSG_TYPE_VOICE(2),
  /**
   * <pre>
   *视频
   * </pre>
   *
   * <code>MSG_TYPE_VIDEO = 3;</code>
   */
  MSG_TYPE_VIDEO(3),
  /**
   * <pre>
   *音乐
   * </pre>
   *
   * <code>MSG_TYPE_MUSIC = 4;</code>
   */
  MSG_TYPE_MUSIC(4),
  /**
   * <pre>
   *图文
   * </pre>
   *
   * <code>MSG_TYPE_NEWS = 5;</code>
   */
  MSG_TYPE_NEWS(5),
  /**
   * <pre>
   * 红包
   * </pre>
   *
   * <code>MSG_TYPE_REDPACKET = 6;</code>
   */
  MSG_TYPE_REDPACKET(6);

  public final int getNumber() {
    return value;
  }
  public static MsgType valueOf(int value) {
    return forNumber(value);
  }

  public static MsgType forNumber(int value) {
    switch (value) {
      case 0: return MSG_TYPE_TEXT;
      case 1: return MSG_TYPE_IMG;
      case 2: return MSG_TYPE_VOICE;
      case 3: return MSG_TYPE_VIDEO;
      case 4: return MSG_TYPE_MUSIC;
      case 5: return MSG_TYPE_NEWS;
      case 6: return MSG_TYPE_REDPACKET;
      default: return null;
    }
  }
  private final int value;

  private MsgType(int value) {
    this.value = value;
  }
}

