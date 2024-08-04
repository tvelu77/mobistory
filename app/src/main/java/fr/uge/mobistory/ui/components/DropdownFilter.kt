package fr.uge.mobistory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import fr.uge.mobistory.R

/**
 * A composable representing a dropdown menu containing filters.
 * The user can choose a filter which will be applied on the value.
 *
 * @param state MutableState of Int, represents the filters that has been chosen.
 */
@Composable
fun DropdownFilter(state: MutableState<Int>) {
    var expanded by remember { mutableStateOf(false) }
    val items = getAllFilterValue()
    var selectedIndex by remember { mutableIntStateOf(0) }
    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        Text(items[selectedIndex],modifier = Modifier.fillMaxWidth()
            .clickable(onClick = { expanded = true }).background(Color.Gray))
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth().background(
                Color.Gray)
        ) {
            items.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    selectedIndex = index
                    state.value = index
                    expanded = false
                }) {
                    Text(text = s)
                }
            }
        }
    }
}

/**
 * For the filters, all the string value has been declared in the value/string.
 * This is kind of redundant but it creates a list for the dropdown menu.
 *
 * @return List of String, a list containing all the filters.
 */
@Composable
private fun getAllFilterValue(): List<String> {
    return arrayListOf(LocalContext.current.getString(R.string.filter_date_asc),
        LocalContext.current.getString(R.string.filter_date_des), LocalContext.current.getString(R.string.filter_popularity_asc),
        LocalContext.current.getString(R.string.filter_popularity_des))
}