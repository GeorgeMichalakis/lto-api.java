package com.ltonetwork.client.core;

import com.ltonetwork.client.types.Address;
import com.ltonetwork.client.types.Key;
import com.ltonetwork.client.types.KeyPair;
import com.ltonetwork.client.types.Signature;
import com.ltonetwork.client.utils.CryptoUtil;
import com.ltonetwork.client.utils.Encoder;

public class Account {

    private final Address address;
    private final KeyPair encrypt;
    private final KeyPair sign;

    public Account(Address address, KeyPair encrypt, KeyPair sign) {
        this.address = address;
        this.encrypt = encrypt;
        this.sign = sign;
    }

    public Address getAddressStruct() {
        return this.address;
    }

    public String getAddress(String encoding) {
        return encode(this.address.getAddress(), encoding);
    }

    public String getAddress() {
        return getAddress("base58");
    }

    public byte getChainId() {
        return this.address.getChainId();
    }

    public KeyPair getEncrypt() {
        return encrypt;
    }

    public KeyPair getSign() {
        return sign;
    }

    public Key getPublicSignKey() {
        return sign.getPublickey();
    }

    public Key getPublicEncryptKey() {
        return encrypt.getPublickey();
    }

    public Signature sign(String message) {
        if (sign == null || sign.getSecretkey() == null) {
            throw new RuntimeException("Unable to sign message; no secret sign key");
        }
        byte[] signature = CryptoUtil.crypto_sign_detached(message.getBytes(), sign.getSecretkey().getValueBytes());
        return new Signature(signature);
    }

    public Signature sign(byte[] message) {
        if (sign == null || sign.getSecretkey() == null) {
            throw new RuntimeException("Unable to sign message; no secret sign key");
        }
        byte[] signature = CryptoUtil.crypto_sign_detached(message, sign.getSecretkey().getValueBytes());
        return new Signature(signature);
    }

    public boolean verify(Signature signature, String message) {
        if (sign == null || sign.getPublickey() == null) {
            throw new RuntimeException("Unable to verify message; no public sign key");
        }

        return signature.verify(sign.getPublickey(), message);
    }

    public boolean verify(Signature signature, byte[] message) {
        if (sign == null || sign.getPublickey() == null) {
            throw new RuntimeException("Unable to verify message; no public sign key");
        }

        return signature.verify(sign.getPublickey(), message);
    }

    public byte[] encrypt(Account recipient, String message) {
        if (encrypt == null || encrypt.getSecretkey() == null) {
            throw new RuntimeException("Unable to encrypt message; no secret encryption key");
        }
        if (recipient.encrypt == null || recipient.encrypt.getPublickey() == null) {
            throw new RuntimeException("Unable to encrypt message; no public encryption key for recipient");
        }

        byte[] nonce = getNonce();

        byte[] retEncrypt = CryptoUtil.crypto_box(
                nonce,
                message.getBytes(),
                recipient.encrypt.getPublickey().getValueBytes(),
                encrypt.getSecretkey().getValueBytes()
        );

        byte[] ret = new byte[retEncrypt.length + nonce.length];
        System.arraycopy(retEncrypt, 0, ret, 0, retEncrypt.length);
        System.arraycopy(nonce, 0, ret, retEncrypt.length, nonce.length);

        return ret;
    }

    public byte[] decrypt(Account sender, byte[] ciphertext) {
        if (encrypt == null || encrypt.getSecretkey() == null) {
            throw new RuntimeException("Unable to decrypt message; no secret encryption key");
        }
        if (sender.encrypt == null || sender.encrypt.getPublickey() == null) {
            throw new RuntimeException("Unable to decrypt message; no public encryption key for recipient");
        }

        byte[] encryptedMessage = new byte[ciphertext.length - 24];
        System.arraycopy(ciphertext, 0, encryptedMessage, 0, ciphertext.length - 24);

        byte[] nonce = new byte[24];
        System.arraycopy(ciphertext, ciphertext.length - 24, nonce, 0, 24);

        return CryptoUtil.crypto_box_open(
                nonce,
                encryptedMessage,
                encrypt.getPublickey().getValueBytes(),
                sender.encrypt.getSecretkey().getValueBytes()
        );
    }

    protected byte[] getNonce() {
        return CryptoUtil.random_bytes(CryptoUtil.crypto_box_noncebytes());
    }

    protected static String encode(String string, String encoding) {
        if (encoding.equals("base58")) {
            string = Encoder.base58Encode(string);
        }

        if (encoding.equals("base64")) {
            string = Encoder.base64Encode(string);
        }

        return string;
    }

    protected static String encode(byte[] string, String encoding) {
        if (encoding.equals("base58")) {
            return Encoder.base58Encode(string);
        }

        if (encoding.equals("base64")) {
            return Encoder.base64Encode(string);
        }
        return null;
    }

    protected static String encode(String string) {
        return encode(string, "base58");
    }

    protected static String encode(byte[] string) {
        return encode(string, "base58");
    }

    protected static byte[] decode(String string, String encoding) {
        if (encoding.equals("base58")) {
            return Encoder.base58Decode(string);
        }

        if (encoding.equals("base64")) {
            return Encoder.base64Decode(string);
        }

        return null;
    }

    protected static byte[] decode(String string) {
        return decode(string, "base58");
    }
}