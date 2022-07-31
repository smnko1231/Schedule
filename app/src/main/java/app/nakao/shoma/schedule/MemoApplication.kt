package app.nakao.shoma.schedule

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class MemoApplication: Application() {
    override fun onCreate(){
        super.onCreate()

        Realm.init(this)
        val realmConfiguration = RealmConfiguration
            .Builder()
            .allowWritesOnUiThread(true)
            .build()
        Realm.deleteRealm(realmConfiguration) //Realmを消す
        Realm.setDefaultConfiguration(realmConfiguration)
    }
}