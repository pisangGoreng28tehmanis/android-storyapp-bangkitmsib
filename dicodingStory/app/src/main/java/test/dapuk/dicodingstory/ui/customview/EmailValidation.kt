package test.dapuk.dicodingstory.ui.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import com.google.android.material.textfield.TextInputLayout

class EmailValidation @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : TextInputLayout(context, attrs) {

    init {
        addOnEditTextAttachedListener {
            editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    validatePassword(s)
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun validatePassword(email: CharSequence?) {
        if (email.isNullOrEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            error = "Email tidak valid"
        } else {
            error = null
        }
    }
}