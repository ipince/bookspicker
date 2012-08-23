package com.bookspicker.server.services;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.bookspicker.server.queries.Base64;

public class SigningUtil {
	
	private static final String ALG = "HmacSHA256";
	private static final String UTF8_CHARSET = "UTF-8";
	private static final String SECRET_KEY = "1775069671"; // our password's hashcode
	
	private static Mac signer;
	
	static { init(); }
	
	private static void init() {
		try {
			Mac mac = Mac.getInstance(ALG);
			byte[] secretBytes = SECRET_KEY.getBytes(UTF8_CHARSET);
			SecretKeySpec sks = new SecretKeySpec(secretBytes, UTF8_CHARSET);
			mac.init(sks);
			signer = mac;
			return;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		signer = null; // on failure
	}
	
	public static String sign(String toSign) {
		if (signer != null) {
			try {
				byte[] rawHmac = signer.doFinal(toSign.getBytes(UTF8_CHARSET));
				String signature = new String(Base64.encodeBytes(rawHmac));
				return signature;
			} catch (UnsupportedEncodingException e) {
				// TODO wtf man!
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param signed cannot be null
	 * @param signature cannot be null
	 * @return
	 */
	public static boolean checkSignature(String signed, String signature) {
		return signature.equals(sign(signed));
	}
}
