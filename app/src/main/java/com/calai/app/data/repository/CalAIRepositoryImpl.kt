package com.calai.app.data.repository

import com.calai.app.data.local.CalAIDao
import com.calai.app.data.local.entity.toDomain
import com.calai.app.data.local.entity.toEntity
import com.calai.app.domain.model.Meal
import com.calai.app.domain.model.User
import com.calai.app.domain.model.WeightLog
import com.calai.app.domain.repository.CalAIRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation của Repository
 * Lấy dữ liệu từ Local (Room) và Remote (Retrofit)
 */
class CalAIRepositoryImpl @Inject constructor(
    private val dao: CalAIDao
) : CalAIRepository {

    override fun getUser(userId: String): Flow<User?> {
        return dao.getUser(userId).map { it?.toDomain() }
    }

    override suspend fun saveUser(user: User) {
        dao.saveUser(user.toEntity())
    }

    override fun getMeals(userId: String): Flow<List<Meal>> {
        return dao.getMeals(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertMeal(meal: Meal) {
        dao.insertMeal(meal.toEntity())
    }

    override suspend fun deleteMeal(meal: Meal) {
        dao.deleteMeal(meal.toEntity())
    }

    override fun getWeightLogs(userId: String): Flow<List<WeightLog>> {
        return dao.getWeightLogs(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertWeightLog(log: WeightLog) {
        dao.insertWeightLog(log.toEntity())
    }
}
