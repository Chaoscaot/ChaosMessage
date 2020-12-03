package network;

import main.Main;
import yapion.annotations.object.YAPIONData;
import yapion.annotations.object.YAPIONPostDeserialization;
import yapion.annotations.object.YAPIONPreSerialization;
import yapion.exceptions.YAPIONException;

import javax.crypto.Cipher;
import java.security.PublicKey;

@YAPIONData
public class MessageObject {
    private transient String message;
    private byte[] bytes = null;
    public transient PublicKey publicKey;

    public MessageObject(String message, PublicKey publicKey) {
        this.message = message;
        this.publicKey = publicKey;
    }

    public String getMessage() {
        return message;
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
            cipher.init(Cipher.DECRYPT_MODE, Main.pair.getPrivate());
            bytes = cipher.doFinal(message.getBytes());
            message = new String(bytes);
        } catch (Exception e) {
            throw new YAPIONException("Could not Decrypt", e);
        }
    }
}
