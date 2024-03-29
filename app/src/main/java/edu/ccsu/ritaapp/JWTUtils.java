package edu.ccsu.ritaapp;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;

public class JWTUtils {

    public static String decoded(String JWTEncoded) throws Exception {
        String JWTDecoded = "";
        try {
            String[] split = JWTEncoded.split("\\.");
            Log.d("JWT_DECODED", "Header: " + getJson(split[0]));
            Log.d("JWT_DECODED", "Body: " + getJson(split[1]));
            JWTDecoded = getJson(split[0]);
        } catch (UnsupportedEncodingException e) {
            //Error
        }
        return JWTDecoded;
    }


    private static String getJson(String strEncoded) throws UnsupportedEncodingException{
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
}