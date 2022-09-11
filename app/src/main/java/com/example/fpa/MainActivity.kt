package com.example.fpa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity: AppCompatActivity() {

    private companion object MainActivity {
        private const val TAG = "MainActivity"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var client: GoogleSignInClient

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val reference: DatabaseReference = database.reference

    private lateinit var wordText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        wordText = findViewById(R.id.wordText)
        val wordTextbox: EditText = findViewById(R.id.wordTextbox)
        val submitButton: Button = findViewById(R.id.submitButton)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        client = GoogleSignIn.getClient(this, gso)

        val user: String = setUser()
        setWord(user)

        submitButton.setOnClickListener {
            val word: String = wordTextbox.text.toString()
            if (checkConstraints(word)) {
                val user: String = setUser()
                reference.child("users").child(user).child("word").setValue(word)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Word Submitted", Toast.LENGTH_SHORT).show()
                        wordTextbox.text.clear()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Submission Failed", Toast.LENGTH_SHORT).show()
                    }
                setWord(user)
            } else {
                Toast.makeText(this, "No Spaces; Only the Alphabets; No More than 20", Toast.LENGTH_SHORT).show()
                wordTextbox.text.clear()
            }
        }

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            Log.i(TAG, "Logout")
            auth.signOut()
            client.signOut()
            val logoutIntent = Intent(this, LoginActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutIntent)
        }

    }

    private fun setWord(user: String) {
        wordText.text = ""
        if (user != "default") {
            reference.child("users").child(user).child("word").get()
                .addOnSuccessListener {
                    val result: String = it.value.toString()
                    if (result != "null") {
                        wordText.text = result
                    } else {
                        Toast.makeText(this, "No Saved Word", Toast.LENGTH_SHORT).show()
                    }
                }

        }
    }

    private fun setUser(): String {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        var user: String = "default"
        if (account != null) {
            user = account.id.toString()
            Log.w(TAG, "User " + account.id.toString())
        }
        return user
    }

    private fun checkConstraints(word: String): Boolean {
        for (i in word.indices) {
            if (!word[i].isLetter()) {
                return false
            }
        }
        if (word.length > 20) {
            return false
        }
        return true
    }

}