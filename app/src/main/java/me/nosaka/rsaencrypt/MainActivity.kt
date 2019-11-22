package me.nosaka.rsaencrypt

import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.security.KeyPair
import java.util.*


class MainActivity : AppCompatActivity() {

    sealed class Source(open val keyPair: KeyPair) {
        data class Generate(override val keyPair: KeyPair, val date: Date) : Source(keyPair)
        data class Restore(override val keyPair: KeyPair, val filename: String) : Source(keyPair)
    }

    private lateinit var source: Source

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        generateKeyPair()

        regenerateKeyPareButton.setOnClickListener {
            generateKeyPair()
            scrollView.scrollToDescendant(publicKeyTextView)
        }
        saveKeyPareButton.setOnClickListener {
            saveKeyPair()
            scrollView.scrollToDescendant(publicKeyTextView)
        }
        restoreKeyPairButton.setOnClickListener {
            restoreKeyPair()
            scrollView.scrollToDescendant(publicKeyTextView)
        }
        encryptButton.setOnClickListener {
            encrypted()
            scrollView.scrollToDescendant(encryptedTextView)
        }
        decryptButton.setOnClickListener {
            decrypted()
            scrollView.scrollToDescendant(decryptTextView)
        }

    }

    private fun setupKeyPairInformation() {
        val source = source
        privateKeyTextView.text =
            String(Base64.encode(source.keyPair.private.encoded, Base64.NO_WRAP))
        publicKeyTextView.text =
            String(Base64.encode(source.keyPair.public.encoded, Base64.NO_WRAP))
        val hint = when (source) {
            is Source.Generate -> {
                "${DateFormat.format("yyyy-MM-dd HH:mm:ss", source.date)}に生成した鍵"
            }
            is Source.Restore -> {
                "「${source.filename}」からリストアした鍵"
            }
        }
        privateKeyHintTextView.text = hint
        publicKeyHintTextView.text = hint
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateKeyPair() {
        source = Source.Generate(RSAUtil.generateKeyPair(), Date())
        setupKeyPairInformation()
        showToast("新しい鍵を作成しました")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun restoreKeyPair() {
        val items = RSAUtil.gePrivateKeyFileList(this)
        AlertDialog.Builder(this)
            .setItems(items.toTypedArray()) { _, position ->
                val filename = items[position]
                source = Source.Restore(RSAUtil.restore(this, filename), filename)
                setupKeyPairInformation()
                showDialog("「$filename」\nで現在の鍵をリストアしました")
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun saveKeyPair() {
        val filename = RSAUtil.saveKeyPair(this, source.keyPair)
        showDialog("「$filename」\nで保存しました")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun encrypted() {
        val text = inputEditText.text.toString()
        if (text.isEmpty()) {
            showDialog("文字を入力してから試そうね？")
            return
        }
        val source = source

        val encrypted: ByteArray = RSAUtil.encrypt(text, source.keyPair.public)
        encryptedTextView.text = String(Base64.encode(encrypted, Base64.NO_WRAP))

        val hint = when (source) {
            is Source.Generate -> {
                "${DateFormat.format("yyyy-MM-dd HH:mm:ss", source.date)}に生成した公開鍵で暗号化"
            }
            is Source.Restore -> {
                "「${source.filename}」からリストアした公開鍵で暗号化"
            }
        }
        encryptedHintTextView.text = hint
        showToast("暗号化成功")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun decrypted() {
        val text = encryptedTextView.text.toString()
        if (text.isEmpty()) {
            showDialog("暗号化してから試そうね？")
            return
        }
        val source = source
        try {
            val decrypted =
                RSAUtil.decrypt(Base64.decode(text, Base64.NO_WRAP), source.keyPair.private)
            decryptTextView.text = String(decrypted)
            showToast("複合化成功")
        } catch (e: Exception) {
            showDialog("複合化できませんでした\n暗号化に使用した公開鍵と対になる秘密鍵でないと複合化できません")
        }

    }

}
