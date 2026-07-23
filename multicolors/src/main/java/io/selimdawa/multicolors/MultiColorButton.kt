package io.selimdawa.multicolors

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import io.selimdawa.multicolors.databinding.McButtonThemeBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MultiColorButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val binding: McButtonThemeBinding =
        McButtonThemeBinding.inflate(LayoutInflater.from(context), this)

    init {
        setupDefaultStyle()
        setupListeners()
    }

    private fun setupDefaultStyle() {
        radius = dpToPx(17f)
        setCardBackgroundColor(ColorStateList.valueOf(Color.TRANSPARENT))
        strokeColor = ContextCompat.getColor(context, R.color.mc_border_color)
        strokeWidth = dpToPx(1f).toInt()
        elevation = 0f
    }

    private fun setupListeners() {
        setOnClickListener {
            (context as? AppCompatActivity)?.let { MultiColorManager.showThemeDialog(it) }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            MultiColorManager.currentTheme.collectLatest {
                updateAppearance()
            }
        }
    }

    private fun updateAppearance() {
        binding.mcInnerColor.background =
            MultiColorManager.getThemeBackground(context, context.theme)
    }

    private fun dpToPx(dp: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
    )
}