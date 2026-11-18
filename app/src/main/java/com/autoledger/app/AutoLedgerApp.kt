package com.autoledger.app

import android.app.Application
import com.autoledger.app.data.AppDatabase

class AutoLedgerApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}
