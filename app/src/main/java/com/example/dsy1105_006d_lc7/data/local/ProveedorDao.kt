package com.example.dsy1105_006d_lc7.data.local



import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProveedorDao {

    @Insert
    suspend fun insertar(proveedor: ProveedorEntity)

    @Update
    suspend fun actualizar(proveedor: ProveedorEntity)

    @Query("SELECT * FROM proveedores ORDER BY nombre ASC")
    fun observarTodos(): Flow<List<ProveedorEntity>>

    @Query("SELECT * FROM proveedores WHERE id = :id")
    suspend fun obtenerPorId(id: Int): ProveedorEntity?

    @Query("SELECT * FROM proveedores WHERE codigo = :codigo LIMIT 1")
    suspend fun obtenerPorCodigo(codigo: String): ProveedorEntity?
}