package network.packets;

import main.Main;
import network.CipherUtils;
import yapion.annotations.object.YAPIONData;
import yapion.annotations.object.YAPIONPostDeserialization;
import yapion.annotations.object.YAPIONPreSerialization;
import yapion.exceptions.YAPIONException;

import java.security.PublicKey;

@YAPIONData
public class MessageObject {
    private transient String internalMessage;
    private String message = null;
    public transient PublicKey publicKey;

    public MessageObject(String message, PublicKey publicKey) {
        this.internalMessage = message;
        this.publicKey = publicKey;
    }

    public String getMessage() {
        return internalMessage;
    }

    @YAPIONPreSerialization
    private void encrypt() {
        try {
            message = CipherUtils.encrypt(internalMessage, publicKey);
        } catch (Exception e) {
            throw new YAPIONException("Could not Encrypt", e);
        }
    }

    @YAPIONPostDeserialization
    private void decrypt() {
        try {
            internalMessage = CipherUtils.decrypt(message, Main.pair.getPrivate());
        } catch (Exception e) {
            throw new YAPIONException("Could not Decrypt", e);
        }
    }
}
