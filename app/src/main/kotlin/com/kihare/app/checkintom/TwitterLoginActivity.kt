package com.kihare.app.checkintom

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import com.twitter.sdk.android.Twitter
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import io.fabric.sdk.android.Fabric

class TwitterLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authConfig = TwitterAuthConfig("W2SZFrhkJ2MDCkH4nXMBx2ibC", "y9HmHvFtivenGmPf9NODGRW5oibkpAM1PPb78r5kdYpM3rJBaY")
        Fabric.with(this, Twitter(authConfig))

        setContentView(R.layout.activity_twitter_login)
        val loginButton = findViewById(R.id.login_button) as TwitterLoginButton
        loginButton.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {
                // Do something with result, which provides a TwitterSession for making API calls
            }

            override fun failure(exception: TwitterException) {
                // Do something on failure
            }
        }
    }

}
