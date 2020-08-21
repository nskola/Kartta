package fi.neskola.kartta.database;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fi.neskola.kartta.BuildConfig;
import fi.neskola.kartta.models.Point;
import fi.neskola.kartta.models.Record;

@Database(entities = {Point.class, Record.class}, version = 5, exportSchema = false)
@TypeConverters({Record.TypeConverter.class})
public abstract class KarttaDatabase extends RoomDatabase {

    public abstract PointDao pointDao();
    public abstract RecordDao recordDao();

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
                db.disableWriteAheadLogging();
            }
            super.onOpen(db);
        }
    };

}

