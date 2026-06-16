package com.dodognoman.rungame.common.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

/**
 * 一次性工具：產生 ECDSA P-256 key pair。
 *
 * 執行方式（在 rungame 專案根目錄）：
 *   ./mvnw.cmd exec:java -Dexec.mainClass=com.dodognoman.rungame.common.util.ActuatorKeyGen -Dexec.classpathScope=compile
 *
 * 輸出：
 *   PUBLIC KEY  → 貼到 application.yaml 的 actuator.public-key
 *   PRIVATE KEY → 放進 App（永遠不上 git / 不傳送）
 */
public class ActuatorKeyGen {

    public static void main(String[] args) throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
        gen.initialize(256); // P-256
        KeyPair pair = gen.generateKeyPair();

        String pub = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        String prv = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());

        System.out.println("========== ACTUATOR KEY PAIR ==========");
        System.out.println();
        System.out.println("# application.yaml");
        System.out.println("actuator:");
        System.out.println("  public-key: " + pub);
        System.out.println();
        System.out.println("# 私鑰（放進呼叫端 App，絕對不要 commit）");
        System.out.println("PRIVATE KEY (Base64): " + prv);
        System.out.println();
        System.out.println("======================================");
    }
}
