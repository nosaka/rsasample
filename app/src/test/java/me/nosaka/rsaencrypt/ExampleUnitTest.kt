package me.nosaka.rsaencrypt


import android.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Test
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val test = "ABCDEFG"
        val keyPair = RSAUtil.generateKeyPair()
        val encrypted = RSAUtil.encrypt(test, keyPair.public)
        val decrypted = RSAUtil.decrypt(encrypted, keyPair.private)


//        val pubKeyStr = String(Base64.encode(keyPair.public.encoded,Base64.DEFAULT))
//        val pubDecode = Base64.decode(pubKeyStr, Base64.DEFAULT)
        val spec = X509EncodedKeySpec(keyPair.public.encoded)
        val kf: KeyFactory = KeyFactory.getInstance("RSA")
        val publicKey: PublicKey = kf.generatePublic(spec)



        assertEquals(String(decrypted), test)
    }

}
