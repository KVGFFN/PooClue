package edu.ap.pooclueapplication

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import androidx.core.content.ContentProviderCompat.requireContext
import edu.ap.pooclueapplication.ui.map.MapFragment

object ToiletContract {
    // Table contents are grouped together in an anonymous object.
    object ToiletEntry : BaseColumns {
        const val TABLE_NAME = "toilets"
        const val COLUMN_NAME_LONGITUDE = "LONGITUDE"
        const val COLUMN_NAME_LATITUDE = "LATITUDE"
        const val COLUMN_NAME_TARGET = "TARGET"
        const val COLUMN_NAME_WHEELCHAIR = "WHEELCHAIR"
    }
}
private const val SQL_CREATE_ENTRIES =
    "CREATE TABLE ${ToiletContract.ToiletEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${ToiletContract.ToiletEntry.COLUMN_NAME_LONGITUDE} REAL," +
            "${ToiletContract.ToiletEntry.COLUMN_NAME_LATITUDE} REAL," +
            "${ToiletContract.ToiletEntry.COLUMN_NAME_TARGET} TEXT," +
            "${ToiletContract.ToiletEntry.COLUMN_NAME_WHEELCHAIR} TEXT)"

private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ToiletContract.ToiletEntry.TABLE_NAME}"

class ToiletDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun writeToilet(longitude: Double, latitude: Double, target: String, wheelchair: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(ToiletContract.ToiletEntry.COLUMN_NAME_LONGITUDE, longitude)
            put(ToiletContract.ToiletEntry.COLUMN_NAME_LATITUDE, latitude)
            put(ToiletContract.ToiletEntry.COLUMN_NAME_TARGET, target)
            put(ToiletContract.ToiletEntry.COLUMN_NAME_WHEELCHAIR, wheelchair)
        }
        val newRowId = db?.insert(ToiletContract.ToiletEntry.TABLE_NAME, null, values)
    }
    fun readToilets(): Cursor {
        val db = readableDatabase
        val projection = arrayOf(BaseColumns._ID, ToiletContract.ToiletEntry.COLUMN_NAME_LONGITUDE, ToiletContract.ToiletEntry.COLUMN_NAME_LATITUDE, ToiletContract.ToiletEntry.COLUMN_NAME_TARGET, ToiletContract.ToiletEntry.COLUMN_NAME_WHEELCHAIR)

        return db.query(
            ToiletContract.ToiletEntry.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            null,              // The columns for the WHERE clause
            null,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null               // The sort order
        )

    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Toilet.db"
    }

}
