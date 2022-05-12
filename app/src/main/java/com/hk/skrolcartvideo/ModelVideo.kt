package com.hk.skrolcartvideo

class ModelVideo{

    var id:String? = null
    var title:String? = null
//    var timeStamp :String? = null
    var videoUri :String? = null

    constructor(){

    }

    constructor(id: String?, title: String?, timeStamp: String?, videoUri: String?) {
        this.id = id
        this.title = title
//        this.timeStamp = timeStamp
        this.videoUri = videoUri
    }
}
