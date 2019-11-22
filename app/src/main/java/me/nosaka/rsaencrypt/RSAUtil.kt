package me.nosaka.rsaencrypt

import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import android.util.Base64
import androidx.annotation.RequiresApi
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher


@RequiresApi(Build.VERSION_CODES.M)
object RSAUtil {

    private const val DATE_TIME_FORMAT = "yyyyMMddHHmmss"

    private const val PRIVATE_KEY_FILE_SUFFIX = ".ppk"

    private const val PUBLIC_KEY_FILE_SUFFIX = ".pub"

    fun generateKeyPair(): KeyPair {

        val kg = KeyPairGenerator.getInstance("RSA")
        kg.initialize(1024)
        return kg.generateKeyPair()
    }

    fun saveKeyPair(context: Context, keyPair: KeyPair): String {
        val privateBase64 = Base64.encode(keyPair.private.encoded, Base64.NO_WRAP)
        val publicBase64 = Base64.encode(keyPair.public.encoded, Base64.NO_WRAP)
        val date = DateFormat.format(DATE_TIME_FORMAT, Date())

        val privateFile = "id_rsa_$date$PRIVATE_KEY_FILE_SUFFIX"
        val publicFile = "id_rsa_$date$PUBLIC_KEY_FILE_SUFFIX"

        context.openFileOutput(privateFile, Context.MODE_PRIVATE).use {
            it.write(privateBase64)
            it.flush()
        }
        context.openFileOutput(publicFile, Context.MODE_PRIVATE).use {
            it.write(publicBase64)
            it.flush()
        }
        return privateFile.removePrefix(PRIVATE_KEY_FILE_SUFFIX)
    }

    fun gePrivateKeyFileList(context: Context): List<String> {
        return context.fileList().filter {
            it.endsWith(PRIVATE_KEY_FILE_SUFFIX)
        }.map { it.removeSuffix(PRIVATE_KEY_FILE_SUFFIX) }
    }

    fun restore(context: Context, file: String): KeyPair {
        val privateFile = "$file$PRIVATE_KEY_FILE_SUFFIX"
        val publicFile = "$file$PUBLIC_KEY_FILE_SUFFIX"
        val privateKey = restoreRSAPrivateKey(context, privateFile)
        val publicKey = restoreRSAPublicKey(context, publicFile)
        return KeyPair(publicKey, privateKey)
    }

    private fun restoreRSAPrivateKey(context: Context, file: String): PrivateKey {
        val bytes: ByteArray = context.openFileInput(file).use {
            Base64.decode(it.readBytes(), Base64.NO_WRAP)
        }
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePrivate(PKCS8EncodedKeySpec(bytes))
        // Must use RSAPublicKeySpec or PKCS8EncodedKeySpec; was java.security.spec.X509EncodedKeySpec
    }

    private fun restoreRSAPublicKey(context: Context, file: String): PublicKey {
        val bytes: ByteArray = context.openFileInput(file).use {
            Base64.decode(it.readBytes(), Base64.NO_WRAP)
        }
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePublic(X509EncodedKeySpec(bytes))
    }

    fun encrypt(text: String, publicKey: PublicKey): ByteArray {
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(text.toByteArray())
    }

    @Throws(BadPaddingException::class)
    fun decrypt(bytes: ByteArray, privateKey: PrivateKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(bytes)
    }

}