package com.calai.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.calai.app.data.local.entity.MealEntity
import com.calai.app.data.local.entity.PendingCustomFoodEntity
import com.calai.app.data.local.entity.PendingFavoriteEntity
import com.calai.app.data.local.entity.PendingRequestEntity
import com.calai.app.data.local.entity.UserEntity
import com.calai.app.data.local.entity.WeightLogEntity

@Database(
    entities = [
        UserEntity::class, MealEntity::class, WeightLogEntity::class,
        PendingFavoriteEntity::class, PendingCustomFoodEntity::class, PendingRequestEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class CalAIDatabase : RoomDatabase() {
    abstract val dao: CalAIDao

    companion object {
        /**
         * Thêm hàng đợi ghi bữa ăn/buổi tập khi mất mạng (BR-07.5, BR-10.4). Có Migration thủ công để KHÔNG xoá hàng đợi:
         * dữ liệu chưa gửi lên máy chủ không tải lại được như bộ nhớ đệm bữa ăn.
         */
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pending_requests` (" +
                        "`localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`kind` TEXT NOT NULL, " +
                        "`clientRequestId` TEXT NOT NULL, " +
                        "`payload` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`attempts` INTEGER NOT NULL, " +
                        "`status` TEXT NOT NULL, " +
                        "`lastError` TEXT)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_pending_requests_clientRequestId` " +
                        "ON `pending_requests` (`clientRequestId`)"
                )
            }
        }
    }
}
