package com.example.review

data class StockItem(val sku: String, val quantity: Int, val warehouseZone: String?, val isPerishable: Boolean, val isFragile: Boolean)

/**
 * Kotlin counterpart to OrderProcessor.java -- deliberately triggers
 * LONG_FUNCTION and NESTED_CONDITIONAL, and includes TODO comments for
 * TODO_FIXME_DENSITY. NULL_DEREFERENCE is Java-only in v0.1 (see the
 * main README's "Why built this way" for why), so nothing here targets
 * that rule.
 */
class InventoryService {

    // Deliberately long function with deep if/when nesting.
    fun reorganizeWarehouse(items: List<StockItem>, targetZones: List<String>): Map<String, List<StockItem>> {
        // TODO: this whole reorganization strategy needs a rewrite once
        // TODO: multi-warehouse support ships, this only handles one site
        val result = mutableMapOf<String, MutableList<StockItem>>()
        for (zone in targetZones) {
            result[zone] = mutableListOf()
        }

        for (item in items) {
            if (item.isPerishable) {
                if (item.warehouseZone != null) {
                    if (item.warehouseZone in targetZones) {
                        if (item.quantity > 0) {
                            result.getValue(item.warehouseZone).add(item)
                        } else {
                            result.getValue("overflow")?.add(item)
                        }
                    } else {
                        result.getOrPut("cold-storage") { mutableListOf() }.add(item)
                    }
                } else {
                    result.getOrPut("unassigned") { mutableListOf() }.add(item)
                }
            } else if (item.isFragile) {
                when {
                    item.quantity > 100 -> result.getOrPut("bulk-fragile") { mutableListOf() }.add(item)
                    item.quantity > 10 -> result.getOrPut("standard-fragile") { mutableListOf() }.add(item)
                    else -> result.getOrPut("small-fragile") { mutableListOf() }.add(item)
                }
            } else {
                result.getOrPut("general") { mutableListOf() }.add(item)
            }
        }

        return result
    }

    // Short, shallow -- should NOT trigger LONG_FUNCTION or NESTED_CONDITIONAL.
    fun totalQuantity(items: List<StockItem>): Int = items.sumOf { it.quantity }
}
