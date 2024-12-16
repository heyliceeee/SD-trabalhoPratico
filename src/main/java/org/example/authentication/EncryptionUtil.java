package org.example.authentication;

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class EncryptionUtil {

    // Caminhos para as chaves
    private static final String PUBLIC_KEY_FILE = "public.key";
    private static final String PRIVATE_KEY_FILE = "private.key";

    // Gera as chaves RSA e guarda nos ficheiros
    public static void generateKeys() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // Salvar a chave pública e privada
            saveKeyToFile(PUBLIC_KEY_FILE, keyPair.getPublic().getEncoded());
            saveKeyToFile(PRIVATE_KEY_FILE, keyPair.getPrivate().getEncoded());

            System.out.println("Chaves RSA geradas e guardadas com sucesso!");
        } catch (NoSuchAlgorithmException | IOException e) {
            System.err.println("Erro ao gerar as chaves: " + e.getMessage());
        }
    }


    // Criptografa a senha com a chave pública
    public static String encryptPassword(String password) {
        try {
            // Carregar a chave pública
            PublicKey publicKey = loadPublicKey();

            // Configurar o Cipher para criptografia
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(password.getBytes());

            // Retornar a senha criptografada como Base64
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Salva a chave em um arquivo
    private static void saveKeyToFile(String fileName, byte[] keyBytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(keyBytes);
        }
    }

    // Carrega a chave pública do arquivo
    private static PublicKey loadPublicKey() throws Exception {
        FileInputStream fis = new FileInputStream(PUBLIC_KEY_FILE);
        byte[] keyBytes = fis.readAllBytes();
        fis.close();

        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }
}
