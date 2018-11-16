
package DataBase;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class PasswordEncrypt {
    public static String getMD5(String input) throws NoSuchAlgorithmException{
    MessageDigest mymd = MessageDigest.getInstance("MD5");
    byte[] messageDigest = mymd.digest(input.getBytes());
    BigInteger number = new BigInteger(1, messageDigest);
    String myhash = number.toString(16);
    while (myhash.length() < 32) {
        myhash = "0" + myhash;
    }
    return myhash;
    }   
}
