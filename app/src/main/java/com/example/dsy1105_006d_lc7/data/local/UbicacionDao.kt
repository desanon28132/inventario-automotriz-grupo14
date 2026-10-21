package com.example.dsy1105_006d_lc7.data.local



import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UbicacionDao {

    @Insert
    suspend fun insertar(ubicacion: UbicacionEntity)

    @Update
    suspend fun actualizar(ubicacion: UbicacionEntity)

    @Query("SELECT * FROM ubicaciones ORDER BY codigo ASC")
    fun observarTodas(): Flow<List<UbicacionEntity>>

    @Query("SELECT * FROM ubicaciones WHERE id = :id")
    suspend fun obtenerPorId(id: Int): UbicacionEntity?

    @Query("SELECT * FROM ubicaciones WHERE codigo = :codigo LIMIT 1")
    suspend fun obtenerPorCodigo(codigo: String): UbicacionEntity?
}