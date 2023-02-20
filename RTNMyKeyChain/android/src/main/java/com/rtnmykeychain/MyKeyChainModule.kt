package com.rtnmykeychain

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.rtnmykeychain.NativeMyKeyChainSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.facebook.react.bridge.ReactContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class MyKeyChainModule(context: ReactApplicationContext): NativeMyKeyChainSpec(context) {
    override fun getName(): String {
        return NAME
    }

    companion object {
        const val NAME = "RTNMyAsyncStorage"
    }

    private val lifecycle: Lifecycle by lazy {
        ((context as ReactContext).currentActivity as AppCompatActivity).lifecycle
    }

    private val ANDROID_KEY_STORE = "AndroidKeyStore"
    val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    val SERVICE_NAME = "myservicename"
    val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
     val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
    val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    val FILE_NAME = "Keychain.txt"

    private val keystore = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
        load(null)
    }
    private lateinit var encryptCipher: Cipher
    private var myKeyChainPromise: Promise? = null

    override fun savePassword(password: String?, promise: Promise?) {
        myKeyChainPromise = promise
        lifecycle.coroutineScope.launch {
            password?.let { storePassword(it) }
        }
    }

    override fun getPassword(promise: Promise?) {
        myKeyChainPromise = promise
        lifecycle.coroutineScope.launch {
            getStoredPassword()
        }
    }

    override fun deletePassword(promise: Promise?) {
        myKeyChainPromise = promise
        lifecycle.coroutineScope.launch {
            deleteFile()
        }
    }

    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(
                    SERVICE_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true).build()
            )
        }.generateKey()
    }

    private fun getDecryptCipherForIv(iv: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        }
    }

    private fun getKey(): SecretKey {
        val existingKey = keystore.getEntry(
            SERVICE_NAME,
            null
        ) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private suspend fun encrypt(bytes: ByteArray, outputStream: OutputStream): ByteArray =
        withContext(Dispatchers.Default) {
            val encryptedBytes = encryptCipher.doFinal(bytes)
            outputStream.use {
                it.write(encryptCipher.iv.size)
                it.write(encryptCipher.iv)
                it.write(encryptedBytes.size)
                it.write(encryptedBytes)
            }
            return@withContext encryptedBytes
        }

    private suspend fun decrypt(inputStream: InputStream): ByteArray = withContext(
        Dispatchers.Default
    ) {
        return@withContext inputStream.use {
            val ivSize = it.read()
            val iv = ByteArray(ivSize)
            it.read(iv)

            val encryptedBytesSize = it.read()
            val encryptedBytes = ByteArray(encryptedBytesSize)
            it.read(encryptedBytes)

            getDecryptCipherForIv(iv).doFinal(encryptedBytes)
        }
    }

    private suspend fun deleteFile() {
        withContext(Dispatchers.IO) {
            keystore.deleteEntry(SERVICE_NAME)
            val file = File(currentActivity?.filesDir, FILE_NAME)

            if (file.exists()) file.delete()

            myKeyChainPromise?.resolve(true)
        }
    }

    private suspend fun getStoredPassword() {
        withContext(Dispatchers.IO) {
            val file = File(currentActivity?.filesDir, FILE_NAME)
            if (!file.exists()) {
                myKeyChainPromise?.resolve(false)
            } else {
                myKeyChainPromise?.resolve(
                    decrypt(inputStream = FileInputStream(file)).decodeToString()
                )
            }
        }
    }

    private suspend fun storePassword(password: String) {
        withContext(Dispatchers.IO) {
            encryptCipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.ENCRYPT_MODE, getKey())
            }
            val bytes = password.encodeToByteArray()
            val file = File(currentActivity?.filesDir, FILE_NAME)

            if (!file.exists()) file.createNewFile()

            val fos = FileOutputStream(file)
            encrypt(bytes = bytes, outputStream = fos)
            myKeyChainPromise?.resolve(true)
        }
    }
}