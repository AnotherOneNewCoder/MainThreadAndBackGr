package ru.netology.nmedia.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg

import ru.netology.nmedia.viewmodel.AuthViewModel
import javax.inject.Inject


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class AppActivity : AppCompatActivity(R.layout.activity_app) {


    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability

    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationsPermission()

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment)
                .navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = text
                    }
                )
        }

        checkGoogleApiAvailability()

        val authViewModel by viewModels<AuthViewModel>()

        var currentMenuProvider: MenuProvider? = null

        authViewModel.data.observe(this) {

            currentMenuProvider?.let(::removeMenuProvider)

            addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

                    menuInflater.inflate(R.menu.menu_auth, menu)
                    val authenticated = authViewModel.isAuthenticated

                    menu.setGroupVisible(R.id.authorized, authenticated)
                    menu.setGroupVisible(R.id.unauthorized, !authenticated)

                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.singUp -> {
//                            findNavController(R.id.nav_host_fragment).navigate(R.id.action_feedFragment_to_registrationFragment)
//                            true
                            var enter = true

                            val navController = findNavController(R.id.nav_host_fragment)

                            navController.addOnDestinationChangedListener {  _, destination, _ ->

                                val currentFragment = destination.label.toString()

                                if (currentFragment == "fragment_feed" && enter) {

                                    enter = false

                                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_feedFragment_to_registrationFragment)

                                } else enter = false

                            }

                            true
                        }
                        R.id.singIn -> {
//                            findNavController(R.id.nav_host_fragment).navigate(R.id.action_feedFragment_to_logInFragment)
//                            true
                            var enter = true
                            val navController = findNavController(R.id.nav_host_fragment)
                            navController.addOnDestinationChangedListener { _, destination, _ ->
                                val currentFragment = destination.label.toString()
                                if (currentFragment == "fragment_feed" && enter) {
                                    enter = false
                                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_feedFragment_to_logInFragment)
                                } else enter = false
                            }
                            true
                        }
                        R.id.logout -> {


                            findNavController(R.id.nav_host_fragment).navigate(R.id.logOutDialog)
                            true
                        }
                        else -> false

                    }

            }.also {
                   currentMenuProvider = it
            }, this)

        }
    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS

        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        requestPermissions(arrayOf(permission), 1)
    }

    private fun checkGoogleApiAvailability() {
        with(googleApiAvailability) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
        }

        firebaseMessaging.token.addOnSuccessListener {
            println(it)
        }
    }

}