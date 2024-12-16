package org.example.authentication;

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class DecryptionUtil {
    private static final String PRIVATE_KEY_FILE = "private.key";

    // Descriptografa a senha com a chave privada
    public static String decryptPassword(String encryptedPassword) {
        try {
            // Carregar a chave privada
            PrivateKey privateKey = loadPrivateKey();

            // Configurar o Cipher para descriptografia
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));

            // Retornar a senha descriptografada como String
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Carrega a chave privada do arquivo
    private static PrivateKey loadPrivateKey() throws Exception {
        FileInputStream fis = new FileInputStream(PRIVATE_KEY_FILE);
        byte[] keyBytes = fis.readAllBytes();
        fis.close();

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }
}
