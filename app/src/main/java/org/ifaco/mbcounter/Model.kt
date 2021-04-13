package org.ifaco.mbcounter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.ifaco.mbcounter.data.Report

class Model : ViewModel() {
    val onani: MutableLiveData<ArrayList<Report>?> by lazy { MutableLiveData<ArrayList<Report>?>() }
    val crush: MutableLiveData<String?> by lazy { MutableLiveData<String?>() }
    val summary: MutableLiveData<Summary.Result?> by lazy { MutableLiveData<Summary.Result?>() }
}
