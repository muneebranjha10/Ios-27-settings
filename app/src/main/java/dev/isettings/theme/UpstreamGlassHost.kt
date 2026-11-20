package dev.isettings.theme

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.example.liquidglass.LiquidGlassView
import com.example.liquidglass.LiquidGlassToggle
import com.example.liquidglass.GlassMaterial
import kotlin.math.roundToInt

/**
 * Native View bridge to JosephMontfort/Liquid-Glass-on-Android-Views.
 * No Compose lifecycle or recomposer is used by this host.
 */
class UpstreamGlassHost private constructor(
    private val activity: Activity,
    private val source: View?,
    private val bg: Int,
    private val dark: Boolean,
    private val isToggle: Boolean,
    initial: Boolean,
    private val action: Runnable?,
    private val toggleAction: ToggleAction?,
    private val failure: Runnable
) : FrameLayout(activity) {
    fun interface ToggleAction { fun select(value: Boolean) }
    private var state = initial
    private var syncing = false
    private var closed = false
    private var failed = false
    private var toggle: LiquidGlassToggle? = null
    private var search: LiquidGlassView? = null
    private var configuredWidth = 0
    init { clipChildren = false; clipToPadding = false }
    private fun dp(v: Float) = (v * resources.displayMetrics.density).roundToInt()
    private fun moduleContext(): Context = activity.createPackageContext("dev.isettings.theme", Context.CONTEXT_IGNORE_SECURITY)
    private fun report(e: Throwable) {
        if (failed || closed) return
        failed = true
        Entry.log("v0.13 native glass failure: " + android.util.Log.getStackTraceString(e))
        post { if (!closed) failure.run() }
    }
    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w,h,ow,oh)
        if (w <= 0 || h <= 0 || closed || failed) return
        // Add the widgets outside onMeasure/onLayout to avoid a layout loop.
        post { if (!closed && !failed && width > 0 && configuredWidth != width) setup() }
    }
    private fun setup() {
        try {
            if (closed || failed || width <= 0) return
            configuredWidth = width
            removeAllViews()
            val module = moduleContext()
            if (isToggle) {
                val cfg = Configuration(module.resources.configuration)
                cfg.densityDpi = (width * 160f / 76f).roundToInt().coerceIn(120, 1000)
                val fit = module.createConfigurationContext(cfg)
                val widget = LiquidGlassToggle(fit)
                widget.accentColor = if(dark) 0xFF30D158.toInt() else 0xFF34C759.toInt()
                widget.trackColor = if(dark) 0xFF57575B.toInt() else 0xFFC5C6C8.toInt()
                widget.setChecked(state, false)
                widget.contentDescription = "Switch"
                widget.onCheckedChangeListener = { checked ->
                    if (!closed && !syncing) toggleAction?.select(checked)
                }
                toggle = widget
                addView(widget, LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER))
            } else {
                val widget = LiquidGlassView(module)
                widget.material = GlassMaterial.CLEAR
                widget.cornerRadius = height / 2f
                widget.bevelWidth = dp(9f).toFloat()
                widget.refractionHeight = dp(23f).toFloat()
                widget.dispersionStrength = .18f
                widget.blurAmount = .045f
                widget.saturation = 145f
                widget.enableSensorHighlight = false
                widget.enableDynamicBackground = false
                widget.enableAdaptiveTint = false
                widget.setGlassTint(if (dark) 0xFF242428.toInt() else Color.WHITE, .12f)
                widget.backdropSource = source
                val ink = if (dark) 0xFFF0F0F5.toInt() else 0xFF55555B.toInt()
                val labels = LinearLayout(activity)
                labels.gravity = Gravity.CENTER_VERTICAL
                labels.setPadding(dp(15f), 0, dp(15f), 0)
                labels.addView(Entry.Glyph(activity, "search", ink, ink, false, dark),LinearLayout.LayoutParams(dp(22f),dp(22f)))
                val label=TextView(activity)
                label.text="Search"
                label.setTextColor(ink)
                label.textSize=16f
                val lp=LinearLayout.LayoutParams(0,-2,1f)
                lp.leftMargin=dp(8f)
                labels.addView(label,lp)
                widget.addView(labels,FrameLayout.LayoutParams(-1,-1))
                widget.setOnClickListener { action?.run() }
                widget.contentDescription="Search Samsung Settings"
                search=widget
                addView(widget,LayoutParams(-1,-1))
            }
            Entry.log("v0.13 native glass component mounted: " + if (isToggle) "toggle" else "search")
        } catch(e: Throwable) { report(e) }
    }
    override fun dispatchDraw(canvas: android.graphics.Canvas) {
        try { super.dispatchDraw(canvas) } catch(e: Exception) { report(e) } catch(e: LinkageError) { report(e) }
    }
    fun refresh() { if (!closed) search?.invalidate() }
    fun confirm(value: Boolean) {
        state=value
        syncing=true
        try { toggle?.setChecked(value,true) } catch(e: Throwable) { report(e) } finally { syncing=false }
    }
    fun close() { if(closed)return;closed=true;toggle?.onCheckedChangeListener=null;search?.backdropSource=null;removeAllViews();toggle=null;search=null }
    companion object {
        @JvmStatic fun search(activity: Activity,source: View,bg: Int,dark: Boolean,action: Runnable,failure: Runnable)=
            UpstreamGlassHost(activity,source,bg,dark,false,false,action,null,failure)
        @JvmStatic fun toggle(activity: Activity,bg: Int,dark: Boolean,initial: Boolean,action: ToggleAction,failure: Runnable)=
            UpstreamGlassHost(activity,null,bg,dark,true,initial,null,action,failure)
    }
}
