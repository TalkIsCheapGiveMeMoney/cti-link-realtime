package com.tinet.ctilink.realtime.ivr;

import com.tinet.ctilink.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;

public class DecryptBase64DESServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.doPost(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		String input = request.getParameter("input");
		String key = request.getParameter("key");
		if(StringUtils.isEmpty(input) || StringUtils.isEmpty(key)){
			out.append("{\"func_output\":\"\"}");
			out.flush();
			out.close();
		}
		byte[] bt = null;
		try {
			sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
			bt = decoder.decodeBuffer(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String output = "";

		try {
			// DES算法要求有一个可信任的随机数源
			SecureRandom sr = new SecureRandom();
			DESKeySpec dks;
			dks = new DESKeySpec(key.getBytes());

			// 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = keyFactory.generateSecret(dks);
			// Cipher对象实际完成加密操作
			Cipher cipher = Cipher.getInstance("DES");
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.DECRYPT_MODE, secretKey, sr);
			// 正式执行解密操作
			byte decryptedData[] = cipher.doFinal(bt);
			output = new String(decryptedData);
			System.out.println("解密后===>" + output);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject jsonObject = new JSONObject();

		jsonObject.put("func_output", output);
		out.append(jsonObject.toString());
		out.flush();
		out.close();
	}

	public static void main(String[] argv) {
		String key = "ty_mtwaimai_des_key";
		String input = "K/SOfE0KZk0=";//532800

		String output = "";
		byte[] bt = null;
		try {

			sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
			bt = decoder.decodeBuffer(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			// DES算法要求有一个可信任的随机数源
			SecureRandom sr = new SecureRandom();
			DESKeySpec dks;
			dks = new DESKeySpec(key.getBytes());

			// 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = keyFactory.generateSecret(dks);
			// Cipher对象实际完成加密操作
			Cipher cipher = Cipher.getInstance("DES");
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.DECRYPT_MODE, secretKey, sr);
			// 正式执行解密操作
			byte decryptedData[] = cipher.doFinal(bt);
			output = new String(decryptedData);
			System.out.println("解密后===>" + output);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
