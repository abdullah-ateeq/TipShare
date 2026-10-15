package com.example.tipshare.data.repository

import android.content.SharedPreferences
import com.example.tipshare.data.model.TipShareHistory

/**
 * Repository interface for managing calculation history in SharedPreferences.
 */
interface TipShareHistoryRepository {
    fun getHistory(): List<TipShareHistory>
    fun saveCalculation(historyItem: TipShareHistory)
    fun deleteCalculation(id: String)
    fun clearHistory()
}

/**
 * SharedPreferences implementation of TipShareHistoryRepository.
 * Maintains maximum 10 recent calculations.
 */
class SharedPreferencesHistoryRepository(
    private val sharedPreferences: SharedPreferences
) : TipShareHistoryRepository {

    companion object {
        const val KEY_HISTORY = "tipshare_history_json"
        const val MAX_HISTORY_ITEMS = 10
    }

    override fun getHistory(): List<TipShareHistory> {
        val json = sharedPreferences.getString(KEY_HISTORY, null) ?: return emptyList()
        return HistoryJsonCodec.fromJson(json)
    }

    override fun saveCalculation(historyItem: TipShareHistory) {
        val current = getHistory().toMutableList()
        // Prevent immediate duplicates with identical data
        val isDuplicate = current.firstOrNull()?.let { first ->
            first.billMinorUnits == historyItem.billMinorUnits &&
                    first.tipMinorUnits == historyItem.tipMinorUnits &&
                    first.currencyCode == historyItem.currencyCode &&
                    first.tipPercentageBasisPoints == historyItem.tipPercentageBasisPoints &&
                    first.customTip == historyItem.customTip &&
                    first.peopleCount == historyItem.peopleCount
        } == true

        if (isDuplicate) return

        current.add(0, historyItem)
        while (current.size > MAX_HISTORY_ITEMS) {
            current.removeAt(current.lastIndex)
        }
        sharedPreferences.edit()
            .putString(KEY_HISTORY, HistoryJsonCodec.toJson(current))
            .apply()
    }

    override fun deleteCalculation(id: String) {
        val current = getHistory().toMutableList()
        val changed = current.removeAll { it.id == id }
        if (changed) {
            sharedPreferences.edit()
                .putString(KEY_HISTORY, HistoryJsonCodec.toJson(current))
                .apply()
        }
    }

    override fun clearHistory() {
        sharedPreferences.edit()
            .remove(KEY_HISTORY)
            .apply()
    }
}

/**
 * Pure Kotlin JSON codec for TipShareHistory with zero external dependencies.
 */
object HistoryJsonCodec {
    fun toJson(items: List<TipShareHistory>): String {
        val sb = StringBuilder()
        sb.append("[")
        for (i in items.indices) {
            val item = items[i]
            val bpStr = item.tipPercentageBasisPoints?.toString() ?: "null"
            val escapedId = escape(item.id)
            val escapedCode = escape(item.currencyCode)
            val escapedSym = escape(item.currencySymbol)
            sb.append("{\"id\":\"$escapedId\",\"bill\":${item.billMinorUnits},\"tip\":${item.tipMinorUnits},\"curr\":\"$escapedCode\",\"sym\":\"$escapedSym\",\"bp\":$bpStr,\"custom\":${item.customTip},\"people\":${item.peopleCount},\"time\":${item.calculatedAt}}")
            if (i < items.size - 1) sb.append(",")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun escape(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"")

    fun fromJson(json: String): List<TipShareHistory> {
        val trimmed = json.trim()
        if (trimmed.isEmpty() || trimmed == "[]") return emptyList()
        val list = mutableListOf<TipShareHistory>()
        val regex = Regex("""\{"id":"(.*?)","bill":(-?\d+),"tip":(-?\d+),"curr":"(.*?)","sym":"(.*?)","bp":(null|-?\d+),"custom":(true|false),"people":(-?\d+),"time":(-?\d+)\}""")
        val matches = regex.findAll(trimmed)
        for (m in matches) {
            try {
                val id = m.groupValues[1].replace("\\\"", "\"").replace("\\\\", "\\")
                val bill = m.groupValues[2].toLong()
                val tip = m.groupValues[3].toLong()
                val curr = m.groupValues[4].replace("\\\"", "\"").replace("\\\\", "\\")
                val sym = m.groupValues[5].replace("\\\"", "\"").replace("\\\\", "\\")
                val bp = if (m.groupValues[6] == "null") null else m.groupValues[6].toInt()
                val custom = m.groupValues[7].toBoolean()
                val people = m.groupValues[8].toInt()
                val time = m.groupValues[9].toLong()
                list.add(TipShareHistory(id, bill, tip, curr, sym, bp, custom, people, time))
            } catch (_: Exception) {
            }
        }
        return list
    }
}
