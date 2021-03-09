package kr.dformula.psydiary.model

data class ContentDTO (
    var category_title : String? = null,
    var category_sub : String? = null,
    var explain : String? = null,
    var explain1: String? = null,
    var imageUrl : String? = null,
    var uid : String? = null,
    var userId : String? = null,
    var timestamp : Long? = null,
    var favoriteCount : Int = 0,
    var favorites : MutableMap<String, Boolean> = HashMap()){

    data class Comment(
        var userId : String? = null,
        var uid : String? = null,
        var comment : String? = null,
        var timestamp: Long? = null) {
    }



}