package com.android.recipebook.presentation.ui.recipe_list

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ScrollableRow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.android.recipebook.R
import com.android.recipebook.presentation.BaseApplication
import com.android.recipebook.presentation.components.*
import com.android.recipebook.presentation.components.HeartAnimationDefinition.HeartButtonState.*
import com.android.recipebook.presentation.theme.AppTheme
import com.android.recipebook.util.TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class RecipeListFragment : Fragment() {

    @Inject
    lateinit var application: BaseApplication

    private val viewModel: RecipeListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return ComposeView(requireContext()).apply {
            setContent {

                AppTheme(
                    darkTheme = application.isDark.value
                ) {
                    val recipes = viewModel.recipes.value

                    val query = viewModel.query.value

                    val selectedCategory = viewModel.selectedCategory.value

                    val loading = viewModel.loading.value

                    Scaffold(
                        topBar = {
                            SearchAppBar(
                                query = query,
                                onQueryChanged = viewModel::onQueryChanged,
                                onExecuteSearch = viewModel::newSearch,
                                scrollPosition = viewModel.categoryScrollPosition,
                                selectedCategory = selectedCategory,
                                onSelectedCategoryChanged = viewModel::onSelectedCategoryChanged,
                                onChangedCategoryScrollPosition = viewModel::onChangedCategoryScrollPosition,
                                onToggleTheme = {
                                    application.toggleLightTheme()
                                }
                            )
                        },
                        bottomBar = {
                            MyBottomBar()
                        },
                        drawerContent = {
                            MyDrawer()
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colors.background)
                        ) {
                            if (loading) {
                                LoadingRecipeListShimmer(imageHeight = 250.dp)
                            } else {
                                LazyColumn {
                                    itemsIndexed(
                                        items = recipes
                                    ) { index, recipe ->
                                        RecipeCard(recipe = recipe, onClick = {})
                                    }
                                }
                            }
                            CircularIndeterminateProgressBar(isDisplayed = loading)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyBottomBar() {
    BottomNavigation(
        elevation = 12.dp
    ) {
        BottomNavigationItem(
            icon = {Icon(Icons.Default.BrokenImage)},
            selected = false,
            onClick = {

            }
        )
        BottomNavigationItem(
            icon = {Icon(Icons.Default.Search)},
            selected = true,
            onClick = {

            }
        )
        BottomNavigationItem(
            icon = {Icon(Icons.Default.AccountBalanceWallet)},
            selected = false,
            onClick = {

            }
        )
    }
}

@Composable
fun MyDrawer() {
    Column {
        Text(text = "Item1")
        Text(text = "Item2")
        Text(text = "Item3")
        Text(text = "Item4")
        Text(text = "Item5")
    }
}