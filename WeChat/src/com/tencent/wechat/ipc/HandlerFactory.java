package com.tencent.wechat.ipc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.ipc.handler.BaseHandler;

/**
 * Created by qxb-810 on 2016/12/9.
 */
public class HandlerFactory {

	private static final String PROJECT_INI_FILE_PATH = "handler.cfg";
	private static final String UTF8_BOM = "\uFEFF";

	private static JSONObject configJson;

	static {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(WeChatApplication
					.getContext().getAssets().open(PROJECT_INI_FILE_PATH),
					"UTF-8"));

			StringBuilder responseStrBuilder = new StringBuilder();
			String inputStr;

			// 处理UTF-8编码，不同编辑器可能出现的BOM头问题
			inputStr = reader.readLine();
			if (inputStr.startsWith(UTF8_BOM)) {
				inputStr = inputStr.substring(1);
			}

			do {
				// 删除新的一行开头的那些空格
				// 出于效率问题考虑，采用计算开头空格数，然后一次删除。
				int delNum = 0;
				while (delNum < inputStr.length()) {
					if (inputStr.charAt(delNum) == ' ')
						delNum++;
					else
						break;
				}
				if (delNum != 0) {
					if (delNum < inputStr.length()) {
						inputStr = inputStr
								.substring(delNum, inputStr.length());
					} else {
						inputStr = "";
					}
				}

				// 如果非注释行，则添加到responseStrBuilder
				if (!inputStr.startsWith("#")) {
					responseStrBuilder.append(inputStr);
				}
			} while ((inputStr = reader.readLine()) != null);

			configJson = new JSONObject(responseStrBuilder.subSequence(0,
					responseStrBuilder.length()).toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (Exception e) {
			}
		}
	}

	public static BaseHandler getHandler(String category) {

		BaseHandler ret = null;
		try {
			String className = configJson.getString(category);
			ret = (BaseHandler) Class.forName(className).newInstance();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}
}
