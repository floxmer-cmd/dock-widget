if (selected < apps.size - 1) selected++
        persist()
        refreshSlots()
    }

    private fun persist() {
        DockWidgetProvider.saveApps(this, apps)
        DockWidgetProvider.updateAll(this)
    }

    private fun refreshSlots() {
        slotViews.forEachIndexed { i, slot ->
            val pkg = apps[i]
            slot.background = GradientDrawable().apply {
                cornerRadius = dp(14).toFloat()
                setColor(if (i == selected) 0x66FFFFFF else 0x22FFFFFF)
            }
            val icon = all.find { it.pkg == pkg }?.icon
                ?: getDrawable(android.R.drawable.ic_input_add)
            slot.setImageDrawable(icon)
        }
    }

    private inner class AppAdapter : BaseAdapter() {
        override fun getCount(): Int = all.size
        override fun getItem(position: Int): Any = all[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val item = all[position]
            return LinearLayout(this@AppPickerActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8), dp(10), dp(8), dp(10))

                addView(ImageView(this@AppPickerActivity).apply {
                    setImageDrawable(item.icon)
                    layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
                })
                addView(TextView(this@AppPickerActivity).apply {
                    text = item.label
                    textSize = 16f
                    setPadding(dp(16), 0, 0, 0)
                })
            }
        }
    }
}
