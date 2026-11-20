package dev.isettings.theme

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop
import com.kyant.backdrop.catalog.components.LiquidButton
import com.kyant.backdrop.catalog.components.LiquidToggle

/**
 * Compose bridge for Samsung's native Settings homepage.
 * Uses the actual upstream 2.0.1 catalog composables and published backdrop library.
 * Only the view interop / Android lifecycle / native navigation are new.
 */
class UpstreamGlassHost private constructor(
    private val activity: Activity,
    private val source: View?,
    private val backgroundColorInt: Int,
    private val dark: Boolean,
    private val isToggle: Boolean,
    initial: Boolean,
    private val searchAction: Runnable?,
    private val toggleAction: ToggleAction?,
    private val onFailure: Runnable
) : FrameLayout(activity) {
    fun interface ToggleAction { fun select(value: Boolean) }
    private val selected = mutableStateOf(initial)
    private val snapshot = mutableStateOf<Bitmap?>(null, neverEqualPolicy())
    private val owner = GlassOwner()
    private val compose: ComposeView
    private var closed = false
    private var failed = false
    private val scope = CoroutineScope(AndroidUiDispatcher.CurrentThread + SupervisorJob() + CoroutineExceptionHandler { _, error -> reportFailure(error) })
    private val recomposer = Recomposer(scope.coroutineContext)
    private val sample = Runnable { capture() }

    init {
        owner.controller.performAttach()
        owner.controller.performRestore(null)
        owner.registry.currentState = Lifecycle.State.CREATED
        // The Settings window belongs to another app. Do not use its window recomposer.
        setViewTreeLifecycleOwner(owner)
        setViewTreeSavedStateRegistryOwner(owner)
        setViewTreeViewModelStoreOwner(owner)
        // Use module resources to prevent Settings' resource IDs from being interpreted
        // as Compose library resources. The outer FrameLayout still uses host context.
        val moduleContext = activity.createPackageContext("dev.isettings.theme", Context.CONTEXT_IGNORE_SECURITY)
        compose = ComposeView(moduleContext)
        compose.setParentCompositionContext(recomposer)
        compose.setViewTreeLifecycleOwner(owner)
        compose.setViewTreeSavedStateRegistryOwner(owner)
        compose.setViewTreeViewModelStoreOwner(owner)
        compose.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        compose.clipChildren = false
        compose.clipToPadding = false
        clipChildren = false
        clipToPadding = false
        addView(compose, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        scope.launch { recomposer.runRecomposeAndApplyChanges() }
        compose.setContent {
            val backdrop = rememberCanvasBackdrop {
                val image = snapshot.value
                drawRect(Color(backgroundColorInt))
                if (image != null) drawImage(image.asImageBitmap())
            }
            val systemDensity = LocalDensity.current
            if (isToggle) {
                // Fit the upstream 64 dp toggle in the measured reference slot.
                BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val widthPx = with(systemDensity) { maxWidth.toPx() }
                    val fitDensity = (widthPx / 72f).coerceAtLeast(.5f)
                    CompositionLocalProvider(LocalDensity provides Density(fitDensity, systemDensity.fontScale)) {
                        LiquidToggle(
                            selected = { selected.value },
                            onSelect = { target -> toggleAction?.select(target) },
                            backdrop = backdrop,
                            modifier = Modifier.size(64.dp, 42.dp)
                        )
                    }
                }
            } else {
                val ink = if (dark) Color(0xFFE5E5EA) else Color(0xFF76767B)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LiquidButton(
                        onClick = { searchAction?.run() },
                        backdrop = backdrop,
                        modifier = Modifier.fillMaxSize(),
                        surfaceColor = if (dark) Color(0x4437373B) else Color(0x36F7F7FA)
                    ) {
                        Canvas(Modifier.size(20.dp)) {
                            val stroke = 1.7.dp.toPx()
                            drawCircle(ink, size.minDimension * .3f, Offset(size.width*.42f,size.height*.42f),
                                style=androidx.compose.ui.graphics.drawscope.Stroke(stroke))
                            drawLine(ink,Offset(size.width*.65f,size.height*.65f),Offset(size.width*.9f,size.height*.9f),stroke)
                        }
                        BasicText("Search", Modifier.weight(1f), style=TextStyle(color=ink, fontSize=16.sp))
                    }
                }
            }
        }
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!closed) {
            owner.registry.currentState = Lifecycle.State.RESUMED
            refresh()
            Entry.log("v0.12 isolated Compose glass attached " + if (isToggle) "toggle" else "search")
        }
    }
    private fun reportFailure(error: Throwable) {
        if (closed || failed) return
        failed = true
        Entry.log("v0.12 Compose host failure: " + android.util.Log.getStackTraceString(error))
        android.os.Handler(android.os.Looper.getMainLooper()).post { if (!closed) onFailure.run() }
    }
    override fun onMeasure(w: Int, h: Int) {
        try { super.onMeasure(w,h) } catch (e: Exception) {
            reportFailure(e)
            setMeasuredDimension(View.MeasureSpec.getSize(w),View.MeasureSpec.getSize(h))
        } catch (e: LinkageError) {
            reportFailure(e)
            setMeasuredDimension(View.MeasureSpec.getSize(w),View.MeasureSpec.getSize(h))
        }
    }
    override fun onLayout(changed: Boolean,l: Int,t: Int,r: Int,b: Int) {
        try { super.onLayout(changed,l,t,r,b) } catch (e: Exception) { reportFailure(e) } catch (e: LinkageError) { reportFailure(e) }
    }
    override fun dispatchDraw(canvas: AndroidCanvas) {
        try { super.dispatchDraw(canvas) } catch (e: Exception) { reportFailure(e) } catch (e: LinkageError) { reportFailure(e) }
    }
    override fun onDetachedFromWindow() {
        close()
        super.onDetachedFromWindow()
    }
    override fun onSizeChanged(w: Int,h: Int,ow: Int,oh: Int) {
        super.onSizeChanged(w,h,ow,oh);refresh()
    }
    fun refresh() {
        if (closed || source == null) return
        removeCallbacks(sample)
        postOnAnimation(sample)
    }
    private fun capture() {
        if (closed || source == null || width <= 0 || height <= 0) return
        try {
            val bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
            val canvas = AndroidCanvas(bitmap)
            canvas.drawColor(backgroundColorInt)
            val from=IntArray(2);val to=IntArray(2)
            source.getLocationOnScreen(from);getLocationOnScreen(to)
            canvas.translate((from[0]-to[0]).toFloat(),(from[1]-to[1]).toFloat())
            source.draw(canvas)
            snapshot.value=bitmap
        } catch (e: Throwable) { Entry.log("upstream backdrop capture failed " + e) }
    }
    fun confirm(value: Boolean) { selected.value=value }
    fun close() {
        if (closed) return
        closed=true;removeCallbacks(sample);snapshot.value=null
        runCatching { compose.disposeComposition() }
        runCatching { recomposer.cancel() }
        scope.cancel()
        runCatching { owner.registry.currentState=Lifecycle.State.DESTROYED }
        runCatching { owner.viewModelStore.clear() }
    }
    private class GlassOwner : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
        val registry=LifecycleRegistry(this)
        val controller=SavedStateRegistryController.create(this)
        override val lifecycle: Lifecycle get()=registry
        override val savedStateRegistry: SavedStateRegistry get()=controller.savedStateRegistry
        override val viewModelStore=ViewModelStore()
    }
    companion object {
        @JvmStatic fun search(activity: Activity, source: View, bg: Int, dark: Boolean, action: Runnable, failure: Runnable)=
            UpstreamGlassHost(activity,source,bg,dark,false,false,action,null,failure)
        @JvmStatic fun toggle(activity: Activity, bg: Int, dark: Boolean, initial: Boolean, action: ToggleAction, failure: Runnable)=
            UpstreamGlassHost(activity,null,bg,dark,true,initial,null,action,failure)
    }
}
