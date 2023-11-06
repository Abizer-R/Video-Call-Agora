package com.example.teachjr.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB: ViewBinding>(private val inflate: Inflate<VB>) : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding get() = requireNotNull(_binding)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflate.invoke(layoutInflater)
        setContentView(binding.root)
        initUserInterface()
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    protected abstract fun initUserInterface()
}