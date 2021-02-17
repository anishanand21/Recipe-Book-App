@file:Suppress("DEPRECATION")

package com.android.recipebook.presentation.ui.recipe_list

import android.nfc.tech.MifareUltralight.PAGE_SIZE
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.recipebook.domain.model.Recipe
import com.android.recipebook.network.model.RecipeDtoMapper
import com.android.recipebook.repository.RecipeRepository
import com.android.recipebook.util.TAG
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Named

class RecipeListViewModel
@ViewModelInject constructor(
    private val repository: RecipeRepository,
    @Named("auth_token") private val token: String
) : ViewModel() {

    val recipes: MutableState<List<Recipe>> = mutableStateOf(listOf())

    val query = mutableStateOf("")

    val selectedCategory: MutableState<FoodCategory?> = mutableStateOf(null)

    var categoryScrollPosition: Float = 0f

    val loading = mutableStateOf(false)

    val page = mutableStateOf(1)

    private var recipeListScrollPosition = 0

    init {
        newSearch()
    }

    fun newSearch() {
        viewModelScope.launch {
            loading.value = true
            resetSearchState()
            delay(3000)

            val result = repository.search(
                token = token,
                page = 1,
                query = query.value
            )

            recipes.value = result
            loading.value = false
        }
    }

    fun nextPage() {
        viewModelScope.launch {
            //prevent duplicate events due to recompose happening to quickly
            if((recipeListScrollPosition + 1) >= (page.value * PAGE_SIZE)) {
                loading.value = true
                incrementPage()
                Log.d(TAG, "nextPage: triggered: ${page.value}")

                delay(1000)   //just to see pagination

                if(page.value > 1) {
                    val result = repository.search(
                        token = token,
                        page = page.value,
                        query = query.value
                    )
                    Log.d(TAG, "nextPage: ${result}")
                    appendRecipes(result)
                }
                loading.value = false
            }
        }
    }

    /**
    * Append new recipes to the current list of recipes
    */

    private fun appendRecipes(recipes: List<Recipe>) {
        val current = ArrayList(this.recipes.value)
        current.addAll(recipes)
        this.recipes.value = current
    }

    private fun incrementPage() {
        page.value = page.value + 1
    }

    fun onChangeRecipeScrollPosition(position: Int) {
        recipeListScrollPosition = position
    }

    /**
     * called when a new search is executed
     * */

    private fun resetSearchState() {
        recipes.value = emptyList()
        page.value = 1
        onChangeRecipeScrollPosition(0)
        if (selectedCategory.value?.value != query.value)
            clearSelectedCategory()
    }

    private fun clearSelectedCategory() {
        selectedCategory.value = null
    }

    fun onQueryChanged(query: String) {
        this.query.value = query
    }

    fun onSelectedCategoryChanged(category: String) {
        val newCategory = getFoodCategory(category)
        selectedCategory.value = newCategory
        onQueryChanged(category)
    }

    fun onChangedCategoryScrollPosition(position: Float) {
        categoryScrollPosition = position
    }

}