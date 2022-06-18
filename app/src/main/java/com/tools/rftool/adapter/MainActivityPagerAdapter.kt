package com.tools.rftool.adapter

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tools.rftool.fragment.FftFragment
import com.tools.rftool.fragment.RecordingsFragment
import com.tools.rftool.fragment.SignalFragment
import kotlin.reflect.KClass

class MainActivityPagerAdapter(
    private val activity: AppCompatActivity,
    private val fragmentManager: FragmentManager
) : FragmentStateAdapter(fragmentManager, activity.lifecycle) {

    private val frags = arrayOf(
        FftFragment::class,
        SignalFragment::class,
        RecordingsFragment::class
    )

    override fun getItemCount(): Int = frags.size

    override fun createFragment(position: Int): Fragment {
        return frags[position].constructors.firstOrNull()?.call() ?: FftFragment()
    }
}