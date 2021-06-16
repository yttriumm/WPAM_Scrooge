package com.wpam.scrooge

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationMenu
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.wpam.scrooge.fragments.NewFragment
import com.wpam.scrooge.fragments.OverviewFragment
import com.wpam.scrooge.fragments.ReceiptDetailsFragment
import com.wpam.scrooge.fragments.StatsFragment

class DashboardActivity : AppCompatActivity() {

    val newFragment = NewFragment()
    val overviewFragment = OverviewFragment()
    val statsFragment = StatsFragment()
    val  receiptDetailsFragment = ReceiptDetailsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        replaceFragment(overviewFragment)
        val bottomNavigation : BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.ic_dashboard_new -> replaceFragment(newFragment)
                R.id.ic_dashboard_overview -> replaceFragment(overviewFragment)
                R.id.ic_dashboard_stats -> replaceFragment(statsFragment)
            }
            true
        }


    }

    private fun replaceFragment(fragment: Fragment) {
        if(fragment != null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.commit()
        }
    }

}