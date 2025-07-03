package com.dicoding.picodiploma.loginwithanimation.view.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import test.dapuk.dicodingstory.R

class CustomPasswordEditText : AppCompatEditText {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        // Tambahkan TextWatcher untuk validasi password
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Tidak digunakan
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Validasi panjang password
                if (!s.isNullOrEmpty() && s.length < 8) {
                    error = context.getString(R.string.password_error_message) // Pesan error
                } else {
                    error = null // Hilangkan pesan error jika valid
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Tidak digunakan
            }
        })
    }
}
