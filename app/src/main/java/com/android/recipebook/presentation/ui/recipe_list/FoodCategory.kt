package com.android.recipebook.presentation.ui.recipe_list

import com.android.recipebook.presentation.ui.recipe_list.FoodCategory.*

enum class FoodCategory(val value: String) {
    CHICKEN("Chicken"),
    SOUP("Soup"),
    DESSERT("Dessert"),
    FISH("Fish"),
    VEGETARIAN("Vegetarian"),
    MILK("Milk"),
    VEGAN("Vegan"),
    PIZZA("Pizza"),
    DONUT("Donut")
}

fun getAllFoodCategories(): List<FoodCategory> {
    return listOf(
        CHICKEN,
        SOUP,
        DESSERT,
        FISH,
        VEGETARIAN,
        MILK,
        VEGAN,
        PIZZA,
        DONUT
    )
}

fun getFoodCategory(value: String): FoodCategory? {
    val map = FoodCategory.values().associateBy(FoodCategory::value)
    return map[value]
}