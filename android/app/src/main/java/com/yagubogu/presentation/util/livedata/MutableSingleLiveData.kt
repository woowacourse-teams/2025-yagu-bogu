package com.yagubogu.presentation.util.livedata

class MutableSingleLiveData<T> : SingleLiveData<T> {
    constructor() : super()

    constructor(value: T) : super(value)

    public override fun postValue(value: T) {
        super.postValue(value)
    }

    public override fun setValue(value: T) {
        super.setValue(value)
    }
}
