package co.realinventor.forblind.Helpers

class Student {
    lateinit var uid : String
    lateinit var name : String
    lateinit var phone : String
    var confirmed : Boolean = false
    var reviewed : Boolean = false

    constructor()



    constructor(uid: String, name: String, phone: String, confirmed: Boolean, reviewed: Boolean) {
        this.uid = uid
        this.name = name
        this.phone = phone
        this.confirmed = confirmed
        this.reviewed = reviewed
    }

    constructor(uid: String, name: String, phone: String) {
        this.uid = uid
        this.name = name
        this.phone = phone
    }


}