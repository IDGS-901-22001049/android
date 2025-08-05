package com.example.aguainteligente.data.repository

import com.example.aguainteligente.data.model.WaterConsumption
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class FirestoreRepository {

    private val db: FirebaseFirestore = Firebase.firestore
    private val WATER_COLLECTION = "water_data"
    private val DAILY_RECORDS_SUBCOLLECTION = "daily_records"

    suspend fun addWaterConsumption(consumption: WaterConsumption) {
        db.collection(WATER_COLLECTION)
            .document(consumption.userId)
            .collection(DAILY_RECORDS_SUBCOLLECTION)
            .add(consumption)
            .await()
    }

    suspend fun getDailyWaterConsumption(userId: String): Float {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        val snapshot = db.collection(WATER_COLLECTION)
            .document(userId)
            .collection(DAILY_RECORDS_SUBCOLLECTION)
            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
            .get()
            .await()

        var totalLitros = 0f
        for (doc in snapshot.documents) {
            val consumption = doc.toObject(WaterConsumption::class.java)
            totalLitros += consumption?.liters ?: 0f
        }
        return totalLitros
    }

    suspend fun getWeeklyWaterConsumption(userId: String): Float {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val startOfWeek = calendar.timeInMillis

        val snapshot = db.collection(WATER_COLLECTION)
            .document(userId)
            .collection(DAILY_RECORDS_SUBCOLLECTION)
            .whereGreaterThanOrEqualTo("timestamp", startOfWeek)
            .get()
            .await()

        var totalLitros = 0f
        for (doc in snapshot.documents) {
            val consumption = doc.toObject(WaterConsumption::class.java)
            totalLitros += consumption?.liters ?: 0f
        }
        return totalLitros
    }

    suspend fun getMonthlyWaterConsumption(userId: String): Float {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = calendar.timeInMillis

        val snapshot = db.collection(WATER_COLLECTION)
            .document(userId)
            .collection(DAILY_RECORDS_SUBCOLLECTION)
            .whereGreaterThanOrEqualTo("timestamp", startOfMonth)
            .get()
            .await()

        var totalLitros = 0f
        for (doc in snapshot.documents) {
            val consumption = doc.toObject(WaterConsumption::class.java)
            totalLitros += consumption?.liters ?: 0f
        }
        return totalLitros
    }

    suspend fun getPreviousMonthWaterConsumption(userId: String): Float {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfPreviousMonth = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfPreviousMonth = calendar.timeInMillis

        val snapshot = db.collection(WATER_COLLECTION)
            .document(userId)
            .collection(DAILY_RECORDS_SUBCOLLECTION)
            .whereGreaterThanOrEqualTo("timestamp", startOfPreviousMonth)
            .whereLessThanOrEqualTo("timestamp", endOfPreviousMonth)
            .get()
            .await()

        var totalLitros = 0f
        for (doc in snapshot.documents) {
            val consumption = doc.toObject(WaterConsumption::class.java)
            totalLitros += consumption?.liters ?: 0f
        }
        return totalLitros
    }

    suspend fun getMonthlyWaterHistory(userId: String, numMonths: Int = 6): List<Pair<String, Float>> {
        val monthlyHistory = mutableListOf<Pair<String, Float>>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.MONTH, -(numMonths - 1))

        val startOfPeriod = calendar.timeInMillis

        val allConsumptionInPeriod = db.collection(WATER_COLLECTION)
            .document(userId)
            .collection(DAILY_RECORDS_SUBCOLLECTION)
            .whereGreaterThanOrEqualTo("timestamp", startOfPeriod)
            .get()
            .await()
            .mapNotNull { it.toObject(WaterConsumption::class.java) }

        for (i in 0 until numMonths) {
            val monthCalendar = Calendar.getInstance()
            monthCalendar.timeInMillis = startOfPeriod
            monthCalendar.add(Calendar.MONTH, i)

            val monthStart = monthCalendar.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val monthEnd = monthCalendar.apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val monthName = if (i == numMonths - 1) "Actual" else dateFormat.format(monthCalendar.time)

            val totalForMonth = allConsumptionInPeriod
                .filter { it.timestamp in monthStart..monthEnd }
                .sumOf { it.liters.toDouble() }
                .toFloat()

            monthlyHistory.add(monthName to totalForMonth)
        }

        return monthlyHistory
    }

    suspend fun getPeakConsumptionData(userId: String): Map<String, Float> {
        val peakData = mutableMapOf<String, Float>()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val thirtyDaysAgo = calendar.timeInMillis

        val snapshot = db.collection(WATER_COLLECTION)
            .document(userId)
            .collection(DAILY_RECORDS_SUBCOLLECTION)
            .whereGreaterThanOrEqualTo("timestamp", thirtyDaysAgo)
            .get()
            .await()

        val dailyConsumptions = mutableMapOf<String, Float>()
        val weeklyConsumptions = mutableMapOf<String, Float>()
        val monthlyConsumptions = mutableMapOf<String, Float>()

        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val weekFormat = SimpleDateFormat("yyyy-ww", Locale.getDefault())
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

        for (doc in snapshot.documents) {
            val consumption = doc.toObject(WaterConsumption::class.java)
            if (consumption != null) {
                val consumptionDate = Date(consumption.timestamp)

                val dayKey = dayFormat.format(consumptionDate)
                dailyConsumptions.compute(dayKey) { _, currentTotal ->
                    (currentTotal ?: 0f) + consumption.liters
                }

                val weekKey = weekFormat.format(consumptionDate)
                weeklyConsumptions.compute(weekKey) { _, currentTotal ->
                    (currentTotal ?: 0f) + consumption.liters
                }

                val monthKey = monthFormat.format(consumptionDate)
                monthlyConsumptions.compute(monthKey) { _, currentTotal ->
                    (currentTotal ?: 0f) + consumption.liters
                }
            }
        }

        val maxDaily = dailyConsumptions.values.maxOrNull() ?: 0f
        val maxWeekly = weeklyConsumptions.values.maxOrNull() ?: 0f
        val maxMonthly = monthlyConsumptions.values.maxOrNull() ?: 0f

        peakData["Día con mayor consumo"] = maxDaily
        peakData["Semana con mayor consumo"] = maxWeekly
        peakData["Mes con mayor consumo"] = maxMonthly

        return peakData
    }
}