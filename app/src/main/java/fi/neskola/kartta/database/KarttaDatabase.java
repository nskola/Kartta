package fi.neskola.kartta.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import fi.neskola.kartta.BuildConfig;
import fi.neskola.kartta.models.Point;
import fi.neskola.kartta.models.Target;

@Database(entities = {Point.class, Target.class}, version = 6, exportSchema = false)
public abstract class KarttaDatabase extends RoomDatabase {

    public abstract PointDao pointDao();
    public abstract TargetDao targetDao();

    public static KarttaDatabase buildDataBase(Context applicationContext) {
         return Room.databaseBuilder(applicationContext,
                KarttaDatabase.class,
                "kartta_database")
                .fallbackToDestructiveMigration()
                .addCallback(KarttaDatabase.sRoomDatabaseOnCreateCallback)
                .build();
    }

    public static RoomDatabase.Callback sRoomDatabaseOnCreateCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            if (BuildConfig.DEBUG) {
                //Causes db warnings, but makes reading database easier
                db.disableWriteAheadLogging();
            }
            super.onOpen(db);
        }
    };

}

