package kr.dformula.psydiary.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import kr.dformula.psydiary.PadMartActivity
import kr.dformula.psydiary.R

class MainActivity : AppCompatActivity() {
    private  val handler by lazy{ Handler(Looper.myLooper()!!)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val navController = findNavController(R.id.navHostfragment)

        bottomNavigationView_main.setupWithNavController(
            navController
        )

        bottomNavigationView_main.setOnNavigationItemSelectedListener {

            if (it.itemId == R.id.navi_padMart) {
                startActivity(Intent(this, PadMartActivity::class.java))

            } else
                it.onNavDestinationSelected(navController)

            true
        }

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onResume() {
        super.onResume()
        var iconChange = 0


        fun infinityHandlerPlay(){
            handler.postDelayed(
                {
                    val newIcon: Int = when (iconChange) {
                        0 -> {
                            iconChange++
                            R.drawable.ic_outline_sms_24
                        }
                        1 -> {
                            iconChange++
                            R.drawable.ic_home_black_24dp
                        }
                        else  -> {
                            iconChange = 0
                            R.drawable.ic_baseline_attach_money_24
                        }
                    }

                    bottomNavigationView_main.menu.getItem(3).icon =
                        resources.getDrawable(newIcon,null)
                    infinityHandlerPlay()
                },1000)
        }

        infinityHandlerPlay()
    }

    override fun onPause() {
        handler.removeCallbacksAndMessages(null)
        super.onPause()
    }
}