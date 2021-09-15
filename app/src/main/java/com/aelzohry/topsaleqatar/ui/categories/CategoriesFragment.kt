package com.aelzohry.topsaleqatar.ui.categories

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.FragmentCategoriesBinding
import com.aelzohry.topsaleqatar.databinding.ViewHolderCategoryBinding
import com.aelzohry.topsaleqatar.databinding.ViewHolderSubcategoryBinding
import com.aelzohry.topsaleqatar.model.Category
import com.aelzohry.topsaleqatar.model.LocalStanderModel
import com.aelzohry.topsaleqatar.utils.base.BaseActivity
import com.aelzohry.topsaleqatar.utils.base.BaseAdapter
import com.aelzohry.topsaleqatar.utils.extenions.dpToPx
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import io.cabriole.decorator.ColumnProvider
import io.cabriole.decorator.GridMarginDecoration
import retrofit2.Call

class CategoriesFragment : BaseActivity<FragmentCategoriesBinding, CategoriesViewModel>() {

    override fun getLayoutID(): Int = R.layout.fragment_categories

    override fun getViewModel(): CategoriesViewModel =
        ViewModelProvider(this)[CategoriesViewModel::class.java]

    override fun setupUI() {
        initToolbar(getString(R.string.categories))
    }

    override fun onClickedListener() {
    }

    override fun observerLiveData() {
        vm.categoriesRes.observe(this, Observer {
            binding.recyclerView.adapter = BaseAdapter<Category, ViewHolderCategoryBinding>(
                R.layout.view_holder_category,
                vm,
                it
            )
            { bind, model, position, adapter ->

                val list = model.subcategories
                list?.add(
                    0, LocalStanderModel(
                        model._id,
                        LocalStanderModel.LocalizedModel(model.title.ar, model.title.en)
                    )
                )
                bind.rvSubCats.adapter =
                    BaseAdapter<LocalStanderModel, ViewHolderSubcategoryBinding>(
                        R.layout.view_holder_subcategory,
                        vm, list
                    ) { subBind, subModel, subPosition, _ ->
                        subBind.root.setOnClickListener {
                            if (subPosition == 0)
                                vm.onCatClickedListener(it, model)
                            else
                                vm.onSubCatClickedListener(it, model, subModel)
                        }
                    }
            }
        }
        )
    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        repository = RemoteRepository()
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_categories, container, false)
//    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//        activity?.title = getString(R.string.categories)
////        recyclerView.layoutManager = GridLayoutManager(activity, 2)
////        adapter = CategoriesAdapter(categories) {
////            onCategoryPressed(it)
////        }
////        binding.recyclerView.adapter = adapter
//
//
////        loadCategories()
//    }

    override fun onDestroy() {
        vm.call?.cancel()
        super.onDestroy()
    }

//    private fun loadCategories() {
//        startLoading()
//
//    }

//    private fun startLoading() {
//        categories.clear()
//        swipeRefreshLayout.isRefreshing = true
//    }

//    private fun endLoading() {
//        adapter.cats = categories
//        adapter.notifyDataSetChanged()
//        swipeRefreshLayout.isRefreshing = false
//    }

//    private fun onCategoryPressed(category: Category) {
//        val adsFragment = AdsFragment.newInstance(category)
//        (activity as MainActivity).pushFragment(adsFragment)
//    }

}
