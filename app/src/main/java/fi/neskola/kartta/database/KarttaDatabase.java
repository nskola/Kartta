package fi.neskola.kartta.database;

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

    private static volatile KarttaDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static KarttaDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (KarttaDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            KarttaDatabase.class, "kartta_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseOnCreateCallback)
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            databaseWriteExecutor.execute(() -> {

                // Populate the database in the background.
                RecordDao recordDao = INSTANCE.recordDao();
                PointDao pointDao = INSTANCE.pointDao();

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
    };

    private static RoomDatabase.Callback sRoomDatabaseOnCreateCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                if (BuildConfig.DEBUG) {
                    db.disableWriteAheadLogging();
                }
            });
        }
    };

}

