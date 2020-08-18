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

@Database(entities = {Point.class, Record.class}, version = 3, exportSchema = false)
@TypeConverters({Record.TypeConverter.class})
public abstract class KarttaDatabase extends RoomDatabase {

    public abstract PointDao pointDao();
    public abstract RecordDao recordDao();

    public static KarttaDatabase buildDataBase(Context applicationContext) {
         KarttaDatabase database = Room.databaseBuilder(applicationContext,
                KarttaDatabase.class,
                "kartta_database")
                .fallbackToDestructiveMigration()
                .addCallback(KarttaDatabase.sRoomDatabaseOnCreateCallback)
                .build();

        KarttaDatabase.addTestData(database);
        return database;
    }

    public static void addTestData(KarttaDatabase karttaDatabase) {
            Executor.execute(() -> {
                // Populate the database in the background.
                RecordDao recordDao = karttaDatabase.recordDao();
                PointDao pointDao = karttaDatabase.pointDao();

                recordDao.deleteAll();
                pointDao.deleteAll();

                Record record = new Record();
                record.setId(1);
                record.setType(Record.Type.TARGET);
                Point point = new Point(1,1);
                point.setFk_record_id(1);
                long result = recordDao.insert(record);
                pointDao.insert(point);
            });
        }

    public static RoomDatabase.Callback sRoomDatabaseOnCreateCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            if (BuildConfig.DEBUG) {
                db.disableWriteAheadLogging();
            }
            super.onCreate(db);
        }
    };

}

