
package com.example.democalling
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.azure.android.communication.calling.Call
import com.azure.android.communication.calling.CallAgent
import com.azure.android.communication.calling.CallClient
import com.azure.android.communication.calling.CallState
import com.azure.android.communication.calling.IncomingCall
import com.azure.android.communication.calling.PropertyChangedEvent
import com.azure.android.communication.calling.PropertyChangedListener
import com.azure.android.communication.calling.StartCallOptions
import com.azure.android.communication.common.CommunicationIdentifier
import com.azure.android.communication.common.CommunicationTokenCredential
import com.azure.android.communication.common.CommunicationUserIdentifier
import java.util.concurrent.ExecutionException


class MainActivity : Activity() {

    private var callAgent: CallAgent? = null
    private var incomingCall: IncomingCall? = null
    private var call: Call? = null
    private var onStateChangedListener: PropertyChangedListener? = null
    private var alertDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getAllPermissions()
        val acceptButton: Button = findViewById(R.id.accept_button)
        acceptButton.setOnClickListener {
            createIncomingAgent()
            handleIncomingCall()
        }
        val callButton: Button = findViewById(R.id.call_button)

        callButton.setOnClickListener {
            createOutgoingAgent()
            startCall()}

        val hangButton: Button = findViewById(R.id.hang_up)
        hangButton.setOnClickListener {
            hangUpCall()
            }

        volumeControlStream = AudioManager.STREAM_VOICE_CALL
    }

    /**
     * Request each required permission if the app doesn't already have it.
     */
    private fun getAllPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
        )
        val permissionsToAskFor = ArrayList<String>()
        for (permission in requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToAskFor.add(permission)
            }
        }
        if (permissionsToAskFor.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToAskFor.toTypedArray(), 1)
        }
    }

    /**
     * Create the call agent for placing calls
     */
    private fun createOutgoingAgent() {
        val userToken = ""

        try {
            val credential = CommunicationTokenCredential(userToken)
            callAgent = CallClient().createCallAgent(applicationContext, credential).get()
        } catch (ex: Exception) {
            Toast.makeText(applicationContext, "Failed to create call agent.", Toast.LENGTH_SHORT).show()
        }
    }
    /**
     * Create the call agent for incoming calls
     */
    private fun createIncomingAgent() {
//      Callee Id - 
        val userToken = ""
        try {
            val credential = CommunicationTokenCredential(userToken)
            callAgent = CallClient().createCallAgent(applicationContext, credential).get()
        } catch (ex: Exception) {
            Toast.makeText(applicationContext, "Failed to create call agent.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Place a call to the callee id provided in `callee_id` text input.
     */
    private fun startCall() {
       val calleeIdView = findViewById<EditText>(R.id.callee_id)
       val calleeId: String = calleeIdView.text.toString()
        val participants = ArrayList<CommunicationIdentifier>()
        val options = StartCallOptions()
        participants.add(CommunicationUserIdentifier(calleeId));
        call = callAgent?.startCall(
            applicationContext,
            arrayOf(CommunicationUserIdentifier(calleeId)),
            options
        )

        val onStateChangedListener = PropertyChangedListener { state ->
            handleCallOnStateChanged(state)
        }

        call?.addOnStateChangedListener(onStateChangedListener)
    }
    /**
     * Show AlertBox Function
     */
    private fun showAlertBox()
    {
        val builder = AlertDialog.Builder(this)


        builder.setMessage("Jane Doe")

        builder.setTitle("Incoming Call")

        builder.setCancelable(false)

        builder.setPositiveButton("Accept") {
                _, _ ->
                answerIncomingCall()

        }

        builder.setNegativeButton("Decline") { _, _ ->
            declineIncomingCall()
        }
        alertDialog = builder.create()
        if (alertDialog != null) {
            alertDialog!!.show()
        }
    }

    /**
     * Code for hangup Call
     */
    private fun hangUpCall() {
        try {
            call?.hangUp(null)?.get()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val onStateChangedListener = PropertyChangedListener { state ->
            handleCallOnStateChanged(state)
        }
        call?.addOnStateChangedListener(onStateChangedListener)
    }
    /**
     * Code for ListenIncomingCall
     */
    private fun handleIncomingCall() {

        callAgent?.addOnIncomingCallListener { incomingCall ->
            val myTextView = findViewById<TextView>(R.id.myTextView)
            runOnUiThread {
                myTextView.text = "Call State is:"
            }
            this.incomingCall = incomingCall
            val onStateChangedListener = PropertyChangedListener { state ->
                handleCallOnStateChanged(state)
            }

            call?.addOnStateChangedListener(onStateChangedListener)
            runOnUiThread{
                showAlertBox()
            }
//            call?.addOnStateChangedListener { state ->
//                if (state.toString() == "DISCONNECTED") {
//                    runOnUiThread {
//                        dismissAlertBox()
//                    }
//                }
//            }
        }



    }
    private fun dismissAlertBox() {
        alertDialog?.dismiss()
    }


    /**
     * Code for Declining IncomingCall
     */
    private fun declineIncomingCall() {
        incomingCall?.reject()
    }

    /**
     * Code for Answer IncomingCall
     */
    private fun answerIncomingCall() {
        val context = applicationContext
        if (incomingCall == null) {
            return
        }

        try {
            call = incomingCall?.accept(context)?.get() as Call?
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
        val onStateChangedListener = PropertyChangedListener { state ->
            handleCallOnStateChanged(state)
        }

        call?.addOnStateChangedListener(onStateChangedListener)


    }
    /**
     * Code for Print Call State
     */
    private fun handleCallOnStateChanged(args: PropertyChangedEvent) {
        val myTextView = findViewById<TextView>(R.id.myTextView)
        runOnUiThread {
            myTextView.text = "Call State is:" + call?.getState()

        }


    }
}


