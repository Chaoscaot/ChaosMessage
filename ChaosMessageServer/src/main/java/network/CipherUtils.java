package network;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class CipherUtils {

    private CipherUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
    }

    public static String encrypt(String s, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = getCipher();
        cipher.init(Cipher.ENCRYPT_MODE, key);

        List<String> stringList = new ArrayList<>();
        byte[] buffer = new byte[200];
        int bufferIndex = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            buffer[bufferIndex++] = (byte)(c >> 8);
            buffer[bufferIndex++] = (byte)(c >> 0);
            if (bufferIndex < buffer.length) {
                continue;
            }
            bufferIndex = 0;
            stringList.add(Base64.getEncoder().encodeToString(cipher.doFinal(buffer)));
        }
        if (bufferIndex != 0) {
            stringList.add(Base64.getEncoder().encodeToString(cipher.doFinal(Arrays.copyOfRange(buffer, 0, bufferIndex))));
        }
        return String.join("", stringList);
    }

    public static String decrypt(String s, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = getCipher();
        cipher.init(Cipher.DECRYPT_MODE, key);

        StringBuilder st = new StringBuilder(s);
        List<String> stringList = new ArrayList<>();
        while (st.length() > 344) {
            stringList.add(st.substring(0, 344));
            st.delete(0, 344);
        }
        stringList.add(st.toString());

        st = new StringBuilder();
        for (String string : stringList) {
            byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(string));
            for (int i = 0; i < bytes.length / 2; i++) {
                st.append((char)(bytes[i * 2] << 8 | bytes[i * 2 + 1] & 0xFF));
            }
        }
        return st.toString();
    }

}
