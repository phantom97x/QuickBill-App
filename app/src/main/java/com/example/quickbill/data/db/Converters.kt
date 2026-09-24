package com.example.quickbill.data.db

import androidx.room.TypeConverter
import com.example.quickbill.data.model.BillItem
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromBillItemList(items: List<BillItem>?): String {
        if (items.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("itemNo", item.itemNo)
            obj.put("name", item.name)
            obj.put("qty", item.qty)
            obj.put("unit", item.unit)
            obj.put("price", item.price)
            obj.put("lineTotal", item.lineTotal)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toBillItemList(jsonString: String?): List<BillItem> {
        if (jsonString.isNullOrBlank()) return emptyList()
        val list = mutableListOf<BillItem>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    BillItem(
                        itemNo = obj.optInt("itemNo", 0),
                        name = obj.optString("name", ""),
                        qty = obj.optDouble("qty", 0.0),
                        unit = obj.optString("unit", "pcs"),
                        price = obj.optDouble("price", 0.0),
                        lineTotal = obj.optDouble("lineTotal", 0.0)
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }
}
