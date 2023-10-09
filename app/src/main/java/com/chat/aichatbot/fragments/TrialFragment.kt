package com.chat.aichatbot.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.*
import com.chat.aichatbot.databinding.FragmentTrialBinding
import com.chat.aichatbot.utils.Constants
import com.chat.aichatbot.utils.MySharedPreference
import com.chat.aichatbot.utils.Security
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TrialFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var _binding: FragmentTrialBinding? = null
    private var billingClient: BillingClient? = null
    private lateinit var mySharedPreference: MySharedPreference
    private var isBillingFlowInProgress = false
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrialBinding.inflate(inflater, container, false)
        mySharedPreference = MySharedPreference(requireContext())
        billingClient = BillingClient
            .newBuilder(requireContext())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        runBlocking {
            getPrice()
        }

        binding.apply {

            startTrial.setOnClickListener {
                subsClick()
            }
            privacyPolicy.setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://happyengprivacypolicy.blogspot.com/2023/03/privacy-policy-ai-chatbot.html")
                )
                startActivity(intent)
            }

            restore.setOnClickListener {
                lifecycleScope.launchWhenCreated {
                    checkAndRestorePurchase()
                }
            }

        }

        return binding.root
    }


    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchase ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchase != null) {
            isBillingFlowInProgress = false
            for (pur in purchase) {
                handlePurchase(pur)

            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Log.d("hello", "Already supported")
            isBillingFlowInProgress = false
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
            Toast.makeText(requireContext(), "Not supported", Toast.LENGTH_SHORT).show()
            isBillingFlowInProgress = false
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            isBillingFlowInProgress = false
        }
    }

    private fun handlePurchase(pur: Purchase?) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(pur?.purchaseToken!!)
            .build()

        val listener = ConsumeResponseListener { billingResult, s ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Toast.makeText(
                    requireActivity(),
                    "BillingClient BillingResponseCode OK",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        billingClient!!.consumeAsync(consumeParams, listener)

        if (pur.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!verifyValidSignature(pur.originalJson, pur.signature)) {
                Toast.makeText(requireContext(), "Purchased", Toast.LENGTH_SHORT).show()
                mySharedPreference.setBoolPreference(Constants.PROMO_ACTIVE, true)
                findNavController().popBackStack()
                return
            }
            if (!pur.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(pur.purchaseToken)
                    .build()
                billingClient!!.acknowledgePurchase(
                    acknowledgePurchaseParams,
                    purchaseAcknowledgeListener
                )
                Log.d("hello", "Subscribed")
            } else {
                Log.d("hello", "Already subscribed")
            }
        } else if (pur.purchaseState == Purchase.PurchaseState.PENDING) {
            Log.d("hello", "Pending")
        } else if (pur.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            Log.d("hello", "UNSPECIFIED_STATE")
        }

    }

    private var purchaseAcknowledgeListener = AcknowledgePurchaseResponseListener {
        if (it.responseCode == BillingClient.BillingResponseCode.OK) {
            Toast.makeText(requireContext(), "Subscribed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            val base64Key =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0woa4AYo4o+yh5dCUUyJGzlFWapg6sVNWBFL39+xKwGxL9ksKX4kcZZk/NYceOcUqHzyTYfQNC+ohJ0NSHRFy1AjdH/hDuEO5ALTZFCVKIvuKxcuLamtYuViEiuYTZfERYEmFIMSnSLwE51kZOcRnVNDxLd5WMUXP4UFsVThuBxWWN95yuBQXevBYOJclgw7GRScMw1P3i1n6L/WsnXX+1qmnUDQ5Vkuup1MN5icCLb3k02p3Vummi8BGwDGR+b6IYtlu5bq5w5U4Gf+Rzm5H05QpkS+a54BvItZdFnFxC2pRGSZPzjg3a0e3WVdSbF0aBlFOdMR42dnukmQ9cCA7QIDAQAB"
            val security = Security()
            security.verifyPurchase(base64Key, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }


    private fun subsClick() {
        if (isBillingFlowInProgress) {
            return
        } else {
            isBillingFlowInProgress = true
            billingClient!!.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {

                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {


                    val productList = listOf(
                        QueryProductDetailsParams.Product.newBuilder().setProductId("1")
                            .setProductType(
                                BillingClient.ProductType.SUBS
                            ).build()
                    )

                    val params = QueryProductDetailsParams.newBuilder().setProductList(productList)
                    billingClient!!.queryProductDetailsAsync(params.build()) { _, productDetails ->
                        for (productDetail in productDetails) {
                            val offerToken =
                                productDetail.subscriptionOfferDetails?.get(0)?.offerToken
                            val productDetailsParamsList = listOf(offerToken?.let {
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetail)
                                    .setOfferToken(it)
                                    .build()
                            })
                            val billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build()

                            val billingResult =
                                billingClient!!.launchBillingFlow(
                                    requireActivity(),
                                    billingFlowParams
                                )

                        }

                    }
                }

            })
        }

    }


    private fun getPrice() {
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val productDetailsParams =
                        listOf(
                            QueryProductDetailsParams.Product.newBuilder().setProductId("1")
                                .setProductType(BillingClient.ProductType.SUBS).build()
                        )

                    val params =
                        QueryProductDetailsParams.newBuilder().setProductList(productDetailsParams)

                    billingClient!!.queryProductDetailsAsync(params.build()) { billingResult, productList ->
                        var period =
                            productList[0].subscriptionOfferDetails!![0].pricingPhases.pricingPhaseList[0].billingPeriod
                        val price =
                            productList[0].subscriptionOfferDetails!![0].pricingPhases.pricingPhaseList[0].formattedPrice
                        if (period.contains("D")) {
                            period = period.replace("P", "")
                            period = period.replace("D", "")
                            period = if (period == "1") {
                                "$period day"
                            } else {
                                "$period days"
                            }
                        } else if (period.contains("W")) {
                            period = period.replace("P", "")
                            period = period.replace("W", "")
                            period = if (period == "1") {
                                "$period week"
                            } else {
                                "$period weeks"
                            }
                        }
                        binding.explanation.text =
                            "$period trial auto-renews at $price /monthly. No commitment. Cancel any time. Full access to app and all features, no extra charge for any features."
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                Toast.makeText(
                    requireContext(),
                    "Try to restart the connection on the next request to the billing service",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }

    private suspend fun checkAndRestorePurchase() {
        val result = billingClient!!.queryPurchaseHistory(BillingClient.SkuType.SUBS)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val purchases = result.purchaseHistoryRecordList
            if (purchases != null && purchases.isNotEmpty()) {
                for (purchase in purchases) {
                    // Do any necessary verification of the purchase here
                    // ...

                    // Restore the purchase for the user
                    val consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    val listener = ConsumeResponseListener { billingResult, purchaseToken ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            // The purchase has been successfully restored
                            mySharedPreference.setBoolPreference(Constants.PROMO_ACTIVE, true)
                            Toast.makeText(
                                requireContext(),
                                "Successfully restored",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    billingClient!!.consumeAsync(consumeParams, listener)
                }
            } else {
                Toast.makeText(requireContext(), "No existing purchase", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Error occurred while querying purchase history
            // ...
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (billingClient != null) {
            billingClient!!.endConnection()
        }
    }


}