package com.aelzohry.topsaleqatar.ui.new_ad

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aelzohry.topsaleqatar.BR
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.CatCarAttrBinding
import com.aelzohry.topsaleqatar.databinding.FragmentNewAdBinding
import com.aelzohry.topsaleqatar.databinding.ListItem2Binding
import com.aelzohry.topsaleqatar.databinding.ListItemBinding
import com.aelzohry.topsaleqatar.helper.Helper
import com.aelzohry.topsaleqatar.model.*
import com.aelzohry.topsaleqatar.repository.Repository
import com.aelzohry.topsaleqatar.repository.remote.RemoteRepository
import com.aelzohry.topsaleqatar.ui.ad_details.AdDetailsFragment
import com.aelzohry.topsaleqatar.ui.new_ad.adLocation.AdLocationActivity
import com.aelzohry.topsaleqatar.utils.base.BaseActivity
import com.aelzohry.topsaleqatar.utils.base.BaseAdapter
import com.aelzohry.topsaleqatar.utils.base.BaseFragment
import com.aelzohry.topsaleqatar.utils.extenions.setupDialog
import com.aelzohry.topsaleqatar.utils.extenions.snackBar
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.model.LatLng
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_publish_no_images.*
import kotlinx.android.synthetic.main.fragment_new_ad.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class NewAdFragment : BaseActivity<FragmentNewAdBinding, NewAdViewModel>() {

    private var deletedPhotos: ArrayList<String> = arrayListOf()
    private lateinit var imagesAdapter: ImagesAdapter
    private var catDialog: Dialog? = null
    private var subCatDialog: Dialog? = null
    private var typeDialog: Dialog? = null

    private var carStepDialog: Dialog? = null
    private var aqarStepDialog: Dialog? = null

    override fun getLayoutID(): Int = R.layout.fragment_new_ad

    override fun getViewModel(): NewAdViewModel =
        ViewModelProvider(this)[NewAdViewModel::class.java]

    override fun setupUI() {

        intent?.let {
            vm.ad = it.getParcelableExtra(ARG_AD)
        }

        imagesAdapter = ImagesAdapter(arrayListOf(), 0, { it, view ->
            // delete or make default
            didTapImage(it, view)
        }) {
            // add new
            pickImages()
        }
        imagesRecyclerView.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.HORIZONTAL,
            false
        )
        imagesRecyclerView.setHasFixedSize(true)
        imagesRecyclerView.adapter = imagesAdapter

        if (vm.ad != null) {
            publishButton.setText(R.string.save)
            editAd()
        }
        initToolbar(getString(if (vm.ad == null) R.string.new_ad else R.string.edit_ad))
    }

    override fun onClickedListener() {
        categoryButton.setOnClickListener { catDialog?.show() }
        subcategoryButton.setOnClickListener { subCatDialog?.show() }
        typeButton.setOnClickListener { typeDialog?.show() }

        propertiesButton.setOnClickListener {
            setupAqarStepDialog()
            aqarStepDialog?.show()
        }

        carAttrButton.setOnClickListener {
            setupCarStepDialog()
            carStepDialog?.show()
        }

        publishButton.setOnClickListener { publish() }
        locationButton.setOnClickListener { picLocation() }

    }

    private fun picLocation() {

        startActivityForResult(Intent(this, AdLocationActivity::class.java), PLACE_PICKER_REQUEST)
//        var builder = PlacePicker.IntentBuilder()
//        try {
//            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST)
//        } catch (e: GooglePlayServicesRepairableException) {
//            e.printStackTrace()
//        } catch (e: GooglePlayServicesNotAvailableException) {
//            e.printStackTrace()
//        }
    }

    override fun observerLiveData() {

        vm.catsRes.observe(this, androidx.lifecycle.Observer {

            val list = ArrayList<LocalStanderModel>()
            it.forEach {
                list.add(
                    LocalStanderModel(
                        it._id,
                        LocalStanderModel.LocalizedModel(it.title.ar, it.title.en)
                    )
                )
            }
            catDialog = Dialog(this)
            setupDialog(catDialog, list, { modelLocal: LocalStanderModel, i ->
                vm.onCatSelectedListener(it[i])
            })
        })

        vm.subCatsRes.observe(this, androidx.lifecycle.Observer {
            subCatDialog = Dialog(this)
            setupDialog(subCatDialog, it) { modelLocal: LocalStanderModel ->
                vm.onSubCatSelectedListener(modelLocal)
            }
        })

        vm.typeRes.observe(this, androidx.lifecycle.Observer {
            typeDialog = Dialog(this)
            setupDialog(typeDialog, it) { modelLocal: LocalStanderModel ->
                vm.onSelectedTypeListener(modelLocal)
            }
        })

    }

    // cars
    private fun setupCarStepDialog() {
        val years = ArrayList<LocalStanderModel>()
        for (i in 2020 downTo 1800) {
            years.add(
                LocalStanderModel(
                    i.toString(),
                    LocalStanderModel.LocalizedModel(i.toString(), i.toString())
                )
            )
        }
        carStepDialog = Dialog(this)
        var b = DataBindingUtil.inflate<CatCarAttrBinding>(
            layoutInflater,
            R.layout.cat_car_attr,
            null,
            false
        )
        b.setVariable(BR.vm, vm)
        b.executePendingBindings()
        carStepDialog?.setContentView(b.root)

        carStepDialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        vm.currentStep.set(0)
        vm.tvTitle.set(getString(R.string.car_year))
        vm.selectCarText.set("")

        b.rv.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        b.rv.adapter = BaseAdapter<LocalStanderModel, ListItemBinding>(
            R.layout.list_item,
            vm,
            years
        ) { bind, model, postion, adapter ->
            bind.root.setOnClickListener {
                vm.selectCarText.set(model.title.localized)
                model.isChecked = true
                vm.onSelectedYearsListener(model)
                setupMakeStep(b)
            }
        }
    }

    private fun setupMakeStep(bind: CatCarAttrBinding) {
        vm.tvTitle.set(getString(R.string.car_make))
        vm.currentStep.set(1)

        vm.isLoading.postValue(true)
        vm.repository.getCarMake {

            vm.isLoading.postValue(false)
            bind.rv.adapter =
                BaseAdapter<StanderModel, ListItem2Binding>(
                    R.layout.list_item2,
                    vm,
                    it?.response
                ) { b, m, i, a ->
                    b.root.setOnClickListener {
                        vm.isLoading.postValue(true)
                        val oldText = vm.selectCarText.get() ?: ""
                        vm.selectCarText.set(oldText + "  ,  " + m.title)

                        vm.onMakeSelectedListener(m) { data ->
                            vm.isLoading.postValue(false)
                            setupModelStep(bind, data)

                        }
                    }
                }
        }

    }

    private fun setupModelStep(bind: CatCarAttrBinding, data: List<StanderModel>) {
        vm.tvTitle.set(getString(R.string.car_model))
        vm.currentStep.set(2)
        bind.rv.adapter =
            BaseAdapter<StanderModel, ListItem2Binding>(
                R.layout.list_item2,
                vm,
                data
            ) { b, m, i, a ->
                b.root.setOnClickListener {
                    val oldText = vm.selectCarText.get() ?: ""
                    vm.selectCarText.set(oldText + "  ,  " + m.title)
                    vm.onModelSelectedListener(m)
                    carStepDialog?.dismiss()
                }
            }
    }

    //aqar
    private fun setupAqarStepDialog() {

        aqarStepDialog = Dialog(this)
        var b = DataBindingUtil.inflate<CatCarAttrBinding>(
            layoutInflater,
            R.layout.cat_car_attr,
            null,
            false
        )
        b.setVariable(BR.vm, vm)
        b.executePendingBindings()
        aqarStepDialog?.setContentView(b.root)

        aqarStepDialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        vm.currentStep.set(0)
        vm.tvTitle.set(getString(R.string.region))
        vm.selectPropertiesText.set("")

        b.tvTitle.setText(R.string.region_number_of_rooms_number_of_bathrooms)
        b.rv.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        vm.isLoading.postValue(true)
        vm.repository.getRegion {

            vm.isLoading.postValue(false)
            b.rv.adapter = BaseAdapter<LocalStanderModel, ListItemBinding>(
                R.layout.list_item,
                vm,
                it?.response
            ) { bind, model, postion, adapter ->
                bind.root.setOnClickListener {
                    vm.selectPropertiesText.set(model.title.localized)
                    vm.onRegionSelectedListener(model)
                    setupRoomsStep(b)
                }
            }

        }
    }

    private fun setupRoomsStep(bind: CatCarAttrBinding) {
        vm.tvTitle.set(getString(R.string.rooms_numbers))
        vm.currentStep.set(1)

        val list = ArrayList<StanderModel>()
        list.add(StanderModel("1", getString(R.string.room)))
        list.add(StanderModel("2", "2 ${getString(R.string.room)}"))
        list.add(StanderModel("3", "3 ${getString(R.string.room)}"))
        list.add(StanderModel("4", "4 ${getString(R.string.room)}"))
        list.add(StanderModel("5", "5 ${getString(R.string.room)}"))

        bind.rv.adapter =
            BaseAdapter<StanderModel, ListItem2Binding>(
                R.layout.list_item2,
                vm, list
            ) { b, m, i, a ->
                b.root.setOnClickListener {
                    val oldText = vm.selectPropertiesText.get() ?: ""
                    vm.selectPropertiesText.set(oldText + "  ,  " + m.title)
                    vm.onRoomSelectedListener(m)
                    setupBathRoomsStep(bind)
                }
            }

    }

    private fun setupBathRoomsStep(bind: CatCarAttrBinding) {
        vm.tvTitle.set(getString(R.string.bath_room_number))
        vm.currentStep.set(2)

        val list = ArrayList<StanderModel>()
        list.add(StanderModel("1", getString(R.string.room)))
        list.add(StanderModel("2", "2 ${getString(R.string.bath_room)}"))
        list.add(StanderModel("3", "3 ${getString(R.string.bath_room)}"))
        list.add(StanderModel("4", "4 ${getString(R.string.bath_room)}"))
        list.add(StanderModel("5", "5 ${getString(R.string.bath_room)}"))

        bind.rv.adapter =
            BaseAdapter<StanderModel, ListItem2Binding>(
                R.layout.list_item2,
                vm, list
            ) { b, m, i, a ->
                b.root.setOnClickListener {
                    val oldText = vm.selectPropertiesText.get() ?: ""
                    vm.selectPropertiesText.set(oldText + "  ,  " + m.title)
                    vm.onBathRoomSelectedListener(m)
                    aqarStepDialog?.dismiss()
                }
            }
    }


    private fun editAd() {
        val ad = vm.ad ?: return

        val images = ArrayList(ad.photos.map { NewImage(it, null) })
        imagesAdapter.images = images
        imagesDidChange()

        vm.onCatSelectedListener(ad.category)
        vm.onSubCatSelectedListener(ad.subcategory)
        vm.onSelectedTypeListener(ad.type)
        vm.onRegionSelectedListener(ad.region)

        vm.onMakeSelectedListener(ad.carMake) {}
        vm.onModelSelectedListener(ad.carModel)
        vm.onSelectedYearsListener(
            LocalStanderModel(
                ad.carYear ?: "",
                LocalStanderModel.LocalizedModel(ad.carYear ?: "", ad.carYear ?: "")
            )
        )
        vm.etDesc.set(ad.details)
        vm.etTitle.set(ad.title)
        vm.etPrice.set(ad.price.toString())
        vm.etKm.set(ad.km)
    }

    private fun pickImages() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) proceedPickImages()

                    if (report?.isAnyPermissionPermanentlyDenied == true)
                        Helper.showSettingsDialog(this@NewAdFragment, getString(R.string.storage))
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private fun proceedPickImages() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(
            Intent.createChooser(
                intent,
                getString(R.string.select_photos)
            ), SELECT_PICTURES
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode != RESULT_OK)
            return

        if (requestCode == SELECT_PICTURES) {
            if (resultCode == Activity.RESULT_OK) {
                if (data?.clipData != null) {
                    val clipData = data.clipData ?: return
                    val count = clipData.itemCount
                    val items = arrayListOf<String>()

                    // check clipData.itemCount
                    if ((imagesAdapter.images.size + count) > imagesAdapter.MAX_IMAGES) {
                        vm.showToast.postValue(getString(R.string.max_images_alert))
                        return
                    }

                    for (i in 0 until count) {
                        clipData.getItemAt(i).uri?.let {
                            Log.i("NewAdFragment", it.toString())
                            Helper.getImagePath(it, this)?.let { path ->
                                items.add(path)
                            }
                        }
                    }

                    if (items.isNotEmpty())

                        didPickImages(items)

                } else if (data?.data != null) {
                    data.data?.let {
                        Helper.getImagePath(it, this)?.let { path ->
                            didPickImages(arrayListOf(path))
                        }
                    }
                }
            }
        }

        if (requestCode == PLACE_PICKER_REQUEST) {

            vm.lat = data?.getDoubleExtra("lat", 0.0)
            vm.lng = data?.getDoubleExtra("lng", 0.0)
            vm.etLocation.set(data?.getStringExtra("address"))
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun didPickImages(images: List<String>) {
        val newImages = images.map { NewImage(null, it) }
        imagesAdapter.images.addAll(newImages)
        imagesDidChange()
    }

    private fun imagesDidChange() {
        imagesAdapter.notifyDataSetChanged()
        if (imagesAdapter.images.isEmpty()) {
            defaultImageView.setImageResource(R.mipmap.logo)
        } else {
            val image = imagesAdapter.images[imagesAdapter.defaultImageIndex]
            if (image.bitmap != null) {
                defaultImageView.setImageBitmap(image.bitmap)
            } else {
                Picasso.get().load(image.photo?.thumbUrl).placeholder(R.mipmap.logo).into(
                    defaultImageView
                )
            }
        }
    }

    private fun didTapImage(position: Int, v: View) {
        val image = imagesAdapter.images.getOrNull(position) ?: return
        val popupMenu = PopupMenu(this, v)
        val menu = popupMenu.menu
        popupMenu.menuInflater.inflate(R.menu.ad_details_menu, menu)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.make_default -> {
                    imagesAdapter.defaultImageIndex = position
                    imagesDidChange()
                }

                R.id.delete -> {
                    image.photo?.let {
                        deletedPhotos.add(it._id)
                    }
                    if (imagesAdapter.defaultImageIndex == position) {
                        if (imagesAdapter.defaultImageIndex > 0) {
                            imagesAdapter.defaultImageIndex -= 1
                        }
                    } else if (imagesAdapter.defaultImageIndex > position) {
                        imagesAdapter.defaultImageIndex -= 1
                    }
                    imagesAdapter.images.removeAt(position)
                    imagesDidChange()
                }
            }
            return@setOnMenuItemClickListener true
        }

        popupMenu.show()

//        val dialog = AlertDialog.Builder(context)
//            .setView(R.layout.dialog_image_actions)
//            .create()
//
//        dialog.show()
//
//        dialog.makeDefaultButton.setOnClickListener {
//            imagesAdapter.defaultImageIndex = position
//            imagesDidChange()
//            dialog.dismiss()
//        }
//
//        dialog.deleteButton.setOnClickListener {
//            image.photo?.let {
//                deletedPhotos.add(it._id)
//            }
//            if (imagesAdapter.defaultImageIndex == position) {
//                if (imagesAdapter.defaultImageIndex > 0) {
//                    imagesAdapter.defaultImageIndex -= 1
//                }
//            } else if (imagesAdapter.defaultImageIndex > position) {
//                imagesAdapter.defaultImageIndex -= 1
//            }
//            imagesAdapter.images.removeAt(position)
//            imagesDidChange()
//            dialog.dismiss()
//        }
    }

    private fun publish(withoutPhotos: Boolean = false) {

        if (vm.etTitle.get()?.isNullOrEmpty() == true) {
            vm.showToast.postValue(R.string.enter_ad_name)
            return
        }

        if (vm.selectedCat == null) {
            snackBar(R.string.select_category, R.string.select) {
                catDialog?.show()
            }
            return
        }

        if (vm.selectedCat?.subcategories?.isNotEmpty() == true && vm.selectedSubCat == null) {
            snackBar(R.string.select_sub_category, R.string.select) {
                subCatDialog?.show()
            }
            return
        }

        if (vm.selectedCat?.types?.isNotEmpty() == true && vm.selectedType == null) {
            snackBar(R.string.select_type, R.string.select) {
                typeDialog?.show()
            }
            return
        }

        if (vm.selectedCat?.categoryClass == CategoryClass.CARS && vm.selectedCarMake == null) {
            snackBar(R.string.select_make, R.string.select) {
                carStepDialog?.show()
            }
            return
        }

        if (vm.selectedCat?.categoryClass == CategoryClass.CARS && vm.selectedModelLocal == null) {
            snackBar(R.string.select_model, R.string.select) {
                carStepDialog?.show()
            }
            return
        }

        if (vm.selectedCat?.categoryClass == CategoryClass.CARS && vm.selectedYear == null) {
            snackBar(R.string.select_year, R.string.select) {
                carStepDialog?.show()
            }
            return
        }

        if (vm.selectedCat?.categoryClass == CategoryClass.PROPERTIES && vm.selectedRegion == null) {
            snackBar(R.string.select_region, R.string.select) {
                aqarStepDialog?.show()
            }
            return
        }

        if (vm.selectedCat?.categoryClass == CategoryClass.CARS && vm.etKm.get()
                ?.isNullOrEmpty() == true
        ) {
            vm.showToast.postValue(R.string.km_error)
            return
        }

        if (vm.selectedCat?.categoryClass == CategoryClass.PROPERTIES && vm.selectedRooms == null
        ) {
            vm.showToast.postValue(R.string.room_error)
            return
        }

        if (vm.selectedCat?.categoryClass == CategoryClass.PROPERTIES && vm.selectedBathRoom == null
        ) {
            vm.showToast.postValue(R.string.bath_room_error)
            return
        }



        if (vm.etPrice.get()?.isNullOrEmpty() == true) {
            vm.showToast.postValue(R.string.enter_price)
            return
        }

        if (!withoutPhotos && imagesAdapter.images.isEmpty()) {

            snackBar(R.string.select_photos, R.string.select) {
                val dialog = AlertDialog.Builder(this)
                    .setView(R.layout.dialog_publish_no_images)
                    .create()

                dialog.show()

                dialog.addPhotosButton.setOnClickListener {
                    pickImages()
                    dialog.dismiss()
                }

                dialog.withoutPhotoButton.setOnClickListener {
                    publish(true)
                    dialog.dismiss()
                }
            }
            return
        }

        val images = ArrayList(imagesAdapter.images.mapNotNull { it.resizedLocalFile })
        val thumbnailIndex = imagesAdapter.defaultImageIndex
        if (thumbnailIndex > 0) {
            val thumbnail = images[thumbnailIndex]
            images.removeAt(thumbnailIndex)
            images.add(0, thumbnail)
        }

        vm.ad?.let {
            editBtn()
        } ?: vm.onAddNewClickedListener(binding.root, images)
    }

    private fun editBtn() {
        val newImages = ArrayList(imagesAdapter.images.mapNotNull { it.resizedLocalFile })
        val thumbnailIndex = imagesAdapter.defaultImageIndex
        val thumbnailImage = imagesAdapter.images.elementAtOrNull(thumbnailIndex)
        var thumbnailType: String? = null
        var thumbnailId: String? = null
        thumbnailImage?.resizedLocalFile?.let {
            thumbnailType = "new"
            newImages.remove(it)
            newImages.add(0, it)
        }
        thumbnailImage?.photo?.let {
            thumbnailType = "old"
            thumbnailId = it._id
        }

        vm.onEditAdClickedListener(
            binding.root, newImages,
            deletedPhotos,
            thumbnailType,
            thumbnailId
        )
    }

    companion object {
        const val SELECT_PICTURES = 1001
        private const val PLACE_PICKER_REQUEST = 5434
        const val ARG_AD = "AD"

        fun newAd(context: Context) {
            context.startActivity(Intent(context, NewAdFragment::class.java))
        }

        fun editAd(context: Context, ad: Ad) {
            context.startActivity(
                Intent(context, NewAdFragment::class.java)
                    .putExtra(ARG_AD, ad)
            )
        }
    }
}

class NewImage(var photo: Photo?, var localPath: String?) {
    var bitmap: Bitmap? = null
    var resizedLocalFile: File? = null

    init {
        localPath?.let {
            Log.i("NewAdFragment", it)
//            val image = File(it)
//            val originalBitmap = BitmapFactory.decodeFile(image.absolutePath)
            val originalBitmap = rotateImageIfRequired(it)
            bitmap = Helper.resizeBitmapImage(originalBitmap, 1000, 1000)

            val folder = Environment.getExternalStorageDirectory().absolutePath +
                    "/topsale/ads/";
            val resizedFile = File(folder, "${UUID.randomUUID()}.jpeg")
            resizedFile.writeBitmap(bitmap!!, Bitmap.CompressFormat.JPEG, 100)
            this.resizedLocalFile = resizedFile
        }
    }

    private fun rotateImageIfRequired(path: String): Bitmap {
        val ei = ExifInterface(path)
        val img = BitmapFactory.decodeFile(path)

        val orientation: Int =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val result =
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
                else -> img
            }

        return result ?: img
    }

    private fun rotateImage(img: Bitmap, degree: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }
}

fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    this.parentFile?.mkdirs() // create folder(s) if not exist
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}