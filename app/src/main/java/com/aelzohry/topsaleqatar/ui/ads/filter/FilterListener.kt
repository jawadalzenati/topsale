package com.aelzohry.topsaleqatar.ui.ads.filter

import com.aelzohry.topsaleqatar.model.Category
import com.aelzohry.topsaleqatar.model.LocalStanderModel
import com.aelzohry.topsaleqatar.model.StanderModel

interface FilterListener {
    fun onFilterClickedApplyListener(
        category: Category?,
        selectedSubCat: LocalStanderModel?,
        selectedType: LocalStanderModel?,
        selectedCarMake: StanderModel?,
        selectedModelLocal: ArrayList<StanderModel>?,
        selectedYear: ArrayList<StanderModel>?,
        selectedRegion: LocalStanderModel?,
        fromPrice: String?, toPrice: String?,
        fromRooms: String?, toRooms: String?,
        fromBathRooms: String?, toBathRooms: String?
    )
}