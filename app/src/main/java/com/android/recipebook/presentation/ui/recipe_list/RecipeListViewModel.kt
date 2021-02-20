@file:Suppress("DEPRECATION")

package com.android.recipebook.presentation.ui.recipe_list

import android.nfc.tech.MifareUltralight.PAGE_SIZE
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.android.recipebook.domain.model.Recipe
import com.android.recipebook.network.model.RecipeDtoMapper
import com.android.recipebook.presentation.ui.recipe_list.RecipeListEvent.*
import com.android.recipebook.repository.RecipeRepository
import com.android.recipebook.util.TAG
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Named

const val PAGE_SIZE = 30

const val STATE_KEY_PAGE = "recipe.state.page.key"
const val STATE_KEY_QUERY = "recipe.state.query.key"
const val STATE_KEY_LIST_POSITION = "recipe.state.query.list_position"
const val STATE_KEY_SELECTED_CATEGORY = "recipe.state.query.selected_category"

class RecipeListViewModel
@ViewModelInject constructor(
    private val repository: RecipeRepository,
    @Named("auth_token") private val token: String,
    @Assisted private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val recipes: MutableState<List<Recipe>> = mutableStateOf(listOf())

    val query = mutableStateOf("")

    val selectedCategory: MutableState<FoodCategory?> = mutableStateOf(null)

    var categoryScrollPosition: Float = 0f

    val loading = mutableStateOf(false)

    val page = mutableStateOf(1)

    private var recipeListScrollPosition = 0

    init {
        savedStateHandle.get<Int>(STATE_KEY_PAGE)?.let { p ->
            Log.d(TAG, "restoring page: ${p}")
            setPage(p)
        }
        savedStateHandle.get<String>(STATE_KEY_QUERY)?.let { q ->
            setQuery(q)
        }
        savedStateHandle.get<Int>(STATE_KEY_LIST_POSITION)?.let { p ->
            Log.d(TAG, "restoring scroll position: ${p}")
            setListScrollPosition(p)
        }
        savedStateHandle.get<FoodCategory>(STATE_KEY_SELECTED_CATEGORY)?.let { c ->
            setSelectedCategory(c)
        }

        // Were they doing something before the process died?
        if (recipeListScrollPosition != 0) {
            onTriggerEvent(RestoreStateEvent)
        } else {
            onTriggerEvent(NewSearchEvent)
        }
    }

    fun onTriggerEvent(event: RecipeListEvent) {
        viewModelScope.launch {
            try {
                when (event) {
                    is NewSearchEvent -> {
                        newSearch()
                    }
                    is NextPageEvent -> {
                        nextPage()
                    }
                    is RestoreStateEvent -> {
                        restoreState()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "onTriggerEvent: Exception: ${e}, ${e.cause}")
            }
        }
    }

    private suspend fun restoreState() {
        loading.value = true
        // Must retrieve each page of results.
        val results: MutableList<Recipe> = mutableListOf()
        for (p in 1..page.value) {
            val result = repository.search(
                token = token,
                page = p,
                query = query.value
            )
            results.addAll(result)
            if (p == page.value) { // done
                recipes.value = results
                loading.value = false
            }
        }
    }

    // use case #1
    private suspend fun newSearch() {
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

    // use case #2
    private suspend fun nextPage() {
        //prevent duplicate events due to recompose happening to quickly
        if ((recipeListScrollPosition + 1) >= (page.value * com.android.recipebook.presentation.ui.recipe_list.PAGE_SIZE)) {
            loading.value = true
            incrementPage()
            Log.d(TAG, "nextPage: triggered: ${page.value}")

            delay(1000)   //just to see pagination

            if (page.value > 1) {
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

    /**
     * Append new recipes to the current list of recipes
     */

    private fun appendRecipes(recipes: List<Recipe>) {
        val current = ArrayList(this.recipes.value)
        current.addAll(recipes)
        this.recipes.value = current
    }

    private fun incrementPage() {
        setPage(page.value + 1)
    }

    fun onChangeRecipeScrollPosition(position: Int) {
        setListScrollPosition(position = position)
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
        setSelectedCategory(null)
        selectedCategory.value = null
    }

    fun onQueryChanged(query: String) {
        setQuery(query)
    }

    fun onSelectedCategoryChanged(category: String) {
        val newCategory = getFoodCategory(category)
        setSelectedCategory(newCategory)
        onQueryChanged(category)
    }

    fun onChangedCategoryScrollPosition(position: Float) {
        categoryScrollPosition = position
    }

    private fun setListScrollPosition(position: Int) {
        recipeListScrollPosition = position
        savedStateHandle.set(STATE_KEY_LIST_POSITION, position)
    }

    private fun setPage(page: Int) {
        this.page.value = page
        savedStateHandle.set(STATE_KEY_PAGE, page)
    }

    private fun setSelectedCategory(category: FoodCategory?) {
        selectedCategory.value = category
        savedStateHandle.set(STATE_KEY_SELECTED_CATEGORY, category)
    }

    private fun setQuery(query: String) {
        this.query.value = query
        savedStateHandle.set(STATE_KEY_QUERY, query)
    }

}