package app.nakao.shoma.schedule

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.time.LocalDate

open class Memo(
    open var year:String = "",
    open var month:String = "",
    open var day:String = "",
    open var title:String = "",
    open var content: String = "",
    open var isComplete: Boolean = false
):RealmObject()