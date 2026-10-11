package com.evgeny.dock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.widget.RemoteViews

class DockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) drawWidget(context, manager, id)
    }

    companion object {
        const val CELLS = 12
        private const val PREFS = "dock_prefs"
        private const val KEY = "apps"

        // ID ячеек из разметки widget_dock.xml
        private val CELL_IDS = intArrayOf(
            R.id.cell_0, R.id.cell_1, R.id.cell_2, R.id.cell_3,
            R.id.cell_4, R.id.cell_5, R.id.cell_6, R.id.cell_7,
            R.id.cell_8, R.id.cell_9, R.id.cell_10, R.id.cell_11
        )

        /** Читает из памяти список из 12 пакетов. Пустая ячейка = null. */
        fun loadApps(ctx: Context): Array<String?> {
            val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, "") ?: ""
            val parts = raw.split(",")
            return Array(CELLS) { i -> parts.getOrNull(i)?.takeIf { it.isNotBlank() } }
        }

        /** Сохраняет список в память телефона. */
        fun saveApps(ctx: Context, apps: Array<String?>) {
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY, apps.joinToString(",") { it ?: "" })
                .apply()
        }

        /** Перерисовывает все виджеты на экране. */
        fun updateAll(ctx: Context) {
            val manager = AppWidgetManager.getInstance(ctx)
            val ids = manager.getAppWidgetIds(ComponentName(ctx, DockWidgetProvider::class.java))
            for (id in ids) drawWidget(ctx, manager, id)
        }

        /** Рисует один виджет: иконки и обработчики нажатий. */
        fun drawWidget(ctx: Context, manager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(ctx.packageName, R.layout.widget_dock)
            val pm = ctx.packageManager
            val apps = loadApps(ctx)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            // Нажатие на пустую ячейку или на фон открывает экран выбора
            val pickerIntent = Intent(ctx, AppPickerActivity::class.java)
            val pickerPending = PendingIntent.getActivity(ctx, 999, pickerIntent, flags)

            for (i in 0 until CELLS) {
                val pkg = apps[i]
                val icon: Drawable? = pkg?.let {
                    try {
                        pm.getApplicationIcon(it)
                    } catch (e: PackageManager.NameNotFoundException) {
                        null // приложение удалили
                    }
                }
                val launch = pkg?.let { pm.getLaunchIntentForPackage(it) }

                if (icon != null && launch != null) {
                    views.setImageViewBitmap(CELL_IDS[i], icon.toBitmap())
                    views.setOnClickPendingIntent(
                        CELL_IDS[i],
                        PendingIntent.getActivity(ctx, i, launch, flags)
                    )
                } else {
                    views.setImageViewResource(CELL_IDS[i], android.R.drawable.ic_input_add)
                    views.setOnClickPendingIntent(CELL_IDS[i], pickerPending)
                }
            }

            views.setOnClickPendingIntent(R.id.dock_root, pickerPending)
            manager.updateAppWidget(widgetId, views)
        }
    }
}

/** Превращает иконку приложения в Bitmap, который можно показать в виджете. */
private fun Drawable.toBitmap(size: Int = 128): Bitmap {
  val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, size, size)
    draw(canvas)
    return bitmap
}
