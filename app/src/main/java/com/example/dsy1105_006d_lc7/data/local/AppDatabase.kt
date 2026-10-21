package com.example.dsy1105_006d_lc7.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ProductoEntity::class,
        MovimientoEntity::class,
        ProveedorEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun proveedorDao(): ProveedorDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    "ALTER TABLE productos ADD COLUMN descripcion TEXT NOT NULL DEFAULT ''"
                )

                db.execSQL(
                    "ALTER TABLE productos ADD COLUMN marca TEXT NOT NULL DEFAULT ''"
                )

                db.execSQL(
                    "ALTER TABLE productos ADD COLUMN unidadMedida TEXT NOT NULL DEFAULT 'Unidad'"
                )

                db.execSQL(
                    "ALTER TABLE productos ADD COLUMN precioCompra INTEGER NOT NULL DEFAULT 0"
                )

                db.execSQL(
                    "ALTER TABLE productos ADD COLUMN precioVenta INTEGER NOT NULL DEFAULT 0"
                )

                db.execSQL(
                    "ALTER TABLE productos ADD COLUMN estado TEXT NOT NULL DEFAULT 'Activo'"
                )

                db.execSQL(
                    "ALTER TABLE productos ADD COLUMN proveedorId INTEGER"
                )

                db.execSQL(
                    "ALTER TABLE productos ADD COLUMN ubicacionId INTEGER"
                )

                db.execSQL(
                    "ALTER TABLE productos ADD COLUMN cantidadReposicion INTEGER NOT NULL DEFAULT 0"
                )

                db.execSQL(
                    "ALTER TABLE productos ADD COLUMN fechaUltimoIngreso INTEGER"
                )

                db.execSQL(
                    "ALTER TABLE productos ADD COLUMN fechaUltimaSalida INTEGER"
                )
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS movimientos (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                productoId INTEGER NOT NULL,
                codigoProducto TEXT NOT NULL,
                nombreProducto TEXT NOT NULL,
                tipo TEXT NOT NULL,
                cantidad INTEGER NOT NULL,
                variacion INTEGER NOT NULL,
                fechaHora INTEGER NOT NULL,
                motivo TEXT NOT NULL,
                observacion TEXT NOT NULL,
                stockAnterior INTEGER NOT NULL,
                stockResultante INTEGER NOT NULL,
                usuario TEXT NOT NULL
            )
            """.trimIndent()
                )

                db.execSQL(
                    """
            CREATE INDEX IF NOT EXISTS index_movimientos_productoId
            ON movimientos (productoId)
            """.trimIndent()
                )
            }
        }
        private val MIGRATION_3_4 = object : Migration(3, 4) {

            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS proveedores (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                codigo TEXT NOT NULL,
                nombre TEXT NOT NULL,
                correo TEXT NOT NULL,
                activo INTEGER NOT NULL
            )
            """.trimIndent()
                )

                db.execSQL(
                    """
            CREATE UNIQUE INDEX IF NOT EXISTS index_proveedores_codigo
            ON proveedores (codigo)
            """.trimIndent()
                )
            }
        }


        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventario_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4
                    )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}