package com.chat.aichatbot.utils

import android.text.TextUtils
import android.util.Base64

import java.io.IOException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec


class Security {
    private val KEY_FACTORY_ALGORITHM = "RSA"
    private val SIGNATURE_ALGORITHM = "SHA1withRSA"

    fun verifyPurchase(base64PublicKey: String?, signedData: String, signature: String): Boolean {
        return if (TextUtils.isEmpty(base64PublicKey) || TextUtils.isEmpty(signedData) || TextUtils.isEmpty(
                signature
            )
        ) {
            false
        } else {
            val key = generatePublicKey(base64PublicKey)
            verifyIt(key, signedData, signature)
        }
    }

    private fun verifyIt(key: PublicKey, base64PublicKey: String?, signature: String): Boolean {
        val signatureVerify: ByteArray = try {
            Base64.decode(base64PublicKey, Base64.DEFAULT)
        } catch (e: java.lang.IllegalArgumentException) {
            return false
        }
        try {
            val signatureAlgorithm = Signature.getInstance(SIGNATURE_ALGORITHM)
            signatureAlgorithm.initVerify(key)
            signatureAlgorithm.update(signature.toByteArray())
            return signatureAlgorithm.verify(signatureVerify)
        } catch (e: NoSuchAlgorithmException) {
            throw java.lang.RuntimeException(e)
        } catch (e: InvalidKeySpecException) {
            //
        } catch (e: SignatureException) {
            //
        }
        return false
    }

    @Throws(IOException::class)
    private fun generatePublicKey(base64PublicKey: String?): PublicKey {
        return try {
            val decodedKey = Base64.decode(base64PublicKey, Base64.DEFAULT)
            val keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
            keyFactory.generatePublic(X509EncodedKeySpec(decodedKey))
        } catch (e: NoSuchAlgorithmException) {
            throw java.lang.RuntimeException(e)
        } catch (e: InvalidKeySpecException) {
            val msg = "Invalid message key $e"
            throw IOException(msg)
        }
    }
}