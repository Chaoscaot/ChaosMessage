package network.packets;

import yapion.annotations.object.YAPIONData;
import yapion.annotations.object.YAPIONPostDeserialization;
import yapion.annotations.object.YAPIONPreSerialization;
import yapion.exceptions.YAPIONException;

import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.security.PublicKey;

@YAPIONData
public class MessageObject {
    private transient final String message;
    private byte[] bytes = null;
    private static transient PrivateKey privateKey;
    public static transient PublicKey publicKey;

    public MessageObject(String message) {
        this.message = message;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @YAPIONPreSerialization
    private void encrypt() {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            bytes = cipher.doFinal(message.getBytes());
        } catch (Exception e) {
            throw new YAPIONException("Could not Encrypt", e);
        }
    }

    @YAPIONPostDeserialization
    private void decrypt() {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            bytes = cipher.doFinal(message.getBytes());
        } catch (Exception e) {
            throw new YAPIONException("Could not Decrypt", e);
        }
    }
}
